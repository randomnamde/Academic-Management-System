<template>
  <div class="dashboard">
    <el-row :gutter="20" class="statistics-row">
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('student')">
          <div class="stat-icon student-icon">
            <el-icon><User /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">学生总数</div>
            <div class="stat-value">{{ statistics.studentCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('teacher')">
          <div class="stat-icon teacher-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">教师总数</div>
            <div class="stat-value">{{ statistics.teacherCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('course')">
          <div class="stat-icon course-icon">
            <el-icon><Reading /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">课程总数</div>
            <div class="stat-value">{{ statistics.courseCount }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6" class="stat-col">
        <el-card class="stat-card glow-card" @click="openStatDetail('class')">
          <div class="stat-icon class-icon">
            <el-icon><School /></el-icon>
          </div>
          <div class="stat-info">
            <div class="stat-title">班级总数</div>
            <div class="stat-value">{{ statistics.classCount }}</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :lg="12" class="chart-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <span>学生性别分布</span>
          </template>
          <div ref="genderChartRef" class="chart chart-gender"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="12" class="chart-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <span>课程类型分布</span>
          </template>
          <div ref="courseChartRef" class="chart chart-course"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="ops-row">
      <el-col :xs="24" :lg="8" class="info-col">
        <el-card class="panel-card glow-card">
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
      </el-col>
      <el-col :xs="24" :lg="16" class="info-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <span>近7日异常考勤趋势</span>
          </template>
          <div ref="trendChartRef" class="chart chart-trend"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="info-row">
      <el-col :xs="24" :lg="12" class="info-col">
        <el-card class="panel-card glow-card">
          <template #header>
            <div class="card-header">
              <span>最新公告</span>
              <el-link type="primary" @click="$router.push('/announcement')">查看更多</el-link>
            </div>
          </template>
          <el-empty v-if="announcementLoading || !announcements.length" :description="announcementLoading ? '加载中...' : '暂无公告'" />
          <el-timeline v-else>
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
      </el-col>
      <el-col :xs="24" :lg="12" class="info-col">
        <el-card class="panel-card glow-card">
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
      </el-col>
    </el-row>

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
  padding: 20px;
  position: relative;
}

.statistics-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  text-align: center;
  gap: 10px;
  padding: 20px 12px;
  min-height: 150px;
  cursor: pointer;
}

.stat-card::before,
.panel-card::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.16), transparent 45%);
}

.glow-card {
  transition: transform 0.28s ease, box-shadow 0.28s ease;
}

.glow-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 20px 36px rgba(65, 48, 126, 0.24);
}

.stat-col,
.chart-col,
.info-col {
  opacity: 0;
  animation: revealUp 0.68s cubic-bezier(0.22, 1, 0.36, 1) forwards;
}

.stat-col:nth-child(1) { animation-delay: 0.04s; }
.stat-col:nth-child(2) { animation-delay: 0.12s; }
.stat-col:nth-child(3) { animation-delay: 0.2s; }
.stat-col:nth-child(4) { animation-delay: 0.28s; }
.chart-col:nth-child(1) { animation-delay: 0.26s; }
.chart-col:nth-child(2) { animation-delay: 0.34s; }
.info-col:nth-child(1) { animation-delay: 0.36s; }
.info-col:nth-child(2) { animation-delay: 0.44s; }

.panel-card {
  position: relative;
  overflow: hidden;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 0;
  font-size: 30px;
  color: #fff;
  box-shadow: inset 0 0 0 1px rgba(255, 255, 255, 0.2), 0 10px 24px rgba(116, 92, 199, 0.18);
  backdrop-filter: blur(2px);
}

.student-icon {
  background:
    radial-gradient(circle at 24% 24%, rgba(255, 255, 255, 0.38) 0%, rgba(255, 255, 255, 0.08) 30%, rgba(255, 255, 255, 0) 56%),
    radial-gradient(circle at 70% 74%, rgba(126, 104, 248, 0.42) 0%, rgba(126, 104, 248, 0.16) 58%, rgba(126, 104, 248, 0) 100%);
  border: 1px solid rgba(126, 104, 248, 0.22);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, 0.18), 0 8px 22px rgba(118, 95, 212, 0.16);
  color: rgba(255, 255, 255, 0.85);
}

.student-icon :deep(svg) {
  opacity: 0.82;
  transform: translateY(1px);
  filter: drop-shadow(0 2px 6px rgba(109, 87, 198, 0.22));
}

.teacher-icon {
  background:
    radial-gradient(circle at 24% 24%, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0.1) 30%, rgba(255, 255, 255, 0) 56%),
    radial-gradient(circle at 70% 74%, rgba(166, 126, 255, 0.42) 0%, rgba(166, 126, 255, 0.16) 58%, rgba(166, 126, 255, 0) 100%);
  border: 1px solid rgba(166, 126, 255, 0.24);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, 0.2), 0 8px 22px rgba(130, 96, 226, 0.16);
  color: rgba(255, 255, 255, 0.86);
}

.course-icon {
  background:
    radial-gradient(circle at 24% 24%, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0.1) 30%, rgba(255, 255, 255, 0) 56%),
    radial-gradient(circle at 70% 74%, rgba(133, 171, 255, 0.42) 0%, rgba(133, 171, 255, 0.16) 58%, rgba(133, 171, 255, 0) 100%);
  border: 1px solid rgba(133, 171, 255, 0.24);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, 0.2), 0 8px 22px rgba(92, 133, 219, 0.16);
  color: rgba(255, 255, 255, 0.86);
}

