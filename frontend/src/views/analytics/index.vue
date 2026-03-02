<template>
  <div class="analytics-page">
    <el-card class="filter-card">
      <div class="filter-header">
        <h2>鍒嗘瀽涓績</h2>
        <p>鎸夋椂闂翠笌缁村害鏌ョ湅鍑哄嫟瓒嬪娍銆佹垚缁╄川閲忎笌椋庨櫓瀛︾敓鍒嗗竷銆?/p>
      </div>
      <el-form :inline="true" class="filter-form">
        <el-form-item label="鏃堕棿鑼冨洿">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            unlink-panels
            value-format="YYYY-MM-DD"
            start-placeholder="寮€濮嬫棩鏈?
            end-placeholder="缁撴潫鏃ユ湡"
          />
        </el-form-item>
        <el-form-item label="瀛︽湡">
          <el-input v-model="filters.semester" clearable placeholder="濡?2026-2027-1" />
        </el-form-item>
        <el-form-item v-if="userRole !== 'STUDENT'" label="鐝骇ID">
          <el-input-number v-model="filters.classId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="userRole === 'ADMIN'" label="鏁欏笀ID">
          <el-input-number v-model="filters.teacherId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="绮掑害">
          <el-select v-model="filters.granularity" style="width: 120px">
            <el-option label="鎸夊ぉ" value="day" />
            <el-option label="鎸夊懆" value="week" />
            <el-option label="鎸夋湀" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="refreshAll">鍒锋柊鍒嗘瀽</el-button>
          <el-button @click="resetFilters">閲嶇疆</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <section class="kpi-grid" :class="{ 'kpi-grid-student': isStudent }">
      <el-card v-if="!isStudent" class="kpi-card">
        <span class="kpi-label">瀛︾敓瑙勬ā</span>
        <strong class="kpi-value">{{ overview.studentCount }}</strong>
      </el-card>
      <el-card class="kpi-card">
        <span class="kpi-label">{{ pendingKpiLabel }}</span>
        <strong class="kpi-value">{{ overview.pendingApprovalCount }}</strong>
      </el-card>
      <el-card class="kpi-card">
        <span class="kpi-label">鍑哄嫟鐜?/span>
        <strong class="kpi-value">{{ formatPercent(overview.attendanceRate) }}</strong>
        <span class="kpi-trend" :class="trendClass(overview.attendanceRateChange)">
          {{ formatDelta(overview.attendanceRateChange) }}
        </span>
      </el-card>
      <el-card class="kpi-card">
        <span class="kpi-label">瀹℃壒骞冲潎鏃堕暱(灏忔椂)</span>
        <strong class="kpi-value">{{ formatNumber(overview.approvalAvgHours) }}</strong>
        <span class="kpi-trend" :class="trendClass(-overview.approvalAvgHoursChange)">
          {{ formatDelta(overview.approvalAvgHoursChange) }}
        </span>
      </el-card>
      <el-card class="kpi-card">
        <span class="kpi-label">{{ lowScoreKpiLabel }}</span>
        <strong class="kpi-value">{{ overview.lowScoreRiskCount }}</strong>
        <span class="kpi-trend" :class="trendClass(-overview.lowScoreRiskChange)">
          {{ formatDelta(overview.lowScoreRiskChange) }}
        </span>
      </el-card>
    </section>

    <section class="chart-grid">
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>寮傚父鑰冨嫟瓒嬪娍</span>
          </div>
        </template>
        <div ref="attendanceTrendRef" class="chart-canvas"></div>
      </el-card>
      <el-card class="chart-card">
        <template #header>
          <div class="card-header">
            <span>鎴愮哗璐ㄩ噺瓒嬪娍</span>
          </div>
        </template>
        <div ref="scoreTrendRef" class="chart-canvas"></div>
      </el-card>
    </section>

    <el-card class="risk-card">
      <template #header>
        <div class="card-header">
          <span>椋庨櫓瀛︾敓姒滃崟</span>
          <el-radio-group v-model="riskType" size="small" @change="handleRiskTypeChange">
            <el-radio-button label="low_score">浣庡垎椋庨櫓</el-radio-button>
            <el-radio-button label="abnormal_attendance">寮傚父鑰冨嫟</el-radio-button>
            <el-radio-button label="approval_overdue">瀹℃壒瓒呮椂</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="riskRecords" v-loading="riskLoading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column v-if="!isStudent" prop="studentNo" label="瀛﹀彿" width="140" />
        <el-table-column v-if="!isStudent" prop="studentName" label="濮撳悕" width="120" />
        <el-table-column v-if="!isStudent" prop="className" label="鐝骇" min-width="140" />
        <el-table-column v-if="!isStudent" prop="riskCount" label="椋庨櫓娆℃暟" width="120" />
        <el-table-column v-if="!isStudent" label="椋庨櫓鍊? width="140">
          <template #default="{ row }">{{ formatNumber(row.riskValue) }}</template>
        </el-table-column>

        <template v-if="isStudent && riskType === 'low_score'">
          <el-table-column prop="courseName" label="璇剧▼" min-width="180" />
          <el-table-column label="鏈€浣庡垎" width="120">
            <template #default="{ row }">{{ formatNumber(row.score) }}</template>
          </el-table-column>
          <el-table-column prop="riskCount" label="浣庡垎娆℃暟" width="120" />
        </template>

        <template v-if="isStudent && riskType === 'abnormal_attendance'">
          <el-table-column label="鏃ユ湡" width="140">
            <template #default="{ row }">{{ row.attendanceDate || '-' }}</template>
          </el-table-column>
          <el-table-column prop="courseName" label="璇剧▼" min-width="180" />
          <el-table-column label="鑰冨嫟鐘舵€? width="120">
            <template #default="{ row }">{{ formatAttendanceStatus(row.attendanceStatus) }}</template>
          </el-table-column>
        </template>

        <template v-if="isStudent && riskType === 'approval_overdue'">
          <el-table-column prop="leaveRequestId" label="瀹℃壒鍗曞彿" width="120" />
          <el-table-column label="鎻愪氦鏃堕棿" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
          </el-table-column>
          <el-table-column label="瓒呮椂鐘舵€? width="120">
            <template #default="{ row }">
              <el-tag :type="row.overdue ? 'danger' : 'info'" size="small">
                {{ row.overdue ? '宸茶秴鏃? : '鏈秴鏃? }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="瓒呮椂灏忔椂" width="120">
            <template #default="{ row }">{{ formatNumber(row.overdueHours) }}</template>
          </el-table-column>
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="riskPage"
        v-model:page-size="riskSize"
        :total="riskTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchRiskStudents"
        @current-change="fetchRiskStudents"
      />
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { init } from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { use } from 'echarts/core'
import { getAnalyticsOverview, getAttendanceTrend, getRiskStudents, getScoreTrend } from '@/api/analytics'

use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const route = useRoute()
const loading = ref(false)
const riskLoading = ref(false)
const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const userRole = userInfo?.role || 'STUDENT'
const isAdmin = userRole === 'ADMIN'
const isStudent = userRole === 'STUDENT'
const pendingKpiLabel = isStudent ? '鎴戠殑寰呭姙瀹℃壒' : '寰呭鐞嗗鎵?
const lowScoreKpiLabel = isStudent ? '浣庡垎璇剧▼缁熻鏁? : '浣庡垎椋庨櫓浜烘暟'

const today = new Date()
const thirtyDaysAgo = new Date(today.getTime() - 29 * 24 * 60 * 60 * 1000)
const dateRange = ref([formatDate(thirtyDaysAgo), formatDate(today)])
const filters = reactive({
  semester: '',
  classId: null,
  teacherId: null,
  granularity: 'day'
})

const overview = reactive({
  studentCount: 0,
  pendingApprovalCount: 0,
  attendanceRate: 0,
  attendanceRateChange: 0,
  approvalAvgHours: 0,
  approvalAvgHoursChange: 0,
  lowScoreRiskCount: 0,
  lowScoreRiskChange: 0
})

const riskType = ref('low_score')
const riskPage = ref(1)
const riskSize = ref(10)
const riskTotal = ref(0)
const riskRecords = ref([])

const attendanceTrendRef = ref(null)
const scoreTrendRef = ref(null)
let attendanceTrendChart = null
let scoreTrendChart = null

function formatDate(date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function buildParams() {
  const params = {
    startDate: dateRange.value?.[0],
    endDate: dateRange.value?.[1],
    semester: filters.semester || undefined
  }
  if (!isStudent) {
    params.classId = filters.classId || undefined
  }
  if (isAdmin) {
    params.teacherId = filters.teacherId || undefined
  }
  return params
}

function formatNumber(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2).replace(/\.00$/, '') : '0'
}

function formatAttendanceStatus(status) {
  if (status === 'ABSENT') return '缂哄嫟'
  if (status === 'LATE') return '杩熷埌'
  return status || '-'
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) {
    return String(value)
  }
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  const d = String(date.getDate()).padStart(2, '0')
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  return `${y}-${m}-${d} ${hh}:${mm}`
}

