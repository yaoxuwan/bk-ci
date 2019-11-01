package com.tencent.devops.store.resources.image

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.store.api.image.UserMarketImageVisibleDeptResource
import com.tencent.devops.store.pojo.common.StoreVisibleDeptResp
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.pojo.image.request.ImageVisibleDeptReq
import com.tencent.devops.store.service.common.impl.StoreVisibleDeptServiceImpl
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class UserMarketImageVisibleDeptResourceImpl @Autowired constructor(private val storeVisibleDeptService: StoreVisibleDeptServiceImpl) :
    UserMarketImageVisibleDeptResource {

    override fun deleteVisibleDept(userId: String, imageCode: String, deptIds: String): Result<Boolean> {
        return storeVisibleDeptService.deleteVisibleDept(
            userId = userId,
            storeCode = imageCode,
            deptIds = deptIds,
            storeType = StoreTypeEnum.IMAGE
        )
    }

    override fun addVisibleDept(userId: String, imageVisibleDeptRequest: ImageVisibleDeptReq): Result<Boolean> {
        return storeVisibleDeptService.addVisibleDept(
            userId = userId,
            storeCode = imageVisibleDeptRequest.imageCode,
            deptInfos = imageVisibleDeptRequest.deptInfos,
            storeType = StoreTypeEnum.IMAGE
        )
    }

    override fun getVisibleDept(imageCode: String): Result<StoreVisibleDeptResp?> {
        return storeVisibleDeptService.getVisibleDept(
            storeCode = imageCode,
            storeType = StoreTypeEnum.IMAGE,
            deptStatus = null
        )
    }
}