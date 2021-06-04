
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
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import request from '@/utils/request'
import {
    FETCH_ERROR,
    BACKEND_API_URL_PREFIX,
    PROCESS_API_URL_PREFIX,
    QUALITY_API_URL_PREFIX,
    ARTIFACTORY_API_URL_PREFIX,
    REPOSITORY_API_URL_PREFIX,
    STORE_API_URL_PREFIX
} from '@/store/constants'

import {
    REPOSITORY_MUTATION,
    GCLOUD_TEMPLATE_MUTATION,
    JOBEXECUTE_TASK_MUTATION,
    TEMPLATE_CATEGORY_MUTATION,
    PIPELINE_TEMPLATE_MUTATION,
    STORE_TEMPLATE_MUTATION,
    TEMPLATE_MUTATION,
    PROJECT_GROUP_USERS_MUTATION,
    PIPELINE_SETTING_MUTATION,
    UPDATE_PIPELINE_SETTING_MUNTATION,
    RESET_PIPELINE_SETTING_MUNTATION,
    REFRESH_QUALITY_LOADING_MUNTATION,
    QUALITY_ATOM_MUTATION,
    INTERCEPT_ATOM_MUTATION,
    INTERCEPT_TEMPLATE_MUTATION
} from './constants'

function rootCommit (commit, ACTION_CONST, payload) {
    commit(ACTION_CONST, payload, { root: true })
}

export const state = {
    templateCategory: null,
    refreshLoading: false,
    pipelineTemplate: null,
    storeTemplate: null,
    template: null,
    reposList: null,
    gcloudTempList: null,
    jobTaskList: null,
    appNodes: {},
    pipelineSetting: {},
    ruleList: [],
    templateRuleList: [],
    qualityAtom: [],
    projectGroupAndUsers: [],
    dockerWhiteList: []
}

export const mutations = {
    [TEMPLATE_CATEGORY_MUTATION]: (state, { categoryList }) => {
        const customCategory = {
            categoryCode: 'custom',
            categoryName: (window.pipelineVue.$i18n && window.pipelineVue.$i18n.t('storeMap.projectCustom')) || 'projectCustom'
        }
        const storeCategory = {
            categoryCode: 'store',
            categoryName: (window.pipelineVue.$i18n && window.pipelineVue.$i18n.t('store')) || 'store'
        }
        return Object.assign(state, {
            templateCategory: [customCategory, ...categoryList, storeCategory]
        })
    },
    [PIPELINE_TEMPLATE_MUTATION]: (state, { pipelineTemplate }) => {
        return Object.assign(state, {
            pipelineTemplate
        })
    },
    [STORE_TEMPLATE_MUTATION]: (state, { storeTemplate }) => {
        return Object.assign(state, {
            storeTemplate
        })
    },
    [TEMPLATE_MUTATION]: (state, { template }) => {
        return Object.assign(state, {
            template
        })
    },

    [PIPELINE_SETTING_MUTATION]: (state, { pipelineSetting }) => {
        return Object.assign(state, {
            pipelineSetting
        })
    },

    [QUALITY_ATOM_MUTATION]: (state, { qualityAtom }) => {
        const atoms = []
        qualityAtom.forEach(item => atoms.push(...item.controlPoints))
        return Object.assign(state, {
            qualityAtom: atoms
        })
    },

    [INTERCEPT_ATOM_MUTATION]: (state, { ruleList }) => {
        const refreshLoading = false
        return Object.assign(state, {
            ruleList,
            refreshLoading
        })
    },

    [INTERCEPT_TEMPLATE_MUTATION]: (state, { templateRuleList }) => {
        const refreshLoading = false
        return Object.assign(state, {
            templateRuleList,
            refreshLoading
        })
    },

    [REPOSITORY_MUTATION]: (state, { records }) => {
        Object.assign(state, {
            reposList: records
        })
        return state
    },
    [GCLOUD_TEMPLATE_MUTATION]: (state, records) => {
        const gcloudTempList = records
        Object.assign(state, {
            gcloudTempList
        })
        return state
    },
    [JOBEXECUTE_TASK_MUTATION]: (state, records) => {
        const jobTaskList = records
        Object.assign(state, {
            jobTaskList
        })
        return state
    },
    [PROJECT_GROUP_USERS_MUTATION]: (state, { projectGroupAndUsers }) => {
        return Object.assign(state, {
            projectGroupAndUsers
        })
    },
    [UPDATE_PIPELINE_SETTING_MUNTATION]: (state, { container, param }) => {
        Object.assign(container, param)
        return state
    },
    [RESET_PIPELINE_SETTING_MUNTATION]: (state, payload) => {
        return Object.assign(state, {
            pipelineSetting: {}
        })
    },
    [REFRESH_QUALITY_LOADING_MUNTATION]: (state, status) => {
        const refreshLoading = status
        Object.assign(state, {
            refreshLoading
        })
        return state
    }
}

