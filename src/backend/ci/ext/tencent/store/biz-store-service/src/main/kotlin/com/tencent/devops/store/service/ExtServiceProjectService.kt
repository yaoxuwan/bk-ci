package com.tencent.devops.store.service

import com.tencent.devops.common.api.constant.CommonMessageCode
import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.api.util.DateTimeUtil
import com.tencent.devops.common.api.util.UUIDUtil
import com.tencent.devops.common.api.util.timestamp
import com.tencent.devops.common.client.Client
import com.tencent.devops.common.pipeline.enums.ChannelCode
import com.tencent.devops.common.service.utils.MessageCodeUtil
import com.tencent.devops.project.api.service.ServiceProjectResource
import com.tencent.devops.store.dao.ExtServiceDao
import com.tencent.devops.store.dao.ExtServiceFeatureDao
import com.tencent.devops.store.dao.common.ReasonRelDao
import com.tencent.devops.store.dao.common.StoreMemberDao
import com.tencent.devops.store.dao.common.StoreProjectRelDao
import com.tencent.devops.store.pojo.common.InstalledProjRespItem
import com.tencent.devops.store.pojo.common.UnInstallReq
import com.tencent.devops.store.pojo.common.enums.ReasonTypeEnum
import com.tencent.devops.store.pojo.common.enums.StoreProjectTypeEnum
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.pojo.enums.ExtServiceStatusEnum
import com.tencent.devops.store.pojo.vo.ExtServiceRespItem
import com.tencent.devops.store.service.common.StoreProjectService
import org.jooq.DSLContext
import org.jooq.impl.DSL
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.util.StopWatch
import java.lang.RuntimeException
import java.time.LocalDateTime

