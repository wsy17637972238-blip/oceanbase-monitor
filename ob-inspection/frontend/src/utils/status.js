// 状态 → Element Plus tag 类型 / 展示色 的统一映射
export function statusTagType(status) {
  switch (status) {
    case 'OK':
    case 'ACKED':
    case 'SUCCESS':
      return 'success'
    case 'WARN':
      return 'warning'
    case 'CRITICAL':
    case 'FAILED':
    case 'PENDING':
      return 'danger'
    case 'RUNNING':
      return 'primary'
    default:
      return 'info'
  }
}

// 状态中文文案
export function statusText(status) {
  const map = {
    OK: '正常',
    WARN: '警告',
    CRITICAL: '严重',
    FAILED: '失败',
    RUNNING: '执行中',
    PENDING: '待确认',
    ACKED: '已确认',
    SUCCESS: '成功',
    UNKNOWN: '暂无数据',
    MANUAL: '手动',
    SCHEDULED: '定时',
    LOW: '低',
    MEDIUM: '中',
    HIGH: '高'
  }
  return map[status] || status
}
