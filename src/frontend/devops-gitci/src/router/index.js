/**
 * @file router 配置
 * @author Blueking
 */

import Vue from 'vue'
import VueRouter from 'vue-router'
import websocket from '@/utils/websocket'
Vue.use(VueRouter)

const main = () => import(/* webpackChunkName: 'entry' */'@/views/index')
const exception = () => import(/* webpackChunkName: 'entry' */'@/views/exception')
const notifications = () => import(/* webpackChunkName: 'notifications' */'@/views/notifications')
const pipeline = () => import(/* webpackChunkName: 'pipelines' */'@/views/pipeline')
const buildList = () => import(/* webpackChunkName: 'pipelines' */'@/views/pipeline/build-list')
const pipelineDetail = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/index')
const buildDetail = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/detail')
const buildArtifacts = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/artifacts')
const buildReports = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/reports')
const buildConfig = () => import(/* webpackChunkName: 'buildDetail' */'@/views/pipeline/build-detail/config')
const webConsole = () => import(/* webpackChunkName: "webConsole" */'@/views/pipeline/web-console.vue')
const setting = () => import(/* webpackChunkName: 'setting' */'@/views/setting/index')
const basicSetting = () => import(/* webpackChunkName: 'setting' */'@/views/setting/basic')
const credentialList = () => import(/* webpackChunkName: 'setting' */'@/views/setting/credential')
const agentPools = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/index')
const addAgent = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/add-agent')
const agentList = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/agent-list')
const agentDetail = () => import(/* webpackChunkName: 'setting' */'@/views/setting/agent-pools/agent-detail')

const routes = [
    {
        path: '',
        components: {
            default: main,
            exception: exception
        },
        children: [
            {
                path: 'webConsole',
                name: 'webConsole',
                component: webConsole
            },
            {
                path: '',
                component: pipeline,
                name: 'pipeline',
                children: [
                    {
                        path: 'pipeline/:pipelineId?',
                        name: 'buildList',
                        component: buildList,
                        meta: {
                            websocket: true
                        }
                    },
                    {
                        path: 'pipeline/:pipelineId/detail/:buildId',
                        name: 'pipelineDetail',
                        component: pipelineDetail,
                        children: [
                            {
                                path: '',
                                name: 'buildDetail',
                                component: buildDetail,
                                meta: {
                                    websocket: true
                                }
                            },
                            {
                                path: 'artifacts',
                                name: 'buildArtifacts',
                                component: buildArtifacts
                            },
                            {
                                path: 'reports',
                                name: 'buildReports',
                                component: buildReports
                            },
                            {
                                path: 'config',
                                name: 'buildConfig',
                                component: buildConfig
                            }
                        ]
                    }
                ]
            },
            {
                path: 'setting',
                name: 'setting',
                component: setting,
                children: [
                    {
                        path: '',
                        name: 'basicSetting',
                        component: basicSetting
                    },
                    {
                        path: 'credential',
                        name: 'credentialList',
                        component: credentialList
                    },
                    {
                        path: 'agent-pools',
                        name: 'agentPools',
                        component: agentPools
                    },
                    {
                        path: 'add-agent/:poolId/:poolName',
                        name: 'addAgent',
                        component: addAgent
                    },
                    {
                        path: 'agent-list/:poolId/:poolName',
                        name: 'agentList',
                        component: agentList
                    },
                    {
                        path: 'agent-detail/:poolId/:poolName/:agentId',
                        name: 'agentDetail',
                        component: agentDetail
                    }
                ]
            },
            {
                path: 'notifications',
                name: 'notifications',
                component: notifications
            },
            {
                path: '*',
                name: '404',
                component: exception
            }
        ]
    }
]

const router = new VueRouter({
    mode: 'history',
    routes: routes
})

router.afterEach(route => {
    websocket.changeRoute(route)
})

// 自动携带项目信息
router.beforeEach((to, from, next) => {
    websocket.loginOut(from)
    const params = {
        ...to,
        hash: to.hash || from.hash
    }
    if (to.hash || (!to.hash && !from.hash)) {
        next()
    } else {
        next(params)
    }
})

export default router
