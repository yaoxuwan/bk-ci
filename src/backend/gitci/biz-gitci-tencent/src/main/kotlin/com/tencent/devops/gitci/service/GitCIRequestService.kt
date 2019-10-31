package com.tencent.devops.gitci.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.tencent.devops.common.api.exception.CustomException
import com.tencent.devops.common.api.util.YamlUtil
import com.tencent.devops.common.client.Client
import com.tencent.devops.gitci.OBJECT_KIND_MERGE_REQUEST
import com.tencent.devops.gitci.OBJECT_KIND_PUSH
import com.tencent.devops.gitci.OBJECT_KIND_TAG_PUSH
import com.tencent.devops.gitci.OBJECT_KIND_MANUAL
import com.tencent.devops.gitci.TASK_TYPE
import com.tencent.devops.gitci.dao.GitCISettingDao
import com.tencent.devops.gitci.dao.GitRequestEventBuildDao
import com.tencent.devops.gitci.dao.GitRequestEventDao
import com.tencent.devops.gitci.dao.GitRequestEventNotBuildDao
import com.tencent.devops.gitci.listener.GitCIRequestDispatcher
import com.tencent.devops.gitci.listener.GitCIRequestTriggerEvent
import com.tencent.devops.gitci.pojo.EnvironmentVariables
import com.tencent.devops.gitci.pojo.GitRequestEvent
import com.tencent.devops.gitci.pojo.TriggerBuildReq
import com.tencent.devops.gitci.pojo.enums.NotBuildReason
import com.tencent.devops.gitci.pojo.git.GitEvent
import com.tencent.devops.gitci.pojo.git.GitPushEvent
import com.tencent.devops.gitci.pojo.git.GitTagPushEvent
import com.tencent.devops.gitci.pojo.git.GitMergeRequestEvent
import com.tencent.devops.gitci.pojo.git.GitCommit
import com.tencent.devops.gitci.pojo.yaml.CIBuildYaml
import com.tencent.devops.gitci.pojo.yaml.MatchRule
import com.tencent.devops.gitci.pojo.yaml.Trigger
import com.tencent.devops.gitci.pojo.yaml.MergeRequest
import com.tencent.devops.gitci.pojo.yaml.Stage
import com.tencent.devops.gitci.pojo.yaml.Job
import com.tencent.devops.gitci.pojo.yaml.JobDetail
import com.tencent.devops.gitci.pojo.yaml.Pool
import com.tencent.devops.scm.api.ServiceGitResource
import org.jooq.DSLContext
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Date
import javax.ws.rs.core.Response

