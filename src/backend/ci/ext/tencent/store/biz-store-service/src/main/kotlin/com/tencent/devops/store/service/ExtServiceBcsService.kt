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

package com.tencent.devops.store.service

import com.tencent.devops.common.api.pojo.Result
import com.tencent.devops.store.pojo.dto.DeployExtServiceDTO
import com.tencent.devops.store.resources.UserBcsServiceResourceImpl
import com.tencent.devops.store.util.BcsClientUtils
import io.fabric8.kubernetes.api.model.Container
import io.fabric8.kubernetes.api.model.ContainerPort
import io.fabric8.kubernetes.api.model.IntOrString
import io.fabric8.kubernetes.api.model.LabelSelector
import io.fabric8.kubernetes.api.model.LocalObjectReference
import io.fabric8.kubernetes.api.model.ObjectMeta
import io.fabric8.kubernetes.api.model.PodSpec
import io.fabric8.kubernetes.api.model.PodTemplateSpec
import io.fabric8.kubernetes.api.model.ServiceBuilder
import io.fabric8.kubernetes.api.model.apps.Deployment
import io.fabric8.kubernetes.api.model.apps.DeploymentBuilder
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec
import io.fabric8.kubernetes.api.model.extensions.HTTPIngressPathBuilder
import io.fabric8.kubernetes.api.model.extensions.IngressBackend
import io.fabric8.kubernetes.api.model.extensions.IngressBackendBuilder
import io.fabric8.kubernetes.api.model.extensions.IngressBuilder
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Collections

@Service
class ExtServiceBcsService @Autowired constructor() {

    private val logger = LoggerFactory.getLogger(UserBcsServiceResourceImpl::class.java)

    fun deployExtService(
        userId: String,
        deployExtServiceDTO: DeployExtServiceDTO
    ): Result<Boolean> {
        logger.info("deployExtService userId is: $userId,deployExtServiceDTO is: $deployExtServiceDTO")
        val namespaceName = deployExtServiceDTO.namespaceName
        val serviceCode = deployExtServiceDTO.serviceCode
        val containerPort = deployExtServiceDTO.containerPort
        // 创建deployment无状态部署
        val deployment = DeploymentBuilder()
            .withNewMetadata()
            .withName(serviceCode)
            .withNamespace(namespaceName)
            .endMetadata()
            .withNewSpec()
            .withReplicas(deployExtServiceDTO.replicas)
            .withNewTemplate()
            .withNewMetadata()
            .addToLabels("app", serviceCode)
            .endMetadata()
            .withNewSpec()
            .addNewContainer()
            .withName(serviceCode)
            .withImage(deployExtServiceDTO.serviceImage)
            .addNewPort()
            .withContainerPort(containerPort)
            .endPort()
            .endContainer()
            .addNewImagePullSecret()
            .withName(deployExtServiceDTO.pullImageSecretName)
            .endImagePullSecret()
            .endSpec()
            .endTemplate()
            .withNewSelector()
            .addToMatchLabels("app", serviceCode)
            .endSelector()
            .endSpec()
            .build()
        BcsClientUtils.createDeployment(deployment)
        logger.info("created deployment:$deployment")
        val servicePort = deployExtServiceDTO.servicePort
        val service = ServiceBuilder()
            .withNewMetadata()
            .withName("$serviceCode-service")
            .withNamespace(namespaceName)
            .endMetadata()
            .withNewSpec()
            .withSelector(Collections.singletonMap("app", serviceCode))
            .addNewPort()
            .withName("$serviceCode-port")
            .withProtocol("TCP")
            .withPort(servicePort)
            .withTargetPort(IntOrString(containerPort))
            .endPort()
            .withType("NodePort")
            .endSpec()
            .build()
        BcsClientUtils.createService(service)
        logger.info("created service:$service")
        // 创建ingress
        //generate ingress backend
        val ingressBackend: IngressBackend = IngressBackendBuilder()
            .withServiceName("$serviceCode-service")
            .withNewServicePort(servicePort)
            .build()
        //generate ingress path
        val ingressPath = HTTPIngressPathBuilder()
            .withBackend(ingressBackend)
            .withPath(deployExtServiceDTO.contextPath).build()
        val ingress = IngressBuilder()
            .withNewMetadata()
            .withName("$serviceCode-ingress")
            .withNamespace(namespaceName)
            .addToLabels("app", serviceCode)
            .addToAnnotations(deployExtServiceDTO.ingressAnnotationMap)
            .endMetadata()
            .withNewSpec()
            .addNewRule()
            .withHost(deployExtServiceDTO.host)
            .withNewHttp()
            .withPaths(ingressPath)
            .endHttp()
            .endRule()
            .endSpec()
            .build()
        BcsClientUtils.createIngress(ingress)
        logger.info("created ingress:$ingress")
        return Result(true)
    }
}