package com.tencent.devops.monitoring.resources

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.kafka.KafkaClient
import com.tencent.devops.common.kafka.KafkaTopic.BUILD_ATOM_REPORT_TOPIC_PREFIX
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.monitoring.api.service.BuildReportResource
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class BuildReportResourceImpl @Autowired constructor(
    private val kafkaClient: KafkaClient
) : BuildReportResource {

    override fun atomReport(atomCode: String, data: String): Result<Boolean> {
        kafkaClient.send(topic = "$BUILD_ATOM_REPORT_TOPIC_PREFIX-$atomCode", msg = data)
        return Result(true)
    }
}
