/*
 * Tencent is pleased to support the open source community by making BK-REPO 蓝鲸制品库 available.
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

package com.tencent.devops.misc.cron

import com.tencent.devops.common.environment.agent.client.EsbAgentClient
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.environment.pojo.enums.NodeType
import com.tencent.devops.misc.dao.NodeDao
import com.tencent.devops.model.environment.tables.records.TNodeRecord
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class UpdateCcNodeStatus @Autowired constructor(
    private val dslContext: DSLContext,
    private val nodeDao: NodeDao,
    private val redisOperation: RedisOperation,
    private val esbAgentClient: EsbAgentClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UpdateCcNodeStatus::class.java)
        private const val LOCK_KEY = "env_cron_updateCcNodeStatus"
        private const val LOCK_VALUE = "env_cron_updateCcNodeStatus"
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 2 * 60 * 1000)
    fun runUpdateCcNodeStatus() {
        logger.info("runUpdateCcNodeStatus")
        val lockValue = redisOperation.get(LOCK_KEY)
        if (lockValue != null) {
            logger.info("get lock failed, skip")
            return
        } else {
            redisOperation.set(
                LOCK_KEY,
                LOCK_VALUE, 3 * 30)
        }

        try {
            updateCmdbNodeStatus()
        } catch (t: Throwable) {
            logger.warn("update server node status failed", t)
        }

        try {
            updateCcNodeStatus()
        } catch (t: Throwable) {
            logger.warn("update server node status failed", t)
        }
    }

    private fun updateCmdbNodeStatus() {
        logger.info("updateCmdbNodeStatus")
        val allCmdbNodes = nodeDao.listAllNodesByType(dslContext, NodeType.CMDB)

        if (allCmdbNodes.isEmpty()) {
            return
        }

        // 分页处理
        val pageSize = 1000
        val totalCount = allCmdbNodes.size
        val totalPage = Math.ceil(totalCount * 1.0 / pageSize).toInt()
        for (page in 0 until totalPage) {
            val from = pageSize * page
            val end = from + Math.min(pageSize, totalCount - from)
            val nodes = allCmdbNodes.subList(from, end)
            updateCmdbNodes(nodes)
        }
    }

    private fun updateCmdbNodes(nodes: List<TNodeRecord>) {
        val rawCmdbNodes = esbAgentClient.getCmdbNodeByIps("devops", nodes.map { it.nodeIp })
        val ipToNodeMap = rawCmdbNodes.nodes.associateBy { it.ip }
        nodes.forEach {
            if (ipToNodeMap.containsKey(it.nodeIp)) {
                val cmdbNode = ipToNodeMap.getValue(it.nodeIp)
                it.operator = cmdbNode.operator
                it.bakOperator = cmdbNode.bakOperator
                it.osName = cmdbNode.osName
            }
        }
        nodeDao.batchUpdateNode(dslContext, nodes)
    }

    private fun updateCcNodeStatus() {
        logger.info("updateCcNodeStatus")
        val allCcNodes = nodeDao.listAllNodesByType(dslContext, NodeType.CC)

        if (allCcNodes.isEmpty()) {
            return
        }

        val rawCcNodes = esbAgentClient.getCcNodeByIps("devops", allCcNodes.map { it.nodeIp })
        val ipToNodeMap = rawCcNodes.associateBy { it.ip }

        allCcNodes.forEach {
            if (ipToNodeMap.containsKey(it.nodeIp)) {
                val ccNode = ipToNodeMap.getValue(it.nodeIp)
                it.operator = ccNode.operator
                it.bakOperator = ccNode.bakOperator
                it.osName = ccNode.osName
            }
        }
        nodeDao.batchUpdateNode(dslContext, allCcNodes)
    }
}