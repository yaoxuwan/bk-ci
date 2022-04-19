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

package com.tencent.devops.stream.resources

import com.tencent.devops.common.client.Client
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.repository.api.ServiceOauthResource
import com.tencent.devops.stream.api.ExternalStreamResource
import com.tencent.devops.stream.service.StreamPipelineBadgeService
import com.tencent.devops.stream.service.TXStreamBasicSettingService
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

@RestResource
class ExternalStreamResourceImpl(
    private val streamPipelineBadgeService: StreamPipelineBadgeService,
    private val basicSettingService: TXStreamBasicSettingService,
    private val client: Client
) : ExternalStreamResource {
    override fun getPipelineBadge(
        gitProjectId: Long,
        filePath: String,
        branch: String?,
        objectKind: String?
    ): String {
        return streamPipelineBadgeService.get(
            gitProjectId = gitProjectId,
            filePath = filePath,
            branch = branch,
            objectKind = objectKind
        )
    }

    override fun gitCallback(code: String, state: String): Response {
        val gitOauthCallback = client.get(ServiceOauthResource::class).gitCallback(code = code, state = state).data!!
        with(gitOauthCallback) {
            if (gitOauthCallback.gitProjectId != null) {
                basicSettingService.updateOauthSetting(
                    gitProjectId = gitProjectId!!,
                    userId = userId,
                    oauthUserId = oauthUserId
                )

                // 更新项目信息
                basicSettingService.updateProjectOrganizationInfo(
                    projectId = gitProjectId!!.toString(),
                    userId = oauthUserId
                )
            }
            return Response.temporaryRedirect(UriBuilder.fromUri(gitOauthCallback.redirectUrl).build()).build()
        }
    }
}
