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

package com.tencent.devops.common.wechatwork

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class WechatWorkProperties {
    @Value("\${wechatWork.dev.corpId:#{null}}")
    val devCorpId: String? = null
    @Value("\${wechatWork.dev.serviceId:#{null}}")
    val devServiceId: String? = null
    @Value("\${wechatWork.dev.secret:#{null}}")
    val devSecret: String? = null
    @Value("\${wechatWork.dev.token:#{null}}")
    val devToken: String? = null
    @Value("\${wechatWork.dev.aesKey:#{null}}")
    val devAesKey: String? = null
    @Value("\${wechatWork.dev.url:#{null}}")
    val devUrl: String? = null
    @Value("\${wechatWork.prod.corpId:#{null}}")
    val prodCorpId: String? = null
    @Value("\${wechatWork.prod.serviceId:#{null}}")
    val prodServiceId: String? = null
    @Value("\${wechatWork.prod.secret:#{null}}")
    val prodSecret: String? = null
    @Value("\${wechatWork.prod.token:#{null}}")
    val prodToken: String? = null
    @Value("\${wechatWork.prod.aesKey:#{null}}")
    val prodAesKey: String? = null
    @Value("\${wechatWork.prod.url:#{null}}")
    val prodUrl: String? = null
}
