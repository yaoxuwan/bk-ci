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

package com.tencent.devops.artifactory.resources.external

import com.tencent.devops.artifactory.api.external.ExternalReportResource
import com.tencent.devops.artifactory.service.artifactory.ArtifactoryReportService
import com.tencent.devops.artifactory.service.bkrepo.BkRepoReportService
import com.tencent.devops.common.api.exception.ParamBlankException
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.common.service.gray.RepoGray
import com.tencent.devops.common.web.RestResource
import org.springframework.beans.factory.annotation.Autowired
import javax.ws.rs.core.Response

@RestResource
class ExternalReportResourceImpl @Autowired constructor(
    private val artifactoryReportService: ArtifactoryReportService,
    private val bkRepoReportService: BkRepoReportService,
    val redisOperation: RedisOperation,
    val repoGray: RepoGray
) : ExternalReportResource {
    override fun get(
        projectId: String,
        pipelineId: String,
        buildId: String,
        elementId: String,
        path: String
    ): Response {
        if (path.endsWith(".js") ||
            path.endsWith(".css") ||
            path.endsWith(".tff") ||
            path.endsWith(".bmp") ||
            path.endsWith(".jpg") ||
            path.endsWith(".png") ||
            path.endsWith(".tif") ||
            path.endsWith(".gif") ||
            path.endsWith(".pcx") ||
            path.endsWith(".tga") ||
            path.endsWith(".exif") ||
            path.endsWith(".fpx") ||
            path.endsWith(".svg") ||
            path.endsWith(".psd") ||
            path.endsWith(".cdr") ||
            path.endsWith(".pcd") ||
            path.endsWith(".dxf") ||
            path.endsWith(".ufo") ||
            path.endsWith(".eps") ||
            path.endsWith(".ai") ||
            path.endsWith(".raw") ||
            path.endsWith(".wmf") ||
            path.endsWith(".webp")
        ) {
        } else {
            throw ParamBlankException("Invalid file sufix.")
        }
        if (projectId.isBlank()) {
            throw ParamBlankException("Invalid projectId")
        }
        if (pipelineId.isBlank()) {
            throw ParamBlankException("Invalid pipelineId")
        }
        if (buildId.isBlank()) {
            throw ParamBlankException("Invalid buildId")
        }
        if (elementId.isBlank()) {
            throw ParamBlankException("Invalid elementId")
        }
        if (path.isBlank()) {
            throw ParamBlankException("Invalid path")
        }
        return if (repoGray.isGray(projectId, redisOperation)) {
            bkRepoReportService.get(projectId, pipelineId, buildId, elementId, path)
        } else {
            artifactoryReportService.get(projectId, pipelineId, buildId, elementId, path)
        }
    }
}