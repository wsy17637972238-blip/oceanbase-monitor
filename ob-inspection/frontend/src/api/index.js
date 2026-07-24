import request from './request'

// ===== 巡检 =====

/** 手动触发巡检，body 示例 { taskType: 'MANUAL', collectorType: 'jdbc' } */
export function triggerInspection(data = {}) {
  return request.post('/inspection/trigger', data)
}

/** 巡检任务列表（倒序） */
export function listTasks() {
  return request.get('/inspection/tasks')
}

/** 指定任务的巡检结果明细 */
export function listResults(taskId) {
  return request.get(`/inspection/tasks/${taskId}/results`)
}

/** 巡检报告（结构化数据） */
export function getReport(taskId) {
  return request.get(`/inspection/tasks/${taskId}/report`)
}

/** 巡检报告 HTML 下载地址（直接浏览器下载，不走 axios 包装） */
export function reportDownloadUrl(taskId) {
  return `/api/inspection/tasks/${taskId}/report/download`
}

/** 巡检规则配置列表 */
export function listRules() {
  return request.get('/inspection/rules')
}

// ===== 告警 =====

/** 告警列表，params 可选 { status, level } */
export function listAlerts(params = {}) {
  return request.get('/alerts', { params })
}

/** 确认告警 */
export function ackAlert(id, ackedBy) {
  return request.post(`/alerts/${id}/ack`, { ackedBy })
}

/** 触发 AI 诊断（异步，立即返回） */
export function diagnoseAlert(id) {
  return request.post(`/alerts/${id}/diagnose`)
}

/** 查询 AI 诊断结果（未诊断返回 null） */
export function getDiagnosis(id) {
  return request.get(`/alerts/${id}/diagnosis`)
}

// ===== 看板 =====

/** 看板汇总数据 */
export function getDashboardSummary() {
  return request.get('/dashboard/summary')
}
