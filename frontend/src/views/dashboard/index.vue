<template>
  <div class="dashboard app-page">
    <section class="hero-panel">
      <div class="hero-content">
        <p class="hero-kicker">{{ dashboardCopy.heroKicker }}</p>
        <h2 class="hero-title">{{ dashboardCopy.heroTitle }}</h2>
        <p class="hero-description">
          {{ dashboardCopy.heroDescription }}
        </p>
        <div class="hero-chip-list">
          <div class="hero-chip">
            <span>待处理审批</span>
            <strong>{{ operationOverview.pendingApprovalCount }}</strong>
          </div>
          <div class="hero-chip">
            <span>今日异常考勤</span>
            <strong>{{ operationOverview.abnormalTodayCount }}</strong>
          </div>
          <div class="hero-chip">
            <span>低分预警</span>
            <strong>{{ operationOverview.lowScoreWarningCount }}</strong>
          </div>
        </div>
      </div>

      <div class="hero-metric-grid">
        <AppCard class="metric-card" content-class="metric-body" :class="{ disabled: isMetricDisabled('student') }" @click="handleMetricClick('student')">
          <div class="metric-icon student-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">{{ metricLabels.student }}</span>
            <strong v-if="!isStudent" class="metric-value">{{ statistics.studentCount }}</strong>
          </div>
        </AppCard>

        <AppCard class="metric-card" content-class="metric-body" :class="{ disabled: isMetricDisabled('teacher') }" @click="handleMetricClick('teacher')">
          <div class="metric-icon teacher-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">{{ metricLabels.teacher }}</span>
            <strong class="metric-value">{{ statistics.teacherCount }}</strong>
          </div>
        </AppCard>

        <AppCard class="metric-card" content-class="metric-body" :class="{ disabled: isMetricDisabled('course') }" @click="handleMetricClick('course')">
          <div class="metric-icon course-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">{{ metricLabels.course }}</span>
            <strong class="metric-value">{{ statistics.courseCount }}</strong>
          </div>
        </AppCard>

        <AppCard class="metric-card" content-class="metric-body" :class="{ disabled: isMetricDisabled('class') }" @click="handleMetricClick('class')">
          <div class="metric-icon class-icon">
            <el-icon><School /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">{{ metricLabels.class }}</span>
            <strong class="metric-value">{{ statistics.classCount }}</strong>
          </div>
        </AppCard>
      </div>
    </section>

    <section class="bento-grid">
      <AppCard v-if="!isStudent" class="panel-card panel-gender" title="学生性别分布" content-class="panel-body">
        <div ref="genderChartRef" class="chart chart-gender"></div>
      </AppCard>

      <AppCard v-if="!isStudent" class="panel-card panel-course" title="课程类型分布" content-class="panel-body">
        <div ref="courseChartRef" class="chart chart-course"></div>
      </AppCard>

      <AppCard v-else class="panel-card panel-personal" :title="dashboardCopy.personalPanelTitle" content-class="panel-body">
        <div class="personal-grid">
          <div class="personal-item">
            <span>任课教师</span>
            <strong>{{ statistics.teacherCount }}</strong>
          </div>
          <div class="personal-item">
            <span>我的课程</span>
            <strong>{{ statistics.courseCount }}</strong>
          </div>
          <div class="personal-item">
            <span>我的班级</span>
            <strong>{{ statistics.classCount }}</strong>
          </div>
          <div class="personal-item">
            <span>待处理请假</span>
            <strong>{{ operationOverview.pendingApprovalCount }}</strong>
          </div>
        </div>
      </AppCard>

      <AppCard class="panel-card panel-ops" :title="dashboardCopy.opsPanelTitle" content-class="panel-body">
        <template #header>
          <AppButton variant="ghost" size="sm" @click="goAnalytics()">进入分析中心</AppButton>
        </template>
        <div class="ops-list">
          <div class="ops-item clickable" @click="goAnalytics({ riskType: 'approval_overdue' })">
            <span class="ops-label">待处理审批</span>
            <strong class="ops-value">{{ operationOverview.pendingApprovalCount }}</strong>
          </div>
          <div class="ops-item clickable" @click="goAnalytics({ riskType: 'abnormal_attendance' })">
            <span class="ops-label">今日异常考勤</span>
            <strong class="ops-value">{{ operationOverview.abnormalTodayCount }}</strong>
          </div>
          <div class="ops-item clickable" @click="goAnalytics({ riskType: 'low_score' })">
            <span class="ops-label">{{ isStudent ? '我的低分预警' : '低分预警人数' }}</span>
            <strong class="ops-value">{{ operationOverview.lowScoreWarningCount }}</strong>
          </div>
        </div>
      </AppCard>

      <AppCard class="panel-card panel-trend" :title="isStudent ? '近7日我的异常考勤趋势' : '近7日异常考勤趋势'" content-class="panel-body">
        <div ref="trendChartRef" class="chart chart-trend"></div>
      </AppCard>

      <AppCard class="panel-card panel-announcement" title="最新公告" content-class="panel-body">
        <template #header>
          <AppButton variant="ghost" size="sm" @click="$router.push('/announcement')">查看更多</AppButton>
        </template>
        <el-empty
          v-if="announcementLoading || !announcements.length"
          :description="announcementLoading ? '加载中...' : '暂无公告'"
        />
        <el-timeline v-else class="announcement-timeline">
          <el-timeline-item
            v-for="(item, index) in announcements"
            :key="item.id || index"
            :timestamp="item.createTime"
            :type="index === 0 ? 'primary' : ''"
            class="announcement-item"
            :style="{ '--announcement-delay': `${index * 60}ms` }"
          >
            <button class="announcement-title" @click="openAnnouncementDetail(item)">
              {{ item.title }}
            </button>
          </el-timeline-item>
        </el-timeline>
      </AppCard>

      <AppCard class="panel-card panel-todo" title="待办事项" content-class="panel-body">
        <template #header>
          <AppButton variant="ghost" size="sm" :loading="todoLoading" @click="refreshTodos">刷新</AppButton>
        </template>
        <div class="todo-create">
          <el-input
            v-model="todoDraft"
            clearable
            maxlength="60"
            show-word-limit
            placeholder="添加个人待办，例如：准备班会材料"
            @keyup.enter="addCustomTodo"
          >
            <template #append>
              <AppButton size="sm" @click="addCustomTodo">添加</AppButton>
            </template>
          </el-input>
        </div>
        <el-skeleton :loading="todoLoading" animated :rows="4">
          <template #default>
            <el-empty v-if="!todoList.length" description="暂无待办事项" />
            <transition-group v-else name="todo-fade" tag="div" class="todo-list">
              <div
                v-for="(item, index) in todoList"
                :key="item.id"
                class="todo-item"
                :class="{ completed: item.completed }"
                :style="{ '--todo-delay': `${index * 48}ms` }"
              >
                <div class="todo-main">
                  <el-checkbox :model-value="item.completed" @change="(val) => toggleTodo(item, val)" />
                  <div class="todo-content">
                    <div class="todo-title">{{ item.title }}</div>
                    <div class="todo-meta">
                      <el-tag size="small" effect="plain">{{ item.sourceLabel }}</el-tag>
                      <span class="todo-time">{{ item.timeText }}</span>
                    </div>
                  </div>
                </div>
                <div class="todo-actions">
                  <AppButton v-if="item.route" variant="ghost" size="sm" @click="openTodoRoute(item)">前往</AppButton>
                  <AppButton v-if="item.type === 'custom'" variant="danger" size="sm" @click="removeCustomTodo(item.id)">删除</AppButton>
                </div>
              </div>
            </transition-group>
          </template>
        </el-skeleton>
      </AppCard>
    </section>

    <AppModal v-model="detailDialogVisible" title="公告详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ detail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ detail.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标角色">{{ detail.targetRole || '-' }}</el-descriptions-item>
        <el-descriptions-item label="优先级">{{ detail.priority ?? '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ detail.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <div class="detail-content">{{ detail.content || '暂无内容' }}</div>
      <template #footer>
        <AppButton variant="secondary" @click="detailDialogVisible = false">关闭</AppButton>
      </template>
    </AppModal>

    <AppModal v-model="statDialogVisible" :title="currentStatTitle" width="520px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="当前统计值">{{ currentStatValue }}</el-descriptions-item>
        <el-descriptions-item label="可见范围">{{ statScopeText }}</el-descriptions-item>
      </el-descriptions>
      <el-alert
        style="margin-top: 14px"
        type="info"
        :closable="false"
        title="详细记录请在对应业务模块中查看。"
      />
      <template #footer>
        <AppButton variant="secondary" @click="statDialogVisible = false">关闭</AppButton>
        <AppButton v-if="canOpenStatRoute" @click="goToModule">进入对应模块</AppButton>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import AppCard from '@/components/ui/AppCard.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { use, init, graphic } from 'echarts/core'
import { PieChart, BarChart, PictorialBarChart, LineChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { User, UserFilled, Reading, School } from '@element-plus/icons-vue'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'
import { getAttendanceList } from '@/api/attendance'
import { getLeaveRequestList, getPendingLeaveRequests } from '@/api/leaveRequest'
import { getDashboardOverview } from '@/api/dashboard'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { canRoute } from '@/permission/ability'

use([PieChart, BarChart, PictorialBarChart, LineChart, TooltipComponent, LegendComponent, GridComponent, GraphicComponent, CanvasRenderer])

const router = useRouter()
const store = useStore()
const genderChartRef = ref(null)
const courseChartRef = ref(null)
const trendChartRef = ref(null)
let genderChartInstance = null
let courseChartInstance = null
let trendChartInstance = null

const genderStatistics = ref({
  male: 0,
  female: 0
})

const courseCategoryStatistics = ref({
  required: 0,
  elective: 0,
  practical: 0
})

const statistics = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  classCount: 0
})

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

const statDialogVisible = ref(false)
const statType = ref('student')

const statMeta = {
  student: { title: '学生详情', route: '/student', routeName: 'Student', metricKey: 'studentCount' },
  teacher: { title: '教师详情', route: '/teacher', routeName: 'Teacher', metricKey: 'teacherCount' },
  course: { title: '课程详情', route: '/course', routeName: 'Course', metricKey: 'courseCount' },
  class: { title: '班级详情', route: '/class', routeName: 'Class', metricKey: 'classCount' }
}

const currentStatTitle = computed(() => statMeta[statType.value]?.title || '详情')
const userInfo = computed(() => store.state.userInfo || {})
const userRole = computed(() => userInfo.value.role || 'STUDENT')
const isStudent = computed(() => userRole.value === 'STUDENT')
const isTeacher = computed(() => userRole.value === 'TEACHER')
const userPermissions = computed(() => userInfo.value?.permissions || [])

const studentDashboardCopy = {
  heroKicker: 'Personal Snapshot',
  heroTitle: '个人情况总览',
  heroDescription: '在同一视图查看我的课程、考勤、请假与成绩预警，快速掌握近期学习状态。',
  personalPanelTitle: '个人学习情况',
  opsPanelTitle: '个人情况概览',
  studentMetricWarning: '学生仅可查看个人情况总览数据'
}

const defaultDashboardCopy = {
  heroKicker: 'Campus Operations',
  heroTitle: '教学运营总览',
  heroDescription: '在同一视图追踪学生、课程、考勤与审批数据，快速定位今日重点任务。',
  personalPanelTitle: '我的学习概览',
  opsPanelTitle: '运营概览',
  studentMetricWarning: '学生仅可查看个人总览数据'
}

const dashboardCopy = computed(() => (isStudent.value ? studentDashboardCopy : defaultDashboardCopy))

const currentStatValue = computed(() => {
  const metricKey = statMeta[statType.value]?.metricKey
  if (!metricKey) return 0
  return Number(statistics.value?.[metricKey] || 0)
})

const statScopeText = computed(() => {
  if (userRole.value === 'ADMIN') return '全校数据'
  if (userRole.value === 'TEACHER') return '仅本人授课范围'
  return '仅个人范围'
})

const canOpenStatRoute = computed(() => {
  const routeName = statMeta[statType.value]?.routeName
  if (!routeName) return false
  return canRoute(userRole.value, routeName, userPermissions.value)
})

const isMetricDisabled = (type) => isTeacher.value && type === 'teacher'

const studentMetricRouteMap = {
  student: '/profile',
  teacher: '/teacher',
  course: '/course',
  class: '/class'
}

const metricLabels = computed(() => {
  if (isStudent.value) {
    return {
      student: '我的信息',
      teacher: '任课教师数',
      course: '我的课程数',
      class: '我的班级数'
    }
  }
  return {
    student: '学生总数',
    teacher: '教师总数',
    course: '课程总数',
    class: '班级总数'
  }
})

const todoLoading = ref(false)
const todoDraft = ref('')
const systemTodos = ref([])
const customTodos = ref([])
const completedTodoIds = ref([])

const buildTodoStorageKey = (type) => `dashboard:todo:${type}:${userInfo.value.id || 'guest'}`

const padZero = (value) => String(value).padStart(2, '0')

const formatDate = (date) =>
  `${date.getFullYear()}-${padZero(date.getMonth() + 1)}-${padZero(date.getDate())}`

const formatDateTime = (value) => {
  if (!value) return '刚刚'
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

const loadTodoState = () => {
  const cachedCustom = parseSafe(localStorage.getItem(buildTodoStorageKey('custom')), [])
  const cachedCompleted = parseSafe(localStorage.getItem(buildTodoStorageKey('completed')), [])
  customTodos.value = Array.isArray(cachedCustom) ? cachedCustom : []
  completedTodoIds.value = Array.isArray(cachedCompleted) ? cachedCompleted : []
}

const saveCustomTodos = () => {
  localStorage.setItem(buildTodoStorageKey('custom'), JSON.stringify(customTodos.value))
}

const saveCompletedTodoIds = () => {
  localStorage.setItem(buildTodoStorageKey('completed'), JSON.stringify(completedTodoIds.value))
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
      const timeText = formatDateTime(item.createdAt)
      const timestamp = new Date(item.createdAt).getTime()
      return {
        ...item,
        timeText,
        completed: completedSet.has(item.id),
        sortTs: Number.isNaN(timestamp) ? 0 : timestamp
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
  saveCompletedTodoIds()
}

const addCustomTodo = () => {
  const title = todoDraft.value.trim()
  if (!title) {
    ElMessage.warning('请输入待办内容')
    return
  }

  customTodos.value.unshift(
    createTodoItem({
      id: `custom-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`,
      title,
      sourceLabel: '个人',
      createdAt: new Date().toISOString(),
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
  if (checked) {
    idSet.add(item.id)
  } else {
    idSet.delete(item.id)
  }
  completedTodoIds.value = Array.from(idSet)
  saveCompletedTodoIds()
}

const openTodoRoute = (item) => {
  if (item.route) {
    router.push(item.route)
  }
}

const refreshTodos = async () => {
  todoLoading.value = true
  const tasks = []
  const role = userRole.value
  const today = formatDate(new Date())
  try {
    const announcementRes = await getAnnouncementList({
      page: 1,
      size: 6,
      status: 1
    })
    const announcementRecords = announcementRes.data?.records || []
    const matchedAnnouncements = announcementRecords
      .filter((item) => !item.targetRole || item.targetRole === 'ALL' || item.targetRole === role)
      .sort((a, b) => {
        const priorityDiff = Number(b.priority || 0) - Number(a.priority || 0)
        if (priorityDiff !== 0) return priorityDiff
        return Number(b.isTop || 0) - Number(a.isTop || 0)
      })

    matchedAnnouncements.slice(0, 3).forEach((item) => {
      tasks.push(
        createTodoItem({
          id: `announcement-${item.id}`,
          title: `阅读公告：${item.title}`,
          sourceLabel: '公告',
          route: '/announcement',
          createdAt: item.createTime
        })
      )
    })
  } catch (_e) {}

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
          title: `处理今日缺勤记录（${absentTotal} 条）`,
          sourceLabel: '考勤',
          route: '/attendance',
          createdAt: new Date().toISOString()
        })
      )
    }

    if (lateTotal > 0) {
      tasks.push(
        createTodoItem({
          id: `attendance-late-${today}-${lateTotal}`,
          title: `处理今日迟到记录（${lateTotal} 条）`,
          sourceLabel: '考勤',
          route: '/attendance',
          createdAt: new Date().toISOString()
        })
      )
    }
  } catch (_e) {}

  try {
    if (role === 'ADMIN' || role === 'TEACHER') {
      const leaveRes = await getPendingLeaveRequests()
      const pendingTotal = Array.isArray(leaveRes.data) ? leaveRes.data.length : 0
      if (pendingTotal > 0) {
        tasks.push(
          createTodoItem({
            id: `leave-pending-${pendingTotal}`,
            title: `审批请假申请（${pendingTotal} 条待处理）`,
            sourceLabel: '审批',
            route: '/leave-request',
            createdAt: new Date().toISOString()
          })
        )
      }
    } else if (userInfo.value.id) {
      const myLeaveRes = await getLeaveRequestList({
        page: 1,
        size: 1,
        studentId: userInfo.value.id,
        status: 'PENDING'
      })
      const myPendingTotal = Number(myLeaveRes.data?.total || 0)
      if (myPendingTotal > 0) {
        tasks.push(
          createTodoItem({
            id: `my-leave-pending-${myPendingTotal}`,
            title: `跟进我的请假申请（${myPendingTotal} 条审核中）`,
            sourceLabel: '请假',
            route: '/leave-request',
            createdAt: new Date().toISOString()
          })
        )
      }
    }
  } catch (_e) {}

  systemTodos.value = tasks
  cleanupCompletedTodos()
  todoLoading.value = false
}

const renderGenderChart = () => {
  if (!genderChartInstance) return
  const maleValue = Number(genderStatistics.value.male || 0)
  const femaleValue = Number(genderStatistics.value.female || 0)
  const total = maleValue + femaleValue
  genderChartInstance.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '4%', left: 'center' },
    series: [
      {
        type: 'pie',
        radius: ['74%', '84%'],
        silent: true,
        z: 0,
        label: { show: false },
        data: [
          {
            value: 1,
            itemStyle: {
              color: new graphic.RadialGradient(0.5, 0.45, 1, [
                { offset: 0, color: 'rgba(229, 218, 255, 0.09)' },
                { offset: 1, color: 'rgba(154, 128, 245, 0.012)' }
              ]),
              shadowBlur: 4,
              shadowColor: 'rgba(18, 98, 102, 0.045)'
            }
          }
        ]
      },
      {
        type: 'pie',
        radius: ['42%', '72%'],
        avoidLabelOverlap: false,
        startAngle: 210,
        itemStyle: {
          borderRadius: 12,
          borderColor: '#fff',
          borderWidth: 2,
          shadowBlur: 8,
          shadowOffsetY: 4,
          shadowColor: 'rgba(21, 89, 99, 0.12)'
        },
        label: { show: false },
        labelLine: { show: false },
        emphasis: {
          scale: true,
          scaleSize: 8,
          label: {
            show: true,
            formatter: ({ name, value }) => {
              const percent = total ? ((value / total) * 100).toFixed(1) : '0.0'
              return `${name}\n${percent}%`
            },
            fontSize: 18,
            fontWeight: 'bold'
          }
        },
        data: [
          {
            value: maleValue,
            name: '男生',
            itemStyle: {
              color: new graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#c2e4e1' },
                { offset: 1, color: '#1d8a84' }
              ])
            }
          },
          {
            value: femaleValue,
            name: '女生',
            itemStyle: {
              color: new graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#d8ece9' },
                { offset: 1, color: '#3a9a95' }
              ])
            }
          }
        ]
      },
      {
        type: 'pie',
        radius: ['0%', '26%'],
        silent: true,
        z: 0,
        label: { show: false },
        data: [
          {
            value: 1,
            itemStyle: {
              color: new graphic.RadialGradient(0.5, 0.45, 0.9, [
                { offset: 0, color: 'rgba(255, 255, 255, 0.8)' },
                { offset: 1, color: 'rgba(110, 181, 173, 0.22)' }
              ]),
              shadowBlur: 6,
              shadowColor: 'rgba(17, 89, 84, 0.1)'
            }
          }
        ]
      }
    ],
    graphic: [
      {
        type: 'group',
        left: 'center',
        top: 'middle',
        silent: true,
        z: 0,
        children: [
          {
            type: 'ellipse',
            shape: { cx: 0, cy: 98, rx: 124, ry: 22 },
            style: { fill: 'rgba(15, 88, 98, 0.032)' }
          },
          {
            type: 'ellipse',
            shape: { cx: 0, cy: 98, rx: 94, ry: 15 },
            style: { fill: 'rgba(250, 246, 255, 0.06)' }
          }
        ]
      }
    ]
  })
}

