package com.tencent.devops.external.service.github

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.api.exception.OperationException
import com.tencent.devops.common.api.util.HashUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.external.pojo.GithubOauth
import com.tencent.devops.external.pojo.GithubToken
import com.tencent.devops.repository.api.ServiceGithubResource
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import javax.ws.rs.core.Response
import javax.ws.rs.core.UriBuilder

@Service
class GithubOauthService @Autowired constructor(
    private val client: Client,
    private val objectMapper: ObjectMapper
) {
    private val GITHUB_URL = "https://github.com"

    @Value("\${github.clientId}")
    private lateinit var clientId: String

    @Value("\${github.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${github.callbackUrl}")
    private lateinit var callbackUrl: String

    @Value("\${github.redirectUrl}")
    private lateinit var redirectUrl: String

    @Value("\${github.appUrl}")
    private lateinit var appUrl: String

    fun getGithubOauth(projectId: String, userId: String, repoHashId: String?): GithubOauth {
        val repoId = if (!repoHashId.isNullOrBlank()) HashUtil.decodeOtherIdToLong(repoHashId!!).toString() else ""
        val state = "$userId,$projectId,$repoId,BK_DEVOPS__${RandomStringUtils.randomAlphanumeric(8)}"
        val redirectUrl = "$GITHUB_URL/login/oauth/authorize?client_id=$clientId&redirect_uri=$callbackUrl&state=$state"
        return GithubOauth(redirectUrl)
    }

    fun getGithubAppUrl() = appUrl

    fun githubCallback(code: String, state: String): Response {
        if (!state.contains(",BK_DEVOPS__")) {
            throw OperationException("TGIT call back contain invalid parameter: $state")
        }

        val arr = state.split(",")
        val userId = arr[0]
        val projectId = arr[1]
        val repoHashId = if (arr[2].isNotBlank()) HashUtil.encodeOtherLongId(arr[2].toLong()) else ""
        val githubToken = getAccessToken(code)

        client.get(ServiceGithubResource::class).createAccessToken(userId, githubToken.accessToken, githubToken.tokenType, githubToken.scope)
        return javax.ws.rs.core.Response.temporaryRedirect(UriBuilder.fromUri("$redirectUrl/$projectId#popupGithub$repoHashId").build()).build()
    }

    private fun getAccessToken(code: String): GithubToken {
        val url = "$GITHUB_URL/login/oauth/access_token?client_id=$clientId&client_secret=$clientSecret&code=$code"

        val request = Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"), ""))
                .build()
        OkhttpUtils.doHttp(request).use { response ->
//        okHttpClient.newCall(request).execute().use { response ->
            val data = response.body()!!.string()
            if (!response.isSuccessful) {
                logger.info("Github get code(${response.code()}) and response($data)")
                throw CustomException(Response.Status.INTERNAL_SERVER_ERROR, "获取Github access_token失败")
            }
            return objectMapper.readValue(data)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(this::class.java)
//        private val okHttpClient = okhttp3.OkHttpClient.Builder()
//                .connectTimeout(5L, TimeUnit.SECONDS)
//                .readTimeout(60L, TimeUnit.SECONDS)
//                .writeTimeout(60L, TimeUnit.SECONDS)
//                .build()
    }
}
