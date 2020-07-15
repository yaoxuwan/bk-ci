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

package com.tencent.devops.common.web.constant

enum class BkStyleEnum(val style: String) {
    COMMON_STYLE("^(.|\\r|\\n)*\$"), // 通用正则表达式
    NUMBER_STYLE("[0-9]*\$"), // 数字正则表达式
    CODE_STYLE("^[a-zA-Z_][\\w-]{0,31}\$"), // 标识正则表达式
    NAME_STYLE("^[\\w-\\u4E00-\\u9FBB\\u3400-\\u4DBF\\uF900-\\uFAD9\\u3000-\\u303F\\u2000-\\u206F\\uFF00-\\uFFEF]{1,32}\$"), // 名称正则表达式
    EMAIL_STYLE("^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*\$"), // 电子邮箱正则表达式
    AUTH_STYLE("^HTTP|HTTPS|OAUTH|SSH\$"), // 权限认证类型正则表达式
    NOTE_STYLE("^[A-Za-z0-9\\u4E00-\\u9FBB\\u3400-\\u4DBF\\uF900-\\uFAD9\\u3000-\\u303F\\u2000-\\u206F\\uFF00-\\uFFEF.。?？！!,()，、；;：:'‘’“”\"…\\s]{2,256}\$"), // 备注正则表达式
    VISIBILITY_LEVEL_STYLE("^PRIVATE|LOGIN_PUBLIC\$"), // 项目可视范围正则表达式
    LANGUAGE_STYLE("^java|python|nodejs|golang|c|c++|php|c#\$"), // 开发语言正则表达式
    BOOLEAN_STYLE("^true|false\$"), // 布尔型正则表达式
    SCOPE_STYLE("^TEST|PRD|ALL\$"), // 适用范围正则表达式
    SERVICE_CODE_STYLE("^[a-z][([-a-z-0-9]*[a-z-0-9])?]{0,31}\$"), // 研发商店扩展服务标识正则表达式
}