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

package com.tencent.devops.common.cos.response;


import okhttp3.Response;

/**
 * Created by schellingma on 2017/05/08.
 * Powered By Tencent
 */
public class DeleteBucketResponse extends BaseResponse {

    private Boolean existBucket;
    private Boolean hasPermission;

    @Override
    public void parseResponse(Response response) {
        if (response.isSuccessful()) {
            setSuccess(true);
            setExistBucket(true);
            setHasPermission(true);
        } else {
            setSuccess(false);
            int code = response.code();
            switch (code) {
                case 401:
                    setErrorMessage("Invalid signature");
                    break;
                case 404:
                    setSuccess(true);
                    setExistBucket(false);
                    setHasPermission(false);
                    break;
                case 403:
                    setSuccess(true);
                    setExistBucket(true);
                    setHasPermission(false);
                    break;
                case 500:
                    setErrorMessage("COS system error");
                    break;
                default:
                    setErrorMessage("Unknown COS error");
                    break;
            }
        }
    }

    public Boolean isExistBucket() {
        return existBucket;
    }

    private void setExistBucket(Boolean existBucket) {
        this.existBucket = existBucket;
    }

    public Boolean getHasPermission() {
        return hasPermission;
    }

    public void setHasPermission(Boolean hasPermission) {
        this.hasPermission = hasPermission;
    }
}
