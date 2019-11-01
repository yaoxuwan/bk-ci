package com.tencent.devops.process.engine.atom.task.deploy

import com.fasterxml.jackson.databind.ObjectMapper
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.gcloud.GcloudClient
import com.tencent.devops.common.gcloud.api.pojo.CommonParam
import com.tencent.devops.common.gcloud.api.pojo.DeleteVerParam
import com.tencent.devops.common.pipeline.element.GcloudDeleteVersionElement
import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.log.utils.LogUtils
import com.tencent.devops.plugin.api.ServiceGcloudConfResource
import com.tencent.devops.process.engine.atom.AtomResponse
import com.tencent.devops.process.engine.atom.IAtomTask
import com.tencent.devops.process.engine.pojo.PipelineBuildTask
import com.tencent.devops.process.util.gcloud.TicketUtil
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
class GcloudDeleteVersionTaskAtom @Autowired constructor(
    private val client: Client,
    private val objectMapper: ObjectMapper,
    private val rabbitTemplate: RabbitTemplate
) : IAtomTask<GcloudDeleteVersionElement> {

    override fun execute(task: PipelineBuildTask, param: GcloudDeleteVersionElement, runVariables: Map<String, String>): AtomResponse {
        parseParam(param, runVariables)
        LogUtils.addLine(rabbitTemplate, task.buildId, "gcloud element params:\n $param", task.taskId, task.executeCount ?: 1)

        val gcloudUtil = TicketUtil(client)
        with(param) {
            val elementId = task.taskId
            LogUtils.addLine(rabbitTemplate, task.buildId, "正在开始更新gcloud版本配置信息，结果可以稍后前往查看：\n", elementId, task.executeCount ?: 1)
            LogUtils.addLine(rabbitTemplate, task.buildId, "<a target='_blank' href='http://console.gcloud.oa.com/dolphin/edit/$gameId/$productId/$versionStr'>查看详情</a>", elementId, task.executeCount ?: 1)

            val projectId = task.projectId
            val buildId = task.buildId
            val userId = task.starter

            // 获取accessId和accessKey
            val keyPair = gcloudUtil.getAccesIdAndToken(projectId, ticketId)
            val accessId = keyPair.first
            val accessKey = keyPair.second

            val commonParam = CommonParam(gameId, accessId, accessKey)
            val host = client.get(ServiceGcloudConfResource::class).getByConfigId(configId.toInt()).data
                    ?: throw RuntimeException("unknown configId($configId)")
            val gcloudClient = GcloudClient(objectMapper, host.address, host.fileAddress)

            // step1
            val delVerParam = DeleteVerParam(userId, productId.toInt(), versionStr)
            LogUtils.addLine(rabbitTemplate, buildId, "删除版本的配置信息：\n$delVerParam", elementId, task.executeCount ?: 1)
            gcloudClient.deleteVersion(delVerParam, commonParam)
            LogUtils.addLine(rabbitTemplate, buildId, "删除版本成功：$versionStr!(gameId: $gameId, productId: $productId)", elementId, task.executeCount ?: 1)
            return AtomResponse(BuildStatus.SUCCEED)
        }
    }

    private fun parseParam(param: GcloudDeleteVersionElement, runVariables: Map<String, String>) {
        param.configId = parseVariable(param.configId, runVariables)
        param.gameId = parseVariable(param.gameId, runVariables)
        param.productId = parseVariable(param.productId, runVariables)
        param.ticketId = parseVariable(param.ticketId, runVariables)
        param.versionStr = parseVariable(param.versionStr, runVariables)
    }

    override fun getParamElement(task: PipelineBuildTask): GcloudDeleteVersionElement {
        return JsonUtil.mapTo(task.taskParams, GcloudDeleteVersionElement::class.java)
    }
}
