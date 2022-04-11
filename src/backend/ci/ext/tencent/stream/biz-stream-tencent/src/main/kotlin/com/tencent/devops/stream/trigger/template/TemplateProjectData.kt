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

package com.tencent.devops.stream.trigger.template

import com.tencent.devops.common.ci.v2.enums.TemplateType
import com.tencent.devops.common.webhook.pojo.code.git.GitEvent

// 传入template的泛型接口
data class TemplateProjectData(
    val gitRequestEventId: Long,
    // 发起者的库ID,用户名,分支
    val triggerProjectId: Long,
    // sourceProjectId，在fork时是源库的ID
    val sourceProjectId: Long,
    val triggerUserId: String,
    val triggerRef: String,
    val triggerToken: String,
    val forkGitToken: String?,
    val changeSet: Set<String>?,
    val event: GitEvent?
)

// 获取远程模板需要是用的参数
data class StreamGetTemplateData(
    val gitRequestEventId: Long,
    val token: String?,
    val forkToken: String?,
    val gitProjectId: Long,
    val targetRepo: String?,
    val ref: String?,
    val personalAccessToken: String?,
    val fileName: String,
    val changeSet: Set<String>?,
    val event: GitEvent?,
    // 正在被替换的远程库
    val nowRemoteGitProjectId: String?,
    val templateType: TemplateType?
)