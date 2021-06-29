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

package com.tencent.devops.common.webhook.service.code.filter

import org.junit.Assert
import org.junit.Test

class BranchFilterTest {

    private val response = WebhookFilterResponse()

    @Test
    fun includeBranches() {
        var branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = emptyList(),
            excludedBranches = emptyList()
        )
        Assert.assertTrue(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = listOf("master"),
            excludedBranches = emptyList()
        )
        Assert.assertTrue(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = listOf("master", "develop"),
            excludedBranches = emptyList()
        )
        Assert.assertTrue(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "not-included-branch",
            includedBranches = listOf("master", "develop"),
            excludedBranches = emptyList()
        )
        Assert.assertFalse(branchFilter.doFilter(response))
    }

    @Test
    fun excludeBranches() {
        var branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = emptyList(),
            excludedBranches = emptyList()
        )
        Assert.assertTrue(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = emptyList(),
            excludedBranches = listOf("master")
        )
        Assert.assertFalse(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = emptyList(),
            excludedBranches = listOf("master", "develop")
        )
        Assert.assertFalse(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "not-excluded-branch",
            includedBranches = emptyList(),
            excludedBranches = listOf("master", "develop")
        )
        Assert.assertTrue(branchFilter.doFilter(response))
    }

    @Test
    fun includeAndExcludeBranches() {
        var branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "master",
            includedBranches = listOf("master"),
            excludedBranches = listOf("develop")
        )
        Assert.assertTrue(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "develop",
            includedBranches = listOf("master"),
            excludedBranches = listOf("develop")
        )
        Assert.assertFalse(branchFilter.doFilter(response))

        branchFilter = BranchFilter(
            pipelineId = "p-8a49b34bfd834adda6e8dbaad01eedea",
            triggerOnBranchName = "not-excluded-and-not-included-branch",
            includedBranches = listOf("master"),
            excludedBranches = listOf("develop")
        )
        Assert.assertFalse(branchFilter.doFilter(response))
    }
}
