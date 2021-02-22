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

package com.tencent.devops.auth.service

import com.tencent.bk.sdk.iam.dto.callback.response.FetchInstanceInfoResponseDTO
import com.tencent.bk.sdk.iam.dto.callback.response.InstanceInfoDTO
import com.tencent.bk.sdk.iam.dto.callback.response.ListInstanceResponseDTO
import com.tencent.devops.auth.pojo.FetchInstanceInfo
import com.tencent.devops.auth.pojo.ListInstanceInfo
import com.tencent.devops.auth.pojo.SearchInstanceInfo
import com.tencent.devops.common.client.Client
import com.tencent.devops.environment.api.RemoteNodeResource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AuthNodeService @Autowired constructor(
    val client: Client
) {

    fun getNodeInfo(hashIds: List<Any>?): FetchInstanceInfoResponseDTO? {
        val nodeInfos =
                client.get(RemoteNodeResource::class)
                        .getNodeInfos(hashIds as List<String>).data
        val result = FetchInstanceInfo()
        if (nodeInfos == null || nodeInfos.isEmpty()) {
            logger.info("$hashIds 无节点")
            return result.buildFetchInstanceFailResult()
        }
        val entityInfo = mutableListOf<InstanceInfoDTO>()
        nodeInfos.map {
            val entity = InstanceInfoDTO()
            entity.id = it.nodeHashId
            entity.displayName = it.displayName
            entityInfo.add(entity)
        }
        logger.info("entityInfo $entityInfo, count ${nodeInfos.size.toLong()}")
        return result.buildFetchInstanceResult(entityInfo)
    }

    fun getNode(projectId: String, offset: Int, limit: Int): ListInstanceResponseDTO? {
        val nodeInfos =
                client.get(RemoteNodeResource::class)
                        .listNodeForAuth(projectId, offset, limit).data
        val result = ListInstanceInfo()
        if (nodeInfos?.records == null) {
            logger.info("$projectId 项目下无节点")
            return result.buildListInstanceFailResult()
        }
        val entityInfo = mutableListOf<InstanceInfoDTO>()
        nodeInfos?.records?.map {
            val entity = InstanceInfoDTO()
            entity.id = it.nodeHashId
            entity.displayName = it.displayName
            entityInfo.add(entity)
        }
        logger.info("entityInfo $entityInfo, count ${nodeInfos?.count}")
        return result.buildListInstanceResult(entityInfo, nodeInfos.count)
    }

    fun searchNode(projectId: String, keyword: String, limit: Int, offset: Int): SearchInstanceInfo {
        val nodeInfos =
                client.get(RemoteNodeResource::class)
                        .searchByName(
                                projectId = projectId,
                                offset = offset,
                                limit = limit,
                                displayName = keyword).data
        val result = SearchInstanceInfo()
        if (nodeInfos?.records == null) {
            logger.info("$projectId 项目下无节点")
            return result.buildSearchInstanceFailResult()
        }
        val entityInfo = mutableListOf<InstanceInfoDTO>()
        nodeInfos?.records?.map {
            val entity = InstanceInfoDTO()
            entity.id = it.nodeHashId
            entity.displayName = it.displayName
            entityInfo.add(entity)
        }
        logger.info("entityInfo $entityInfo, count ${nodeInfos?.count}")
        return result.buildSearchInstanceResult(entityInfo, nodeInfos.count)
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }
}