export const actions = {
    requestTemplate: async ({ commit }, { projectId, templateId, version }) => {
        try {
            const url = version ? `/${PROCESS_API_URL_PREFIX}/user/templates/projects/${projectId}/templates/${templateId}?version=${version}` : `/${PROCESS_API_URL_PREFIX}/user/templates/projects/${projectId}/templates/${templateId}`
            const response = await request.get(url)
            commit(TEMPLATE_MUTATION, {
                template: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    // 获取模板的所有范畴
    requestCategory: async ({ commit }) => {
        try {
            const response = await request.get(`/${STORE_API_URL_PREFIX}/user/market/template/categorys`)
            commit(TEMPLATE_CATEGORY_MUTATION, {
                categoryList: response.data
            })
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    // 新增流水线时拉取模板
    requestPipelineTemplate: async ({ commit }, { projectId }) => {
        try {
            const response = await request.get(`/${PROCESS_API_URL_PREFIX}/user/templates/projects/${projectId}/allTemplates`)
            commit(PIPELINE_TEMPLATE_MUTATION, {
                pipelineTemplate: (response.data || {}).templates
            })
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    // 获取RD Store模板
    requestStoreTemplate: async ({ commit }, params) => {
        return request.get(`/${STORE_API_URL_PREFIX}/user/market/template/list`, { params })
    },
    requestPipelineSetting: async ({ commit }, { projectId, pipelineId }) => {
        try {
            const response = await request.get(`/${PROCESS_API_URL_PREFIX}/user/setting/get?pipelineId=${pipelineId}&projectId=${projectId}`)
            commit(PIPELINE_SETTING_MUTATION, {
                pipelineSetting: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestTemplateSetting: async ({ commit }, { projectId, templateId }) => {
        try {
            const response = await request.get(`/${PROCESS_API_URL_PREFIX}/user/templates/projects/${projectId}/templates/${templateId}/settings`)
            commit(PIPELINE_SETTING_MUTATION, {
                pipelineSetting: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestQualityAtom: async ({ commit }, { projectId }) => {
        try {
            const response = await request.get(`/${QUALITY_API_URL_PREFIX}/user/controlPoints/v2/list?projectId=${projectId}`)

            commit(QUALITY_ATOM_MUTATION, {
                qualityAtom: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestInterceptAtom: async ({ commit }, { projectId, pipelineId }) => {
        const params = {
            pipelineId: pipelineId
        }
        try {
            const response = await request.get(`/${QUALITY_API_URL_PREFIX}/user/rules/v2/${projectId}/matchRuleList`, { params })

            commit(INTERCEPT_ATOM_MUTATION, {
                ruleList: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestPipelineCheckVersion: async ({ commit }, { projectId, pipelineId, atomCode, version }) => {
        return request.get(`/${QUALITY_API_URL_PREFIX}/user/rules/v2/project/${projectId}/pipeline/${pipelineId}/listAtomRule?atomCode=${atomCode}&atomVersion=${version}`).then(response => {
            return response.data
        })
    },
    requestTemplateCheckVersion: async ({ commit }, { projectId, templateId, atomCode, version }) => {
        return request.get(`/${QUALITY_API_URL_PREFIX}/user/rules/v2/project/${projectId}/template/${templateId}/listTemplateAtomRule?atomCode=${atomCode}&atomVersion=${version}`).then(response => {
            return response.data
        })
    },
    requestMatchTemplateRuleList: async ({ commit }, { projectId, templateId }) => {
        try {
            const response = await request.get(`/${QUALITY_API_URL_PREFIX}/user/rules/v2/${projectId}/matchTemplateRuleList?templateId=${templateId}`)

            commit(INTERCEPT_TEMPLATE_MUTATION, {
                templateRuleList: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestProjectGroupAndUsers: async ({ commit }, { projectId }) => {
        try {
            const response = await request.get(`/experience/api/user/groups/${projectId}/projectGroupAndUsers`)

            commit(PROJECT_GROUP_USERS_MUTATION, {
                projectGroupAndUsers: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    startDebugDevcloud: async ({ commit }, data) => {
        const buildIdQuery = data.buildId ? `?buildId=${data.buildId}` : ''
        return request.post(`dispatch-devcloud/api/user/dispatchDevcloud/startDebug/pipeline/${data.pipelineId}/vmSeq/${data.vmSeqId}${buildIdQuery}`, {}).then(response => {
            return response.data
        })
    },
    stopDebugDevcloud: async ({ commit }, data) => {
        return request.post(`dispatch-devcloud/api/user/dispatchDevcloud/stopDebug/pipeline/${data.pipelineId}/vmSeq/${data.vmSeqId}?containerName=${data.containerName}`, {}).then(response => {
            return response.data
        })
    },
    startDebugDocker: async ({ commit }, data) => {
        return request.post(`dispatch-docker/api/user/dockerhost/startDebug/`, data).then(response => {
            return response.data
        })
    },
    stopDebugDocker: async ({ commit }, { projectId, pipelineId, vmSeqId }) => {
        return request.post(`dispatch-docker/api/user/dockerhost/stopDebug/${projectId}/${pipelineId}/${vmSeqId}`).then(response => {
            return response.data
        })
    },
    getContainerInfoByBuildId: ({ commit }, { projectId, pipelineId, buildId, vmSeqId }) => {
        return request.get(`dispatch-docker/api/user/dockerhost/getContainerInfo/${projectId}/${pipelineId}/${buildId}/${vmSeqId}`).then(response => {
            return response.data
        })
    },
    getContainerInfo: ({ commit }, { projectId, pipelineId, vmSeqId }) => {
        return request.get(`dispatch-docker/api/user/dockerhost/getDebugStatus/${projectId}/${pipelineId}/${vmSeqId}`).then(response => {
            return response.data
        })
    },
    getDockerExecId: async ({ commit }, { containerId, projectId, pipelineId, cmd, targetIp }) => {
        return request.post(`http://${PROXY_URL_PREFIX}/docker-console-create?pipelineId=${pipelineId}&projectId=${projectId}&targetIp=${targetIp}`, { container_id: containerId, cmd }).then(response => {
            return response && response.Id
        })
    },
    resizeTerm: async ({ commit }, { resizeUrl, params }) => {
        return request.post(`http://${PROXY_URL_PREFIX}/${resizeUrl}`, params).then(response => {
            return response && response.Id
        })
    },
    requestPartFile: async ({ commit }, { projectId, params }) => {
        return request.post(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/search`, params).then(response => {
            return response.data
        })
    },
    requestExternalUrl: async ({ commit }, { projectId, artifactoryType, path }) => {
        return request.post(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/${artifactoryType}/externalUrl?path=${encodeURIComponent(path)}`).then(response => {
            return response.data
        })
    },
    requestDevnetGateway: async ({ commit }) => {
        const baseUrl = CHECK_ENV_URL
        return request.get(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/checkDevnetGateway`, { baseURL: baseUrl }).then(response => {
            return response.data
        }).catch(e => {
            return false
        })
    },
    requestDownloadUrl: async ({ commit }, { projectId, artifactoryType, path }) => {
        return request.post(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/${artifactoryType}/downloadUrl?path=${encodeURIComponent(path)}`).then(response => {
            return response.data
        })
    },
    requestCopyArtifactory: async ({ commit }, { projectId, pipelineId, buildId, params }) => {
        return request.post(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/${pipelineId}/${buildId}/copyToCustom`, params).then(response => {
            return response.data
        })
    },
    requestExecPipPermission: async ({ commit }, { projectId, pipelineId, permission }) => {
        return request.get(`${PROCESS_API_URL_PREFIX}/user/pipelines/${projectId}/${pipelineId}/hasPermission?permission=${permission}`).then(response => {
            return response.data
        })
    },
    requestCommitList: async ({ commit }, { buildId }) => {
        return request.get(`${REPOSITORY_API_URL_PREFIX}/user/repositories/${buildId}/commit/get/record`).then(response => {
            return response.data
        })
    },
    requestFileInfo: async ({ commit }, { projectId, path, type }) => {
        return request.get(`/${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/${type}/show?path=${encodeURIComponent(path)}`).then(response => {
            return response.data
        })
    },
    requestReportList: async ({ commit }, { projectId, pipelineId, buildId, taskId }) => {
        return request.get(`/${PROCESS_API_URL_PREFIX}/user/reports/${projectId}/${pipelineId}/${buildId}`, { params: { taskId } }).then(response => {
            return response.data
        })
    },
    /**
     * wetest测试报告
     */
    requestWetestReport: async ({ commit }, { projectId, pipelineId, buildId }) => {
        return request.get(`wetest/api/user/wetest/taskInst/${projectId}/listByBuildId?pipelineId=${pipelineId}&buildId=${buildId}`).then(response => {
            return response.data
        })
    },
    requestRepository: async ({ commit }, payload) => {
        try {
            const { data } = await request.get(`/${REPOSITORY_API_URL_PREFIX}/user/repositories/${payload.projectId}?repositoryType=${payload.repoType}`)
            commit(REPOSITORY_MUTATION, data)
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestGcloudTempList: async ({ commit }, payload) => {
        try {
            const { data } = await request.get(`${BACKEND_API_URL_PREFIX}/api/ci/pipeline/gcloud/templates/${payload.projectId}/`)
            const finalData = data
            await Promise.all(data.map(function (item, index) {
                return request.get(`${BACKEND_API_URL_PREFIX}/api/ci/pipeline/gcloud/templates/${payload.projectId}/${item.id}/`)
            })).then((array) => {
                data.map((item, index) => {
                    item = Object.assign(item, { 'param': array[index].data })
                    data.splice(index, 1, item)
                })
            })
            commit(GCLOUD_TEMPLATE_MUTATION, finalData)
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },

    requestJobTaskParam: async ({ commit }, { projectId, taskId }) => {
        return request.get(`/plugin/api/user/job/projects/${projectId}/tasks/${taskId}/`).then(response => {
            return (response.data && response.data.globalVarList) || []
        })
    },
    reviewExcuteAtom: async ({ commit }, { projectId, pipelineId, buildId, elementId, action }) => {
        return request.post(`/${PROCESS_API_URL_PREFIX}/user/builds/${projectId}/${pipelineId}/${buildId}/${elementId}/qualityGateReview/${action}`).then(response => {
            return response.data
        })
    },
    requestAuditUserList: async ({ commit }, { projectId, pipelineId, buildId, params }) => {
        return request.get(`${QUALITY_API_URL_PREFIX}/user/intercepts/${projectId}/${pipelineId}/${buildId}/auditUserList`, { params }).then(response => {
            return response.data
        })
    },
    requestTrendData: async ({ commit }, { pipelineId, startTime, endTime }) => {
        return request.get(`${ARTIFACTORY_API_URL_PREFIX}/user/pipeline/artifactory/construct/${pipelineId}/trend?startTime=${startTime}&endTime=${endTime}`).then(response => {
            return response.data
        })
    },
    updatePipelineSetting: ({ commit }, payload) => {
        commit(UPDATE_PIPELINE_SETTING_MUNTATION, payload)
    },
    resetPipelineSetting: ({ commit }, payload) => {
        commit(RESET_PIPELINE_SETTING_MUNTATION, payload)
    },
    updateRefreshQualityLoading: ({ commit }, status) => {
        commit(REFRESH_QUALITY_LOADING_MUNTATION, status)
    }
}

export const getters = {
    getAppNodes: state => (os) => state.appNodes[os] || {},
    getHasAtomCheck: state => (stages, atom) => {
        return stages.some((stage, index) => {
            if (index) {
                return stage.containers.some(container => {
                    return container.elements.find(el => {
                        return el.atomCode === atom
                    })
                })
            }
        })
    }
}
