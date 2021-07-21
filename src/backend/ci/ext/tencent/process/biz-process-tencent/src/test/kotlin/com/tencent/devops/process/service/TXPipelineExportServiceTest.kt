package com.tencent.devops.process.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.nhaarman.mockito_kotlin.mock
import com.tencent.devops.process.engine.service.PipelineRepositoryService
import com.tencent.devops.process.engine.service.store.StoreImageHelper
import com.tencent.devops.process.permission.PipelinePermissionService
import com.tencent.devops.process.service.label.PipelineGroupService
import com.tencent.devops.process.service.scm.ScmProxyService
import org.junit.Assert
import org.junit.Test

class TXPipelineExportServiceTest {
    private val stageTagService: StageTagService = mock()
    private val pipelineGroupService: PipelineGroupService = mock()
    private val pipelinePermissionService: PipelinePermissionService = mock()
    private val pipelineRepositoryService: PipelineRepositoryService = mock()
    private val storeImageHelper: StoreImageHelper = mock()
    private val scmProxyService: ScmProxyService = mock()

    private val txPipelineExportService = TXPipelineExportService(
        stageTagService = stageTagService,
        pipelineGroupService = pipelineGroupService,
        pipelinePermissionService = pipelinePermissionService,
        pipelineRepositoryService = pipelineRepositoryService,
        storeImageHelper = storeImageHelper,
        scmProxyService = scmProxyService
    )

    @Test
    fun testReplaceMapWithDoubleCurlybraces1() {
        val inputMap: MutableMap<String, Any>? = mutableMapOf()

        val resultMap = txPipelineExportService.replaceMapWithDoubleCurlyBraces(
            inputMap = inputMap,
            output2Elements = mutableMapOf(),
            variables = mutableMapOf()
        )
        val result = jacksonObjectMapper().writeValueAsString(resultMap)
        Assert.assertEquals(result, "null")
    }

    @Test
    fun testReplaceMapWithDoubleCurlybraces2() {
        val inputMap = mutableMapOf(
            "key1" to "value" as Any,
            "key2" to "\${haha}" as Any,
            "key3" to "abcedf\${haha}hijklmn" as Any,
            "key4" to "aaaaaa\${haha}hijklmn\${aaaa}" as Any,
            "key5" to "\${123456}aaaaaa\${haha}hijklmn\${aaaa}" as Any,
            "\${key}" to "\${123456}aaaaaa\${haha}hijklmn\${aaaa}" as Any
        )
        val variables = mapOf("key1" to "value")

        val resultMap = txPipelineExportService.replaceMapWithDoubleCurlyBraces(
            inputMap = inputMap,
            output2Elements = mutableMapOf(),
            variables = variables
        )
        val result = jacksonObjectMapper().writeValueAsString(resultMap)
        Assert.assertEquals(result, "{\"key1\":\"variables.value\",\"key2\":\"\${{ haha }}\"," +
            "\"key3\":\"abcedf\${{ haha }}hijklmn\",\"key4\":\"aaaaaa\${{ haha }}hijklmn" +
            "\${{ aaaa }}\",\"key5\":\"\${{ 123456 }}aaaaaa\${{ haha }}hijklmn" +
            "\${{ aaaa }}\",\"\${key}\":\"\${{ 123456 }}aaaaaa\${{ haha }}hijklmn" +
            "\${{ aaaa }}\"}")
    }

    @Test
    fun testReplaceMapWithDoubleCurlybraces3() {
        val inputMap = mutableMapOf(
            "key1" to "value" as Any,
            "key2" to listOf("\${haha}", "abcedf\${haha}hijklmn", "\${123456}aaaaaa\${haha}hijklmn\${aaaa}", 123) as Any
        )
        val variables = mapOf("key1" to "value")

        val resultMap = txPipelineExportService.replaceMapWithDoubleCurlyBraces(
            inputMap = inputMap,
            output2Elements = mutableMapOf(),
            variables = variables
        )
        val result = jacksonObjectMapper().writeValueAsString(resultMap)
        Assert.assertEquals(result, "{\"key1\":\"variables.value\",\"key2\":[\"\${{ haha }}\"," +
            "\"abcedf\${{ haha }}hijklmn\",\"\${{ 123456 }}aaaaaa" +
            "\${{ haha }}hijklmn\${{ aaaa }}\",123]}")
    }
}