@Service
class ExtServiceProjectService @Autowired constructor(
    val extServiceDao: ExtServiceDao,
    val extServiceFeatureDao: ExtServiceFeatureDao,
    val storeMemberDao: StoreMemberDao,
    val reasonRelDao: ReasonRelDao,
    private val storeProjectRelDao: StoreProjectRelDao,
    val dslContext: DSLContext,
    val client: Client,
    private val storeProjectService: StoreProjectService
) {

    /**
     * 安装扩展服务到项目
     */
    fun installService(
        userId: String,
        projectCodeList: ArrayList<String>,
        serviceCode: String,
        channelCode: ChannelCode
    ): Result<Boolean> {
        logger.info("installService:Input:($userId,$projectCodeList,$serviceCode)")
        // 判断扩展服务标识是否合法
        val serviceRecord = extServiceDao.getServiceLatestByCode(dslContext, serviceCode)
            ?: throw RuntimeException("serviceCode=$serviceCode")
        val serviceFeature = extServiceFeatureDao.getServiceByCode(dslContext, serviceCode)
        val validateInstallResult = storeProjectService.validateInstallPermission(
            publicFlag = serviceFeature?.publicFlag ?: false,
            userId = userId,
            storeCode = serviceRecord.serviceCode,
            storeType = StoreTypeEnum.SERVICE,
            projectCodeList = projectCodeList
        )
        if (validateInstallResult.isNotOk()) {
            return validateInstallResult
        }
        logger.info("installService:Inner:service.id=${serviceRecord.id},serviceFeature.publicFlag=${serviceFeature?.publicFlag}")
        return storeProjectService.installStoreComponent(
            userId = userId,
            projectCodeList = projectCodeList,
            storeId = serviceRecord.id,
            storeCode = serviceRecord.serviceCode,
            storeType = StoreTypeEnum.SERVICE,
            publicFlag = serviceFeature?.publicFlag ?: false,
            channelCode = channelCode
        )
    }

    fun getInstalledProjects(
        accessToken: String,
        userId: String,
        storeCode: String,
        storeType: StoreTypeEnum
    ): Result<List<InstalledProjRespItem>> {
        logger.info("getInstalledProjects accessToken is :$accessToken, userId is :$userId, storeCode is :$storeCode, storeType is :$storeType")
        val watch = StopWatch()
        // 获取用户有权限的项目列表
        watch.start("get accessible projects")
        val projectList = client.get(ServiceProjectResource::class).list(userId).data
        watch.stop()
        logger.info("$userId accessible projectList is :size=${projectList?.size},$projectList")
        if (projectList?.count() == 0) {
            return Result(mutableListOf())
        }
        watch.start("projectCodeMap")
        val projectCodeMap = projectList?.map { it.projectCode to it }?.toMap()!!
        watch.stop()
        watch.start("getInstalledProject")
        val records =
            storeProjectRelDao.getInstalledProject(dslContext, storeCode, storeType.type.toByte(), projectCodeMap.keys)
        watch.stop()
        watch.start("generate InstalledProjRespItem")
        val result = mutableListOf<InstalledProjRespItem>()
        records?.forEach {
            result.add(
                InstalledProjRespItem(
                    projectCode = it.projectCode,
                    projectName = projectCodeMap[it.projectCode]?.projectName,
                    creator = it.creator,
                    createTime = DateTimeUtil.toDateTime(it.createTime)
                )
            )
        }
        watch.stop()
        logger.info("getInstalledProjects:watch:$watch")
        return Result(result)
    }

    fun getServiceByProjectCode(projectCode: String, itemId: String?): Result<List<ExtServiceRespItem>> {
        logger.info("getServiceByProjectCode projectCode[$projectCode], itemId[$itemId]")
        val projectRelRecords = extServiceDao.getProjectServiceBy(dslContext, projectCode, itemId)
        if (projectRelRecords == null || projectRelRecords.size == 0) {
            return Result(emptyList<ExtServiceRespItem>())
        }
        val serviceRecords = mutableListOf<ExtServiceRespItem>()
        projectRelRecords.forEach {
            val publicFlag = it["publicFlag"] as Boolean
            val projectType = it["projectType"] as Byte
            logger.info("getServiceByProjectCode $it")
            serviceRecords?.add(
                ExtServiceRespItem(
                    serviceId = it["serviceId"] as String,
                    serviceName = it["serviceName"] as String,
                    serviceCode = it["serviceCode"] as String,
                    language = "",
                    category = "",
                    version = it["version"] as String,
                    logoUrl = "",
                    serviceStatus = ExtServiceStatusEnum.getServiceStatus((it["serviceStatus"] as Byte).toInt()),
                    projectName = projectCode,
                    creator = it["creator"] as String,
                    releaseFlag = true,
                    modifier = it["modifier"] as String,
                    itemName = "",
                    isUninstall = canUninstall(publicFlag, projectType),
                    publisher = it["publisher"] as String,
                    publishTime = (it["pubTime"] as LocalDateTime)?.timestamp().toString(),
                    createTime = (it["createTime"] as LocalDateTime)?.timestamp().toString(),
                    updateTime = (it["updateTime"] as LocalDateTime)?.timestamp().toString()
                )
            )
        }
        return Result(serviceRecords)
    }

    // 卸载扩展
    fun uninstallService(
        userId: String,
        projectCode: String,
        serviceCode: String,
        unInstallReq: UnInstallReq
    ): Result<Boolean> {
        logger.info("uninstallService, $projectCode | $serviceCode | $userId")
        // 用户是否有权限卸载
        val isInstaller =
            storeProjectRelDao.isInstaller(dslContext, userId, serviceCode, StoreTypeEnum.SERVICE.type.toByte())
        logger.info("uninstallService, isInstaller=$isInstaller")

        val isAdmin = storeMemberDao.isStoreAdmin(dslContext, userId, serviceCode, StoreTypeEnum.SERVICE.type.toByte())
        logger.info("uninstallService, isAdmin=$isAdmin")

        if (!(isAdmin || isInstaller)) {
            return MessageCodeUtil.generateResponseDataObject(CommonMessageCode.PERMISSION_DENIED, arrayOf(serviceCode))
        }

        dslContext.transaction { t ->
            val context = DSL.using(t)

            // 卸载
            storeProjectService.uninstall(StoreTypeEnum.SERVICE, serviceCode, projectCode)

            // 入库卸载原因
            unInstallReq.reasonList.forEach {
                if (it?.reasonId != null) {
                    val id = UUIDUtil.generate()
                    reasonRelDao.add(
                        context,
                        id,
                        userId,
                        serviceCode,
                        it.reasonId,
                        it.note,
                        ReasonTypeEnum.UNINSTALLATOM.type
                    )
                }
            }
        }

        return Result(true)
    }

    private fun canUninstall(publicFlag: Boolean, projectType: Byte): Boolean {
        // 公共的不可卸载
        if (publicFlag) {
            return false
        }
        // 扩展初始化绑定项目不可卸载
        if (StoreProjectTypeEnum.getProjectType(projectType.toInt()).equals(StoreProjectTypeEnum.INIT)) {
            return false
        }
        return true
    }

    companion object {
        val logger = LoggerFactory.getLogger(this::class.java)
    }
}