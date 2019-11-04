/*
 * Tencent is pleased to support the open source community by making BK-REPO 蓝鲸制品库 available.
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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN
 * NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.tencent.devops.store.resources.atom

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.common.web.RestResource
import com.tencent.devops.store.api.atom.UserMarketAtomVisibleDeptResource
import com.tencent.devops.store.pojo.atom.AtomVisibleDeptReq
import com.tencent.devops.store.pojo.common.StoreVisibleDeptResp
import com.tencent.devops.store.pojo.common.enums.StoreTypeEnum
import com.tencent.devops.store.service.StoreVisibleDeptService
import org.springframework.beans.factory.annotation.Autowired

@RestResource
class UserMarketAtomVisibleDeptResourceImpl @Autowired constructor(private val storeVisibleDeptService: StoreVisibleDeptService) :
    UserMarketAtomVisibleDeptResource {

    override fun deleteVisibleDept(userId: String, atomCode: String, deptIds: String): Result<Boolean> {
        return storeVisibleDeptService.deleteVisibleDept(userId, atomCode, deptIds, StoreTypeEnum.ATOM)
    }

    override fun addVisibleDept(userId: String, atomVisibleDeptRequest: AtomVisibleDeptReq): Result<Boolean> {
        return storeVisibleDeptService.addVisibleDept(userId, atomVisibleDeptRequest.atomCode, atomVisibleDeptRequest.deptInfos, StoreTypeEnum.ATOM)
    }

    override fun getVisibleDept(atomCode: String): Result<StoreVisibleDeptResp?> {
        return storeVisibleDeptService.getVisibleDept(atomCode, StoreTypeEnum.ATOM, null)
    }
}