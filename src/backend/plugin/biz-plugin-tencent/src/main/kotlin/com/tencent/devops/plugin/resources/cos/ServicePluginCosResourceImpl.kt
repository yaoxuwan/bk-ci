package com.tencent.devops.plugin.resources.cos

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParser
import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.cos.COSClientConfig
import com.tencent.devops.common.redis.RedisOperation
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.log.utils.LogUtils
import com.tencent.devops.plugin.api.cos.ServicePluginCosResource
import com.tencent.devops.plugin.pojo.cos.CdnUploadFileInfo
import com.tencent.devops.plugin.pojo.cos.SpmFile
import com.tencent.devops.plugin.service.cos.CosService
import com.tencent.devops.plugin.service.cos.UploadCosCdnParam
import com.tencent.devops.plugin.service.cos.UploadCosCdnThread
import com.tencent.devops.plugin.utils.CommonUtils
import com.tencent.devops.ticket.pojo.enums.CredentialType
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import javax.ws.rs.core.Response

@RestResource
class ServicePluginCosResourceImpl @Autowired constructor(
    private val cosService: CosService,
    private val client: Client,
    private val redisOperation: RedisOperation,
    private val rabbitTemplate: RabbitTemplate
) : ServicePluginCosResource {

    @Value("\${gateway.url}")
    private val gatewayUrl: String? = null
    private val parser = JsonParser()

//    override fun uploadCos(projectId: String, pipelineId: String, buildId: String, fileInfo: CosUploadFileInfo): MutableMap<String, String> {
//        this.buildId = buildId
//        this.projectId = projectId
//        this.pipelineId = pipelineId
//        //参数校验
//        checkParam(fileInfo)
//
//        //根据ticketid从ticketService获取凭证信息
//        val ticketsMap = getCredential(projectId, fileInfo.ticketId, CredentialType.COS_APPID_SECRETID_SECRETKEY_REGION)
//        val cosClientConfig = COSClientConfig(ticketsMap["v1"]!!.toLong(), ticketsMap["v2"], ticketsMap["v3"], ticketsMap["v4"], EnvEnum.IDC)
//
//        return uploadFileToCos(fileInfo.regexPaths, fileInfo.customize, fileInfo.bucket, "/", cosClientConfig)
//    }

    override fun uploadCdn(
        projectId: String,
        pipelineId: String,
        buildId: String,
        elementId: String,
        executeCount: Int,
        cdnUploadFileInfo: CdnUploadFileInfo
    ): Result<SpmFile> {

        // 根据ticketid从ticketService获取凭证信息
        val ticketsMap =
            CommonUtils.getCredential(client, projectId, cdnUploadFileInfo.ticketId, CredentialType.APPID_SECRETKEY)
        // 根据spm的appId以及secretKey，调用spm接口，获取cos系统的appid，bucket，root_path以及业务外网CDN域名
        val spmAppId = ticketsMap["v1"].toString()
        val spmSecretKey = ticketsMap["v2"].toString()
        val cosAppInfo = getCosAppInfoFromSpm(spmAppId, spmSecretKey)
        val cosClientConfig = COSClientConfig(cosAppInfo.cosAppId, spmAppId, spmSecretKey)

        var cdnPath = if (cdnUploadFileInfo.cdnPathPrefix.startsWith("/")) {
            "/" + cosAppInfo.rootPath + cdnUploadFileInfo.cdnPathPrefix
        } else {
            "/" + cosAppInfo.rootPath + "/" + cdnUploadFileInfo.cdnPathPrefix
        }
        if (!cdnPath.endsWith("/")) {
            cdnPath = "$cdnPath/"
        }

        val uploadTaskKey = "upload_cdn_task_${projectId}_${pipelineId}_${buildId}_$elementId"
        val uploadCosCdnParam = UploadCosCdnParam(
            projectId, pipelineId, buildId, elementId, cdnUploadFileInfo.regexPaths,
            cdnUploadFileInfo.customize, cosAppInfo.bucket, cdnPath, cosAppInfo.domain, cosClientConfig
        )

        val uploadCosCdnThread =
            UploadCosCdnThread(gatewayUrl!!, rabbitTemplate, cosService, redisOperation, uploadCosCdnParam)
        val uploadThread = Thread(uploadCosCdnThread, uploadTaskKey)
        LogUtils.addLine(rabbitTemplate, buildId, "开始上传CDN...", elementId, executeCount)
        uploadThread.start()
        cdnPath = cosAppInfo.domain + cdnPath
        val spmFile = SpmFile(uploadTaskKey, cdnPath)
        return Result(spmFile)
    }

    private fun getCosAppInfoFromSpm(spmAppId: String, spmSecretKey: String): CosAppInfo {
        val url = "http://spm.oa.com/cdntool/get_bu_info.py"

        val requestData = mapOf(
            "bu_id" to spmAppId,
            "secret_key" to spmSecretKey
        )
        val requestBody = ObjectMapper().writeValueAsString(requestData)
        logger.info("POST url: $url")
        logger.info("requestBody: $requestBody")

        try {
            val request = Request.Builder().url(url).post(RequestBody.create(JSON, requestBody)).build()
            OkhttpUtils.doHttp(request).use { response ->
                val responseBody = response.body()?.string()
                logger.info("responseBody: $responseBody")

                val responseJson = parser.parse(responseBody).asJsonObject
                val code = responseJson["code"].asInt
                if (0 != code) {
                    val msg = responseJson.asJsonObject["msg"]
                    logger.error("Get cos app info from spm failed, msg:$msg")
                    throw CustomException(Response.Status.INTERNAL_SERVER_ERROR, "请求SPM失败, msg:$msg")
                }
                val rootPath = responseJson["root_path"].asString
                val domain = responseJson["domain"].asString
                val bucket = responseJson["bucket"].asString
                val appid = responseJson["appid"].asString

                return CosAppInfo(rootPath, domain, bucket, appid.toLong())
            }
        } catch (e: Exception) {
            logger.error("Get cos app info failed", e)
            throw Exception("Get cos app info failed.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ServicePluginCosResourceImpl::class.java)
        private val JSON = MediaType.parse("application/json;charset=utf-8")
    }

    data class CosAppInfo(val rootPath: String, val domain: String, val bucket: String, val cosAppId: Long)
}