<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>巡检任务</span>
          <el-button type="primary" :loading="triggering" @click="onTrigger">
            手动触发巡检
          </el-button>
        </div>
      </template>

      <el-table v-loading="loading" :data="tasks" @expand-change="onExpand">
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="result-panel">
              <el-table
                v-loading="resultLoading[row.taskId]"
                :data="resultsMap[row.taskId] || []"
                size="small"
              >
                <el-table-column prop="itemLabel" label="巡检项" width="120" />
                <el-table-column prop="itemName" label="指标" width="220" />
                <el-table-column label="状态" width="90">
                  <template #default="{ row: r }">
                    <el-tag :type="statusTagType(r.status)" size="small">
                      {{ statusText(r.status) }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="metricValue" label="实测值" width="220" show-overflow-tooltip />
                <el-table-column prop="threshold" label="阈值" width="220" show-overflow-tooltip />
                <el-table-column prop="detail" label="判定依据" show-overflow-tooltip />
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="taskId" label="任务ID" width="170" />
        <el-table-column label="类型" width="90">
          <template #default="{ row }">{{ statusText(row.taskType) }}</template>
        </el-table-column>
        <el-table-column label="总体状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.overallStatus)" size="small">
              {{ statusText(row.overallStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectorType" label="采集器" width="90" />
        <el-table-column prop="startTime" label="开始时间" width="165" />
        <el-table-column prop="endTime" label="结束时间" width="165" />
        <el-table-column label="耗时" width="90">
          <template #default="{ row }">
            {{ row.durationMs != null ? row.durationMs + ' ms' : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openReport(row)">
              查看报告
            </el-button>
            <el-button type="success" size="small" link @click="downloadReport(row)">
              下载报告
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 巡检报告弹窗 -->
    <el-dialog v-model="reportDialog.visible" title="巡检报告" width="760px">
      <div v-loading="reportDialog.loading">
        <template v-if="reportDialog.data">
          <div class="report-head">
            <div class="report-score" :style="{ color: scoreColor(reportDialog.data.healthScore) }">
              {{ reportDialog.data.healthScore }}
              <span class="report-score-max">/ 100</span>
            </div>
            <el-descriptions :column="2" border size="small" class="report-meta">
              <el-descriptions-item label="报告编号">{{ reportDialog.data.taskId }}</el-descriptions-item>
              <el-descriptions-item label="巡检时间">{{ reportDialog.data.startTime }}</el-descriptions-item>
              <el-descriptions-item label="总体状态">
                <el-tag :type="statusTagType(reportDialog.data.overallStatus)" size="small">
                  {{ statusText(reportDialog.data.overallStatus) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="执行耗时">
                {{ reportDialog.data.durationMs != null ? reportDialog.data.durationMs + ' ms' : '-' }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <el-alert type="info" :closable="false" class="report-section">
            <template #title>{{ reportDialog.data.conclusion }}</template>
          </el-alert>
          <p class="score-explain">{{ reportDialog.data.scoreExplanation }}</p>

          <h4 class="report-section">巡检明细</h4>
          <el-table :data="reportDialog.data.items" size="small">
            <el-table-column prop="itemLabel" label="巡检项" width="110" />
            <el-table-column label="状态" width="80">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.status)" size="small">
                  {{ statusText(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="metricValue" label="实测值" width="150" show-overflow-tooltip />
            <el-table-column prop="detail" label="判定依据" show-overflow-tooltip />
          </el-table>

          <template v-if="abnormalItems.length">
            <h4 class="report-section">异常项整改建议</h4>
            <div v-for="item in abnormalItems" :key="item.itemName" class="advice-block">
              <div class="advice-title">
                <el-tag :type="statusTagType(item.status)" size="small">
                  {{ statusText(item.status) }}
                </el-tag>
                {{ item.itemLabel }}（{{ item.itemName }}）
              </div>
              <div class="advice-body">{{ item.suggestion }}</div>
              <div v-if="item.aiSuggestion" class="advice-ai">
                <strong>AI 辅助分析：</strong>{{ item.aiSuggestion }}
              </div>
            </div>
          </template>
        </template>
      </div>
      <template #footer>
        <el-button @click="reportDialog.visible = false">关闭</el-button>
        <el-button type="success" @click="downloadReport({ taskId: reportDialog.data?.taskId })">
          下载 HTML 报告
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getReport, listResults, listTasks, reportDownloadUrl, triggerInspection } from '../api'
import { statusTagType, statusText } from '../utils/status'

const loading = ref(false)
const triggering = ref(false)
const tasks = ref([])
const resultsMap = ref({})
const resultLoading = ref({})

const reportDialog = reactive({ visible: false, loading: false, data: null })

const abnormalItems = computed(
  () => (reportDialog.data?.items || []).filter((item) => item.status !== 'OK')
)

function scoreColor(score) {
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
}

async function openReport(row) {
  reportDialog.visible = true
  reportDialog.loading = true
  reportDialog.data = null
  try {
    reportDialog.data = await getReport(row.taskId)
  } finally {
    reportDialog.loading = false
  }
}

function downloadReport(row) {
  if (!row.taskId) return
  window.open(reportDownloadUrl(row.taskId), '_blank')
}
let pollTimer = null

async function load() {
  loading.value = true
  try {
    tasks.value = await listTasks()
  } finally {
    loading.value = false
  }
}

async function onExpand(row, expandedRows) {
  if (!expandedRows.includes(row) || resultsMap.value[row.taskId]) {
    return
  }
  resultLoading.value[row.taskId] = true
  try {
    resultsMap.value[row.taskId] = await listResults(row.taskId)
  } finally {
    resultLoading.value[row.taskId] = false
  }
}

// 触发后轮询该任务直到终态（RUNNING → OK/WARN/CRITICAL/FAILED）
async function onTrigger() {
  triggering.value = true
  try {
    const task = await triggerInspection({})
    ElMessage.info(`任务 ${task.taskId} 已触发，执行中...`)
    await load()
    await pollTaskUntilFinished(task.taskId)
    await load()
    const finished = tasks.value.find((t) => t.taskId === task.taskId)
    if (finished) {
      const type = ['OK'].includes(finished.overallStatus)
        ? 'success'
        : ['WARN'].includes(finished.overallStatus)
          ? 'warning'
          : 'error'
      ElMessage({
        type,
        message: `任务执行完成，总体状态：${statusText(finished.overallStatus)}`
      })
    }
  } finally {
    triggering.value = false
  }
}

function pollTaskUntilFinished(taskId) {
  return new Promise((resolve) => {
    let attempts = 0
    pollTimer = setInterval(async () => {
      attempts++
      try {
        const list = await listTasks()
        tasks.value = list
        const current = list.find((t) => t.taskId === taskId)
        if (!current || current.overallStatus !== 'RUNNING' || attempts >= 15) {
          clearInterval(pollTimer)
          pollTimer = null
          resolve()
        }
      } catch {
        clearInterval(pollTimer)
        pollTimer = null
        resolve()
      }
    }, 2000)
  })
}

onMounted(load)

onBeforeUnmount(() => {
  if (pollTimer) {
    clearInterval(pollTimer)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.result-panel {
  padding: 8px 24px;
  background-color: #fafafa;
}

.report-head {
  display: flex;
  align-items: center;
  gap: 32px;
}

.report-score {
  font-size: 52px;
  font-weight: 700;
  line-height: 1;
  white-space: nowrap;
}

.report-score-max {
  font-size: 18px;
  color: #909399;
  font-weight: 400;
}

.report-meta {
  flex: 1;
}

.report-section {
  margin-top: 16px;
}

.score-explain {
  color: #909399;
  font-size: 12px;
  margin: 8px 0 0;
}

.advice-block {
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px 14px;
  margin-bottom: 10px;
}

.advice-title {
  font-weight: 600;
  margin-bottom: 6px;
}

.advice-body {
  color: #606266;
  font-size: 13px;
}

.advice-ai {
  background: #f0f5ff;
  border-left: 3px solid #2f54eb;
  padding: 8px 12px;
  margin-top: 8px;
  font-size: 13px;
  white-space: pre-wrap;
}
</style>
