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

package com.tencent.devops.store.pojo.common.enums

enum class StoreTypeEnum(val type: Int) {
    ATOM(0), // 插件
    TEMPLATE(1), // 模板
    IMAGE(2), // 镜像
    IDE_ATOM(3), // IDE插件
    SERVICE(4); // 扩展服务

    companion object {
        fun getStoreType(type: Int): String {
            return when (type) {
                0 -> ATOM.name
                1 -> TEMPLATE.name
                2 -> IMAGE.name
                3 -> IDE_ATOM.name
                4 -> SERVICE.name
                else -> ATOM.name
            }
        }

        fun getStoreTypeObj(type: Int): StoreTypeEnum? {
            return when (type) {
                0 -> ATOM
                1 -> TEMPLATE
                2 -> IMAGE
                3 -> IDE_ATOM
                4 -> SERVICE
                else -> null
            }
        }
    }
}
