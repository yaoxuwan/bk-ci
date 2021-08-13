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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.common.cache.CacheBuilder
import com.tencent.bk.sdk.iam.constants.ManagerScopesEnum
import com.tencent.bk.sdk.iam.dto.response.ResponseDTO
import com.tencent.bk.sdk.iam.util.JsonUtil
import com.tencent.devops.auth.common.Constants.LEVEL
import com.tencent.devops.auth.common.Constants.PARENT
import com.tencent.devops.auth.common.Constants.HTTP_RESULT
import com.tencent.devops.auth.common.Constants.NAME
import com.tencent.devops.auth.common.Constants.USERNAME
import com.tencent.devops.auth.common.Constants.USER_LABLE
import com.tencent.devops.auth.entity.SearchUserAndDeptEntity
import com.tencent.devops.auth.entity.SearchDeptUserEntity
import com.tencent.devops.auth.entity.SearchProfileDeptEntity
import com.tencent.devops.auth.entity.SearchRetrieveDeptEntity
import com.tencent.devops.auth.pojo.vo.BkUserInfoVo
import com.tencent.devops.auth.pojo.vo.DeptInfoVo
import com.tencent.devops.auth.pojo.vo.UserAndDeptInfoVo
import com.tencent.devops.common.api.exception.RemoteServiceException
import com.tencent.devops.common.api.util.OkhttpUtils
import com.tencent.devops.common.auth.api.pojo.EsbBaseReq
import com.tencent.devops.common.redis.RedisOperation
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import java.util.concurrent.TimeUnit

