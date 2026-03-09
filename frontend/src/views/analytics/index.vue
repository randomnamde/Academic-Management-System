<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('analytics.pageTitle')" content-class="p-4 space-y-4">
      <div class="analytics-hero">
        <div class="analytics-hero-copy">
          <p class="analytics-hero-badge">{{ t('analytics.heroBadge') }}</p>
          <h2 class="analytics-hero-title">{{ t('analytics.pageTitle') }}</h2>
          <p class="analytics-hero-desc">{{ t('analytics.pageDesc') }}</p>
          <p class="analytics-hero-insight">
            {{ t('analytics.insight', { start: dateRange[0], end: dateRange[1] }) }}
          </p>
        </div>

        <div class="analytics-hero-pill">
          <span class="analytics-hero-pill-label">{{ t('analytics.heroRoleLabel') }}</span>
          <strong>{{ currentRoleLabel }}</strong>
        </div>
      </div>

      <div class="analytics-stats" :style="statsTrackStyle">
        <article
          v-for="stat in statCards"
          :key="stat.label"
          class="analytics-stat"
          :class="[
            `analytics-stat-${stat.tone}`,
            stat.featured ? 'is-featured' : '',
            stat.compact ? 'is-compact' : ''
          ]"
        >
          <div class="analytics-stat-head">
            <span class="analytics-stat-badge">
              <component :is="stat.icon" class="h-4 w-4" />
            </span>
            <span
              v-if="stat.delta"
              class="analytics-stat-trend"
              :class="trendClass(stat.deltaValue)"
            >
              {{ stat.delta }}
            </span>
          </div>
          <div class="analytics-stat-main">
            <p class="analytics-stat-label">{{ stat.label }}</p>
            <p class="analytics-stat-value">{{ stat.value }}</p>
          </div>
          <p class="analytics-stat-note">{{ stat.note }}</p>
        </article>
      </div>

      <section class="analytics-section">
        <div class="analytics-filter-panel">
          <div class="analytics-workspace-head">
            <div>
              <p class="analytics-workspace-kicker">{{ t('analytics.filterTitle') }}</p>
              <h3 class="analytics-workspace-title">{{ t('analytics.filterDesc') }}</h3>
            </div>
            <div class="analytics-workspace-meta">
              <span class="analytics-workspace-chip">{{ currentRangeLabel }}</span>
              <span class="analytics-workspace-chip">{{ currentGranularityLabel }}</span>
            </div>
          </div>

          <el-form class="analytics-filter-form" label-position="top">
            <el-form-item :label="t('analytics.dateRange')">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                unlink-panels
                value-format="YYYY-MM-DD"
                :start-placeholder="t('analytics.startDate')"
                :end-placeholder="t('analytics.endDate')"
              />
            </el-form-item>
            <el-form-item :label="t('analytics.semester')">
              <el-select
                v-model="filters.semester"
                clearable
                filterable
                :loading="semesterOptionsLoading"
                :placeholder="t('analytics.selectSemester')"
              >
                <el-option
                  v-for="option in semesterOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="!isStudent" :label="t('analytics.classId')">
              <el-select
                v-model="filters.classId"
                clearable
                filterable
                :loading="classOptionsLoading"
                :placeholder="t('analytics.selectClass')"
              >
                <el-option
                  v-for="option in classOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item v-if="isAdmin" :label="t('analytics.teacherId')">
              <el-select
                v-model="filters.teacherId"
                clearable
                filterable
                :loading="teacherOptionsLoading"
                :placeholder="t('analytics.selectTeacher')"
              >
                <el-option
                  v-for="option in teacherOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>
            <el-form-item :label="t('analytics.granularity')">
              <el-select v-model="filters.granularity">
                <el-option :label="t('analytics.day')" value="day" />
                <el-option :label="t('analytics.week')" value="week" />
                <el-option :label="t('analytics.month')" value="month" />
              </el-select>
            </el-form-item>
          </el-form>

          <div class="analytics-filter-actions">
            <AppButton :loading="loading" @click="refreshAll">{{ t('analytics.refresh') }}</AppButton>
            <AppButton variant="secondary" @click="resetFilters">{{ t('common.reset') }}</AppButton>
          </div>
        </div>
      </section>

      <section class="analytics-section">
        <div class="analytics-section-head">
          <div>
            <p class="analytics-section-title">{{ t('analytics.chartSectionTitle') }}</p>
            <p class="analytics-section-desc">{{ t('analytics.chartSectionDesc') }}</p>
          </div>
        </div>

        <div class="analytics-chart-grid">
          <AppCard class="analytics-panel" :title="t('analytics.abnormalTrend')" surface="glass" content-class="p-4">
            <template #header>
              <span class="analytics-workspace-chip">{{ currentGranularityLabel }}</span>
            </template>
            <div class="analytics-panel-copy">
              <p class="analytics-panel-note">{{ t('analytics.attendanceRateNote') }}</p>
            </div>
            <div ref="attendanceTrendRef" class="chart-canvas"></div>
          </AppCard>

          <AppCard class="analytics-panel" :title="t('analytics.scoreTrend')" surface="glass" content-class="p-4">
            <template #header>
              <span class="analytics-workspace-chip">{{ currentRangeLabel }}</span>
            </template>
            <div class="analytics-panel-copy">
              <p class="analytics-panel-note">{{ t('analytics.chartSectionDesc') }}</p>
            </div>
            <div ref="scoreTrendRef" class="chart-canvas"></div>
          </AppCard>
        </div>
      </section>

      <section class="analytics-section">
        <div class="analytics-section-head">
          <div>
            <p class="analytics-section-title">{{ t('analytics.riskSectionTitle') }}</p>
            <p class="analytics-section-desc">{{ t('analytics.riskSectionDesc') }}</p>
          </div>
        </div>

        <AppCard class="analytics-panel analytics-risk-card" :title="t('analytics.riskList')" surface="base" content-class="p-4">
          <template #header>
            <div class="analytics-risk-switcher" role="tablist" :aria-label="t('analytics.riskList')">
              <button
                type="button"
                class="analytics-risk-switcher-button"
                :class="{ 'is-active': riskType === 'low_score' }"
                :aria-selected="riskType === 'low_score'"
                @click="handleRiskTypeChange('low_score')"
              >
                {{ t('analytics.riskLowScore') }}
              </button>
              <button
                type="button"
                class="analytics-risk-switcher-button"
                :class="{ 'is-active': riskType === 'abnormal_attendance' }"
                :aria-selected="riskType === 'abnormal_attendance'"
                @click="handleRiskTypeChange('abnormal_attendance')"
              >
                {{ t('analytics.riskAbnormalAttendance') }}
              </button>
              <button
                type="button"
                class="analytics-risk-switcher-button"
                :class="{ 'is-active': riskType === 'approval_overdue' }"
                :aria-selected="riskType === 'approval_overdue'"
                @click="handleRiskTypeChange('approval_overdue')"
              >
                {{ t('analytics.riskApprovalOverdue') }}
              </button>
            </div>
          </template>

          <div class="analytics-risk-table-wrap">
            <el-table
              :data="riskRecords"
              v-loading="riskLoading"
              stripe
              table-layout="fixed"
              :max-height="520"
              class="analytics-risk-table"
            >
              <el-table-column type="index" :label="t('analytics.index')" width="60" />
              <el-table-column v-if="!isStudent" prop="studentNo" :label="t('analytics.studentNo')" width="140" show-overflow-tooltip />
              <el-table-column v-if="!isStudent" prop="studentName" :label="t('analytics.name')" width="120" show-overflow-tooltip />
              <el-table-column v-if="!isStudent" prop="className" :label="t('analytics.className')" min-width="160" show-overflow-tooltip />
              <el-table-column v-if="!isStudent" prop="riskCount" :label="t('analytics.riskCount')" width="120" />
              <el-table-column v-if="!isStudent" :label="t('analytics.riskValue')" width="140">
                <template #default="{ row }">{{ formatNumber(row.riskValue) }}</template>
              </el-table-column>

              <template v-if="isStudent && riskType === 'low_score'">
                <el-table-column prop="courseName" :label="t('analytics.course')" min-width="220" show-overflow-tooltip />
                <el-table-column :label="t('analytics.lowestScore')" width="120">
                  <template #default="{ row }">{{ formatNumber(row.score) }}</template>
                </el-table-column>
                <el-table-column prop="riskCount" :label="t('analytics.lowScoreCount')" width="120" />
              </template>

              <template v-if="isStudent && riskType === 'abnormal_attendance'">
                <el-table-column :label="t('analytics.date')" width="140">
                  <template #default="{ row }">{{ row.attendanceDate || '-' }}</template>
                </el-table-column>
                <el-table-column prop="courseName" :label="t('analytics.course')" min-width="220" show-overflow-tooltip />
                <el-table-column :label="t('analytics.attendanceStatus')" width="120">
                  <template #default="{ row }">{{ formatAttendanceStatus(row.attendanceStatus) }}</template>
                </el-table-column>
              </template>

              <template v-if="isStudent && riskType === 'approval_overdue'">
                <el-table-column prop="leaveRequestId" :label="t('analytics.approvalId')" width="120" />
                <el-table-column :label="t('analytics.submitTime')" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">{{ formatDateTime(row.submitTime) }}</template>
                </el-table-column>
                <el-table-column :label="t('analytics.overdueStatus')" width="120">
                  <template #default="{ row }">
                    <el-tag :type="row.overdue ? 'danger' : 'info'" size="small">
                      {{ row.overdue ? t('analytics.overdue') : t('analytics.notOverdue') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="t('analytics.overdueHours')" width="120">
                  <template #default="{ row }">{{ formatNumber(row.overdueHours) }}</template>
                </el-table-column>
              </template>
            </el-table>
          </div>

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
      </section>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import { AlarmClockCheck, BadgeAlert, CalendarClock, UsersRound, Waypoints } from 'lucide-vue-next'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import store from '@/store'
import { init } from 'echarts/core'
import { LineChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { use } from 'echarts/core'
import { getAnalyticsOverview, getAttendanceTrend, getRiskStudents, getScoreTrend } from '@/api/analytics'
import { getTeacherList } from '@/api/teacher'
import { getClassList } from '@/api/clazz'
import { getSemesterOptions } from '@/api/semester'

use([LineChart, BarChart, GridComponent, TooltipComponent, LegendComponent, CanvasRenderer])

const route = useRoute()
const { t } = useI18n()
const loading = ref(false)
const riskLoading = ref(false)
const initialUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
const currentUserInfo = computed(() => {
  const storeUserInfo = store.state.userInfo || {}
  return Object.keys(storeUserInfo).length ? storeUserInfo : initialUserInfo
})
const userRole = currentUserInfo.value?.primaryRole || currentUserInfo.value?.role || ''
const isAdmin = userRole === 'SCHOOL_ADMIN' || userRole === 'COLLEGE_ADMIN'
const isTeacherRole = userRole === 'HOMEROOM_TEACHER' || userRole === 'COURSE_TEACHER'
const isStudent = userRole === 'STUDENT'
const pendingKpiLabel = isStudent ? t('analytics.myPendingApproval') : t('analytics.pendingApproval')
const lowScoreKpiLabel = isStudent ? t('analytics.lowScoreCourseCount') : t('analytics.lowScoreRiskPeople')
const roleLabelMap = {
  SCHOOL_ADMIN: 'roles.schoolAdmin',
  COLLEGE_ADMIN: 'roles.collegeAdmin',
  HOMEROOM_TEACHER: 'roles.homeroomTeacher',
  COURSE_TEACHER: 'roles.courseTeacher',
  STUDENT: 'roles.student'
}

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

const teacherOptionsLoading = ref(false)
const classOptionsLoading = ref(false)
const semesterOptionsLoading = ref(false)
const teacherOptions = ref([])
const classOptions = ref([])
const semesterOptions = ref([])

const riskType = ref('low_score')
const riskPage = ref(1)
const riskSize = ref(10)
const riskTotal = ref(0)
const riskRecords = ref([])

const attendanceTrendRef = ref(null)
const scoreTrendRef = ref(null)
let attendanceTrendChart = null
let scoreTrendChart = null
let themeObserver = null

const currentRoleLabel = computed(() => t(roleLabelMap[userRole] || 'common.user'))
const currentGranularityLabel = computed(() => {
  const map = {
    day: t('analytics.day'),
    week: t('analytics.week'),
    month: t('analytics.month')
  }
  return `${t('analytics.granularity')} · ${map[filters.granularity] || t('analytics.day')}`
})
const currentRangeLabel = computed(() => {
  const [start, end] = dateRange.value || []
  if (!start || !end) return t('analytics.dateRange')
  return `${start} - ${end}`
})
const statsTrackStyle = computed(() => ({
  '--analytics-stat-columns': String(Math.max(statCards.value.length, 1))
}))

const statCards = computed(() => {
  const items = []

  if (!isStudent) {
    items.push({
      icon: UsersRound,
      tone: 'slate',
      compact: true,
      label: t('analytics.studentScale'),
      value: formatNumber(overview.studentCount),
      note: t('analytics.studentScaleNote')
    })
  }

  items.push(
    {
      icon: CalendarClock,
      tone: 'amber',
      featured: isStudent,
      label: pendingKpiLabel,
      value: formatNumber(overview.pendingApprovalCount),
      note: t('analytics.pendingApprovalNote')
    },
    {
      icon: Waypoints,
      tone: 'emerald',
      label: t('analytics.attendanceRate'),
      value: formatPercent(overview.attendanceRate),
      note: t('analytics.attendanceRateNote'),
      delta: formatDelta(overview.attendanceRateChange),
      deltaValue: overview.attendanceRateChange
    },
    {
      icon: AlarmClockCheck,
      tone: 'sky',
      label: t('analytics.avgApprovalHours'),
      value: formatNumber(overview.approvalAvgHours),
      note: t('analytics.avgApprovalHoursNote'),
      delta: formatDelta(overview.approvalAvgHoursChange),
      deltaValue: -overview.approvalAvgHoursChange
    },
    {
      icon: BadgeAlert,
      tone: 'rose',
      label: lowScoreKpiLabel,
      value: formatNumber(overview.lowScoreRiskCount),
      note: t('analytics.lowScoreRiskNote'),
      delta: formatDelta(overview.lowScoreRiskChange),
      deltaValue: -overview.lowScoreRiskChange
    }
  )

  return items
})

const readCssVar = (name, fallback) => {
  if (typeof window === 'undefined') return fallback
  const value = getComputedStyle(document.documentElement).getPropertyValue(name).trim()
  return value || fallback
}

const getChartTheme = () => ({
  fontFamily: "'IBM Plex Sans','Noto Sans SC','PingFang SC','Microsoft YaHei',sans-serif",
  text: readCssVar('--text-primary', '#334155'),
  axis: readCssVar('--text-secondary', '#64748B'),
  grid: 'rgba(148, 163, 184, 0.22)',
  border: readCssVar('--panel-border', '#E2E8F0'),
  tooltipBg: readCssVar('--surface-popover', 'rgba(252, 253, 254, 0.96)'),
  primary: readCssVar('--accent-700', '#163454'),
  primarySoft: readCssVar('--accent-600', '#2A527A'),
  secondary: readCssVar('--accent-500', '#64748B'),
  canvasEdge: readCssVar('--surface-base', '#FFFFFF')
})

const buildAxisLabel = (theme) => ({
  color: theme.axis,
  fontSize: 11,
  fontFamily: theme.fontFamily
})

const buildTooltip = (theme) => ({
  backgroundColor: theme.tooltipBg,
  borderColor: theme.border,
  borderWidth: 1,
  textStyle: { color: theme.text, fontSize: 12, fontFamily: theme.fontFamily },
  extraCssText: 'box-shadow:0 8px 24px rgba(15,23,42,.12);border-radius:10px;'
})

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

function mapTeacherOption(item) {
  const teacherNo = item?.teacherNo || '-'
  const name = item?.name || '-'
  return {
    value: item.id,
    label: `${teacherNo} / ${name}`
  }
}

function mapClassOption(item) {
  const classCode = item?.classCode || '-'
  const className = item?.className || '-'
  return {
    value: item.id,
    label: `${classCode} / ${className}`
  }
}

function mapSemesterOption(item) {
  const value = item?.semesterCode || ''
  return {
    value,
    label: value
  }
}

async function ensureUserScopeInfo() {
  const info = currentUserInfo.value || {}
  const needsTeacherScope = isTeacherRole && !info.teacherId
  const needsCollegeScope = userRole === 'COLLEGE_ADMIN' && !info.collegeId
  if (!info.id || needsTeacherScope || needsCollegeScope) {
    await store.dispatch('getUserInfo').catch(() => null)
  }
}

async function loadTeacherOptions() {
  if (!isAdmin) {
    teacherOptions.value = []
    return
  }
  teacherOptionsLoading.value = true
  try {
    const params = { page: 1, size: 500 }
    if (currentUserInfo.value?.collegeId) {
      params.collegeId = currentUserInfo.value.collegeId
    }
    const res = await getTeacherList(params)
    teacherOptions.value = (res.data?.records || []).map(mapTeacherOption)
    if (filters.teacherId && !teacherOptions.value.some((item) => item.value === filters.teacherId)) {
      filters.teacherId = null
    }
  } finally {
    teacherOptionsLoading.value = false
  }
}

async function loadClassOptions() {
  if (isStudent) {
    classOptions.value = []
    return
  }
  classOptionsLoading.value = true
  try {
    const params = { page: 1, size: 500 }
    if (isAdmin && filters.teacherId) {
      params.teacherId = filters.teacherId
    } else if (isTeacherRole && currentUserInfo.value?.teacherId) {
      params.teacherId = currentUserInfo.value.teacherId
    } else if ((userRole === 'COLLEGE_ADMIN' || isTeacherRole) && currentUserInfo.value?.collegeId) {
      params.collegeId = currentUserInfo.value.collegeId
    }
    const res = await getClassList(params)
    classOptions.value = (res.data?.records || []).map(mapClassOption)
    if (filters.classId && !classOptions.value.some((item) => item.value === filters.classId)) {
      filters.classId = null
    }
  } finally {
    classOptionsLoading.value = false
  }
}

async function loadSemesterOptions() {
  semesterOptionsLoading.value = true
  try {
    const res = await getSemesterOptions()
    semesterOptions.value = (res.data || []).filter((item) => item?.semesterCode).map(mapSemesterOption)
    if (filters.semester && !semesterOptions.value.some((item) => item.value === filters.semester)) {
      filters.semester = ''
    }
  } finally {
    semesterOptionsLoading.value = false
  }
}

async function syncFilterOptions() {
  await Promise.all([
    loadTeacherOptions(),
    loadClassOptions(),
    loadSemesterOptions()
  ])
}

function formatNumber(value) {
  const number = Number(value || 0)
  return Number.isFinite(number) ? number.toFixed(2).replace(/\.00$/, '') : '0'
}

function formatAttendanceStatus(status) {
  if (status === 'ABSENT') return t('attendance.statusAbsent')
  if (status === 'LATE') return t('attendance.statusLate')
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
  } catch (_error) {
    riskRecords.value = []
    riskTotal.value = 0
    ElMessage.error(t('analytics.riskLoadFailed'))
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
  } catch (_error) {
    ElMessage.error(t('analytics.loadFailed'))
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
  syncFilterOptions().finally(() => refreshAll())
}

function handleRiskTypeChange(type) {
  riskType.value = type
  riskPage.value = 1
  fetchRiskStudents()
}

function renderAttendanceTrend(records) {
  if (!attendanceTrendChart) return
  const theme = getChartTheme()
  const baseTooltip = buildTooltip(theme)
  const baseAxisLabel = buildAxisLabel(theme)
  const labels = records.map((item) => item.periodLabel)
  const values = records.map((item) => Number(item.count || 0))
  attendanceTrendChart.setOption({
    color: [theme.primarySoft],
    tooltip: {
      ...baseTooltip,
      trigger: 'axis',
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(148,163,184,0.08)' } }
    },
    grid: { top: 18, left: 40, right: 16, bottom: 26 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: baseAxisLabel,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: theme.border } }
    },
    yAxis: {
      type: 'value',
      minInterval: 1,
      axisLabel: baseAxisLabel,
      axisTick: { show: false },
      axisLine: { show: false },
      splitLine: { lineStyle: { color: theme.grid } }
    },
    series: [
      {
        name: t('analytics.abnormalAttendance'),
        type: 'bar',
        barWidth: 16,
        data: values,
        itemStyle: {
          borderRadius: [3, 3, 0, 0],
          color: theme.primarySoft
        }
      }
    ]
  })
}

function renderScoreTrend(records) {
  if (!scoreTrendChart) return
  const theme = getChartTheme()
  const baseTooltip = buildTooltip(theme)
  const baseAxisLabel = buildAxisLabel(theme)
  const labels = records.map((item) => item.periodLabel)
  const avg = records.map((item) => Number(item.avgScore || 0))
  const pass = records.map((item) => Number(item.passRate || 0))
  const excellent = records.map((item) => Number(item.excellentRate || 0))
  scoreTrendChart.setOption({
    color: [theme.primary, theme.primarySoft, theme.secondary],
    tooltip: { ...baseTooltip, trigger: 'axis' },
    legend: {
      data: [t('analytics.avgScore'), t('analytics.passRate'), t('analytics.excellentRate')],
      top: 0,
      itemWidth: 8,
      itemHeight: 8,
      icon: 'circle',
      textStyle: { color: theme.axis, fontSize: 11, fontFamily: theme.fontFamily }
    },
    grid: { top: 34, left: 40, right: 36, bottom: 26 },
    xAxis: {
      type: 'category',
      data: labels,
      axisLabel: baseAxisLabel,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: theme.border } }
    },
    yAxis: [
      {
        type: 'value',
        name: t('analytics.scoreValue'),
        min: 0,
        max: 100,
        nameTextStyle: { color: theme.axis, fontSize: 11, fontFamily: theme.fontFamily, padding: [0, 0, 0, 8] },
        axisLabel: baseAxisLabel,
        axisTick: { show: false },
        axisLine: { show: false },
        splitLine: { lineStyle: { color: theme.grid } }
      },
      {
        type: 'value',
        name: t('analytics.percentage'),
        min: 0,
        max: 100,
        nameTextStyle: { color: theme.axis, fontSize: 11, fontFamily: theme.fontFamily, padding: [0, 8, 0, 0] },
        axisLabel: { ...baseAxisLabel, formatter: '{value}%' },
        axisTick: { show: false },
        axisLine: { show: false },
        splitLine: { show: false }
      }
    ],
    series: [
      {
        name: t('analytics.avgScore'),
        type: 'line',
        smooth: true,
        data: avg,
        symbolSize: 6,
        lineStyle: { color: theme.primary, width: 2.25 },
        itemStyle: { color: theme.primary, borderColor: theme.canvasEdge, borderWidth: 1 },
        areaStyle: { color: 'rgba(22, 52, 84, 0.08)' }
      },
      {
        name: t('analytics.passRate'),
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: pass,
        symbolSize: 5,
        lineStyle: { color: theme.primarySoft, width: 2 },
        itemStyle: { color: theme.primarySoft, borderColor: theme.canvasEdge, borderWidth: 1 }
      },
      {
        name: t('analytics.excellentRate'),
        type: 'line',
        smooth: true,
        yAxisIndex: 1,
        data: excellent,
        symbolSize: 5,
        lineStyle: { color: theme.secondary, width: 2 },
        itemStyle: { color: theme.secondary, borderColor: theme.canvasEdge, borderWidth: 1 }
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
  () => filters.teacherId,
  async (current, previous) => {
    if (!isAdmin || current === previous) {
      return
    }
    filters.classId = null
    await Promise.all([loadClassOptions(), loadSemesterOptions()])
  }
)

watch(
  () => filters.classId,
  async (current, previous) => {
    if (current === previous) {
      return
    }
    await loadSemesterOptions()
  }
)

watch(
  () => route.fullPath,
  async () => {
    applyRoutePreset()
    await syncFilterOptions()
    refreshAll()
  }
)

onMounted(async () => {
  await ensureUserScopeInfo()
  applyRoutePreset()
  await syncFilterOptions()
  initCharts()
  await refreshAll()
  window.addEventListener('resize', handleResize)
  themeObserver = new MutationObserver(() => {
    fetchAttendanceTrend()
    fetchScoreTrend()
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  themeObserver?.disconnect()
  themeObserver = null
  attendanceTrendChart?.dispose()
  scoreTrendChart?.dispose()
  attendanceTrendChart = null
  scoreTrendChart = null
})
</script>

<style scoped lang="scss">
.analytics-hero {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 18%, transparent), transparent 44%),
    linear-gradient(135deg, color-mix(in srgb, var(--surface-elevated) 88%, transparent), color-mix(in srgb, var(--surface-base) 92%, transparent));
}

.analytics-hero-copy {
  display: grid;
  gap: 10px;
  max-width: 820px;
}

.analytics-hero-badge {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.analytics-hero-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.15;
  color: var(--text-primary);
}

.analytics-hero-desc,
.analytics-hero-insight {
  margin: 0;
  font-size: 14px;
  line-height: 1.85;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
}

.analytics-hero-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 74%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  color: var(--text-primary);
  font-size: 13px;
}

.analytics-hero-pill-label {
  color: var(--text-secondary);
}

.analytics-stats {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
}

.analytics-stat {
  --stat-accent: var(--accent-600);
  --stat-accent-soft: color-mix(in srgb, var(--stat-accent) 14%, transparent);
  --stat-accent-border: color-mix(in srgb, var(--stat-accent) 26%, var(--panel-border));
  position: relative;
  display: grid;
  gap: 14px;
  min-height: 168px;
  padding: 18px 18px 16px;
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    radial-gradient(circle at top right, var(--stat-accent-soft), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 96%, transparent), color-mix(in srgb, var(--surface-elevated) 82%, transparent));
  box-shadow: inset 0 1px 0 color-mix(in srgb, var(--color-white) 18%, transparent);
}

.analytics-stat::after {
  content: '';
  position: absolute;
  inset: 0 auto 0 0;
  width: 3px;
  background: linear-gradient(180deg, color-mix(in srgb, var(--stat-accent) 82%, white 8%), color-mix(in srgb, var(--stat-accent) 34%, transparent));
  opacity: 0.92;
}

.analytics-stat.is-featured {
  border-color: var(--stat-accent-border);
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, var(--color-white) 18%, transparent),
    0 14px 34px color-mix(in srgb, var(--stat-accent) 14%, transparent);
}