function formatPercent(value) {
  return `${formatNumber(value)}%`
}

function formatDelta(value) {
  const number = Number(value || 0)
  const prefix = number > 0 ? '+' : ''
  return `${prefix}${formatNumber(number)}%`
}

function trendClass(value) {
  const number = Number(value || 0)
  if (number > 0) return 'positive'
  if (number < 0) return 'negative'
  return 'neutral'
}

async function fetchOverview() {
  const res = await getAnalyticsOverview(buildParams())
  const data = res.data || {}
  overview.studentCount = Number(data.studentCount || 0)
  overview.pendingApprovalCount = Number(data.pendingApprovalCount || 0)
  overview.attendanceRate = Number(data.attendanceRate || 0)
  overview.attendanceRateChange = Number(data.attendanceRateChange || 0)
  overview.approvalAvgHours = Number(data.approvalAvgHours || 0)
  overview.approvalAvgHoursChange = Number(data.approvalAvgHoursChange || 0)
  overview.lowScoreRiskCount = Number(data.lowScoreRiskCount || 0)
  overview.lowScoreRiskChange = Number(data.lowScoreRiskChange || 0)
}

async function fetchAttendanceTrend() {
  const res = await getAttendanceTrend({
    ...buildParams(),
    granularity: filters.granularity
  })
  renderAttendanceTrend(res.data || [])
}