class AuthDeptServiceImpl @Autowired constructor(
    val redisOperation: RedisOperation,
    val objectMapper: ObjectMapper
) : DeptService {

    @Value("\${esb.code:#{null}}")
    val appCode: String? = null

    @Value("\${esb.secret:#{null}}")
    val appSecret: String? = null

    @Value("\${bk.user.host:#{null}}")
    val bkUserHost: String? = null

    private val deptUserCache = CacheBuilder.newBuilder()
        .maximumSize(500)
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build<String/*deptId*/, List<String>>()

    override fun getDeptByLevel(level: Int, accessToken: String?, userId: String): DeptInfoVo {
        val search = SearchUserAndDeptEntity(
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = userId,
            fields = null,
            lookupField = LEVEL,
            exactLookups = level,
            fuzzyLookups = null,
            accessToken = accessToken
        )
        return getDeptInfo(search)
    }

    override fun getDeptByParent(parentId: Int, accessToken: String?, userId: String, pageSize: Int?): DeptInfoVo {
        val search = SearchUserAndDeptEntity(
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = userId,
            fields = null,
            lookupField = PARENT,
            exactLookups = parentId,
            fuzzyLookups = null,
            accessToken = accessToken,
            pageSize = pageSize ?: null
        )
        return getDeptInfo(search)
    }

    override fun getUserAndDeptByName(
        name: String,
        accessToken: String?,
        userId: String,
        type: ManagerScopesEnum
    ): List<UserAndDeptInfoVo?> {
        val deptSearch = SearchUserAndDeptEntity(
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = userId,
            fields = null,
            lookupField = NAME,
            exactLookups = null,
            fuzzyLookups = name,
            accessToken = accessToken
        )
        val userSearch = SearchUserAndDeptEntity(
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = userId,
            fields = USER_LABLE,
            lookupField = USERNAME,
            exactLookups = null,
            fuzzyLookups = name,
            accessToken = accessToken
        )
        val userAndDeptInfos = mutableListOf<UserAndDeptInfoVo>()
        when (type) {
            ManagerScopesEnum.USER -> {
                val userInfos = getUserInfo(userSearch)
                userInfos.results.forEach {
                    userAndDeptInfos.add(
                        UserAndDeptInfoVo(
                            id = it.id,
                            name = it.username,
                            type = ManagerScopesEnum.USER
                        )
                    )
                }
            }
            ManagerScopesEnum.DEPARTMENT -> {
                val depteInfos = getDeptInfo(deptSearch)
                depteInfos.results.forEach {
                    userAndDeptInfos.add(
                        UserAndDeptInfoVo(
                            id = it.id,
                            name = it.name,
                            type = ManagerScopesEnum.DEPARTMENT,
                            hasChild = it.hasChildren
                        )
                    )
                }
            }
            ManagerScopesEnum.ALL -> {
                val userInfos = getUserInfo(userSearch)
                userInfos.results.forEach {
                    userAndDeptInfos.add(
                        UserAndDeptInfoVo(
                            id = it.id,
                            name = it.username,
                            type = ManagerScopesEnum.USER
                        )
                    )
                }
                val depteInfos = getDeptInfo(deptSearch)
                depteInfos.results.forEach {
                    userAndDeptInfos.add(
                        UserAndDeptInfoVo(
                            id = it.id,
                            name = it.name,
                            type = ManagerScopesEnum.DEPARTMENT,
                            hasChild = it.hasChildren
                        )
                    )
                }
            }
        }

        return userAndDeptInfos
    }

    override fun getDeptUser(deptId: Int, accessToken: String?): List<String> {
        return if (deptUserCache.getIfPresent(deptId.toString()) != null) {
            deptUserCache.getIfPresent(deptId.toString())!!
        } else {
            val deptUsers = getAndRefreshDeptUser(deptId, accessToken)
            deptUserCache.put(deptId.toString(), deptUsers)
            deptUsers
        }
    }

    override fun getUserParentDept(userId: String): Int {
        val deptSearch = SearchProfileDeptEntity(
            id = userId,
            with_family = true,
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = userId
        )
        val deptSearchResponse = callUserCenter(LIST_PROFILE_DEPARTMENTS, deptSearch)
        val deptId = getUserDept(deptSearchResponse)
        val parentSearch = SearchRetrieveDeptEntity(
            id = deptId,
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = userId
        )
        val userCenterResponse = callUserCenter(RETRIEVE_DEPARTMENT, parentSearch)
        return getParentDept(userCenterResponse)
    }

    private fun getAndRefreshDeptUser(deptId: Int, accessToken: String?): List<String> {
        val search = SearchDeptUserEntity(
            id = deptId,
            recursive = true,
            bk_app_code = appCode!!,
            bk_app_secret = appSecret!!,
            bk_username = "",
            accessToken = accessToken
        )
        val url = getAuthRequestUrl(LIST_DEPARTMENT_PROFILES)
        val responseStr = callUserCenter(url, search)
        return findUserName(responseStr)
    }

    private fun getDeptInfo(searchDeptEnity: SearchUserAndDeptEntity): DeptInfoVo {
        val url = getAuthRequestUrl(LIST_DEPARTMENTS)
        val responseDTO = callUserCenter(url, searchDeptEnity)
        return objectMapper.readValue<DeptInfoVo>(JsonUtil.toJson(responseDTO))
    }

    private fun getUserInfo(searchUserEntity: SearchUserAndDeptEntity): BkUserInfoVo {
        val url = getAuthRequestUrl(USER_INFO)

        val responseDTO = callUserCenter(url, searchUserEntity)

        return objectMapper.readValue<BkUserInfoVo>(JsonUtil.toJson(responseDTO))
    }

    private fun callUserCenter(url: String, searchEntity: EsbBaseReq): String {
        val url = getAuthRequestUrl(url)
        val content = objectMapper.writeValueAsString(searchEntity)
        val mediaType = MediaType.parse("application/json; charset=utf-8")
        val requestBody = RequestBody.create(mediaType, content)
        val request = Request.Builder().url(url)
            .post(requestBody)
            .build()
        OkhttpUtils.doHttp(request).use {
            if (!it.isSuccessful) {
                // 请求错误
                throw RemoteServiceException("call user center fail, response: ($it)")
            }
            val responseStr = it.body()!!.string()
            logger.info("user center response： $responseStr")
            val responseDTO = JsonUtil.fromJson(responseStr, ResponseDTO::class.java)
            if (responseDTO.code != 0L || responseDTO.result == false) {
                // 请求错误
                throw RemoteServiceException(
                    "call user center fail: $responseStr"
                )
            }
            logger.info("user center response：${objectMapper.writeValueAsString(responseDTO.data)}")
            return objectMapper.writeValueAsString(responseDTO.data)
        }
    }


    fun findUserName(str: String): List<String> {
        val dataMap = JsonUtil.fromJson(str, Map::class.java)
        val userInfoList = JsonUtil.fromJson(JsonUtil.toJson(dataMap[HTTP_RESULT]), List::class.java)
        val users = mutableListOf<String>()
        userInfoList.forEach {
            val userInfo = JsonUtil.toJson(it)
            val userInfoMap = JsonUtil.fromJson(userInfo, Map::class.java)
            val userName = userInfoMap.get("username").toString()
            users.add(userName)
        }

        return users
    }

    private fun getParentDept(responseData: String): Int {
        val dataMap = JsonUtil.fromJson(responseData, Map::class.java)
        return dataMap["parent"]?.toString()?.toInt() ?: 0
    }

    fun getUserDept(responseData: String): Int {
        val deptInfo = JsonUtil.fromJson(responseData, List::class.java)
        val any = deptInfo[0] as Any
        if (any is Map<*, *>) {
            return any["id"].toString().toInt()
        }
        return 0
    }

    /**
     * 生成请求url
     */
    private fun getAuthRequestUrl(uri: String): String {
        return if (bkUserHost?.endsWith("/")!!) {
            bkUserHost + uri
        } else {
            "$bkUserHost/$uri"
        }
    }

    companion object {
        val logger = LoggerFactory.getLogger(AuthDeptServiceImpl::class.java)
        const val LIST_DEPARTMENTS = "api/c/compapi/v2/usermanage/list_departments/"
        const val LIST_DEPARTMENT_PROFILES = "api/c/compapi/v2/usermanage/list_department_profiles/"
        const val USER_INFO = "api/c/compapi/v2/usermanage/list_users/"
        const val RETRIEVE_DEPARTMENT = "api/c/compapi/v2/usermanage/retrieve_department/"
        const val LIST_PROFILE_DEPARTMENTS = "api/c/compapi/v2/usermanage/list_profile_departments/"
    }
}
