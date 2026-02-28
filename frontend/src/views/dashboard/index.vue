<template>
  <div class="dashboard">
    <section class="hero-panel">
      <div class="hero-content">
        <p class="hero-kicker">Campus Operations</p>
        <h2 class="hero-title">教学运营总览</h2>
        <p class="hero-description">
          在同一视图追踪学生、课程、考勤与审批数据，快速定位今日重点任务。
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
        <el-card class="metric-card" @click="openStatDetail('student')">
          <div class="metric-icon student-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">学生总数</span>
            <strong class="metric-value">{{ statistics.studentCount }}</strong>
          </div>
        </el-card>

        <el-card class="metric-card" @click="openStatDetail('teacher')">
          <div class="metric-icon teacher-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">教师总数</span>
            <strong class="metric-value">{{ statistics.teacherCount }}</strong>
          </div>
        </el-card>

        <el-card class="metric-card" @click="openStatDetail('course')">
          <div class="metric-icon course-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">课程总数</span>
            <strong class="metric-value">{{ statistics.courseCount }}</strong>
          </div>
        </el-card>

        <el-card class="metric-card" @click="openStatDetail('class')">
          <div class="metric-icon class-icon">
            <el-icon><School /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">班级总数</span>
            <strong class="metric-value">{{ statistics.classCount }}</strong>
          </div>
        </el-card>
      </div>
    </section>

    <section class="bento-grid">
      <el-card class="panel-card panel-gender">
        <template #header>
          <span>学生性别分布</span>
        </template>
        <div ref="genderChartRef" class="chart chart-gender"></div>
      </el-card>

      <el-card class="panel-card panel-course">
        <template #header>
          <span>课程类型分布</span>
        </template>
        <div ref="courseChartRef" class="chart chart-course"></div>
      </el-card>

      <el-card class="panel-card panel-ops">
        <template #header>
          <span>运营概览</span>
        </template>
        <div class="ops-list">
          <div class="ops-item">
            <span class="ops-label">待处理审批</span>
            <strong class="ops-value">{{ operationOverview.pendingApprovalCount }}</strong>
          </div>
          <div class="ops-item">
            <span class="ops-label">今日异常考勤</span>
            <strong class="ops-value">{{ operationOverview.abnormalTodayCount }}</strong>
          </div>
          <div class="ops-item">
            <span class="ops-label">低分预警人数</span>
            <strong class="ops-value">{{ operationOverview.lowScoreWarningCount }}</strong>
          </div>
        </div>
      </el-card>

      <el-card class="panel-card panel-trend">
        <template #header>
          <span>近7日异常考勤趋势</span>
        </template>
        <div ref="trendChartRef" class="chart chart-trend"></div>
      </el-card>

      <el-card class="panel-card panel-announcement">
        <template #header>
          <div class="card-header">
            <span>最新公告</span>
            <el-link type="primary" @click="$router.push('/announcement')">查看更多</el-link>
          </div>
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
          >
            <el-link type="primary" :underline="false" @click="openAnnouncementDetail(item)">
              {{ item.title }}
            </el-link>
          </el-timeline-item>
        </el-timeline>
      </el-card>

      <el-card class="panel-card panel-todo">
        <template #header>
          <div class="card-header">
            <span>待办事项</span>
            <el-button link type="primary" :loading="todoLoading" @click="refreshTodos">刷新</el-button>
          </div>
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
              <el-button @click="addCustomTodo">添加</el-button>
            </template>
          </el-input>
        </div>
        <el-skeleton :loading="todoLoading" animated :rows="4">
          <template #default>
            <el-empty v-if="!todoList.length" description="暂无待办事项" />
            <div v-else class="todo-list">
              <div
                v-for="item in todoList"
                :key="item.id"
                class="todo-item"
                :class="{ completed: item.completed }"
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
                  <el-button v-if="item.route" link type="primary" @click="openTodoRoute(item)">前往</el-button>
                  <el-button v-if="item.type === 'custom'" link type="danger" @click="removeCustomTodo(item.id)">删除</el-button>
                </div>
              </div>
            </div>
          </template>
        </el-skeleton>
      </el-card>
    </section>

    <el-dialog v-model="detailDialogVisible" title="公告详情" width="720px">
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
        <el-button @click="detailDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="statDialogVisible" :title="currentStatTitle" width="900px">
      <el-table :data="statRecords" v-loading="statLoading" stripe>
        <template v-if="statType === 'student'">
          <el-table-column prop="studentNo" label="学号" width="140" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column label="性别" width="90">
            <template #default="{ row }">{{ getGenderText(row.gender) }}</template>
          </el-table-column>
          <el-table-column prop="className" label="班级" min-width="140" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ getStudentStatusText(row.status) }}</template>
          </el-table-column>
        </template>

        <template v-if="statType === 'teacher'">
          <el-table-column prop="teacherNo" label="教师编号" width="150" />
          <el-table-column prop="name" label="姓名" width="120" />
          <el-table-column label="性别" width="90">
            <template #default="{ row }">{{ getGenderText(row.gender) }}</template>
          </el-table-column>
          <el-table-column prop="title" label="职称" width="170" />
          <el-table-column prop="department" label="院系" min-width="150" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '在职' : '停用' }}</template>
          </el-table-column>
        </template>

        <template v-if="statType === 'course'">
          <el-table-column prop="courseCode" label="课程代码" width="140" />
          <el-table-column prop="courseName" label="课程名称" min-width="180" />
          <el-table-column prop="credit" label="学分" width="90" />
          <el-table-column prop="hours" label="学时" width="90" />
          <el-table-column label="类型" width="110">
            <template #default="{ row }">{{ getCourseCategoryText(row.category) }}</template>
          </el-table-column>
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '启用' : '禁用' }}</template>
          </el-table-column>
        </template>

        <template v-if="statType === 'class'">
          <el-table-column prop="classCode" label="班级代码" width="140" />
          <el-table-column prop="className" label="班级名称" min-width="180" />
          <el-table-column prop="grade" label="年级" width="100" />
          <el-table-column prop="major" label="专业" min-width="160" />
          <el-table-column prop="studentCount" label="人数" width="90" />
          <el-table-column label="状态" width="100">
            <template #default="{ row }">{{ row.status === 1 ? '在读' : '停用' }}</template>
          </el-table-column>
        </template>
      </el-table>

      <el-pagination
        class="pagination"
        v-model:current-page="statPage"
        v-model:page-size="statSize"
        :total="statTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleStatSizeChange"
        @current-change="handleStatPageChange"
      />

      <template #footer>
        <el-button @click="statDialogVisible = false">关闭</el-button>
        <el-button type="primary" @click="goToModule">进入对应模块</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { use, init, graphic } from 'echarts/core'
