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

package com.tencent.devops.common.auth

import com.fasterxml.jackson.databind.ObjectMapper
import com.tencent.bk.sdk.iam.config.IamConfiguration
import com.tencent.bk.sdk.iam.service.impl.ApigwHttpClientServiceImpl
import com.tencent.bk.sdk.iam.service.impl.ManagerServiceImpl
import com.tencent.devops.common.auth.api.BSAuthPermissionApi
import com.tencent.devops.common.auth.api.BSAuthResourceApi
import com.tencent.devops.common.auth.api.BSAuthTokenApi
import com.tencent.devops.common.auth.api.BkAuthProperties
import com.tencent.devops.common.auth.api.gitci.GitCIAuthProjectApi
import com.tencent.devops.common.auth.code.BSArtifactoryAuthServiceCode
import com.tencent.devops.common.auth.code.BSBcsAuthServiceCode
import com.tencent.devops.common.auth.code.BSCodeAuthServiceCode
import com.tencent.devops.common.auth.code.BSEnvironmentAuthServiceCode
import com.tencent.devops.common.auth.code.BSExperienceAuthServiceCode
import com.tencent.devops.common.auth.code.BSPipelineAuthServiceCode
import com.tencent.devops.common.auth.code.BSProjectServiceCodec
import com.tencent.devops.common.auth.code.BSQualityAuthServiceCode
import com.tencent.devops.common.auth.code.BSRepoAuthServiceCode
import com.tencent.devops.common.auth.code.BSTicketAuthServiceCode
import com.tencent.devops.common.auth.code.BSVSAuthServiceCode
import com.tencent.devops.common.auth.code.BSWetestAuthServiceCode
import com.tencent.devops.common.auth.jmx.JmxAuthApi
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.client.ClientTokenService
import com.tencent.devops.common.redis.RedisOperation
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.core.Ordered
import org.springframework.jmx.export.MBeanExporter

@Configuration
@ConditionalOnWebApplication
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@ConditionalOnProperty(prefix = "auth", name = ["idProvider"], havingValue = "git")
class GitCIAuthAutoConfiguration {
    @Value("\${auth.url:}")
    val iamBaseUrl = ""

    @Value("\${auth.iamSystem:}")
    val systemId = ""

    @Value("\${auth.appCode:}")
    val appCode = ""

    @Value("\${auth.appSecret:}")
    val appSecret = ""

    @Value("\${auth.apigwUrl:#{null}}")
    val iamApigw = ""

    // TODO: 优化gitCI实例化project逻辑
    @Bean
    @ConditionalOnMissingBean
    fun iamConfiguration() = IamConfiguration(systemId, appCode, appSecret, iamBaseUrl, iamApigw)

    @Bean
    fun apigwHttpClientServiceImpl() = ApigwHttpClientServiceImpl(iamConfiguration())

    @Bean
    fun iamManagerService() = ManagerServiceImpl(apigwHttpClientServiceImpl(), iamConfiguration())

    @Bean
    @Primary
    fun bkAuthProperties() = BkAuthProperties()

    @Bean
    @Primary
    fun authTokenApi(bkAuthProperties: BkAuthProperties, objectMapper: ObjectMapper, redisOperation: RedisOperation) =
        BSAuthTokenApi(bkAuthProperties, objectMapper, redisOperation)

    @Bean
    @Primary
    fun authPermissionApi(
        bkAuthProperties: BkAuthProperties,
        objectMapper: ObjectMapper,
        bsAuthTokenApi: BSAuthTokenApi,
        jmxAuthApi: JmxAuthApi
    ) =
        BSAuthPermissionApi(bkAuthProperties, objectMapper, bsAuthTokenApi, jmxAuthApi)

    @Bean
    @Primary
    fun authResourceApi(
        bkAuthProperties: BkAuthProperties,
        objectMapper: ObjectMapper,
        bsAuthTokenApi: BSAuthTokenApi
    ) =
        BSAuthResourceApi(bkAuthProperties, objectMapper, bsAuthTokenApi)

    @Bean
    @Primary
    fun authProjectApi(
        client: Client,
        tokenService: ClientTokenService
    ) =
        GitCIAuthProjectApi(client, tokenService)

    @Bean
    fun jmxAuthApi(mBeanExporter: MBeanExporter) = JmxAuthApi(mBeanExporter)

    @Bean
    fun bcsAuthServiceCode() = BSBcsAuthServiceCode()

    @Bean
    fun bsPipelineAuthServiceCode() = BSPipelineAuthServiceCode()

    @Bean
    fun codeAuthServiceCode() = BSCodeAuthServiceCode()

    @Bean
    fun vsAuthServiceCode() = BSVSAuthServiceCode()

    @Bean
    fun environmentAuthServiceCode() = BSEnvironmentAuthServiceCode()

    @Bean
    fun repoAuthServiceCode() = BSRepoAuthServiceCode()

    @Bean
    fun ticketAuthServiceCode() = BSTicketAuthServiceCode()

    @Bean
    fun qualityAuthServiceCode() = BSQualityAuthServiceCode()

    @Bean
    fun wetestAuthServiceCode() = BSWetestAuthServiceCode()

    @Bean
    fun experienceAuthServiceCode() = BSExperienceAuthServiceCode()

    @Bean
    fun projectAuthSeriviceCode() = BSProjectServiceCodec()

    @Bean
    fun artifactoryAuthServiceCode() = BSArtifactoryAuthServiceCode()
}
