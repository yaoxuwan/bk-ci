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

package com.tencent.devops.experience.service

import com.tencent.devops.experience.constant.ExperienceConstant
import com.tencent.devops.experience.constant.GroupIdTypeEnum
import com.tencent.devops.experience.dao.ExperienceDao
import com.tencent.devops.experience.dao.ExperienceGroupDao
import com.tencent.devops.experience.dao.ExperienceGroupInnerDao
import com.tencent.devops.experience.dao.ExperienceInnerDao
import org.jooq.DSLContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

// 服务共用部分在这里
@Service
class ExperienceBaseService @Autowired constructor(
    private val experienceGroupDao: ExperienceGroupDao,
    private val experienceGroupInnerDao: ExperienceGroupInnerDao,
    private val experienceInnerDao: ExperienceInnerDao,
    private val experienceDao: ExperienceDao,
    private val dslContext: DSLContext
) {
    fun getRecordIdsByUserId(
        userId: String,
        groupIdType: GroupIdTypeEnum
    ): MutableSet<Long> {
        val recordIds = mutableSetOf<Long>()
        // 把有自己的组的experience拿出来 && 把公开的experience拿出来
        val groupIds = mutableSetOf<Long>()
        if (groupIdType == GroupIdTypeEnum.JUST_PRIVATE || groupIdType == GroupIdTypeEnum.ALL) {
            groupIds.addAll(experienceGroupInnerDao.listGroupIdsByUserId(dslContext, userId).map { it.value1() }
                .toMutableSet())
        }
        if (groupIdType == GroupIdTypeEnum.JUST_PUBLIC || groupIdType == GroupIdTypeEnum.ALL) {
            groupIds.add(ExperienceConstant.PUBLIC_GROUP)
        }
        recordIds.addAll(experienceGroupDao.listRecordIdByGroupIds(dslContext, groupIds).map { it.value1() }.toSet())
        // 把有自己的experience拿出来
        recordIds.addAll(experienceInnerDao.listRecordIdsByUserId(dslContext, userId).map { it.value1() }.toSet())
        return recordIds
    }

    fun userCanExperience(userId: String, experienceId: Long): Boolean {
        val experienceRecord = experienceDao.get(dslContext, experienceId)
        val groupIdToUserIdsMap = getGroupIdToUserIdsMap(experienceId)
        return groupIdToUserIdsMap.values.asSequence().flatMap { it.asSequence() }.toSet().contains(userId) ||
            userId == experienceRecord.creator
    }

    fun getGroupIdToUserIdsMap(experienceId: Long): MutableMap<Long, MutableSet<String>> {
        val groupIds = experienceGroupDao.listGroupIdsByRecordId(dslContext, experienceId).map { it.value1() }.toSet()
        return getGroupIdToUserIds(groupIds)
    }

    fun getGroupIdToUserIds(groupIds: Set<Long>): MutableMap<Long, MutableSet<String>> {
        val groupIdToUserIds = mutableMapOf<Long, MutableSet<String>>()
        experienceGroupInnerDao.listByGroupIds(dslContext, groupIds).forEach {
            var userIds = groupIdToUserIds[it.groupId]
            if (null == userIds) {
                userIds = mutableSetOf()
            }
            userIds.add(it.userId)
            groupIdToUserIds[it.groupId] = userIds
        }
        if (groupIds.contains(ExperienceConstant.PUBLIC_GROUP)) {
            groupIdToUserIds[0] = ExperienceConstant.PUBLIC_INNER_USERS
        }
        return groupIdToUserIds
    }
}