const renderCourseChart = () => {
  if (!courseChartInstance) return
  const categoryValues = [
    Number(courseCategoryStatistics.value.required || 0),
    Number(courseCategoryStatistics.value.elective || 0),
    Number(courseCategoryStatistics.value.practical || 0)
  ]
  courseChartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { top: 26, left: 46, right: 24, bottom: 34 },
    xAxis: {
      type: 'category',
      data: ['必修课', '选修课', '实践课'],
      axisTick: { show: false },
      axisLine: { lineStyle: { color: 'rgba(71, 118, 136, 0.45)' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(94, 143, 150, 0.16)' } }
    },
    series: [
      {
        type: 'pictorialBar',
        data: categoryValues,
        symbol: 'diamond',
        symbolSize: [42, 14],
        symbolOffset: [0, 7],
        z: 1,
        itemStyle: {
          color: 'rgba(46, 132, 139, 0.16)'
        }
      },
      {
        data: categoryValues,
        type: 'bar',
        barWidth: 42,
        z: 2,
        showBackground: true,
        backgroundStyle: {
          color: 'rgba(59, 142, 150, 0.08)',
          borderRadius: [8, 8, 0, 0]
        },
        itemStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#cde9e6' },
            { offset: 0.5, color: '#68b0ac' },
            { offset: 1, color: '#1a7f79' }
          ]),
          borderRadius: [8, 8, 0, 0],
          shadowBlur: 6,
          shadowColor: 'rgba(24, 95, 102, 0.16)',
          shadowOffsetY: 3
        }
      },
      {
        type: 'pictorialBar',
        symbolPosition: 'end',
        data: categoryValues,
        symbol: 'diamond',
        symbolSize: [42, 14],
        symbolOffset: [0, -7],
        z: 3,
        itemStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#ecf7f6' },
            { offset: 1, color: '#58aaa4' }
          ])
        }
      }
    ],
    graphic: [
      {
        type: 'polygon',
        left: 'center',
        top: '72%',
        silent: true,
        z: 0,
        shape: {
          points: [
            [-148, 56],
            [148, 56],
            [108, 92],
            [-108, 92]
          ]
        },
        style: {
          fill: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(133, 199, 194, 0.04)' },
            { offset: 1, color: 'rgba(26, 102, 108, 0.01)' }
          ])
        }
      }
    ]
  })
}