.class-icon {
  background:
    radial-gradient(circle at 24% 24%, rgba(255, 255, 255, 0.4) 0%, rgba(255, 255, 255, 0.1) 30%, rgba(255, 255, 255, 0) 56%),
    radial-gradient(circle at 70% 74%, rgba(112, 201, 255, 0.42) 0%, rgba(112, 201, 255, 0.16) 58%, rgba(112, 201, 255, 0) 100%);
  border: 1px solid rgba(112, 201, 255, 0.24);
  box-shadow: inset 0 0 18px rgba(255, 255, 255, 0.2), 0 8px 22px rgba(86, 159, 209, 0.16);
  color: rgba(255, 255, 255, 0.86);
}

.teacher-icon :deep(svg),
.course-icon :deep(svg),
.class-icon :deep(svg) {
  opacity: 0.84;
  transform: translateY(1px);
  filter: drop-shadow(0 2px 6px rgba(80, 110, 188, 0.2));
}

.stat-info {
  width: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.stat-title {
  color: #7e739f;
  font-size: 14px;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #2b2150;
}

.chart-row {
  margin-bottom: 20px;
}

.ops-row {
  margin-bottom: 20px;
}

.chart {
  height: 300px;
  border-radius: 16px;
  border: 1px solid rgba(182, 162, 255, 0.08);
  background:
    radial-gradient(circle at 24% 22%, rgba(255, 255, 255, 0.09) 0%, rgba(255, 255, 255, 0.014) 34%, transparent 68%),
    linear-gradient(165deg, rgba(246, 241, 255, 0.22) 0%, rgba(231, 223, 252, 0.07) 100%);
  box-shadow:
    inset 0 1px 0 rgba(255, 255, 255, 0.18),
    inset 0 -8px 16px rgba(126, 99, 225, 0.025),
    0 4px 8px rgba(98, 73, 187, 0.028);
  animation: chartGlow 8s ease-in-out infinite;
}

.chart-gender {
  background:
    radial-gradient(circle at 18% 16%, rgba(248, 241, 255, 0.14) 0%, rgba(222, 207, 255, 0.05) 40%, transparent 72%),
    linear-gradient(165deg, rgba(244, 238, 255, 0.24) 0%, rgba(226, 217, 250, 0.07) 100%);
}

.chart-course {
  background:
    radial-gradient(circle at 82% 20%, rgba(238, 246, 255, 0.12) 0%, rgba(200, 224, 255, 0.04) 38%, transparent 70%),
    linear-gradient(165deg, rgba(243, 239, 255, 0.22) 0%, rgba(220, 231, 253, 0.07) 100%);
}

.chart-trend {
  height: 260px;
  background:
    radial-gradient(circle at 18% 16%, rgba(248, 241, 255, 0.14) 0%, rgba(222, 207, 255, 0.05) 40%, transparent 72%),
    linear-gradient(165deg, rgba(244, 238, 255, 0.24) 0%, rgba(226, 217, 250, 0.07) 100%);
}

.ops-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ops-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border: 1px solid rgba(177, 159, 255, 0.2);
  border-radius: 10px;
  padding: 12px;
  background: linear-gradient(160deg, rgba(250, 246, 255, 0.8) 0%, rgba(241, 234, 255, 0.56) 100%);
}

.ops-label {
  color: #5d4d8f;
  font-size: 14px;
}

.ops-value {
  font-size: 24px;
  color: #2f2458;
}

:deep(.panel-card .el-card__header) {
  border-bottom: 1px solid rgba(175, 157, 255, 0.3);
}

:deep(.el-link--primary) {
  color: #7e67f6;
  text-shadow: 0 0 12px rgba(149, 126, 237, 0.2);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.todo-create {
  margin-bottom: 12px;
}

.todo-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 320px;
  overflow-y: auto;
  padding-right: 4px;
}

.todo-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 12px;
  border: 1px solid rgba(177, 159, 255, 0.2);
  background: linear-gradient(160deg, rgba(250, 246, 255, 0.8) 0%, rgba(241, 234, 255, 0.56) 100%);
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
  color: #3b2f64;
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
  opacity: 0.72;
}

.todo-item.completed .todo-title {
  text-decoration: line-through;
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

@keyframes revealUp {
  from {
    opacity: 0;
    transform: translateY(16px) scale(0.985);
    filter: blur(3px);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
    filter: blur(0);
  }
}

@keyframes chartGlow {
  0%,
  100% {
    filter: drop-shadow(0 0 0 rgba(154, 128, 245, 0));
  }
  50% {
    filter: drop-shadow(0 0 3px rgba(154, 128, 245, 0.05));
  }
}

@media (max-width: 992px) {
  .stat-col,
  .chart-col,
  .info-col {
    margin-bottom: 20px;
  }

  .chart-row,
  .info-row {
    margin-bottom: 0;
  }
}

@media (prefers-reduced-motion: reduce) {
  .stat-col,
  .chart-col,
  .info-col,
  .chart {
    animation: none;
    opacity: 1;
  }

  .glow-card,
  .glow-card:hover {
    transform: none;
  }
}
</style>

