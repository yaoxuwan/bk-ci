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

package com.tencent.devops.environment.permission.impl

import com.tencent.devops.common.auth.api.AuthPermission
import com.tencent.devops.common.auth.utils.GitCIUtils
import com.tencent.devops.common.client.Client
import com.tencent.devops.environment.permission.EnvironmentPermissionService
import com.tencent.devops.scm.api.ServiceGitCiResource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Pipeline专用权限校验接口
 */
@Component
class GitCIEnvironmentPermissionServiceImpl @Autowired constructor(
    val client: Client
) : EnvironmentPermissionService {

    override fun listEnvByPermission(userId: String, projectId: String, permission: AuthPermission): Set<Long> {
        return emptySet()
    }

    override fun listEnvByPermissions(userId: String, projectId: String, permissions: Set<AuthPermission>): Map<AuthPermission, List<String>> {
        return emptyMap()
    }

    override fun checkEnvPermission(userId: String, projectId: String, envId: Long, permission: AuthPermission): Boolean {
        return checkPermission(userId, projectId)
    }

    override fun checkEnvPermission(userId: String, projectId: String, permission: AuthPermission): Boolean {
        return checkPermission(userId, projectId)
    }

    override fun createEnv(userId: String, projectId: String, envId: Long, envName: String) {
        return
    }

    override fun updateEnv(userId: String, projectId: String, envId: Long, envName: String) {
        return
    }

    override fun deleteEnv(projectId: String, envId: Long) {
        return
    }

    override fun listNodeByPermission(userId: String, projectId: String, permission: AuthPermission): Set<Long> {
        return emptySet()
    }

    override fun listNodeByPermissions(userId: String, projectId: String, permissions: Set<AuthPermission>): Map<AuthPermission, List<String>> {
        return emptyMap()
    }

    override fun checkNodePermission(userId: String, projectId: String, nodeId: Long, permission: AuthPermission): Boolean {
        return checkPermission(userId, projectId)
    }

    override fun checkNodePermission(userId: String, projectId: String, permission: AuthPermission): Boolean {
        return checkPermission(userId, projectId)
    }

    override fun createNode(userId: String, projectId: String, nodeId: Long, nodeName: String) {
        return
    }

    override fun updateNode(userId: String, projectId: String, nodeId: Long, nodeName: String) {
        return
    }

    override fun deleteNode(projectId: String, nodeId: Long) {
        return
    }

    private fun checkPermission(userId: String, projectId: String): Boolean {
        val gitProjectId = GitCIUtils.getGitCiProjectId(projectId)
        logger.info("GitCIEnvironmentPermission $userId $projectId $gitProjectId")
        return client.get(ServiceGitCiResource::class).checkUserGitAuth(userId, gitProjectId).data ?: false
    }

    companion object {
        val logger = LoggerFactory.getLogger(GitCIEnvironmentPermissionServiceImpl::class.java)
    }
}