const renderTrendChart = () => {
  if (!trendChartInstance) return
  const trend = Array.isArray(operationOverview.value.abnormalTrend)
    ? operationOverview.value.abnormalTrend
    : []
  const xData = trend.map((item) => item.date || '')
  const yData = trend.map((item) => Number(item.count || 0))

  trendChartInstance.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: 26, left: 46, right: 24, bottom: 34 },
    xAxis: {
      type: 'category',
      data: xData,
      axisTick: { show: false },
      axisLine: { lineStyle: { color: 'rgba(71, 118, 136, 0.45)' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(94, 143, 150, 0.16)' } },
      minInterval: 1
    },
    series: [
      {
        name: '异常考勤',
        type: 'line',
        smooth: true,
        showSymbol: true,
        data: yData,
        lineStyle: { width: 3, color: '#177c77' },
        itemStyle: { color: '#177c77' },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(23, 124, 119, 0.24)' },
            { offset: 1, color: 'rgba(23, 124, 119, 0.02)' }
          ])
        }
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
    if (genderChartRef.value && !genderChartInstance) {
      genderChartInstance = init(genderChartRef.value)
    }
    if (courseChartRef.value && !courseChartInstance) {
      courseChartInstance = init(courseChartRef.value)
    }
    if (trendChartRef.value && !trendChartInstance) {
      trendChartInstance = init(trendChartRef.value)
    }

    renderGenderChart()
    renderCourseChart()
    renderTrendChart()

    window.removeEventListener('resize', handleChartResize)
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
    ElMessage.error('获取首页数据失败')
  }
}

