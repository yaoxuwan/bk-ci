package com.tencent.devops.notify.utils

import com.tencent.devops.common.api.util.JsonUtil
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.PropertySource
import org.springframework.core.Ordered

@Configuration
@PropertySource("classpath:/common-notify.properties")
@ConditionalOnWebApplication
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
class TOFAutoConfiguration {

    @Bean
    fun tofService() = TOFService(JsonUtil.getObjectMapper())

    @Bean
    @Primary
    fun tofConfiguration() = TOFConfiguration()
}