@Service
class GitCIRequestService @Autowired constructor(
    private val client: Client,
    private val objectMapper: ObjectMapper,
    private val dslContext: DSLContext,
    private val gitRequestEventDao: GitRequestEventDao,
    private val gitRequestEventBuildDao: GitRequestEventBuildDao,
    private val gitRequestEventNotBuildDao: GitRequestEventNotBuildDao,
    private val gitCISettingDao: GitCISettingDao,
    private val rabbitTemplate: RabbitTemplate
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GitCIRequestService::class.java)
    }

    private val ciFileName = ".ci.yml"

    fun triggerBuild(userId: String, triggerBuildReq: TriggerBuildReq): Boolean {
        logger.info("Trigger build, userId: $userId, triggerBuildReq: $triggerBuildReq")

        val gitRequestEvent = createGitRequestEvent(userId, triggerBuildReq)
        val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
        gitRequestEvent.id = id

        val yamlStr = if (triggerBuildReq.yaml.isNullOrBlank()) {
            logger.info("trigger request yaml is empty, get from git")
            val yamlGit = getYamlFromGit(gitRequestEvent)
            if (yamlGit.isNullOrBlank()) {
                logger.error("get ci yaml from git return null")
                gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, NotBuildReason.GIT_CI_YAML_NOT_FOUND.name, gitRequestEvent.gitProjectId)
                return false
            }
            yamlGit!!
        } else {
            triggerBuildReq.yaml!!
        }

        val yaml = try {
            createCIBuildYaml(triggerBuildReq.yaml!!, triggerBuildReq.gitProjectId)
        } catch (e: Throwable) {
            logger.error("git ci yaml is invalid")
            gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, yamlStr, null, NotBuildReason.GIT_CI_YAML_INVALID.name, gitRequestEvent.gitProjectId)
            return false
        }

        val normalizedYaml = YamlUtil.toYaml(yaml)
        logger.info("normalize yaml: $normalizedYaml")

        gitRequestEventBuildDao.save(dslContext, gitRequestEvent.id!!, yamlStr, normalizedYaml, gitRequestEvent.gitProjectId, gitRequestEvent.branch, gitRequestEvent.objectKind, triggerBuildReq.description)
        dispatchEvent(GitCIRequestTriggerEvent(gitRequestEvent, yaml))

        return true
    }

    fun externalCodeGitBuild(token: String, e: String): Boolean {
        logger.info("Trigger code git build($e)")

        val event = try {
            objectMapper.readValue<GitEvent>(e)
        } catch (e: Exception) {
            logger.warn("Fail to parse the git web hook commit event, errMsg: ${e.message}")
            return false
        }

        val gitRequestEvent = saveGitRequestEvent(event, e) ?: return true
        return matchAndTriggerPipeline(gitRequestEvent, event)
    }

    private fun matchAndTriggerPipeline(gitRequestEvent: GitRequestEvent, event: GitEvent): Boolean {
        if (!checkGitProjectConf(gitRequestEvent, event)) return false

        val yamlStr = getYamlFromGit(gitRequestEvent)
        if (yamlStr.isNullOrBlank()) {
            logger.error("get ci yaml from git return null")
            gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, NotBuildReason.GIT_CI_YAML_NOT_FOUND.name, gitRequestEvent.gitProjectId)
            return false
        }

        val yaml = try {
            createCIBuildYaml(yamlStr!!, gitRequestEvent.gitProjectId)
        } catch (e: Throwable) {
            logger.error("git ci yaml is invalid")
            gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, yamlStr, null, NotBuildReason.GIT_CI_YAML_INVALID.name, gitRequestEvent.gitProjectId)
            return false
        }

        val normalizedYaml = YamlUtil.toYaml(yaml)
        logger.info("normalize yaml: $normalizedYaml")

        val matcher = GitCIWebHookMatcher(event)
        return if (matcher.isMatch(yaml.trigger!!, yaml.mr!!)) {
            logger.info("Matcher is true, display the event, eventId: ${gitRequestEvent.id}")
            gitRequestEventBuildDao.save(dslContext, gitRequestEvent.id!!, yamlStr, normalizedYaml, gitRequestEvent.gitProjectId, gitRequestEvent.branch, gitRequestEvent.objectKind, "")
            dispatchEvent(GitCIRequestTriggerEvent(gitRequestEvent, yaml))
            true
        } else {
            logger.warn("Matcher is false, return, eventId: ${gitRequestEvent.id}")
            gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, yamlStr, normalizedYaml, NotBuildReason.TRIGGER_NOT_MATCH.name, gitRequestEvent.gitProjectId)
            false
        }
    }

    fun createCIBuildYaml(yamlStr: String, gitProjectId: Long? = null): CIBuildYaml {
        logger.info("input yamlStr: $yamlStr")

        var yaml = formatYaml(yamlStr)
        yaml = replaceEnv(yaml, gitProjectId)
        val yamlObject = YamlUtil.getObjectMapper().readValue(yaml, CIBuildYaml::class.java)
        return normalizeYaml(yamlObject)
    }

    private fun checkGitProjectConf(gitRequestEvent: GitRequestEvent, event: GitEvent): Boolean {
        val gitProjectConf = gitCISettingDao.getSetting(dslContext, gitRequestEvent.gitProjectId)
        if (null == gitProjectConf) {
            logger.info("git ci is not enabled, git project id: ${gitRequestEvent.gitProjectId}")
            gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, NotBuildReason.GIT_CI_DISABLE.name, gitRequestEvent.gitProjectId)
            return false
        }
        if (!gitProjectConf.enableCi) {
            logger.warn("git ci is disabled, git project id: ${gitRequestEvent.gitProjectId}, name: ${gitProjectConf.name}")
            gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, "git ci config is not enabled", gitRequestEvent.gitProjectId)
            return false
        }
        when (event) {
            is GitPushEvent -> {
                if (gitProjectConf.buildPushedBranches == false) {
                    logger.warn("git ci conf buildPushedBranches is false, git project id: ${gitRequestEvent.gitProjectId}, name: ${gitProjectConf.name}")
                    gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, NotBuildReason.BUILD_PUSHED_BRANCHES_DISABLE.name, gitRequestEvent.gitProjectId)
                    return false
                }
            }
            is GitTagPushEvent -> {
                if (gitProjectConf.buildPushedBranches == false) {
                    logger.warn("git ci conf buildPushedBranches is false, git project id: ${gitRequestEvent.gitProjectId}, name: ${gitProjectConf.name}")
                    gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, NotBuildReason.BUILD_PUSHED_BRANCHES_DISABLE.name, gitRequestEvent.gitProjectId)
                    return false
                }
            }
            is GitMergeRequestEvent -> {
                if (gitProjectConf.buildPushedPullRequest == false) {
                    logger.warn("git ci conf buildPushedPullRequest is false, git project id: ${gitRequestEvent.gitProjectId}, name: ${gitProjectConf.name}")
                    gitRequestEventNotBuildDao.save(dslContext, gitRequestEvent.id!!, null, null, NotBuildReason.BUILD_PUSHED_PULL_REQUEST_DISABLE.name, gitRequestEvent.gitProjectId)
                    return false
                }
            }
        }

        return true
    }

    private fun formatYaml(yamlStr: String): String {
        val sb = StringBuilder()
        val br = BufferedReader(StringReader(yamlStr))
        val taskTypeRegex = Regex("- $TASK_TYPE:\\s+")
        val mrNoneRegex = Regex("^(mr:)\\s*(none)\$")
        val triggerNoneRegex = Regex("^(mr:)\\s*(none)\$")
        var line: String? = br.readLine()
        while (line != null) {
            val taskTypeMatches = taskTypeRegex.find(line)
            if (null != taskTypeMatches) {
                val taskType = taskTypeMatches.groupValues[0]
                val taskVersion = line.removePrefix(taskType)
                val task = taskVersion.split("@")
                if (task.size != 2 || (task.size == 2 && task[1].isNullOrBlank())) {
                    line = task[0] + "@latest"
                }
            }

            val mrNoneMatches = mrNoneRegex.find(line)
            if (null != mrNoneMatches) {
                line = "mr:" + "\n" + "  enable: false"
            }

            val triggerNoneMatches = triggerNoneRegex.find(line)
            if (null != triggerNoneMatches) {
                line = "trigger:" + "\n" + "  enable: false"
            }

            sb.append(line).append("\n")
            line = br.readLine()
        }
        return sb.toString()
    }

    private fun replaceEnv(yaml: String, gitProjectId: Long?): String {
        if (gitProjectId == null) {
            return yaml
        }
        val gitProjectConf = gitCISettingDao.getSetting(dslContext, gitProjectId) ?: return yaml
        if (null == gitProjectConf.env) {
            return yaml
        }

        val sb = StringBuilder()
        val br = BufferedReader(StringReader(yaml))
        val envRegex = Regex("\\\$env:\\w+")
        var line: String? = br.readLine()
        while (line != null) {
            val envMatches = envRegex.find(line)
            if (null != envMatches) {
                val envKeyPrefix = envMatches.groupValues[0]
                val envKey = envKeyPrefix.removePrefix("\$env:")
                val envValue = getEnvValue(gitProjectConf.env!!, envKey)
                if (null != envValue) {
                    line = envRegex.replace(line, envValue)
                }
            }

            sb.append(line).append("\n")
            line = br.readLine()
        }

        return yaml
    }

    private fun getEnvValue(env: List<EnvironmentVariables>, key: String): String? {
        env.forEach {
            if (it.name == key) {
                return it.value
            }
        }
        return null
    }

    private fun normalizeYaml(originYaml: CIBuildYaml): CIBuildYaml {
        if (originYaml.stages != null && originYaml.steps != null) {
            logger.error("Invalid yaml: steps and stages conflict") // 不能并列存在steps和stages
            throw CustomException(Response.Status.BAD_REQUEST, "stages和steps不能并列存在!")
        }
        val defaultTrigger = originYaml.trigger ?: Trigger(false, MatchRule(listOf("*"), null), null, null)
        val defaultMr = originYaml.mr ?: MergeRequest(disable = false, autoCancel = true, branches = MatchRule(listOf("*"), null), paths = null)
        val variable = originYaml.variables
        val stages = originYaml.stages ?: listOf(Stage(listOf(Job(JobDetail("job1", Pool(null, null), originYaml.steps!!, null)))))

        return CIBuildYaml(defaultTrigger, defaultMr, variable, stages, null)
    }

    private fun dispatchEvent(event: GitCIRequestTriggerEvent) {
        GitCIRequestDispatcher.dispatch(rabbitTemplate, event)
    }

    private fun getYamlFromGit(gitRequestEvent: GitRequestEvent): String? {
        return try {
            val gitToken = client.getScm(ServiceGitResource::class).getToken(gitRequestEvent.gitProjectId).data!!
            logger.info("get token form scm, token: $gitToken")
            val ref = when {
                gitRequestEvent.branch.startsWith("refs/heads/") -> gitRequestEvent.branch.removePrefix("refs/heads/")
                gitRequestEvent.branch.startsWith("refs/tags/") -> gitRequestEvent.branch.removePrefix("refs/tags/")
                else -> gitRequestEvent.branch
            }
            val result = client.getScm(ServiceGitResource::class).getGitCIFileContent(gitRequestEvent.gitProjectId, ciFileName, gitToken.accessToken, ref)
            result.data
        } catch (e: Throwable) {
            logger.error("Get yaml from git failed", e)
            null
        }
    }

    private fun saveGitRequestEvent(event: GitEvent, e: String): GitRequestEvent? {
        when (event) {
            is GitPushEvent -> {
                if (event.total_commits_count <= 0) {
                    logger.info("Git web hook no commit(${event.total_commits_count})")
                    return null
                }
                val gitRequestEvent = createGitRequestEvent(event, e)
                val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
                gitRequestEvent.id = id
                return gitRequestEvent
            }
            is GitTagPushEvent -> {
                if (event.total_commits_count <= 0) {
                    logger.info("Git web hook no commit(${event.total_commits_count})")
                    return null
                }
                val gitRequestEvent = createGitRequestEvent(event, e)
                val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
                gitRequestEvent.id = id
                return gitRequestEvent
            }
            is GitMergeRequestEvent -> {
                if (event.object_attributes.action == "close" || event.object_attributes.action == "merge" ||
                        (event.object_attributes.action == "update" && event.object_attributes.extension_action != "push-update")
                ) {
                    logger.info("Git web hook is ${event.object_attributes.action} merge request")
                    return null
                }
                val gitRequestEvent = createGitRequestEvent(event, e)
                val id = gitRequestEventDao.saveGitRequest(dslContext, gitRequestEvent)
                gitRequestEvent.id = id
                return gitRequestEvent
            }
        }
        logger.info("event invalid: $event")
        return null
    }

    private fun createGitRequestEvent(gitPushEvent: GitPushEvent, e: String): GitRequestEvent {
        val latestCommit = getLatestCommit(gitPushEvent.after, gitPushEvent.commits)
        return GitRequestEvent(null,
                OBJECT_KIND_PUSH,
                gitPushEvent.operation_kind,
                null,
                gitPushEvent.project_id,
                gitPushEvent.ref.removePrefix("refs/heads/"),
                null,
                gitPushEvent.after,
                latestCommit?.message,
                latestCommit?.timestamp,
                gitPushEvent.user_name,
                gitPushEvent.total_commits_count.toLong(),
                null,
                e,
                ""
        )
    }

    private fun createGitRequestEvent(gitTagPushEvent: GitTagPushEvent, e: String): GitRequestEvent {
        val latestCommit = getLatestCommit(gitTagPushEvent.after, gitTagPushEvent.commits)
        return GitRequestEvent(null,
                OBJECT_KIND_TAG_PUSH,
                gitTagPushEvent.operation_kind,
                null,
                gitTagPushEvent.project_id,
                gitTagPushEvent.ref.removePrefix("refs/tags/"),
                null,
                gitTagPushEvent.after,
                latestCommit?.message,
                latestCommit?.timestamp,
                gitTagPushEvent.user_name,
                gitTagPushEvent.total_commits_count.toLong(),
                null,
                e,
                ""
        )
    }

    private fun createGitRequestEvent(gitMrEvent: GitMergeRequestEvent, e: String): GitRequestEvent {
        val latestCommit = gitMrEvent.object_attributes.last_commit
        return GitRequestEvent(null,
                OBJECT_KIND_MERGE_REQUEST,
                null,
                gitMrEvent.object_attributes.extension_action,
                gitMrEvent.object_attributes.source_project_id,
                gitMrEvent.object_attributes.source_branch,
                gitMrEvent.object_attributes.target_branch,
                latestCommit.id,
                latestCommit.message,
                latestCommit.timestamp,
                latestCommit.author.name,
                0,
                gitMrEvent.object_attributes.id,
                e,
                ""
        )
    }

    private fun createGitRequestEvent(userId: String, triggerBuildReq: TriggerBuildReq): GitRequestEvent {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return GitRequestEvent(null,
                OBJECT_KIND_MANUAL,
                "",
                null,
                triggerBuildReq.gitProjectId,
                triggerBuildReq.branch.removePrefix("refs/heads/"),
                null,
                "",
                triggerBuildReq.customCommitMsg,
                formatter.format(Date()),
                userId,
                0,
                null,
                "",
                triggerBuildReq.description
        )
    }

    private fun getLatestCommit(commitId: String, commits: List<GitCommit>): GitCommit? {
        commits.forEach {
            if (it.id == commitId) {
                return it
            }
        }
        return null
    }

    fun getYaml(gitProjectId: Long, buildId: String): String {
        logger.info("get yaml by buildId:($buildId), gitProjectId: $gitProjectId")
        gitCISettingDao.getSetting(dslContext, gitProjectId) ?: throw CustomException(Response.Status.FORBIDDEN, "项目未开启工蜂CI，无法查询")
        val eventBuild = gitRequestEventBuildDao.getByBuildId(dslContext, buildId)
        return (eventBuild?.originYaml) ?: ""
    }
}