const fetchLatestAnnouncements = async () => {
  announcementLoading.value = true
  try {
    const res = await getAnnouncementList({
      page: 1,
      size: 8,
      status: 1
    })
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
    ElMessage.error('获取公告详情失败')
  }
}

const handleMetricClick = (type) => {
  if (isStudent.value) {
    const route = studentMetricRouteMap[type]
    if (route) {
      router.push(route)
      return
    }
    ElMessage.warning(dashboardCopy.value.studentMetricWarning)
    return
  }
  if (isTeacher.value && type === 'teacher') {
    ElMessage.warning('教师仅可查看本人授课范围数据')
    return
  }
  statType.value = type
  statDialogVisible.value = true
}

const goToModule = () => {
  const route = statMeta[statType.value]?.route
  statDialogVisible.value = false
  if (route) {
    router.push(route)
  }
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
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleChartResize)
  genderChartInstance?.dispose()
  courseChartInstance?.dispose()
  trendChartInstance?.dispose()
  genderChartInstance = null
  courseChartInstance = null
  trendChartInstance = null
})
</script>

<style scoped lang="scss">
.dashboard {
  width: 100%;
  min-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 2px 0 14px;
  box-sizing: border-box;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(340px, 1fr) minmax(420px, 1.1fr);
  gap: 16px;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(20, 84, 101, 0.2);
  background:
    radial-gradient(circle at 8% 12%, rgba(255, 255, 255, 0.46), transparent 38%),
    radial-gradient(circle at 92% 4%, rgba(172, 221, 217, 0.24), transparent 36%),
    linear-gradient(140deg, rgba(255, 255, 255, 0.9), rgba(239, 248, 247, 0.86));
  box-shadow: 0 18px 34px rgba(22, 66, 79, 0.12);
}

