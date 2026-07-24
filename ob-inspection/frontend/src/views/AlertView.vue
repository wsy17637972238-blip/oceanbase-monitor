<template>
  <div>
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <span>告警列表</span>
          <div class="filters">
            <el-select
              v-model="filters.status"
              placeholder="状态"
              clearable
              style="width: 130px"
              @change="load"
            >
              <el-option label="待确认" value="PENDING" />
              <el-option label="已确认" value="ACKED" />
            </el-select>
            <el-select
              v-model="filters.level"
              placeholder="级别"
              clearable
              style="width: 130px"
              @change="load"
            >
              <el-option label="WARN" value="WARN" />
              <el-option label="CRITICAL" value="CRITICAL" />
              <el-option label="INFO" value="INFO" />
            </el-select>
            <el-button :loading="loading" @click="load">刷新</el-button>
          </div>
        </div>
      </template>

      <el-table v-loading="loading" :data="alerts" :row-class-name="pendingRowClass">
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.level)" size="small" effect="dark">
              {{ row.level }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="itemName" label="巡检项" width="200" show-overflow-tooltip />
        <el-table-column prop="content" label="告警内容" min-width="380" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="发生时间" width="165" />
        <el-table-column label="确认人/时间" width="180">
          <template #default="{ row }">
            <span v-if="row.ackedBy">{{ row.ackedBy }} / {{ row.ackedAt }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'PENDING'"
              type="primary"
              size="small"
              link
              @click="onAck(row)"
            >
              确认
            </el-button>
            <el-button type="warning" size="small" link @click="openDiagnosis(row)">
              AI 诊断
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- AI 诊断结果弹窗 -->
    <el-dialog v-model="diagnosisDialog.visible" title="AI 诊断" width="640px">
      <div v-if="diagnosisDialog.loading" v-loading="true" class="diag-loading">
        <p>DeepSeek 分析中，请稍候...</p>
      </div>

      <template v-else-if="diagnosisDialog.data && diagnosisDialog.data.callStatus === 'SUCCESS'">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="风险等级">
            <el-tag :type="riskTagType(diagnosisDialog.data.riskLevel)" effect="dark" size="small">
              {{ statusText(diagnosisDialog.data.riskLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="根因分析">
            {{ diagnosisDialog.data.rootCause }}
          </el-descriptions-item>
          <el-descriptions-item label="处理建议">
            <div class="suggestions">{{ diagnosisDialog.data.suggestions }}</div>
          </el-descriptions-item>
          <el-descriptions-item label="模型 / Token">
            {{ diagnosisDialog.data.modelName }} / {{ diagnosisDialog.data.tokenUsed ?? '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </template>

      <el-result
        v-else-if="diagnosisDialog.data && diagnosisDialog.data.callStatus === 'FAILED'"
        icon="error"
        title="诊断失败"
        :sub-title="diagnosisDialog.data.errorMsg || '未知错误'"
      >
        <template #extra>
          <el-button type="primary" @click="startDiagnosis">重试</el-button>
        </template>
      </el-result>

      <el-empty v-else description="暂无诊断结果">
        <el-button type="primary" @click="startDiagnosis">开始诊断</el-button>
      </el-empty>
    </el-dialog>
  </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ackAlert, diagnoseAlert, getDiagnosis, listAlerts } from '../api'
import { statusTagType, statusText } from '../utils/status'

const loading = ref(false)
const alerts = ref([])
const filters = reactive({ status: '', level: '' })

const diagnosisDialog = reactive({
  visible: false,
  loading: false,
  alertId: null,
  data: null
})
let diagPollTimer = null

function pendingRowClass({ row }) {
  return row.status === 'PENDING' ? 'pending-row' : ''
}

function riskTagType(riskLevel) {
  switch (riskLevel) {
    case 'HIGH':
      return 'danger'
    case 'MEDIUM':
      return 'warning'
    case 'LOW':
      return 'success'
    default:
      return 'info'
  }
}

async function load() {
  loading.value = true
  try {
    const params = {}
    if (filters.status) params.status = filters.status
    if (filters.level) params.level = filters.level
    alerts.value = await listAlerts(params)
  } finally {
    loading.value = false
  }
}

async function onAck(row) {
  try {
    const { value } = await ElMessageBox.prompt('请输入确认人', '确认告警', {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      inputPattern: /\S+/,
      inputErrorMessage: '确认人不能为空'
    })
    await ackAlert(row.alertId, value)
    ElMessage.success('告警已确认')
    await load()
  } catch {
    // 用户取消，忽略
  }
}

function stopDiagPoll() {
  if (diagPollTimer) {
    clearInterval(diagPollTimer)
    diagPollTimer = null
  }
}

async function openDiagnosis(row) {
  stopDiagPoll()
  diagnosisDialog.visible = true
  diagnosisDialog.alertId = row.alertId
  diagnosisDialog.loading = true
  diagnosisDialog.data = null
  try {
    const existing = await getDiagnosis(row.alertId)
    diagnosisDialog.data = existing
    // 已有成功诊断直接展示；失败/进行中则提示后允许重新触发
    if (existing && existing.callStatus === 'SUCCESS') {
      return
    }
    if (existing && existing.callStatus === 'FAILED') {
      return
    }
    // 无诊断记录或仍在进行中：发起/继续轮询
    if (!existing) {
      await diagnoseAlert(row.alertId)
    }
    pollDiagnosis()
  } finally {
    if (diagnosisDialog.data && diagnosisDialog.data.callStatus !== 'RUNNING') {
      diagnosisDialog.loading = false
    }
  }
}

async function startDiagnosis() {
  stopDiagPoll()
  diagnosisDialog.loading = true
  await diagnoseAlert(diagnosisDialog.alertId)
  pollDiagnosis()
}

// 轮询诊断结果：间隔 2.5s，最多 12 次（30s）
function pollDiagnosis() {
  let attempts = 0
  diagPollTimer = setInterval(async () => {
    attempts++
    try {
      const data = await getDiagnosis(diagnosisDialog.alertId)
      if (data && data.callStatus !== 'RUNNING') {
        diagnosisDialog.data = data
        diagnosisDialog.loading = false
        stopDiagPoll()
      } else if (attempts >= 12) {
        diagnosisDialog.loading = false
        stopDiagPoll()
        ElMessage.warning('诊断超时，请稍后重试')
      }
    } catch {
      diagnosisDialog.loading = false
      stopDiagPoll()
    }
  }, 2500)
}

onMounted(load)

onBeforeUnmount(stopDiagPoll)
</script>

<style>
.el-table .pending-row {
  background-color: #fef0f0;
}
</style>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.filters {
  display: flex;
  gap: 8px;
}

.diag-loading {
  height: 160px;
  text-align: center;
  color: #909399;
}

.suggestions {
  white-space: pre-wrap;
  line-height: 1.7;
}
</style>
