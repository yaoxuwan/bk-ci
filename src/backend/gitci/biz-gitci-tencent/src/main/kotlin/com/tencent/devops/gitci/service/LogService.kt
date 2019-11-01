package com.tencent.devops.gitci.service

import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.common.client.Client
import com.tencent.devops.gitci.dao.GitProjectPipelineDao
import com.tencent.devops.log.api.ServiceLogResource
import com.tencent.devops.log.model.pojo.QueryLogs
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Service
class LogService @Autowired constructor(
    private val client: Client,
    private val dslContext: DSLContext,
    private val gitProjectPipelineDao: GitProjectPipelineDao
) {
    companion object {
        private val logger = LoggerFactory.getLogger(LogService::class.java)
    }

    @Value("\${gateway.url}")
    private lateinit var gatewayUrl: String

    fun getInitLogs(gitProjectId: Long, buildId: String, isAnalysis: Boolean?, queryKeywords: String?, tag: String?, jobId: String?, executeCount: Int?): QueryLogs {
        logger.info("get init logs, gitProjectId: $gitProjectId")
        val gitProjectPipeline = getProjectPipeline(gitProjectId)

        return client.get(ServiceLogResource::class).getInitLogs(
                gitProjectPipeline.projectCode,
                gitProjectPipeline.pipelineId,
                buildId,
                isAnalysis,
                queryKeywords,
                tag,
                jobId,
                executeCount
            ).data!!
    }

    fun getMoreLogs(gitProjectId: Long, buildId: String, num: Int?, fromStart: Boolean?, start: Long, end: Long, tag: String?, jobId: String?, executeCount: Int?): QueryLogs {
        logger.info("get more logs, gitProjectId: $gitProjectId")
        val gitProjectPipeline = getProjectPipeline(gitProjectId)
        return client.get(ServiceLogResource::class).getMoreLogs(
                gitProjectPipeline.projectCode,
                gitProjectPipeline.pipelineId,
                buildId,
                num,
                fromStart,
                start,
                end,
                tag,
                jobId,
                executeCount
        ).data!!
    }

    fun getAfterLogs(gitProjectId: Long, buildId: String, start: Long, isAnalysis: Boolean?, queryKeywords: String?, tag: String?, jobId: String?, executeCount: Int?): QueryLogs {
        logger.info("get after logs, gitProjectId: $gitProjectId")
        val gitProjectPipeline = getProjectPipeline(gitProjectId)
        return client.get(ServiceLogResource::class).getAfterLogs(
                gitProjectPipeline.projectCode,
                gitProjectPipeline.pipelineId,
                buildId,
                start,
                isAnalysis,
                queryKeywords,
                tag,
                jobId,
                executeCount
        ).data!!
    }

    fun downloadLogs(gitProjectId: Long, buildId: String, tag: String?, jobId: String?, executeCount: Int?): Response {
        logger.info("download logs, gitProjectId: $gitProjectId")
        val gitProjectPipeline = getProjectPipeline(gitProjectId)
        val response = OkhttpUtils.doLongGet("http://$gatewayUrl/log/api/service/logs/${gitProjectPipeline.projectCode}/${gitProjectPipeline.pipelineId}/$buildId/download?jobId=${jobId ?: ""}")
        return Response
                .ok(response.body()!!.byteStream(), MediaType.APPLICATION_OCTET_STREAM_TYPE)
                .header("content-disposition", "attachment; filename = ${gitProjectPipeline.pipelineId}-$buildId-log.txt")
                .header("Cache-Control", "no-cache")
                .build()
    }

    private fun getProjectPipeline(gitProjectId: Long) = gitProjectPipelineDao.get(dslContext, gitProjectId) ?: throw CustomException(Response.Status.FORBIDDEN, "项目未开启工蜂CI，无法查询")
}
