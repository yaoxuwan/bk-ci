package com.tencent.devops.artifactory.service

import com.tencent.devops.artifactory.pojo.Url
import com.tencent.devops.artifactory.pojo.enums.ArtifactoryType
import com.tencent.devops.artifactory.util.JFrogUtil
import com.tencent.devops.common.api.util.ShaUtils
import com.tencent.devops.common.api.util.timestamp
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.ws.rs.BadRequestException

@Service
class ArtifactoryThirdPartyService @Autowired constructor(
    private val artifactoryService: ArtifactoryService,
    private val jFrogService: JFrogService
) {
    @Value("\${artifactory.thirdPartyUrl:#{null}}")
    private val artifactoryThirdPartyUrl: String? = null

    private val accessToken = "H9KSONm5DWdN2eSGhrXSE62PsjO9pG1l"

    fun createThirdPartyDownloadUrl(projectId: String, artifactoryType: ArtifactoryType, path: String): Url {
        val normalPath = JFrogUtil.normalize(path)
        if (!JFrogUtil.isValid(normalPath)) {
            logger.error("Path $path is not valid")
            throw BadRequestException("非法路径")
        }

        val realPath = JFrogUtil.getRealPath(projectId, artifactoryType, path)
        if (!jFrogService.exist(realPath)) {
            logger.error("Path $path is not exist")
            throw BadRequestException("文件不存在")
        }

        val relativePath = "$projectId/${path.removePrefix("/")}"
        val url = when (artifactoryType) {
            ArtifactoryType.PIPELINE -> "$artifactoryThirdPartyUrl/jfrog/storage/thirdparty/download/archive/$relativePath"
            ArtifactoryType.CUSTOM_DIR -> "$artifactoryThirdPartyUrl/jfrog/storage/thirdparty/download/custom/$relativePath"
        }

        val timestamp = LocalDateTime.now().timestamp()
        val sign = ShaUtils.sha1("access_token=$accessToken&path=$relativePath&timestamp=$timestamp".toByteArray())
        return Url("$url?timestamp=$timestamp&sign=$sign")
    }

    fun createThirdPartyUploadUrl(projectId: String, artifactoryType: ArtifactoryType, path: String): Url {
        val normalPath = JFrogUtil.normalize(path)
        if (!JFrogUtil.isValid(normalPath)) {
            logger.error("Path $path is not valid")
            throw BadRequestException("非法路径")
        }

        val relativePath = "$projectId/${path.removePrefix("/")}"
        val url = when (artifactoryType) {
            ArtifactoryType.PIPELINE -> "$artifactoryThirdPartyUrl/jfrog/storage/thirdparty/upload/archive/$relativePath"
            ArtifactoryType.CUSTOM_DIR -> "$artifactoryThirdPartyUrl/jfrog/storage/thirdparty/upload/custom/$relativePath"
        }

        val timestamp = LocalDateTime.now().timestamp()
        val sign = ShaUtils.sha1("access_token=$accessToken&path=$relativePath&timestamp=$timestamp".toByteArray())
        return Url("$url?timestamp=$timestamp&sign=$sign")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ArtifactoryType::class.java)
    }
}