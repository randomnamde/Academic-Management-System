<template>
  <div class="app-page dashboard-page space-y-3">
    <section class="grid gap-3 xl:grid-cols-12">
      <AppCard class="hero-card xl:col-span-8" surface="glass" content-class="p-4">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p class="text-[12px] uppercase tracking-[0.09em] text-slatex-500">{{ t('dashboard.overviewKicker') }}</p>
            <h2 class="mt-1 text-[30px] font-semibold tracking-tight text-primary-900">{{ dashboardTitle }}</h2>
            <p class="mt-2 text-[15px] text-slatex-600">{{ dashboardDesc }}</p>
          </div>
          <div class="hero-summary grid min-w-[280px] grid-cols-3 gap-2">
            <div class="kpi-chip">
              <span>{{ t('dashboard.pendingApproval') }}</span>
              <strong>{{ operationOverview.pendingApprovalCount }}</strong>
            </div>
            <div class="kpi-chip">
              <span>{{ t('dashboard.abnormalAttendance') }}</span>
              <strong>{{ operationOverview.abnormalTodayCount }}</strong>
            </div>
            <div class="kpi-chip">
              <span>{{ t('dashboard.lowScoreWarning') }}</span>
              <strong>{{ operationOverview.lowScoreWarningCount }}</strong>
            </div>
          </div>
        </div>
      </AppCard>

      <AppCard class="cluster-card xl:col-span-4" :title="t('dashboard.keyMetrics')" surface="elevated" content-class="p-3">
        <div class="grid gap-2 sm:grid-cols-2 xl:grid-cols-1">
          <button
            v-for="item in metricCards"
            :key="item.key"
            class="metric-item"
            :class="{ 'opacity-55 cursor-not-allowed': item.disabled }"
            :disabled="item.disabled"
            @click="handleMetricClick(item.key)"
          >
            <div class="text-[12px] text-slatex-500">{{ item.label }}</div>
            <div class="mt-1 text-[22px] font-semibold tracking-tight text-primary-900">{{ item.value }}</div>
          </button>
        </div>
      </AppCard>

      <AppCard v-if="!isStudent" class="xl:col-span-4" :title="t('dashboard.genderDistribution')" content-class="p-3">
        <div ref="genderChartRef" class="chart-canvas"></div>
      </AppCard>

      <AppCard v-if="!isStudent" class="xl:col-span-4" :title="t('dashboard.courseDistribution')" content-class="p-3">
        <div ref="courseChartRef" class="chart-canvas"></div>
      </AppCard>

      <AppCard v-else class="xl:col-span-8" :title="t('dashboard.personalLearningOverview')" content-class="p-3">
        <div class="grid grid-cols-2 gap-2 md:grid-cols-4">
          <div class="mini-card">
            <span>{{ t('dashboard.metricTeachers') }}</span>
            <strong>{{ statistics.teacherCount }}</strong>
          </div>
          <div class="mini-card">
            <span>{{ t('dashboard.metricMyCourses') }}</span>
            <strong>{{ statistics.courseCount }}</strong>
          </div>
          <div class="mini-card">
            <span>{{ t('dashboard.metricMyClasses') }}</span>
            <strong>{{ statistics.classCount }}</strong>
          </div>
          <div class="mini-card">
            <span>{{ t('dashboard.metricPendingLeave') }}</span>
            <strong>{{ operationOverview.pendingApprovalCount }}</strong>
          </div>
        </div>
      </AppCard>

      <AppCard class="xl:col-span-4" :title="t('dashboard.operations')" content-class="p-3">
        <div class="space-y-2">
          <button class="ops-item" @click="goAnalytics({ riskType: 'approval_overdue' })">
            <span>{{ t('dashboard.pendingApproval') }}</span>
            <strong>{{ operationOverview.pendingApprovalCount }}</strong>
          </button>
          <button class="ops-item" @click="goAnalytics({ riskType: 'abnormal_attendance' })">
            <span>{{ t('dashboard.abnormalAttendance') }}</span>
            <strong>{{ operationOverview.abnormalTodayCount }}</strong>
          </button>
          <button class="ops-item" @click="goAnalytics({ riskType: 'low_score' })">
            <span>{{ isStudent ? t('dashboard.myLowScoreWarning') : t('dashboard.lowScoreWarning') }}</span>
            <strong>{{ operationOverview.lowScoreWarningCount }}</strong>
          </button>
        </div>
      </AppCard>

      <AppCard class="xl:col-span-8" :title="isStudent ? t('dashboard.abnormalTrendStudent') : t('dashboard.abnormalTrend')" content-class="p-3">
        <div ref="trendChartRef" class="chart-canvas"></div>
      </AppCard>

      <AppCard class="xl:col-span-4" :title="t('dashboard.latestAnnouncements')" surface="base" content-class="p-3">
        <template #header>
          <AppButton variant="text" size="sm" @click="$router.push('/announcement')">{{ t('dashboard.viewMore') }}</AppButton>
        </template>
        <el-empty v-if="announcementLoading || !announcements.length" :description="announcementLoading ? t('common.loading') : t('dashboard.noAnnouncement')" />
        <div v-else class="space-y-1">
          <button
            v-for="item in announcements"
            :key="item.id"
            class="announcement-item w-full text-left"
            @click="openAnnouncementDetail(item)"
          >
            <div class="truncate">{{ item.title }}</div>
            <div class="mt-0.5 text-[11px] text-slatex-500">{{ item.createTime || '-' }}</div>
          </button>
        </div>
      </AppCard>

      <AppCard class="xl:col-span-12" :title="t('dashboard.todos')" surface="glass" content-class="p-3">
        <template #header>
          <AppButton variant="ghost" size="sm" :loading="todoLoading" @click="refreshTodos">{{ t('dashboard.refresh') }}</AppButton>
        </template>

        <div class="mb-3 grid gap-2 md:grid-cols-[1fr_auto]">
          <el-input
            v-model="todoDraft"
            clearable
            maxlength="60"
            show-word-limit
            :placeholder="t('dashboard.todoPlaceholder')"
            @keyup.enter="addCustomTodo"
          />
          <AppButton @click="addCustomTodo">{{ t('dashboard.add') }}</AppButton>
        </div>

        <el-skeleton :loading="todoLoading" animated :rows="4">
          <template #default>
            <el-empty v-if="!todoList.length" :description="t('dashboard.noTodos')" />
            <div v-else class="space-y-2">
              <div
                v-for="item in todoList"
                :key="item.id"
                class="todo-item flex flex-wrap items-center justify-between gap-2 rounded-sm border px-2 py-2"
                :class="item.completed ? 'opacity-60' : ''"
              >
                <div class="flex min-w-0 flex-1 items-start gap-2">
                  <el-checkbox :model-value="item.completed" @change="(val) => toggleTodo(item, val)" />
                  <div class="min-w-0 flex-1">
                    <div class="break-words text-[13px] text-slatex-800" :class="item.completed ? 'line-through' : ''">{{ item.title }}</div>
                    <div class="mt-1 flex items-center gap-2 text-[11px] text-slatex-500">
                      <AppBadge type="info">{{ item.sourceLabel }}</AppBadge>
                      <span>{{ item.timeText }}</span>
                    </div>
                  </div>
                </div>
                <div class="flex items-center gap-1">
                  <AppButton v-if="item.route" variant="ghost" size="sm" @click="openTodoRoute(item)">{{ t('dashboard.goTo') }}</AppButton>
                  <AppButton v-if="item.type === 'custom'" variant="danger" size="sm" @click="removeCustomTodo(item.id)">{{ t('dashboard.delete') }}</AppButton>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </AppCard>
    </section>

    <AppModal v-model="detailDialogVisible" :title="t('dashboard.noticeDetailTitle')" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('dashboard.title')" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dashboard.type')">{{ detail.type || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dashboard.targetRole')">{{ detail.targetRole || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dashboard.priority')">{{ detail.priority ?? '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('dashboard.publishTime')">{{ detail.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <div class="text-[13px] leading-6 text-slatex-700">{{ detail.content || t('dashboard.noContent') }}</div>
      <template #footer>
        <AppButton variant="secondary" @click="detailDialogVisible = false">{{ t('common.close') }}</AppButton>
      </template>
    </AppModal>
  </div>
</template>
<script setup>
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { use, init } from 'echarts/core'
import { PieChart, BarChart, LineChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'
import { getAttendanceList } from '@/api/attendance'
import { getLeaveRequestList, getPendingLeaveRequests } from '@/api/leaveRequest'
import { getDashboardOverview } from '@/api/dashboard'
import { getCourseArrangementOptions } from '@/api/courseArrangement'

use([PieChart, BarChart, LineChart, TooltipComponent, LegendComponent, GridComponent, CanvasRenderer])

const router = useRouter()
const store = useStore()
const { t } = useI18n()

const genderChartRef = ref(null)
const courseChartRef = ref(null)
const trendChartRef = ref(null)
let genderChartInstance = null
let courseChartInstance = null
let trendChartInstance = null
let themeObserver = null

const statistics = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  classCount: 0
})

const genderStatistics = ref({ male: 0, female: 0 })
const courseCategoryStatistics = ref({ required: 0, elective: 0, practical: 0 })

const operationOverview = ref({
  pendingApprovalCount: 0,
  abnormalTodayCount: 0,
  lowScoreWarningCount: 0,
  abnormalTrend: []
})

const announcements = ref([])
const announcementLoading = ref(false)
const detailDialogVisible = ref(false)
const detail = ref({
  id: null,
  title: '',
  content: '',
  type: '',
  targetRole: '',
  priority: 0,
  createTime: ''
})

const userInfo = computed(() => store.state.userInfo || {})
const isAuthenticated = computed(() => Boolean(store.state.token))
const userRole = computed(() => userInfo.value.primaryRole || userInfo.value.role || '')
const isStudent = computed(() => isAuthenticated.value && userRole.value === 'STUDENT')
const isTeacher = computed(() => ['HOMEROOM_TEACHER', 'COURSE_TEACHER'].includes(userRole.value))

const dashboardTitle = computed(() => (isStudent.value ? t('dashboard.titleStudent') : t('dashboard.titleAdmin')))
const dashboardDesc = computed(() =>
  isStudent.value
    ? t('dashboard.descStudent')
    : t('dashboard.descAdmin')
)

const metricCards = computed(() => [
  {
    key: 'student',
    label: isStudent.value ? t('dashboard.metricMyInfo') : t('dashboard.metricStudentCount'),
    value: isStudent.value ? 1 : statistics.value.studentCount,
    disabled: false
  },
  {
    key: 'teacher',
    label: isStudent.value ? t('dashboard.metricTeachers') : t('dashboard.metricTeacherCount'),
    value: statistics.value.teacherCount,
    disabled: isTeacher.value && !isStudent.value
  },
  {
    key: 'course',
    label: isStudent.value ? t('dashboard.metricMyCourses') : t('dashboard.metricCourseCount'),
    value: statistics.value.courseCount,
    disabled: false
  },
  {
    key: 'class',
    label: isStudent.value ? t('dashboard.metricMyClasses') : t('dashboard.metricClassCount'),
    value: statistics.value.classCount,
    disabled: false
  }
])

const todoLoading = ref(false)
const todoDraft = ref('')
const systemTodos = ref([])
const customTodos = ref([])
const completedTodoIds = ref([])

const buildTodoStorageKey = (type) => `dashboard:todo:${type}:${userInfo.value.id || 'guest'}`

const padZero = (value) => String(value).padStart(2, '0')
const formatDate = (date) => `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`

const formatDateTime = (value) => {
  if (!value) return t('dashboard.justNow')
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return `${formatDate(date)} ${padZero(date.getHours())}:${padZero(date.getMinutes())}`
}

const parseSafe = (value, fallback) => {
  try {
    const parsed = JSON.parse(value)
    return parsed ?? fallback
  } catch (_e) {
    return fallback
  }
}

const createTodoItem = ({ id, title, sourceLabel, route, createdAt, type = 'system' }) => ({
  id,
  title,
  sourceLabel,
  route,
  createdAt: createdAt || new Date().toISOString(),
  type
})

const todoList = computed(() => {
  const completedSet = new Set(completedTodoIds.value)
  return [...systemTodos.value, ...customTodos.value]
    .map((item) => {
      const ts = new Date(item.createdAt).getTime()
      return {
        ...item,
        timeText: formatDateTime(item.createdAt),
        completed: completedSet.has(item.id),
        sortTs: Number.isNaN(ts) ? 0 : ts
      }
    })
    .sort((a, b) => {
      if (a.completed !== b.completed) return a.completed ? 1 : -1
      return b.sortTs - a.sortTs
    })
})

const cleanupCompletedTodos = () => {
  const availableIds = new Set([...systemTodos.value, ...customTodos.value].map((item) => item.id))
  completedTodoIds.value = completedTodoIds.value.filter((id) => availableIds.has(id))
  localStorage.setItem(buildTodoStorageKey('completed'), JSON.stringify(completedTodoIds.value))
}

const loadTodoState = () => {
  const cachedCustom = parseSafe(localStorage.getItem(buildTodoStorageKey('custom')), [])
  const cachedCompleted = parseSafe(localStorage.getItem(buildTodoStorageKey('completed')), [])
  customTodos.value = Array.isArray(cachedCustom) ? cachedCustom : []
  completedTodoIds.value = Array.isArray(cachedCompleted) ? cachedCompleted : []
}

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

const saveCustomTodos = () => {
  localStorage.setItem(buildTodoStorageKey('custom'), JSON.stringify(customTodos.value))
}

const saveCompletedTodoIds = () => {
  localStorage.setItem(buildTodoStorageKey('completed'), JSON.stringify(completedTodoIds.value))
}

const addCustomTodo = () => {
  const title = todoDraft.value.trim()
  if (!title) {
    ElMessage.warning(t('dashboard.todoRequired'))
    return
  }
  customTodos.value.unshift(
    createTodoItem({
      id: `custom-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      title,
      sourceLabel: t('dashboard.todoSourcePersonal'),
      type: 'custom'
    })
  )
  todoDraft.value = ''
  saveCustomTodos()
}

const removeCustomTodo = (id) => {
  customTodos.value = customTodos.value.filter((item) => item.id !== id)
  completedTodoIds.value = completedTodoIds.value.filter((todoId) => todoId !== id)
  saveCustomTodos()
  saveCompletedTodoIds()
}

const toggleTodo = (item, checked) => {
  const idSet = new Set(completedTodoIds.value)
  if (checked) idSet.add(item.id)
  else idSet.delete(item.id)
  completedTodoIds.value = Array.from(idSet)
  saveCompletedTodoIds()
}

const openTodoRoute = (item) => {
  if (item.route) router.push(item.route)
}

const refreshTodos = async () => {
  todoLoading.value = true
  const tasks = []
  const role = userRole.value
  const today = formatDate(new Date())

  try {
    const announcementRes = await getAnnouncementList({ page: 1, size: 6, status: 1 })
    const announcementRecords = announcementRes.data?.records || []
    announcementRecords
      .filter((item) => !item.targetRole || item.targetRole === 'ALL' || item.targetRole === role)
      .slice(0, 3)
      .forEach((item) => {
        tasks.push(
          createTodoItem({
            id: `announcement-${item.id}`,
            title: t('dashboard.todoReadAnnouncement', { title: item.title }),
            sourceLabel: t('dashboard.todoSourceAnnouncement'),
            route: '/announcement',
            createdAt: item.createTime
          })
        )
      })
  } catch (_e) {
    // Ignore announcement todo source errors.
  }

  try {
    const [absentRes, lateRes] = await Promise.all([
      getAttendanceList({ page: 1, size: 1, attendanceDate: today, status: 'ABSENT' }),
      getAttendanceList({ page: 1, size: 1, attendanceDate: today, status: 'LATE' })
    ])

    const absentTotal = Number(absentRes.data?.total || 0)
    const lateTotal = Number(lateRes.data?.total || 0)

    if (absentTotal > 0) {
      tasks.push(
        createTodoItem({
          id: `attendance-absent-${today}-${absentTotal}`,
          title: t('dashboard.todoAbsent', { count: absentTotal }),
          sourceLabel: t('dashboard.todoSourceAttendance'),
          route: '/attendance'
        })
      )
    }

    if (lateTotal > 0) {
      tasks.push(
        createTodoItem({
          id: `attendance-late-${today}-${lateTotal}`,
          title: t('dashboard.todoLate', { count: lateTotal }),
          sourceLabel: t('dashboard.todoSourceAttendance'),
          route: '/attendance'
        })
      )
    }
  } catch (_e) {
    // Ignore attendance todo source errors.
  }

  try {
    if (['SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER'].includes(role)) {
      const leaveRes = await getPendingLeaveRequests()
      const pendingTotal = Array.isArray(leaveRes.data) ? leaveRes.data.length : 0
      if (pendingTotal > 0) {
        tasks.push(
          createTodoItem({
            id: `leave-pending-${pendingTotal}`,
            title: t('dashboard.todoLeaveApprove', { count: pendingTotal }),
            sourceLabel: t('dashboard.todoSourceApproval'),
            route: '/leave-request'
          })
        )
      }
    } else if (userInfo.value.id) {
      const myLeaveRes = await getLeaveRequestList({ page: 1, size: 1, studentId: userInfo.value.id, status: 'PENDING' })
      const myPendingTotal = Number(myLeaveRes.data?.total || 0)
      if (myPendingTotal > 0) {
        tasks.push(
          createTodoItem({
            id: `my-leave-pending-${myPendingTotal}`,
            title: t('dashboard.todoMyLeave', { count: myPendingTotal }),
            sourceLabel: t('dashboard.todoSourceLeave'),
            route: '/leave-request'
          })
        )
      }
    }
  } catch (_e) {
    // Ignore leave todo source errors.
  }

  systemTodos.value = tasks
  cleanupCompletedTodos()
  todoLoading.value = false
}

const renderGenderChart = () => {
  if (!genderChartInstance || isStudent.value) return
  const theme = getChartTheme()
  const baseTooltip = buildTooltip(theme)
  const male = Number(genderStatistics.value.male || 0)
  const female = Number(genderStatistics.value.female || 0)
  const total = male + female
  genderChartInstance.setOption({
    color: [theme.primary, theme.secondary],
    tooltip: { ...baseTooltip, trigger: 'item' },
    title: {
      text: String(total),
      subtext: t('dashboard.totalPeople'),
      left: 'center',
      top: '40%',
      textStyle: {
        color: theme.text,
        fontSize: 22,
        fontWeight: 700,
        fontFamily: theme.fontFamily
      },
      subtextStyle: {
        color: theme.axis,
        fontSize: 11,
        fontWeight: 500,
        fontFamily: theme.fontFamily
      }
    },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 8,
      itemHeight: 8,
      textStyle: { color: theme.axis, fontSize: 11, fontFamily: theme.fontFamily }
    },
    series: [
      {
        type: 'pie',
        radius: ['56%', '76%'],
        center: ['50%', '42%'],
        label: { show: false },
        labelLine: { show: false },
        itemStyle: { borderColor: theme.canvasEdge, borderWidth: 2 },
        data: [
          { value: male, name: t('dashboard.maleStudents') },
          { value: female, name: t('dashboard.femaleStudents') }
        ]
      }
    ]
  })
}

const renderCourseChart = () => {
  if (!courseChartInstance || isStudent.value) return
  const theme = getChartTheme()
  const baseTooltip = buildTooltip(theme)
  const baseAxisLabel = buildAxisLabel(theme)
  const values = [
    Number(courseCategoryStatistics.value.required || 0),
    Number(courseCategoryStatistics.value.elective || 0),
    Number(courseCategoryStatistics.value.practical || 0)
  ]
  courseChartInstance.setOption({
    color: [theme.primarySoft],
    tooltip: {
      ...baseTooltip,
      trigger: 'axis',
      axisPointer: { type: 'shadow', shadowStyle: { color: 'rgba(148,163,184,0.08)' } }
    },
    grid: { top: 18, left: 34, right: 14, bottom: 26 },
    xAxis: {
      type: 'category',
      data: [t('course.categoryRequired'), t('course.categoryElective'), t('course.categoryPractical')],
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
        type: 'bar',
        barWidth: 26,
        itemStyle: { color: theme.primarySoft, borderRadius: [3, 3, 0, 0] },
        data: values
      }
    ]
  })
}

const renderTrendChart = () => {
  if (!trendChartInstance) return
  const theme = getChartTheme()
  const baseTooltip = buildTooltip(theme)
  const baseAxisLabel = buildAxisLabel(theme)
  const trend = Array.isArray(operationOverview.value.abnormalTrend) ? operationOverview.value.abnormalTrend : []
  trendChartInstance.setOption({
    color: [theme.primary],
    tooltip: { ...baseTooltip, trigger: 'axis' },
    grid: { top: 18, left: 34, right: 14, bottom: 26 },
    xAxis: {
      type: 'category',
      data: trend.map((item) => item.date || ''),
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
        name: t('dashboard.abnormalAttendance'),
        type: 'line',
        smooth: true,
        showSymbol: true,
        symbolSize: 6,
        lineStyle: { color: theme.primary, width: 2.25 },
        itemStyle: { color: theme.primary, borderColor: theme.canvasEdge, borderWidth: 1 },
        areaStyle: { color: 'rgba(22, 52, 84, 0.08)' },
        data: trend.map((item) => Number(item.count || 0))
      }
    ]
  })
}

const handleChartResize = () => {
  genderChartInstance?.resize()
  courseChartInstance?.resize()
  trendChartInstance?.resize()
}

const initCharts = () => {
  nextTick(() => {
    if (genderChartRef.value && !genderChartInstance && !isStudent.value) genderChartInstance = init(genderChartRef.value)
    if (courseChartRef.value && !courseChartInstance && !isStudent.value) courseChartInstance = init(courseChartRef.value)
    if (trendChartRef.value && !trendChartInstance) trendChartInstance = init(trendChartRef.value)
    renderGenderChart()
    renderCourseChart()
    renderTrendChart()
    window.addEventListener('resize', handleChartResize)
  })
}

const fetchDashboardOverview = async () => {
  try {
    const [overviewRes, arrangementRes] = await Promise.all(
      isStudent.value
        ? [getDashboardOverview(), getCourseArrangementOptions({ status: 1 })]
        : [getDashboardOverview()]
    )

    const data = overviewRes.data || {}
    const nextStatistics = {
      studentCount: Number(data.studentCount || 0),
      teacherCount: Number(data.teacherCount || 0),
      courseCount: Number(data.courseCount || 0),
      classCount: Number(data.classCount || 0)
    }

    if (isStudent.value) {
      const arrangementList = Array.isArray(arrangementRes?.data) ? arrangementRes.data : []
      const teacherIds = new Set(arrangementList.map((item) => item.teacherId).filter(Boolean))
      const courseIds = new Set(arrangementList.map((item) => item.courseId).filter(Boolean))
      const classIds = new Set(arrangementList.map((item) => item.classId).filter(Boolean))
      nextStatistics.studentCount = 1
      nextStatistics.teacherCount = teacherIds.size || nextStatistics.teacherCount
      nextStatistics.courseCount = courseIds.size || nextStatistics.courseCount
      nextStatistics.classCount = classIds.size || nextStatistics.classCount
    }

    statistics.value = nextStatistics
    genderStatistics.value = {
      male: Number(data.genderStatistics?.male ?? data.genderStatistics?.MALE ?? 0),
      female: Number(data.genderStatistics?.female ?? data.genderStatistics?.FEMALE ?? 0)
    }
    courseCategoryStatistics.value = {
      required: Number(data.courseCategoryStatistics?.required ?? data.courseCategoryStatistics?.REQUIRED ?? 0),
      elective: Number(data.courseCategoryStatistics?.elective ?? data.courseCategoryStatistics?.ELECTIVE ?? 0),
      practical: Number(data.courseCategoryStatistics?.practical ?? data.courseCategoryStatistics?.PRACTICAL ?? 0)
    }
    operationOverview.value = {
      pendingApprovalCount: Number(data.pendingApprovalCount || 0),
      abnormalTodayCount: Number(data.abnormalTodayCount || 0),
      lowScoreWarningCount: Number(data.lowScoreWarningCount || 0),
      abnormalTrend: Array.isArray(data.abnormalTrend) ? data.abnormalTrend : []
    }

    renderGenderChart()
    renderCourseChart()
    renderTrendChart()
  } catch (_e) {
    ElMessage.error(t('dashboard.loadFailed'))
  }
}

const fetchLatestAnnouncements = async () => {
  announcementLoading.value = true
  try {
    const res = await getAnnouncementList({ page: 1, size: 6, status: 1 })
    announcements.value = res.data?.records || []
  } finally {
    announcementLoading.value = false
  }
}

const openAnnouncementDetail = async (item) => {
  try {
    const res = await getAnnouncementDetail(item.id)
    detail.value = res.data || {}
    detailDialogVisible.value = true
  } catch (_e) {
    ElMessage.error(t('dashboard.loadNoticeDetailFailed'))
  }
}

const handleMetricClick = (type) => {
  if (isStudent.value) {
    const map = { student: '/profile', teacher: '/teacher', course: '/course', class: '/class' }
    const route = map[type]
    if (route) router.push(route)
    return
  }

  if (isTeacher.value && type === 'teacher') {
    ElMessage.warning(t('dashboard.teacherScopeTip'))
    return
  }

  const map = { student: '/student', teacher: '/teacher', course: '/course', class: '/class' }
  const route = map[type]
  if (route) router.push(route)
}

const goAnalytics = (query = {}) => {
  router.push({ path: '/analytics', query })
}

onMounted(() => {
  loadTodoState()
  fetchDashboardOverview()
  fetchLatestAnnouncements()
  refreshTodos()
  initCharts()
  themeObserver = new MutationObserver(() => {
    renderGenderChart()
    renderCourseChart()
    renderTrendChart()
  })
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  themeObserver?.disconnect()
  themeObserver = null
  genderChartInstance?.dispose()
  courseChartInstance?.dispose()
  trendChartInstance?.dispose()
  genderChartInstance = null
  courseChartInstance = null
  trendChartInstance = null
})
</script>

<style scoped>
.hero-card {
  background:
    radial-gradient(circle at 5% -22%, color-mix(in srgb, var(--accent-500) 16%, transparent), transparent 46%),
    var(--panel-glass);
}

.kpi-chip {
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  border-radius: 12px;
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.kpi-chip span {
  font-size: 12px;
  color: var(--text-secondary);
}

.kpi-chip strong {
  font-size: 22px;
  line-height: 1;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.metric-item {
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  border-radius: 12px;
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  padding: 12px;
  text-align: left;
  transition: all 180ms ease;
}

.metric-item:hover {
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
  border-color: color-mix(in srgb, var(--accent-500) 28%, transparent);
  transform: translateY(-1px);
}

.chart-canvas {
  height: 260px;
}

.mini-card {
  border: 1px solid color-mix(in srgb, var(--panel-border) 80%, transparent);
  border-radius: 12px;
  padding: 10px;
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
}

.mini-card span {
  font-size: 12px;
  color: var(--text-secondary);
}

.mini-card strong {
  display: block;
  margin-top: 4px;
  font-size: 20px;
  line-height: 1;
  color: var(--text-primary);
}

.ops-item {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  border-radius: 12px;
  padding: 10px;
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 180ms ease;
}

.ops-item:hover {
  border-color: color-mix(in srgb, var(--accent-500) 28%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 92%, transparent);
}

.ops-item strong {
  color: var(--text-primary);
  font-size: 18px;
  font-variant-numeric: tabular-nums;
}

.announcement-item {
  border-radius: 10px;
  border: 1px solid transparent;
  padding: 8px;
  color: var(--text-secondary);
  transition: all 180ms ease;
}

.announcement-item:hover {
  border-color: color-mix(in srgb, var(--accent-500) 24%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
  color: var(--text-primary);
}

.todo-item {
  border-color: color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
}
</style>