.hero-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #4a7d8c;
}

.hero-title {
  margin: 10px 0 12px;
  color: #173749;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 700;
}

.hero-description {
  margin: 0;
  max-width: 580px;
  color: #446173;
  line-height: 1.7;
  font-size: 14px;
}

.hero-chip-list {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.hero-chip {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(24, 97, 110, 0.2);
  background: rgba(255, 255, 255, 0.74);
}

.hero-chip span {
  color: #4c6878;
  font-size: 13px;
}

.hero-chip strong {
  color: #173a4c;
  font-size: 24px;
  font-weight: 700;
}

.hero-metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  cursor: pointer;
  border-radius: 16px;
  border: 1px solid rgba(24, 97, 110, 0.2);
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.94), rgba(238, 248, 247, 0.84));
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 16px 30px rgba(20, 74, 87, 0.18);
}

.metric-card.disabled {
  cursor: default;
}

.metric-card.disabled:hover {
  transform: none;
  box-shadow: 0 10px 22px rgba(20, 66, 81, 0.12);
}

:deep(.metric-card .metric-body) {
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

.student-icon {
  background: linear-gradient(135deg, #1f958e, #116c68);
}

.teacher-icon {
  background: linear-gradient(135deg, #2f8e9e, #2a6f93);
}

.course-icon {
  background: linear-gradient(135deg, #3da293, #257f71);
}

.class-icon {
  background: linear-gradient(135deg, #4b9cb4, #2f6f95);
}

.metric-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.metric-label {
  color: #4e6574;
  font-size: 13px;
}

.metric-value {
  margin-top: 4px;
  color: #163548;
  font-size: 28px;
  line-height: 1;
}

.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 16px;
}

.panel-card {
  border-radius: 18px;
  border: 1px solid rgba(21, 88, 102, 0.17);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 248, 0.84));
  box-shadow: 0 10px 24px rgba(20, 68, 80, 0.1);
}

.panel-gender,
.panel-course,
.panel-ops {
  grid-column: span 4;
}

.panel-personal {
  grid-column: span 8;
}

.panel-trend {
  grid-column: span 8;
}

.panel-announcement {
  grid-column: span 4;
}

.panel-todo {
  grid-column: span 12;
}

:deep(.panel-card .app-panel-header) {
  border-bottom: 1px solid rgba(19, 87, 98, 0.15);
  padding: 14px 18px;
}

:deep(.panel-card .panel-body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 250px;
  padding: 16px 18px;
  box-sizing: border-box;
}

.chart {
  width: 100%;
  min-height: 250px;
  flex: 1;
  border-radius: 12px;
  background:
    radial-gradient(circle at 14% 12%, rgba(255, 255, 255, 0.55), transparent 45%),
    linear-gradient(150deg, rgba(255, 255, 255, 0.68), rgba(231, 245, 243, 0.5));
  border: 1px solid rgba(19, 93, 103, 0.15);
}

.chart-trend {
  min-height: 280px;
}

.ops-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  height: 100%;
}

.personal-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  height: 100%;
}

