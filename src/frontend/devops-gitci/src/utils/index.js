const moment = require('moment')

/**
 *  将毫秒值转换成xx:xx(分:秒)的形式
 *  @param {Number} time - 时间的毫秒形式
 *  @return {String} str - 转换后的字符串
 */
export function coverTimer (time, type) {
    let res = ''
    function getSeconds (sec, min) {
        const m = min / 60 >= 1 ? '' : '00:'
        if (type) {
            res = sec < 10 ? `${m}0${sec}秒` : `${m}${sec}秒`
        } else {
            res = sec < 10 ? `${m}0${sec}` : `${m}${sec}`
        }
        return res
    }

    function getMinutes (sec) {
        if (sec / 60 >= 1) {
            let res = ''
            let m = Math.floor(sec / 60)
            m = m < 10 ? `0${m}` : m
            if (type) {
                res = `${m}分${getSeconds(sec % 60, sec)}`
            } else {
                res = `${m}:${getSeconds(sec % 60, sec)}`
            }
            return res
        } else {
            return getSeconds(sec)
        }
    }

    function getHours (sec) {
        if (sec / 3600 >= 1) {
            let res = ''
            let h = Math.floor(sec / 3600)
            h = h < 10 ? `0${h}` : h
            if (type) {
                res = `${h}时${getMinutes(sec % 3600)}`
            } else {
                res = `${h}:${getMinutes(sec % 3600)}`
            }
            return res
        } else {
            return getMinutes(sec)
        }
    }

    return time ? getHours(Math.floor(time / 1000)) : '00:00'
}

/**
 *  将毫秒值转换成x时x分x秒的形式
 *  @param {Number} time - 时间的毫秒形式
 *  @return {String} str - 转换后的字符串
 */
export function convertMStoString (time) {
    function getSeconds (sec) {
        return `${sec}秒`
    }

    function getMinutes (sec) {
        if (sec / 60 >= 1) {
            return `${Math.floor(sec / 60)}分${getSeconds(sec % 60)}`
        } else {
            return getSeconds(sec)
        }
    }

    function getHours (sec) {
        if (sec / 3600 >= 1) {
            return `${Math.floor(sec / 3600)}时${getMinutes(sec % 3600)}`
        } else {
            return getMinutes(sec)
        }
    }

    function getDays (sec) {
        if (sec / 86400 >= 1) {
            return `${Math.floor(sec / 86400)}天${getHours(sec % 86400)}`
        } else {
            return getHours(sec)
        }
    }

    return time ? getDays(Math.floor(time / 1000)) : `0秒`
}

export const uuid = () => {
    let id = ''
    for (let i = 0; i < 7; i++) {
        const randomNum = Math.floor((1 + Math.random()) * 0x10000).toString(16).substring(1)
        id += randomNum
    }
    return id
}

export function convertFileSize (size, unit) {
    const arr = ['B', 'KB', 'MB', 'GB', 'TB']
    const calcSize = size / 1024
    let index

    arr.some((item, _index) => {
        if (unit === item) {
            index = _index
            return true
        }
        return false
    })

    const next = arr[index + 1]

    if (calcSize > 1024) {
        if (!next) {
            return `${calcSize.toFixed(2)}${unit}`
        } else {
            return convertFileSize(calcSize, next)
        }
    } else {
        return `${calcSize.toFixed(2)}${next || unit}`
    }
}

export function goCommit (projectUrl, commitId) {
    if (commitId) {
        window.open(`${projectUrl}/commit/${commitId}`, '_blank')
    }
}

export function goMR (projectUrl, mrId) {
    if (mrId) {
        window.open(`${projectUrl}/merge_requests/${mrId}`, '_blank')
    }
}

export function goBranch (projectUrl, branchName) {
    if (branchName) {
        window.open(`${projectUrl}/tree/${branchName}`, '_blank')
    }
}

export function goTag (projectUrl, tag) {
    if (tag) {
        window.open(`${projectUrl}/-/tags/${encodeURIComponent(tag)}`, '_blank')
    }
}

export function goYaml (projectUrl, branch, yamlName) {
    if (yamlName) {
        window.open(`${projectUrl}/blob/${branch}/${yamlName}`, '_blank')
    }
}

export function preciseDiff (duration) {
    if (!duration) return '--'
    const durationDate = moment.duration(Math.abs(duration))
    const timeMap = {
        y: durationDate.years(),
        mon: durationDate.months(),
        d: durationDate.days(),
        h: durationDate.hours(),
        m: durationDate.minutes(),
        s: durationDate.seconds()
    }
    const diffTime = []
    let hasFirstNum = false
    for (const key in timeMap) {
        const val = timeMap[key]
        if (val <= 0 && !hasFirstNum) continue
        hasFirstNum = true
        diffTime.push(`${val}${key}`)
    }
    return diffTime.join(' ')
}

export function timeFormatter (time, format = 'YYYY-MM-DD hh:mm:ss') {
    return time ? moment(time).format(format) : '--'
}

export function commitIdFormatter (commitId) {
    return commitId ? commitId.slice(0, 9) : '--'
}

export function getbuildTypeIcon (buildType) {
    const buildTypeIconMap = {
        manual: 'manual',
        push: 'commit',
        tag_push: 'commit',
        merge_request: 'merge'
    }
    return buildTypeIconMap[buildType] || 'well'
}

export function getPipelineStatusClass (status) {
    const statusMap = {
        CANCELED: 'warning',
        TERMINATE: 'warning',
        REVIEWING: 'warning',
        REVIEW_ABORT: 'warning',
        FAILED: 'danger',
        HEARTBEAT_TIMEOUT: 'danger',
        QUEUE_TIMEOUT: 'danger',
        EXEC_TIMEOUT: 'danger',
        SUCCEED: 'success',
        RUNNING: 'running',
        PREPARE_ENV: 'running',
        QUEUE: 'running',
        LOOP_WAITING: 'running',
        CALL_WAITING: 'running'
    }
    return statusMap[status]
}
