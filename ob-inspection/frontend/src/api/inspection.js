import axios from 'axios'

const request = axios.create({ baseURL: '/api' })

// TODO: 手动触发巡检，body 示例 { taskType: 'MANUAL', collectorType: 'jdbc' }
export function triggerInspection(data) {
  // return request.post('/inspection/trigger', data)
  return Promise.resolve(null)
}

// TODO: 查询巡检任务列表
export function listTasks() {
  // return request.get('/inspection/tasks')
  return Promise.resolve(null)
}

// TODO: 查询指定任务的巡检结果
export function listResults(taskId) {
  // return request.get(`/inspection/tasks/${taskId}/results`)
  return Promise.resolve(null)
}

// TODO: 查询告警列表
export function listAlerts() {
  // return request.get('/alerts')
  return Promise.resolve(null)
}

// TODO: 确认告警，body 示例 { ackedBy: 'admin' }
export function ackAlert(id, data) {
  // return request.post(`/alerts/${id}/ack`, data)
  return Promise.resolve(null)
}

// TODO: 触发 AI 诊断
export function diagnoseAlert(id) {
  // return request.post(`/alerts/${id}/diagnose`)
  return Promise.resolve(null)
}

// TODO: 查询 AI 诊断结果
export function getDiagnosis(id) {
  // return request.get(`/alerts/${id}/diagnosis`)
  return Promise.resolve(null)
}

// TODO: 查询看板汇总
export function getDashboardSummary() {
  // return request.get('/dashboard/summary')
  return Promise.resolve(null)
}