.personal-item {
  border-radius: 12px;
  border: 1px solid rgba(20, 93, 105, 0.16);
  background: rgba(255, 255, 255, 0.74);
  padding: 14px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.personal-item span {
  color: #4e6777;
  font-size: 14px;
}

.personal-item strong {
  color: #17384a;
  font-size: 24px;
  line-height: 1;
}

.ops-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 12px;
  border: 1px solid rgba(20, 93, 105, 0.16);
  background: rgba(255, 255, 255, 0.74);
  padding: 14px;
}

.ops-item.clickable {
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.ops-item.clickable:hover {
  transform: translateY(-2px);
  border-color: rgba(19, 123, 118, 0.5);
  box-shadow: 0 10px 20px rgba(19, 91, 86, 0.16);
}

.ops-label {
  color: #4e6777;
  font-size: 14px;
}

.ops-value {
  color: #17384a;
  font-size: 24px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.announcement-timeline {
  flex: 1;
  overflow: auto;
  padding-right: 6px;
}

.announcement-item {
  opacity: 0;
  transform: translateX(-8px);
  animation: announcement-enter 0.34s ease forwards;
  animation-delay: var(--announcement-delay, 0ms);
}

.announcement-title {
  display: inline-flex;
  align-items: center;
  color: #1a6766;
  font-size: 14px;
  font-weight: 600;
  line-height: 1.45;
  background: transparent;
  border: none;
  padding: 0;
  cursor: pointer;
  transition: color 0.2s ease, transform 0.2s ease;
}

.announcement-title:hover {
  color: #0f5c5a;
  transform: translateX(3px);
}

.announcement-title:focus-visible {
  outline: 2px solid rgba(25, 118, 113, 0.35);
  outline-offset: 3px;
  border-radius: 4px;
}

:deep(.announcement-timeline .el-timeline-item__tail) {
  border-left-color: rgba(22, 106, 111, 0.2);
}

:deep(.announcement-timeline .el-timeline-item__wrapper) {
  top: -2px;
  padding-left: 12px;
}

:deep(.announcement-timeline .el-timeline-item__timestamp) {
  color: #7693a0;
  font-size: 12px;
  margin-bottom: 6px;
}

:deep(.announcement-timeline .el-timeline-item__node) {
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  box-shadow: 0 0 0 3px rgba(24, 104, 108, 0.08);
}

:deep(.announcement-timeline .el-timeline-item:hover .el-timeline-item__node) {
  transform: scale(1.12);
  box-shadow: 0 0 0 4px rgba(24, 104, 108, 0.16);
}

.todo-create {
  margin-bottom: 14px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow: auto;
  max-height: 372px;
  padding-right: 6px;
}

.todo-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(20, 93, 105, 0.16);
  background: rgba(255, 255, 255, 0.78);
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease, opacity 0.2s ease;
  opacity: 0;
  transform: translateY(7px);
  animation: todo-enter 0.3s ease forwards;
  animation-delay: var(--todo-delay, 0ms);
}

.todo-item:hover {
  transform: translateY(-1px);
  border-color: rgba(22, 112, 116, 0.34);
  box-shadow: 0 10px 20px rgba(19, 88, 85, 0.12);
}

.todo-item:focus-within {
  border-color: rgba(20, 112, 106, 0.5);
  box-shadow: 0 0 0 3px rgba(20, 112, 106, 0.12);
}

.todo-main {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  min-width: 0;
  flex: 1;
}

.todo-content {
  min-width: 0;
  flex: 1;
}

.todo-title {
  color: #214456;
  font-size: 14px;
  line-height: 1.45;
  word-break: break-word;
}

.todo-meta {
  margin-top: 7px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-time {
  font-size: 12px;
  color: #79909b;
}

.todo-actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  padding-top: 1px;
}

.todo-item.completed {
  opacity: 0.64;
  background: rgba(246, 251, 250, 0.92);
  border-color: rgba(95, 141, 143, 0.26);
}

.todo-item.completed:hover {
  transform: none;
  box-shadow: none;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
}

:deep(.todo-main .el-checkbox__inner) {
  transition: all 0.2s ease;
}

:deep(.todo-main .el-checkbox.is-checked .el-checkbox__inner) {
  box-shadow: 0 0 0 3px rgba(24, 111, 105, 0.16);
}

.todo-fade-enter-active,
.todo-fade-leave-active {
  transition: all 0.24s ease;
}

.todo-fade-enter-from,
.todo-fade-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

.todo-fade-move {
  transition: transform 0.24s ease;
}

:deep(.el-link--primary) {
  color: #146f6b;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #2f4c5b;
  min-height: 80px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@keyframes announcement-enter {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes todo-enter {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 1400px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .panel-gender,
  .panel-course,
  .panel-personal,
  .panel-ops {
    grid-column: span 6;
  }

  .panel-trend,
  .panel-announcement,
  .panel-todo {
    grid-column: span 12;
  }
}

@media (max-width: 992px) {
  .dashboard {
    min-height: auto;
  }

  .hero-panel {
    padding: 16px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-chip-list {
    grid-template-columns: 1fr;
  }

  .hero-metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .bento-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-gender,
  .panel-course,
  .panel-personal,
  .panel-ops,
  .panel-trend,
  .panel-announcement,
  .panel-todo {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero-metric-grid {
    grid-template-columns: 1fr;
  }

  .personal-grid {
    grid-template-columns: 1fr;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .panel-gender,
  .panel-course,
  .panel-personal,
  .panel-ops,
  .panel-trend,
  .panel-announcement,
  .panel-todo {
    grid-column: span 1;
  }

  .chart,
  .chart-trend {
    min-height: 220px;
  }

  .todo-item {
    flex-direction: column;
    gap: 10px;
    padding: 12px;
  }

  .todo-actions {
    width: 100%;
    justify-content: flex-end;
    padding-top: 0;
  }

  .todo-meta {
    flex-wrap: wrap;
    gap: 6px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .announcement-item,
  .todo-item {
    animation: none;
    opacity: 1;
    transform: none;
  }

  .announcement-title,
  .todo-item,
  .todo-fade-enter-active,
  .todo-fade-leave-active,
  .todo-fade-move {
    transition: none !important;
  }
}
</style>