async function fetchScoreTrend() {
  const res = await getScoreTrend({
    ...buildParams(),
    granularity: filters.granularity
  })
  renderScoreTrend(res.data || [])
}

async function fetchRiskStudents() {
  riskLoading.value = true
  try {
    const res = await getRiskStudents({
      ...buildParams(),
      riskType: riskType.value,
      page: riskPage.value,
      size: riskSize.value
    })
    riskRecords.value = res.data?.records || []
    riskTotal.value = Number(res.data?.total || 0)
  } finally {
    riskLoading.value = false
  }
}

async function refreshAll() {
  loading.value = true
  try {
    await Promise.all([
      fetchOverview(),
      fetchAttendanceTrend(),
      fetchScoreTrend(),
      fetchRiskStudents()
    ])
  } catch (_e) {
    ElMessage.error('鑾峰彇鍒嗘瀽鏁版嵁澶辫触')
  } finally {
    loading.value = false
  }
}

function resetFilters() {
  dateRange.value = [formatDate(thirtyDaysAgo), formatDate(today)]
  filters.semester = ''
  filters.classId = null
  filters.teacherId = null
  filters.granularity = 'day'
  riskType.value = 'low_score'
  riskPage.value = 1
  refreshAll()
}

function handleRiskTypeChange(type) {
  riskType.value = type
  riskPage.value = 1
  fetchRiskStudents()
}

function renderAttendanceTrend(records) {
  if (!attendanceTrendChart) return
  const labels = records.map((item) => item.periodLabel)
  const values = records.map((item) => Number(item.count || 0))
  attendanceTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 20, left: 40, right: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#4d6f80' }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: { color: '#4d6f80' },
      splitLine: { lineStyle: { color: 'rgba(93, 147, 155, 0.2)' } }
    },
    series: [
      {
        name: '寮傚父鑰冨嫟',
        type: 'bar',
        barWidth: 18,
        data: values,
        itemStyle: {
          borderRadius: [8, 8, 0, 0],
          color: '#1b8e88'
        }
      }
    ]
  })
}

