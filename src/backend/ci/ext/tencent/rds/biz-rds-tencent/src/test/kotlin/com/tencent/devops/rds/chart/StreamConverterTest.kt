/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.rds.chart

import com.nhaarman.mockito_kotlin.mock
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.ci.v2.utils.ScriptYmlUtils
import com.tencent.devops.common.ci.v2.utils.YamlCommonUtils
import com.tencent.devops.rds.pojo.yaml.PreMain
import com.tencent.devops.rds.pojo.yaml.PreResource
import com.tencent.devops.rds.utils.CommonUtils
import com.tencent.devops.rds.utils.Yaml
import java.nio.charset.StandardCharsets
import org.apache.commons.io.FileUtils
import org.junit.Test

import org.springframework.util.ResourceUtils

class StreamConverterTest {

    private val streamConverter = StreamConverter(mock(), mock(), mock())
    private val chartParser = ChartParser()

    @Test
    fun replaceTemplate() {
        val dir = ResourceUtils.getFile("classpath:buildModelTest/templates")
        val pipelines = dir.listFiles()?.toList()?.filter { it.isFile && CommonUtils.ciFile(it.name) } ?: emptyList()
        pipelines.forEach {
            val pipelineYaml = FileUtils.readFileToString(it, StandardCharsets.UTF_8)
            val (yamlOb, pre) = streamConverter.replaceTemplate(dir.parent, it.name, pipelineYaml)
            println("------------------------   pre ------------------------   ")
            println(YamlCommonUtils.toYamlNotNull(pre))
            println("------------------------   nor ------------------------   ")
            println(Yaml.marshal(ScriptYmlUtils.normalizeRdsYaml(yamlOb, it.name)))
        }
    }

    @Test
    fun loadMainYaml() {
        val cachePath = ResourceUtils.getFile("classpath:buildModelTest").absolutePath
        val mainYamlStr = chartParser.getCacheChartMainFile(cachePath)
        println(mainYamlStr!!)
        println("---")
        val yaml = org.yaml.snakeyaml.Yaml()
        val obj = yaml.load(mainYamlStr) as Any
        val formatMainStr = YamlUtil.toYaml(obj)
        println(formatMainStr)
        println("---")
        val mainYaml = YamlUtil.getObjectMapper().readValue(
            mainYamlStr,
            PreMain::class.java
        )
        println(mainYaml)
    }

    @Test
    fun loadResourceYaml() {
        val cachePath = ResourceUtils.getFile("classpath:buildModelTest").absolutePath
        val resourceYamlStr = chartParser.getCacheChartResourceFile(cachePath)
        println(resourceYamlStr!!)
        println("---")
        val yaml = org.yaml.snakeyaml.Yaml()
        val obj = yaml.load(resourceYamlStr) as Any
        val formatResourceStr = YamlUtil.toYaml(obj)
        println(formatResourceStr)
        println("---")
        val resource = YamlUtil.getObjectMapper().readValue(
            resourceYamlStr,
            PreResource::class.java
        )
        println(resource)
        println(resource.getResourceObject())
    }
}