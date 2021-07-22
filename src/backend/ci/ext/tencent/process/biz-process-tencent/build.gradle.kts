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

dependencies {
    api(project(":core:common:common-service"))
    api(project(":core:common:common-web"))
    api(project(":core:common:common-client"))
    api(project(":core:common:common-archive"))
    api(project(":core:common:common-db"))
    api(project(":core:common:common-test"))
    api(project(":ext:tencent:common:common-wechatwork"))
    api(project(":core:common:common-auth:common-auth-api"))
    api(project(":ext:tencent:common:common-auth:common-auth-tencent"))
    api(project(":core:plugin:codecc-plugin:common-codecc"))
    api(project(":ext:tencent:common:common-job"))
    api(project(":ext:tencent:common:common-itest"))
    api(project(":ext:tencent:common:common-gcloud"))
    api(project(":core:process:model-process"))
    api(project(":ext:tencent:process:api-process-tencent"))
    api(project(":core:process:api-process"))
    api(project(":ext:tencent:external:api-external"))
    api(project(":ext:tencent:scm:api-scm"))
    api(project(":core:process:biz-process"))
    api(project(":core:process:plugin-trigger"))
    api(project(":ext:tencent:experience:api-experience-tencent"))
    api(project(":ext:tencent:artifactory:api-artifactory-tencent"))
    api(project(":ext:tencent:support:api-support-tencent"))
    api(project(":ext:tencent:plugin:api-plugin-tencent"))
    api(project(":ext:tencent:image:api-image-tencent"))
    api(project(":ext:tencent:project:api-project-tencent"))
    api(project(":ext:tencent:auth:sdk-auth-tencent"))
    api(project(":core:auth:api-auth"))
    api(project(":ext:tencent:repository:api-repository-tencent"))
    api(project(":ext:tencent:gitci:api-gitci-tencent"))
    api("org.apache.poi:poi")
    api("org.apache.poi:poi-ooxml")
}