function renderScoreTrend(records) {
  if (!scoreTrendChart) return
  const labels = records.map((item) => item.periodLabel)
  const avg = records.map((item) => Number(item.avgScore || 0))
  const pass = records.map((item) => Number(item.passRate || 0))
  const excellent = records.map((item) => Number(item.excellentRate || 0))
  scoreTrendChart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['鍧囧垎', '鍙婃牸鐜?, '浼樼鐜?], top: 0 },
    grid: { top: 36, left: 40, right: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#4d6f80' }
    },
    yAxis: [
      {
        type: 'value',
        name: '鍒嗗€?,
        min: 0,
        max: 100,
        axisLabel: { color: '#4d6f80' },
        splitLine: { lineStyle: { color: 'rgba(93, 147, 155, 0.2)' } }
      },
      {
        type: 'value',
        name: '鐧惧垎姣?,
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}%' }
      }
    ],
    series: [
      {
        name: '鍧囧垎',
        type: 'line',
        smooth: true,
        data: avg,
        lineStyle: { color: '#177c77', width: 3 },
        itemStyle: { color: '#177c77' }
      },
      {
        name: '鍙婃牸鐜?,
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: pass,
        lineStyle: { color: '#3b8fa4', width: 2 },
        itemStyle: { color: '#3b8fa4' }
      },
      {
        name: '浼樼鐜?,
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: excellent,
        lineStyle: { color: '#6e9f5f', width: 2 },
        itemStyle: { color: '#6e9f5f' }
      }
    ]
  })
}

function handleResize() {
  attendanceTrendChart?.resize()
  scoreTrendChart?.resize()
}

function initCharts() {
  nextTick(() => {
    if (attendanceTrendRef.value && !attendanceTrendChart) {
      attendanceTrendChart = init(attendanceTrendRef.value)
    }
    if (scoreTrendRef.value && !scoreTrendChart) {
      scoreTrendChart = init(scoreTrendRef.value)
    }
    handleResize()
  })
}

function applyRoutePreset() {
  const query = route.query || {}
  if (query.riskType) {
    const value = String(query.riskType)
    if (['low_score', 'abnormal_attendance', 'approval_overdue'].includes(value)) {
      riskType.value = value
    }
  }
  if (!isStudent && query.classId) {
    filters.classId = Number(query.classId)
  } else if (isStudent) {
    filters.classId = null
  }
  if (isAdmin && query.teacherId) {
    filters.teacherId = Number(query.teacherId)
  } else if (!isAdmin) {
    filters.teacherId = null
  }
}

watch(
  () => filters.granularity,
  () => {
    fetchAttendanceTrend()
    fetchScoreTrend()
  }
)

watch(
  () => route.fullPath,
  () => {
    applyRoutePreset()
    refreshAll()
  }
)

onMounted(() => {
  applyRoutePreset()
  initCharts()
  refreshAll()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  attendanceTrendChart?.dispose()
  scoreTrendChart?.dispose()
  attendanceTrendChart = null
  scoreTrendChart = null
})

</script>

<style scoped lang="scss">
.analytics-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  border-radius: 18px;
  border: 1px solid rgba(21, 88, 102, 0.17);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 248, 0.84));
}

.filter-header h2 {
  margin: 0;
  font-size: 26px;
  color: #173749;
}

.filter-header p {
  margin: 8px 0 14px;
  color: #446173;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px 0;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(5, minmax(0, 1fr));
  gap: 12px;
}

.kpi-grid-student {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.kpi-card {
  border-radius: 16px;
  border: 1px solid rgba(24, 97, 110, 0.2);
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.94), rgba(238, 248, 247, 0.84));
}

.kpi-card :deep(.el-card__body) {
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.kpi-label {
  font-size: 13px;
  color: #4e6574;
}

.kpi-value {
  font-size: 30px;
  line-height: 1;
  color: #163548;
}

.kpi-trend {
  font-size: 12px;
  font-weight: 600;
}

.kpi-trend.positive {
  color: #2e7d4f;
}

.kpi-trend.negative {
  color: #c35f4e;
}

.kpi-trend.neutral {
  color: #66808f;
}

.chart-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.chart-card,
.risk-card {
  border-radius: 18px;
  border: 1px solid rgba(21, 88, 102, 0.17);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 248, 0.84));
}

.chart-canvas {
  height: 320px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}

@media (max-width: 1400px) {
  .kpi-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .kpi-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .chart-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .kpi-grid {
    grid-template-columns: 1fr;
  }
}
</style>