.analytics-stat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.analytics-stat-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
  color: color-mix(in srgb, var(--stat-accent) 82%, white 10%);
  background: color-mix(in srgb, var(--stat-accent) 14%, var(--surface-elevated));
  border: 1px solid color-mix(in srgb, var(--stat-accent) 24%, transparent);
}

.analytics-stat-main {
  display: grid;
  gap: 10px;
}

.analytics-stat-label {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.analytics-stat-value {
  margin: 0;
  font-size: clamp(28px, 3.2vw, 38px);
  line-height: 0.96;
  font-weight: 700;
  color: var(--text-primary);
}

.analytics-stat-note {
  margin: auto 0 0;
  padding-top: 12px;
  border-top: 1px solid color-mix(in srgb, var(--panel-border) 74%, transparent);
  font-size: 12px;
  line-height: 1.7;
  color: color-mix(in srgb, var(--text-primary) 68%, var(--text-secondary));
}

.analytics-stat-trend {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid currentColor;
  background: color-mix(in srgb, currentColor 10%, transparent);
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.analytics-stat-trend.positive {
  color: var(--success);
}

.analytics-stat-trend.negative {
  color: var(--danger);
}

.analytics-stat-trend.neutral {
  color: var(--text-secondary);
}

.analytics-stat-slate {
  --stat-accent: #64748b;
}

.analytics-stat-amber {
  --stat-accent: #d97706;
}

.analytics-stat-emerald {
  --stat-accent: #059669;
}

.analytics-stat-sky {
  --stat-accent: #0284c7;
}

.analytics-stat-rose {
  --stat-accent: #e11d48;
}

.analytics-section {
  display: grid;
  gap: 14px;
}

.analytics-section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.analytics-section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.analytics-section-desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.75;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
}

.analytics-filter-panel,
.analytics-panel {
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.analytics-filter-panel {
  display: grid;
  gap: 14px;
  padding: 18px;
}

.analytics-workspace-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
  padding-bottom: 4px;
  border-bottom: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
}

.analytics-workspace-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.analytics-workspace-title {
  margin: 6px 0 0;
  font-size: 16px;
  line-height: 1.55;
  font-weight: 600;
  color: color-mix(in srgb, var(--text-primary) 90%, var(--text-secondary));
}

.analytics-workspace-meta {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.analytics-workspace-chip {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
  font-size: 12px;
  font-weight: 600;
  color: color-mix(in srgb, var(--text-primary) 76%, var(--text-secondary));
}

.analytics-filter-form {
  display: grid;
  gap: 10px 14px;
}

.analytics-filter-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.analytics-chart-grid {
  display: grid;
  gap: 14px;
}

.analytics-panel :deep(.app-panel-header) {
  align-items: flex-start;
  padding-bottom: 12px;
  border-bottom: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
}

.analytics-panel :deep(.app-panel-title) {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.analytics-panel-copy {
  margin-bottom: 14px;
}

.analytics-panel-note {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
  color: color-mix(in srgb, var(--text-primary) 68%, var(--text-secondary));
}

.analytics-risk-card :deep(.app-card__header-actions) {
  flex-wrap: wrap;
}

.analytics-risk-card {
  width: 100%;
  overflow: hidden;
}

.analytics-risk-card :deep(.app-panel-header) {
  align-items: flex-start;
}

.analytics-risk-switcher {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: max-content;
  max-width: 100%;
  padding: 6px;
  border-radius: 999px;
  overflow-x: auto;
  overflow-y: hidden;
  border: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, var(--color-white) 20%, transparent);
  white-space: nowrap;
}

.analytics-risk-switcher-button {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex: 0 0 auto;
  min-height: 38px;
  min-width: 96px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  background: transparent;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    box-shadow 0.2s ease,
    transform 0.2s ease;
}

.analytics-risk-switcher-button:hover {
  background: color-mix(in srgb, var(--surface-base) 88%, var(--accent-500) 12%);
  color: var(--text-primary);
}

.analytics-risk-switcher-button:focus-visible {
  outline: none;
  box-shadow:
    0 0 0 3px color-mix(in srgb, var(--accent-500) 22%, transparent);
}

.analytics-risk-switcher-button.is-active {
  background: color-mix(in srgb, var(--accent-500) 16%, var(--surface-elevated));
  color: var(--text-primary);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--accent-500) 16%, transparent);
  transform: translateY(-1px);
}

.analytics-risk-table-wrap {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  overflow-y: hidden;
}

.analytics-risk-table {
  width: 100%;
  min-width: 680px;
}

.analytics-risk-table :deep(.el-table__inner-wrapper) {
  border-radius: 16px;
}

.analytics-risk-table :deep(th),
.analytics-risk-table :deep(td) {
  white-space: nowrap;
}

.chart-canvas {
  height: 300px;
}

.pagination {
  margin-top: 10px;
  justify-content: flex-end;
}

@media (min-width: 768px) {
  .analytics-filter-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (min-width: 1280px) {
  .analytics-stats {
    grid-template-columns: repeat(var(--analytics-stat-columns), minmax(0, 1fr));
  }

  .analytics-chart-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .analytics-filter-form {
    grid-template-columns: repeat(5, minmax(0, 1fr));
  }
}
</style>