import { PieChart, BarChart, PictorialBarChart, LineChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent, GridComponent, GraphicComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'
import { User, UserFilled, Reading, School } from '@element-plus/icons-vue'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'
import { getAttendanceList } from '@/api/attendance'
import { getLeaveRequestList, getPendingLeaveRequests } from '@/api/leaveRequest'
import { getStudentGenderStatistics, getStudentList } from '@/api/student'
import { getTeacherList } from '@/api/teacher'
import { getCourseCategoryStatistics, getCourseList } from '@/api/course'
import { getClassList } from '@/api/clazz'
import { getDashboardOverview } from '@/api/dashboard'

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
const statLoading = ref(false)
const statRecords = ref([])
const statTotal = ref(0)
const statPage = ref(1)
const statSize = ref(10)

const statMeta = {
  student: { title: '学生详情', route: '/student', fetch: getStudentList },
  teacher: { title: '教师详情', route: '/teacher', fetch: getTeacherList },
  course: { title: '课程详情', route: '/course', fetch: getCourseList },
  class: { title: '班级详情', route: '/class', fetch: getClassList }
}

const currentStatTitle = computed(() => statMeta[statType.value]?.title || '详情')
const userInfo = computed(() => store.state.userInfo || {})
const userRole = computed(() => userInfo.value.role || 'STUDENT')

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
              shadowColor: 'rgba(130, 102, 224, 0.035)'
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
          shadowColor: 'rgba(95, 74, 173, 0.1)'
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
                { offset: 0, color: '#b9a7ff' },
                { offset: 1, color: '#7d63f3' }
              ])
            }
          },
          {
            value: femaleValue,
            name: '女生',
            itemStyle: {
              color: new graphic.LinearGradient(0, 0, 0, 1, [
                { offset: 0, color: '#e0c7ff' },
                { offset: 1, color: '#a683ff' }
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
                { offset: 1, color: 'rgba(187, 166, 255, 0.22)' }
              ]),
              shadowBlur: 6,
              shadowColor: 'rgba(157, 128, 247, 0.08)'
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
            style: { fill: 'rgba(120, 97, 214, 0.018)' }
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
      axisLine: { lineStyle: { color: 'rgba(136, 118, 198, 0.45)' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(157, 131, 255, 0.16)' } }
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
          color: 'rgba(110, 88, 214, 0.16)'
        }
      },
      {
        data: categoryValues,
        type: 'bar',
        barWidth: 42,
        z: 2,
        showBackground: true,
        backgroundStyle: {
          color: 'rgba(145, 121, 232, 0.04)',
          borderRadius: [8, 8, 0, 0]
        },
        itemStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: '#d4c2ff' },
            { offset: 0.5, color: '#a186ff' },
            { offset: 1, color: '#7860f2' }
          ]),
          borderRadius: [8, 8, 0, 0],
          shadowBlur: 6,
          shadowColor: 'rgba(111, 84, 216, 0.1)',
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
            { offset: 0, color: '#efe5ff' },
            { offset: 1, color: '#ad90ff' }
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
            { offset: 0, color: 'rgba(187, 166, 255, 0.03)' },
            { offset: 1, color: 'rgba(121, 97, 223, 0.008)' }
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
      axisLine: { lineStyle: { color: 'rgba(136, 118, 198, 0.45)' } }
    },
    yAxis: {
      type: 'value',
      splitLine: { lineStyle: { color: 'rgba(157, 131, 255, 0.16)' } },
      minInterval: 1
    },
    series: [
      {
        name: '异常考勤',
        type: 'line',
        smooth: true,
        showSymbol: true,
        data: yData,
        lineStyle: { width: 3, color: '#7e67f6' },
        itemStyle: { color: '#7e67f6' },
        areaStyle: {
          color: new graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(126, 103, 246, 0.25)' },
            { offset: 1, color: 'rgba(126, 103, 246, 0.02)' }
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

const fetchStatistics = async () => {
  try {
    const [studentRes, teacherRes, courseRes, classRes] = await Promise.all([
      getStudentList({ page: 1, size: 1 }),
      getTeacherList({ page: 1, size: 1 }),
      getCourseList({ page: 1, size: 1 }),
      getClassList({ page: 1, size: 1 })
    ])

    statistics.value = {
      studentCount: Number(studentRes.data?.total || 0),
      teacherCount: Number(teacherRes.data?.total || 0),
      courseCount: Number(courseRes.data?.total || 0),
      classCount: Number(classRes.data?.total || 0)
    }
  } catch (_e) {
    ElMessage.error('获取统计数据失败')
  }
}

const fetchGenderStatistics = async () => {
  try {
    const res = await getStudentGenderStatistics()
    genderStatistics.value = {
      male: Number(res.data?.male ?? res.data?.MALE ?? 0),
      female: Number(res.data?.female ?? res.data?.FEMALE ?? 0)
    }
    renderGenderChart()
  } catch (_e) {
    ElMessage.error('获取性别分布失败')
  }
}

const fetchCourseCategoryStatistics = async () => {
  try {
    const res = await getCourseCategoryStatistics()
    courseCategoryStatistics.value = {
      required: Number(res.data?.required ?? res.data?.REQUIRED ?? 0),
      elective: Number(res.data?.elective ?? res.data?.ELECTIVE ?? 0),
      practical: Number(res.data?.practical ?? res.data?.PRACTICAL ?? 0)
    }
    renderCourseChart()
  } catch (_e) {
    ElMessage.error('获取课程类型分布失败')
  }
}

const fetchDashboardOperationOverview = async () => {
  try {
    const res = await getDashboardOverview()
    operationOverview.value = {
      pendingApprovalCount: Number(res.data?.pendingApprovalCount || 0),
      abnormalTodayCount: Number(res.data?.abnormalTodayCount || 0),
      lowScoreWarningCount: Number(res.data?.lowScoreWarningCount || 0),
      abnormalTrend: Array.isArray(res.data?.abnormalTrend) ? res.data.abnormalTrend : []
    }
    renderTrendChart()
  } catch (_e) {
    ElMessage.error('获取运营概览失败')
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

const fetchStatDetail = async () => {
  const meta = statMeta[statType.value]
  if (!meta) return

  statLoading.value = true
  try {
    const res = await meta.fetch({
      page: statPage.value,
      size: statSize.value
    })
    statRecords.value = res.data?.records || []
    statTotal.value = Number(res.data?.total || 0)
  } catch (_e) {
    ElMessage.error('获取详情数据失败')
  } finally {
    statLoading.value = false
  }
}

const openStatDetail = async (type) => {
  statType.value = type
  statPage.value = 1
  statSize.value = 10
  statDialogVisible.value = true
  await fetchStatDetail()
}

const handleStatSizeChange = (val) => {
  statSize.value = val
  fetchStatDetail()
}

const handleStatPageChange = (val) => {
  statPage.value = val
  fetchStatDetail()
}

const goToModule = () => {
  const route = statMeta[statType.value]?.route
  statDialogVisible.value = false
  if (route) {
    router.push(route)
  }
}

const getGenderText = (gender) => (gender === 'MALE' ? '男' : '女')

const getStudentStatusText = (status) => {
  const map = {
    ENROLLED: '在读',
    SUSPENDED: '休学',
    GRADUATED: '毕业',
    DROPPED: '退学'
  }
  return map[status] || status || '-'
}

const getCourseCategoryText = (category) => {
  const map = {
    REQUIRED: '必修',
    ELECTIVE: '选修',
    PRACTICAL: '实践'
  }
  return map[category] || category || '-'
}

onMounted(() => {
  loadTodoState()
  fetchStatistics()
  fetchGenderStatistics()
  fetchCourseCategoryStatistics()
  fetchDashboardOperationOverview()
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
  border: 1px solid rgba(165, 143, 255, 0.25);
  background:
    radial-gradient(circle at 8% 12%, rgba(255, 255, 255, 0.42), transparent 38%),
    radial-gradient(circle at 92% 4%, rgba(189, 171, 255, 0.22), transparent 36%),
    linear-gradient(140deg, rgba(255, 255, 255, 0.86), rgba(244, 236, 255, 0.82));
  box-shadow: 0 20px 40px rgba(78, 60, 145, 0.14);
}

.hero-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #8d79d0;
}

.hero-title {
  margin: 10px 0 12px;
  color: #2a1f4f;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 700;
}

.hero-description {
  margin: 0;
  max-width: 580px;
  color: #5e5186;
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
  border: 1px solid rgba(168, 143, 255, 0.24);
  background: rgba(255, 255, 255, 0.6);
}

.hero-chip span {
  color: #6f5f9f;
  font-size: 13px;
}

.hero-chip strong {
  color: #2d2150;
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
  border: 1px solid rgba(161, 138, 246, 0.22);
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.93), rgba(244, 237, 255, 0.82));
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.metric-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 18px 32px rgba(83, 61, 170, 0.18);
}

:deep(.metric-card .el-card__body) {
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
  background: linear-gradient(135deg, #7c66ff, #5a47d8);
}

.teacher-icon {
  background: linear-gradient(135deg, #a464ff, #7f4ed7);
}

.course-icon {
  background: linear-gradient(135deg, #55a8ff, #4f77df);
}

.class-icon {
  background: linear-gradient(135deg, #42b8d2, #3f87bc);
}

.metric-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.metric-label {
  color: #6f6296;
  font-size: 13px;
}

.metric-value {
  margin-top: 4px;
  color: #261c49;
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
  border: 1px solid rgba(166, 145, 247, 0.2);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.9), rgba(247, 241, 255, 0.82));
  box-shadow: 0 12px 30px rgba(85, 65, 155, 0.1);
}

.panel-gender,
.panel-course,
.panel-ops {
  grid-column: span 4;
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

:deep(.panel-card .el-card__header) {
  border-bottom: 1px solid rgba(171, 149, 255, 0.25);
  padding: 14px 18px;
}

:deep(.panel-card .el-card__body) {
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
    linear-gradient(150deg, rgba(255, 255, 255, 0.65), rgba(236, 226, 255, 0.46));
  border: 1px solid rgba(180, 160, 255, 0.18);
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

.ops-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 12px;
  border: 1px solid rgba(175, 154, 255, 0.2);
  background: rgba(255, 255, 255, 0.68);
  padding: 14px;
}

.ops-label {
  color: #5e4f8d;
  font-size: 14px;
}

.ops-value {
  color: #2b204f;
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
  padding-right: 4px;
}

.todo-create {
  margin-bottom: 12px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  overflow: auto;
  max-height: 360px;
  padding-right: 4px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(176, 155, 255, 0.22);
  background: rgba(255, 255, 255, 0.72);
}

.todo-main {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  min-width: 0;
  flex: 1;
}

.todo-content {
  min-width: 0;
  flex: 1;
}

.todo-title {
  color: #32265e;
  font-size: 14px;
  line-height: 1.45;
  word-break: break-word;
}

.todo-meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.todo-time {
  font-size: 12px;
  color: #8a7aa7;
}

.todo-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  flex-shrink: 0;
}

.todo-item.completed {
  opacity: 0.66;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
}

:deep(.el-link--primary) {
  color: #6d55ef;
}

.detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #3a315d;
  min-height: 80px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 1400px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .panel-gender,
  .panel-course,
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

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .panel-gender,
  .panel-course,
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
}
</style>

