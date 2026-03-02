<template>
  <AnalyticsPageShell title="分析中心">
    <template #filters>
      <div class="filter-header">
        <h2>分析中心</h2>
        <p>按时间与维度查看出勤趋势、成绩质量与风险学生分布。</p>
      </div>
      <el-form :inline="true" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            unlink-panels
            value-format="YYYY-MM-DD"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="filters.semester" clearable placeholder="如 2026-2027-1" />
        </el-form-item>
        <el-form-item v-if="userRole !== 'STUDENT'" label="班级ID">
          <el-input-number v-model="filters.classId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item v-if="userRole === 'ADMIN'" label="教师ID">
          <el-input-number v-model="filters.teacherId" :min="1" controls-position="right" />
        </el-form-item>
        <el-form-item label="粒度">
          <el-select v-model="filters.granularity" style="width: 120px">
            <el-option label="按天" value="day" />
            <el-option label="按周" value="week" />
            <el-option label="按月" value="month" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton :loading="loading" @click="refreshAll">刷新分析</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="resetFilters">重置</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #insight>
      <p class="text-sm leading-7 text-slate-600">
        当前查询区间为 {{ dateRange[0] }} 至 {{ dateRange[1] }}，可通过风险类型切换查看低分、异常考勤和审批超时分布。
      </p>
    </template>

    <template #kpi>
      <AppCard v-if="!isStudent" class="kpi-card" content-class="kpi-body">
        <span class="kpi-label">学生规模</span>
        <strong class="kpi-value">{{ overview.studentCount }}</strong>
      </AppCard>
      <AppCard class="kpi-card" content-class="kpi-body">
        <span class="kpi-label">{{ pendingKpiLabel }}</span>
        <strong class="kpi-value">{{ overview.pendingApprovalCount }}</strong>
      </AppCard>
      <AppCard class="kpi-card" content-class="kpi-body">
        <span class="kpi-label">出勤率</span>
        <strong class="kpi-value">{{ formatPercent(overview.attendanceRate) }}</strong>
        <span class="kpi-trend" :class="trendClass(overview.attendanceRateChange)">
          {{ formatDelta(overview.attendanceRateChange) }}
        </span>
      </AppCard>
      <AppCard class="kpi-card" content-class="kpi-body">
        <span class="kpi-label">审批平均时长(小时)</span>
        <strong class="kpi-value">{{ formatNumber(overview.approvalAvgHours) }}</strong>
        <span class="kpi-trend" :class="trendClass(-overview.approvalAvgHoursChange)">
          {{ formatDelta(overview.approvalAvgHoursChange) }}
        </span>
      </AppCard>
      <AppCard class="kpi-card" content-class="kpi-body">
        <span class="kpi-label">{{ lowScoreKpiLabel }}</span>
        <strong class="kpi-value">{{ overview.lowScoreRiskCount }}</strong>
        <span class="kpi-trend" :class="trendClass(-overview.lowScoreRiskChange)">
          {{ formatDelta(overview.lowScoreRiskChange) }}
        </span>
      </AppCard>
    </template>

    <template #charts>
      <AppCard class="chart-card" title="异常考勤趋势" content-class="p-4">
        <div ref="attendanceTrendRef" class="chart-canvas"></div>
      </AppCard>
      <AppCard class="chart-card" title="成绩质量趋势" content-class="p-4">
        <div ref="scoreTrendRef" class="chart-canvas"></div>
      </AppCard>
    </template>

    <AppCard class="risk-card" title="风险学生榜单" content-class="p-4">
      <template #header>
        <el-radio-group v-model="riskType" size="small" @change="handleRiskTypeChange">
          <el-radio-button label="low_score">低分风险</el-radio-button>
          <el-radio-button label="abnormal_attendance">异常考勤</el-radio-button>
          <el-radio-button label="approval_overdue">审批超时</el-radio-button>
        </el-radio-group>
      </template>

      <el-table :data="riskRecords" v-loading="riskLoading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column v-if="!isStudent" prop="studentNo" label="学号" width="140" />
        <el-table-column v-if="!isStudent" prop="studentName" label="姓名" width="120" />
        <el-table-column v-if="!isStudent" prop="className" label="班级" min-width="140" />
        <el-table-column v-if="!isStudent" prop="riskCount" label="风险次数" width="120" />
        <el-table-column v-if="!isStudent" label="风险值" width="140">
          <template #default="{ row }">{{ formatNumber(row.riskValue) }}</template>
        </el-table-column>

        <template v-if="isStudent && riskType === 'low_score'">
          <el-table-column prop="courseName" label="课程" min-width="180" />
          <el-table-column label="最低分" width="120">
            <template #default="{ row }">{{ formatNumber(row.score) }}</template>
          </el-table-column>
          <el-table-column prop="riskCount" label="低分次数" width="120" />
        </template>

        <template v-if="isStudent && riskType === 'abnormal_attendance'">
          <el-table-column label="日期" width="140">
            <template #default="{ row }">{{ row.attendanceDate || '-' }}</template>
          </el-table-column>
          <el-table-column prop="courseName" label="课程" min-width="180" />
          <el-table-column label="考勤状态" width="120">
            <template #default="{ row }">{{ formatAttendanceStatus(row.attendanceStatus) }}</template>
          </el-table-column>
        </template>

        <template v-if="isStudent && riskType === 'approval_overdue'">
          <el-table-column prop="leaveRequestId" label="审批单号" width="120" />
          <el-table-column label="提交时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
          </el-table-column>
          <el-table-column label="超时状态" width="120">
            <template #default="{ row }">
              <el-tag :type="row.overdue ? 'danger' : 'info'" size="small">
                {{ row.overdue ? '已超时' : '未超时' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="超时小时" width="120">
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
    </AppCard>
  </AnalyticsPageShell>
</template>
<script setup>
import { nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AnalyticsPageShell from '@/components/shell/AnalyticsPageShell.vue'
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
const pendingKpiLabel = isStudent ? '我的待办审批' : '待处理审批'
const lowScoreKpiLabel = isStudent ? '低分课程统计数' : '低分风险人数'

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
  if (status === 'ABSENT') return '缺勤'
  if (status === 'LATE') return '迟到'
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
    ElMessage.error('获取分析数据失败')
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
        name: '异常考勤',
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
    legend: { data: ['均分', '及格率', '优秀率'], top: 0 },
    grid: { top: 36, left: 40, right: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: { color: '#4d6f80' }
    },
    yAxis: [
      {
        type: 'value',
        name: '分值',
        min: 0,
        max: 100,
        axisLabel: { color: '#4d6f80' },
        splitLine: { lineStyle: { color: 'rgba(93, 147, 155, 0.2)' } }
      },
      {
        type: 'value',
        name: '百分比',
        min: 0,
        max: 100,
        axisLabel: { formatter: '{value}%' }
      }
    ],
    series: [
      {
        name: '均分',
        type: 'line',
        smooth: true,
        data: avg,
        lineStyle: { color: '#177c77', width: 3 },
        itemStyle: { color: '#177c77' }
      },
      {
        name: '及格率',
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: pass,
        lineStyle: { color: '#3b8fa4', width: 2 },
        itemStyle: { color: '#3b8fa4' }
      },
      {
        name: '优秀率',
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

.kpi-card {
  border-radius: 16px;
  border: 1px solid rgba(24, 97, 110, 0.2);
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.94), rgba(238, 248, 247, 0.84));
}

.kpi-body {
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

.chart-card,
.risk-card {
  border-radius: 18px;
  border: 1px solid rgba(21, 88, 102, 0.17);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 248, 0.84));
}

.chart-canvas {
  height: 320px;
}

.pagination {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
