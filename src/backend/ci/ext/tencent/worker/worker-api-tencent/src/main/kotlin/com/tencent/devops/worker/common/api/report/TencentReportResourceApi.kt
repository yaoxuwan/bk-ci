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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.worker.common.api.report

import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.JsonParser
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.archive.constant.ARCHIVE_PROPS_BUILD_ID
import com.tencent.devops.common.archive.constant.ARCHIVE_PROPS_BUILD_NO
import com.tencent.devops.common.archive.constant.ARCHIVE_PROPS_PIPELINE_ID
import com.tencent.devops.common.archive.constant.ARCHIVE_PROPS_PROJECT_ID
import com.tencent.devops.common.archive.constant.ARCHIVE_PROPS_SOURCE
import com.tencent.devops.common.archive.constant.ARCHIVE_PROPS_USER_ID
import com.tencent.devops.process.pojo.BuildVariables
import com.tencent.devops.process.pojo.report.ReportEmail
import com.tencent.devops.process.utils.PIPELINE_BUILD_NUM
import com.tencent.devops.process.utils.PIPELINE_START_USER_ID
import com.tencent.devops.worker.common.api.AbstractBuildResourceApi
import com.tencent.devops.worker.common.api.ApiPriority
import com.tencent.devops.worker.common.logger.LoggerService
import com.tencent.devops.worker.common.logger.LoggerService.elementId
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File

@ApiPriority(priority = 9)
class TencentReportResourceApi : AbstractBuildResourceApi(), ReportSDKApi {
    private val bkrepoMetaDataPrefix = "X-BKREPO-META-"
    private val bkrepoUid = "X-BKREPO-UID"
    private val bkrepoOverride = "X-BKREPO-OVERWRITE"

    override fun getRootUrl(taskId: String): Result<String> {
        val path = "/ms/artifactory/api/build/artifactories/report/$taskId/root"
        val request = buildGet(path)
        val responseContent = request(request, "获取报告跟路径失败")
        return objectMapper.readValue(responseContent)
    }

    override fun createReportRecord(
        taskId: String,
        indexFile: String,
        name: String,
        reportType: String?,
        reportEmail: ReportEmail?
    ): Result<Boolean> {
        val indexFileEncode = encode(indexFile)
        val nameEncode = encode(name)
        val path =
            "/ms/process/api/build/reports/$taskId?indexFile=$indexFileEncode&name=$nameEncode&reportType=$reportType"
        val request = if (reportEmail == null) {
            buildPost(path)
        } else {
            val requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), objectMapper.writeValueAsString(reportEmail))
            buildPost(path, requestBody)
        }
        val responseContent = request(request, "创建报告失败")
        return objectMapper.readValue(responseContent)
    }

    private fun updateBkRepoReport(file: File, taskId: String, relativePath: String, buildVariables: BuildVariables) {
        val url = StringBuilder("/bkrepo/api/build/generic/${buildVariables.projectId}/report/${buildVariables.pipelineId}/${buildVariables.buildId}/$elementId/${relativePath.removePrefix("/")}")
        val header = mutableMapOf<String, String>()
        with(buildVariables) {
            header[bkrepoMetaDataPrefix + ARCHIVE_PROPS_PROJECT_ID] = projectId
            header[bkrepoMetaDataPrefix + ARCHIVE_PROPS_PIPELINE_ID] = pipelineId
            header[bkrepoMetaDataPrefix + ARCHIVE_PROPS_BUILD_ID] = buildId
            header[bkrepoMetaDataPrefix + ARCHIVE_PROPS_USER_ID] = variables[PIPELINE_START_USER_ID] ?: ""
            header[bkrepoMetaDataPrefix + ARCHIVE_PROPS_BUILD_NO] = variables[PIPELINE_BUILD_NUM] ?: ""
            header[bkrepoMetaDataPrefix + ARCHIVE_PROPS_SOURCE] = "pipeline"
            header[bkrepoUid] = variables[PIPELINE_START_USER_ID] ?: ""
            header[bkrepoOverride] = "true"
        }

        val request = buildPut(url.toString(), RequestBody.create(MediaType.parse("application/octet-stream"), file), header, useFileGateway = true)
        val responseContent = request(request, "上传自定义报告失败")
        try {
            val obj = JsonParser().parse(responseContent).asJsonObject
            if (obj.has("code") && obj["code"].asString != "0") throw RuntimeException()
        } catch (e: Exception) {
            LoggerService.addNormalLine(e.message ?: "")
            throw RuntimeException("report archive fail: $responseContent")
        }
    }

    override fun uploadReport(file: File, taskId: String, relativePath: String, buildVariables: BuildVariables) {
        updateBkRepoReport(file, taskId, relativePath, buildVariables)
        setPipelineMetadata(buildVariables)
    }

    private fun setPipelineMetadata(buildVariables: BuildVariables) {
        try {
            val projectId = buildVariables.projectId
            val pipelineId = buildVariables.pipelineId
            val pipelineName = buildVariables.variables[BK_CI_PIPELINE_NAME]
            val buildId = buildVariables.buildId
            val buildNum = buildVariables.variables[BK_CI_BUILD_NUM]
            if (!pipelineName.isNullOrBlank()) {
                val pipelineNameRequest = buildPost(
                    "/bkrepo/api/build/repository/api/metadata/$projectId/report/$pipelineId",
                    RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        JsonUtil.toJson(mapOf("metadata" to mapOf(METADATA_DISPLAY_NAME to pipelineName)))
                    )
                )
                request(pipelineNameRequest, "set pipeline displayName failed")
            }
            if (!buildNum.isNullOrBlank()) {
                val buildNumRequest = buildPost(
                    "/bkrepo/api/build/repository/api/metadata/$projectId/report/$pipelineId/$buildId",
                    RequestBody.create(
                        MediaType.parse("application/json; charset=utf-8"),
                        JsonUtil.toJson(mapOf("metadata" to mapOf(METADATA_DISPLAY_NAME to buildNum)))
                    )
                )
                request(buildNumRequest, "set build displayName failed")
            }
        } catch (e: Exception) {
            logger.warn("set pipeline metadata error: ${e.message}")
        }
    }

    companion object {
        private const val BK_CI_PIPELINE_NAME = "BK_CI_PIPELINE_NAME"
        private const val BK_CI_BUILD_NUM = "BK_CI_BUILD_NUM"
        private const val METADATA_DISPLAY_NAME = "displayName"
    }
}