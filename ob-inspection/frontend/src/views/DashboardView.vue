<template>
  <div v-loading="loading">
    <!-- 状态与关键数字卡片 -->
    <el-row :gutter="16">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">当前健康状态</div>
          <div class="stat-status" :style="{ color: healthColor }">
            {{ statusText(summary.overallStatus || 'UNKNOWN') }}
          </div>
          <div class="stat-sub">最近巡检：{{ summary.lastInspectionTime || '暂无' }}</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">待处理告警</div>
          <div class="stat-number" :style="{ color: summary.pendingAlerts > 0 ? '#f56c6c' : '#303133' }">
            {{ summary.pendingAlerts ?? '-' }}
          </div>
          <div class="stat-sub">PENDING 状态告警数</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">告警总数</div>
          <div class="stat-number">{{ summary.totalAlerts ?? '-' }}</div>
          <div class="stat-sub">历史累计告警</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">巡检任务总数</div>
          <div class="stat-number">{{ summary.totalTasks ?? '-' }}</div>
          <div class="stat-sub">历史累计巡检次数</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="row-gap">
      <!-- 告警级别分布饼图 -->
      <el-col :span="10">
        <el-card shadow="hover">
          <template #header>告警级别分布</template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
      <!-- 最近巡检任务 -->
      <el-col :span="14">
        <el-card shadow="hover">
          <template #header>最近巡检任务</template>
          <el-table :data="summary.recentTasks || []" size="small" max-height="320">
            <el-table-column prop="taskId" label="任务ID" width="170" />
            <el-table-column label="类型" width="80">
              <template #default="{ row }">{{ statusText(row.taskType) }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="statusTagType(row.overallStatus)" size="small">
                  {{ statusText(row.overallStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="160" />
            <el-table-column label="耗时" width="80">
              <template #default="{ row }">
                {{ row.durationMs != null ? row.durationMs + ' ms' : '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import * as echarts from 'echarts'
import { getDashboardSummary } from '../api'
import { statusTagType, statusText } from '../utils/status'

const loading = ref(false)
const summary = ref({})
const pieRef = ref(null)
let pieChart = null

const healthColor = computed(() => {
  switch (summary.value.overallStatus) {
    case 'OK':
      return '#67c23a'
    case 'WARN':
      return '#e6a23c'
    case 'CRITICAL':
    case 'FAILED':
      return '#f56c6c'
    default:
      return '#909399'
  }
})

function renderPie() {
  const dist = summary.value.alertLevelDistribution || {}
  const data = Object.entries(dist).map(([name, value]) => ({ name, value }))
  if (!pieChart) {
    pieChart = echarts.init(pieRef.value)
  }
  pieChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    color: ['#f56c6c', '#e6a23c', '#909399'],
    series: [
      {
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '45%'],
        label: { formatter: '{b}: {c}' },
        data: data.length
          ? data
          : [{ name: '暂无告警', value: 1, itemStyle: { color: '#e4e7ed' } }]
      }
    ]
  })
}

function onResize() {
  pieChart && pieChart.resize()
}

async function load() {
  loading.value = true
  try {
    summary.value = await getDashboardSummary()
    await nextTick()
    renderPie()
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', onResize)
  pieChart && pieChart.dispose()
})
</script>

<style scoped>
.row-gap {
  margin-top: 16px;
}

.stat-card {
  text-align: center;
}

.stat-label {
  color: #909399;
  font-size: 13px;
}

.stat-number {
  font-size: 34px;
  font-weight: 600;
  margin: 8px 0 4px;
}

.stat-status {
  font-size: 26px;
  font-weight: 600;
  margin: 8px 0 4px;
}

.stat-sub {
  color: #c0c4cc;
  font-size: 12px;
}

.chart {
  height: 320px;
}
</style>
