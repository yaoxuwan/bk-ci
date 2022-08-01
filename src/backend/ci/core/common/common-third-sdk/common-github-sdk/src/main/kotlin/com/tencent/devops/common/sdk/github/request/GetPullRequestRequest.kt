package com.tencent.devops.common.sdk.github.request

import com.tencent.devops.common.sdk.enums.HttpMethod
import com.tencent.devops.common.sdk.github.GithubRequest
import com.tencent.devops.common.sdk.github.response.PullRequestResponse

data class GetPullRequestRequest(
    // val owner: String,
    // val repo: String,
    val repoId: Long,
    val pullNumber: String
) : GithubRequest<PullRequestResponse>() {
    override fun getHttpMethod() = HttpMethod.GET

    override fun getApiPath() = "repositories/$repoId/pulls/$pullNumber"
}