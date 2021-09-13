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

package com.tencent.devops.stream.cron

import com.tencent.devops.common.redis.RedisLock
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.stream.config.StreamCronConfig
import com.tencent.devops.stream.dao.GitPipelineResourceDao
import com.tencent.devops.stream.dao.GitRequestEventBuildDao
import com.tencent.devops.stream.dao.GitRequestEventDao
import com.tencent.devops.stream.dao.GitRequestEventNotBuildDao
import com.tencent.devops.stream.v2.dao.GitUserMessageDao
import com.tencent.devops.stream.v2.service.GitCIBasicSettingService
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct

@Component
class StreamEventHistoryClearJob @Autowired constructor(
    private val dslContext: DSLContext,
    private val redisOperation: RedisOperation,
    private val streamCronConfig: StreamCronConfig,
    private val gitPipelineResourceDao: GitPipelineResourceDao,
    private val gitRequestEventDao: GitRequestEventDao,
    private val gitRequestEventBuildDao: GitRequestEventBuildDao,
    private val gitRequestEventNotBuildDao: GitRequestEventNotBuildDao,
    private val gitUserMessageDao: GitUserMessageDao,
    private val streamBasicSettingService: GitCIBasicSettingService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(StreamEventHistoryClearJob::class.java)
        private const val LOCK_KEY = "StreamEventHistoryClear"
        private const val DEFAULT_PAGE_SIZE = 100
        private const val STREAM_PIPELINE_BUILD_HISTORY_CLEAR_GIT_PROJECT_ID_KEY =
            "stream:pipeline:build:history:clear:git:project:id"
        private const val STREAM_PIPELINE_BUILD_HISTORY_CLEAR_GIT_PROJECT_LIST_KEY =
            "stream:pipeline:build:history:clear:git:project:list"
        private const val STREAM_PIPELINE_BUILD_HISTORY_CLEAR_THREAD_SET_KEY =
            "stream:pipeline:build:history:clear:thread:set"
        private var executor: ThreadPoolExecutor? = null
    }

    @PostConstruct
    fun init() {
        logger.info("start init streamEventHistoryClearJob")
        // 启动的时候删除redis中存储的清理线程集合，防止redis中的线程信息因为服务异常停了无法删除
        redisOperation.delete(STREAM_PIPELINE_BUILD_HISTORY_CLEAR_THREAD_SET_KEY, true)
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 12000)
    fun streamEventHistoryClear() {
        logger.info("streamEventHistoryClear start")
        if (executor == null) {
            // 创建带有边界队列的线程池，防止内存爆掉
            logger.info("pipelineBuildHistoryDataClear create executor")
            executor = ThreadPoolExecutor(
                streamCronConfig.maxThreadHandleProjectNum,
                streamCronConfig.maxThreadHandleProjectNum,
                0L,
                TimeUnit.MILLISECONDS,
                LinkedBlockingQueue(10),
                Executors.defaultThreadFactory(),
                ThreadPoolExecutor.DiscardPolicy()
            )
        }
        val lock = RedisLock(
            redisOperation,
            LOCK_KEY, 3000
        )
        try {
            if (!lock.tryLock()) {
                logger.info("get lock failed, skip")
                return
            }
            // 查询project表中的项目数据处理
            val gitProjectIdListConfig = redisOperation.get(
                key = STREAM_PIPELINE_BUILD_HISTORY_CLEAR_GIT_PROJECT_LIST_KEY,
                isDistinguishCluster = true
            )
            // 组装查询项目的条件
            var gitProjectIdList: List<Long>? = null
            if (!gitProjectIdListConfig.isNullOrBlank()) {
                gitProjectIdList = gitProjectIdListConfig.split(",").map { it.toLong() }
            }
            val maxProjectNum = if (!gitProjectIdList.isNullOrEmpty()) {
                gitProjectIdList.size.toLong()
            } else {
                streamBasicSettingService.getMaxId(gitProjectIdList)
            }
            // 获取清理项目构建数据的线程数量
            val maxThreadHandleProjectNum = streamCronConfig.maxThreadHandleProjectNum
            val avgProjectNum = maxProjectNum / maxThreadHandleProjectNum
            for (index in 1..maxThreadHandleProjectNum) {
                // 计算线程能处理的最大项目主键ID
                val maxThreadProjectPrimaryId = if (index != maxThreadHandleProjectNum) {
                    index * avgProjectNum
                } else {
                    index * avgProjectNum + maxProjectNum % maxThreadHandleProjectNum
                }
                // 判断线程是否正在处理任务，如正在处理则不分配新任务(定时任务12秒执行一次，线程启动到往set集合设置编号耗费时间很短，故不加锁)
                if (!redisOperation.isMember(
                        key = STREAM_PIPELINE_BUILD_HISTORY_CLEAR_THREAD_SET_KEY,
                        item = index.toString(),
                        isDistinguishCluster = true
                    )
                ) {
                    doClearBus(
                        threadNo = index,
                        gitProjectIdList = gitProjectIdList,
                        minThreadProjectPrimaryId = (index - 1) * avgProjectNum,
                        maxThreadProjectPrimaryId = maxThreadProjectPrimaryId
                    )
                }
            }
        } catch (t: Throwable) {
            logger.warn("streamEventHistoryClear failed", t)
        } finally {
            lock.unlock()
        }
    }

    private fun doClearBus(
        threadNo: Int,
        gitProjectIdList: List<Long>?,
        minThreadProjectPrimaryId: Long,
        maxThreadProjectPrimaryId: Long
    ): Future<Boolean> {
        val threadName = "Thread-$threadNo"
        return executor!!.submit(Callable<Boolean> {
            var handleProjectPrimaryId =
                redisOperation.get(
                    key = "$threadName:$STREAM_PIPELINE_BUILD_HISTORY_CLEAR_GIT_PROJECT_ID_KEY",
                    isDistinguishCluster = true
                )?.toLong()
            if (handleProjectPrimaryId == null) {
                handleProjectPrimaryId = minThreadProjectPrimaryId
            } else {
                if (handleProjectPrimaryId >= maxThreadProjectPrimaryId) {
                    // 已经清理完全部项目的流水线的过期构建记录，再重新开始清理
                    redisOperation.delete(
                        key = "$threadName:$STREAM_PIPELINE_BUILD_HISTORY_CLEAR_GIT_PROJECT_ID_KEY",
                        isDistinguishCluster = true
                    )
                    logger.info("streamEventHistoryClear $threadName reStart")
                    return@Callable true
                }
            }
            // 将线程编号存入redis集合
            redisOperation.sadd(
                STREAM_PIPELINE_BUILD_HISTORY_CLEAR_THREAD_SET_KEY,
                threadNo.toString(),
                isDistinguishCluster = true
            )
            try {
                val maxEveryProjectHandleNum = streamCronConfig.maxEveryProjectHandleNum
                var maxHandleProjectPrimaryId = handleProjectPrimaryId ?: 0L
                val basicSettingIdList = if (gitProjectIdList.isNullOrEmpty()) {
                    maxHandleProjectPrimaryId = handleProjectPrimaryId + maxEveryProjectHandleNum
                    streamBasicSettingService.getBasicSettingList(
                        minId = handleProjectPrimaryId,
                        maxId = maxHandleProjectPrimaryId
                    )
                } else {
                    streamBasicSettingService.getBasicSettingList(gitProjectIdList = gitProjectIdList)
                }
                // 根据项目依次查询T_GIT_PIPELINE_RESOURCE表中的流水线数据处理
                basicSettingIdList?.forEach { id ->
                    if (id > maxHandleProjectPrimaryId) {
                        maxHandleProjectPrimaryId = id
                    }
                    clearPipelineBuildData(id)
                }
            } catch (ignore: Exception) {
                logger.warn("streamEventHistoryClear doClearBus failed", ignore)
            } finally {
                // 释放redis集合中的线程编号
                redisOperation.sremove(
                    key = STREAM_PIPELINE_BUILD_HISTORY_CLEAR_THREAD_SET_KEY,
                    values = threadNo.toString(),
                    isDistinguishCluster = true
                )
            }
            return@Callable true
        })
    }

    private fun clearPipelineBuildData(
        gitProjectId: Long
    ) {
        // 获取当前项目下流水线记录的最小主键EventID值
        var minId = gitPipelineResourceDao.getMinByGitProjectId(dslContext, gitProjectId)
        do {
            logger.info("streamEventHistoryClear clearPipelineBuildData gitProjectId:$gitProjectId,minId:$minId")
            val pipelineIdList = gitPipelineResourceDao.getPipelineIdListByProjectId(
                dslContext = dslContext,
                gitProjectId = gitProjectId,
                minId = minId,
                limit = DEFAULT_PAGE_SIZE.toLong()
            )?.map { it.getValue(0) as String }
            if (!pipelineIdList.isNullOrEmpty()) {
                // 重置minId的值
                minId = (gitPipelineResourceDao.getPipelineById(
                    dslContext = dslContext,
                    gitProjectId = gitProjectId,
                    pipelineId = pipelineIdList.last()
                )?.id ?: 0L) + 1
            }
            pipelineIdList?.forEach { pipelineId ->
                logger.info("streamEventHistoryClear start..............")
                cleanNormalPipelineData(pipelineId, gitProjectId)
            }
        } while (pipelineIdList?.size == DEFAULT_PAGE_SIZE)
    }

    private fun cleanNormalPipelineData(
        pipelineId: String,
        gitProjectId: Long
    ) {
        // 判断构建记录是否超过系统展示的最大数量，如果超过则需清理超量的数据
        // TODO: notbuild和message和event清理
        val maxPipelineBuildNum =
            gitRequestEventBuildDao.getBuildCountByPipelineId(dslContext, gitProjectId, pipelineId)
        val maxBuildNum = maxPipelineBuildNum - streamCronConfig.maxPipelineHandleNum
        if (maxBuildNum > 0) {
            logger.info("streamEventHistoryClear start.............")
            cleanBuildHistoryData(
                pipelineId = pipelineId,
                gitProjectId = gitProjectId,
                maxBuildNum = maxBuildNum.toInt()
            )
        }
    }

    private fun cleanBuildHistoryData(
        pipelineId: String,
        gitProjectId: Long,
        maxBuildNum: Int? = null
    ) {

    }

}

