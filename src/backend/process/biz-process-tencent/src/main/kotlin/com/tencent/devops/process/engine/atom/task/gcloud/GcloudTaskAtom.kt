package com.tencent.devops.process.engine.atom.task.gcloud

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.api.util.JsonUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.element.GcloudElement
import com.tencent.devops.common.pipeline.enums.BuildStatus
import com.tencent.devops.log.utils.LogUtils
import com.tencent.devops.process.engine.atom.AtomResponse
import com.tencent.devops.process.engine.atom.IAtomTask
import com.tencent.devops.process.engine.atom.defaultFailAtomResponse
import com.tencent.devops.process.engine.pojo.PipelineBuildTask
import com.tencent.devops.process.pojo.AtomErrorCode
import com.tencent.devops.process.pojo.ErrorType
import com.tencent.devops.process.service.PipelineUserService
import com.tencent.devops.project.api.service.ServiceProjectResource
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

@Component
@Scope(SCOPE_PROTOTYPE)
class GcloudTaskAtom @Autowired constructor(
    private val rabbitTemplate: RabbitTemplate,
    private val objectMapper: ObjectMapper,
    private val pipelineUserService: PipelineUserService,
    private val client: Client
) : IAtomTask<GcloudElement> {

    @Value("\${gcloud.url}")
    private val gcloudApiUrl = "http://gcloud.apigw.o.oa.com/prod/api"

    override fun getParamElement(task: PipelineBuildTask): GcloudElement {
        return JsonUtil.mapTo(task.taskParams, GcloudElement::class.java)
    }

    override fun execute(task: PipelineBuildTask, param: GcloudElement, runVariables: Map<String, String>): AtomResponse {

        val executeCount = task.executeCount ?: 1
        val pAppId = client.get(ServiceProjectResource::class).get(task.projectId).data?.ccAppId?.toInt()
            ?: run {
                LogUtils.addLine(rabbitTemplate, task.buildId, "找不到绑定配置平台的业务ID/can not found CC Business ID", task.taskId,
                    executeCount
                )
                return defaultFailAtomResponse
            }
//        val pAppId = param.appId // 业务ID
        val pTemplateId = param.templateId // 模版ID
        val pApiAuthCode = parseVariable(param.apiAuthCode, runVariables) // API授权码
        val pTaskParameters: Map<String, String> = objectMapper.readValue(
                parseVariable(objectMapper.writeValueAsString(param.taskParameters), runVariables)) // 任务参数

        val pTimeoutInSeconds = param.timeoutInSeconds // 超时时间

        var operator = task.starter
        val pipelineId = task.pipelineId
        val lastModifyUserMap = pipelineUserService.listUpdateUsers(setOf(pipelineId))
        val lastModifyUser = lastModifyUserMap[pipelineId]
        if (null != lastModifyUser && operator != lastModifyUser) {
            // 以流水线的最后一次修改人身份执行；如果最后一次修改人也没有这个环境的操作权限，这种情况不考虑，有问题联系产品!
            logger.info("operator:$operator, lastModifyUser:$lastModifyUser")
            LogUtils.addLine(rabbitTemplate, task.buildId, "将以用户${lastModifyUser}执行文件传输/Will use $lastModifyUser to execute gcloud task...", task.taskId, executeCount)
            operator = lastModifyUser
        }

        LogUtils.addLine(rabbitTemplate, task.buildId, "标准运维执行失败/execute gcloud task failed! task($pTemplateId, $pApiAuthCode, $pTaskParameters)", task.taskId, executeCount)

        val gcloudTaskExecutor = GcloudTaskExecutor(rabbitTemplate, gcloudApiUrl, task.buildId)
        val result = gcloudTaskExecutor.syncRunGcloudTask(pAppId, operator, pApiAuthCode, pTemplateId, pTaskParameters, pTimeoutInSeconds, task.taskId, executeCount)
        return if (result.success) {
            LogUtils.addLine(rabbitTemplate, task.buildId, "标准运维执行成功/execute gcloud task success!", task.taskId, executeCount)
            AtomResponse(BuildStatus.SUCCEED)
        } else {
            LogUtils.addLine(rabbitTemplate, task.buildId, "标准运维执行失败/execute gcloud task failed!", task.taskId, executeCount)
            AtomResponse(
                buildStatus = BuildStatus.FAILED,
                errorType = ErrorType.USER,
                errorCode = AtomErrorCode.USER_TASK_OPERATE_FAIL,
                errorMsg = "execute gcloud task failed"
            )
        }
    }
    companion object {
        private val logger = LoggerFactory.getLogger(GcloudTaskAtom::class.java)
    }
}
