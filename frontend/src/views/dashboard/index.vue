<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('dashboard.pageTitle')" content-class="p-4 space-y-4">
      <div class="dashboard-hero">
        <div class="dashboard-hero-copy">
          <p class="dashboard-hero-badge">{{ t('dashboard.heroBadge') }}</p>
          <h2 class="dashboard-hero-title">{{ dashboardTitle }}</h2>
          <p class="dashboard-hero-desc">{{ dashboardDesc }}</p>
        </div>

        <div class="dashboard-hero-pill">
          <span class="dashboard-hero-pill-label">{{ t('dashboard.heroRoleLabel') }}</span>
          <strong>{{ currentRoleLabel }}</strong>
        </div>
      </div>

      <div class="dashboard-stats">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="dashboard-stat"
        >
          <p class="dashboard-stat-label">{{ stat.label }}</p>
          <div class="dashboard-stat-row">
            <p class="dashboard-stat-value">{{ stat.value }}</p>
            <span class="dashboard-stat-note">{{ stat.note }}</span>
          </div>
        </div>
      </div>

      <section class="dashboard-section">
        <div class="dashboard-section-head">
          <div>
            <p class="dashboard-section-title">{{ t('dashboard.todos') }}</p>
            <p class="dashboard-section-desc">{{ t('dashboard.todoSectionDesc') }}</p>
          </div>
          <AppButton variant="ghost" size="sm" :loading="todoLoading" @click="refreshTodos">{{ t('dashboard.refresh') }}</AppButton>
        </div>

        <div class="dashboard-todo-panel">
          <div class="dashboard-todo-input">
            <el-input
              v-model="todoDraft"
              clearable
              maxlength="60"
              show-word-limit
              :placeholder="t('dashboard.todoPlaceholder')"
              @keyup.enter="addCustomTodo"
            />
            <AppButton class="dashboard-todo-add" @click="addCustomTodo">{{ t('dashboard.add') }}</AppButton>
          </div>

          <div v-if="!todoList.length" class="dashboard-todo-empty">
            {{ todoLoading ? t('common.loading') : t('dashboard.noTodos') }}
          </div>

          <div v-else class="dashboard-todo-list">
            <div
              v-for="item in todoList"
              :key="item.id"
              class="dashboard-todo-item"
              :class="{ 'is-completed': item.completed }"
            >
              <div class="dashboard-todo-main">
                <el-checkbox :model-value="item.completed" @change="(value) => toggleTodo(item, value)" />
                <div class="dashboard-todo-copy">
                  <div class="dashboard-todo-title" :class="{ 'is-completed': item.completed }">{{ item.title }}</div>
                  <div class="dashboard-todo-meta">
                    <AppBadge type="info">{{ item.sourceLabel }}</AppBadge>
                    <span>{{ item.timeText }}</span>
                  </div>
                </div>
              </div>

              <div class="dashboard-todo-actions">
                <AppButton v-if="item.route" variant="ghost" size="sm" @click="openTodoRoute(item)">{{ t('dashboard.goTo') }}</AppButton>
                <AppButton v-if="item.type === 'custom'" variant="danger" size="sm" @click="removeCustomTodo(item.id)">{{ t('dashboard.delete') }}</AppButton>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section
        v-for="section in sections"
        :key="section.key"
        class="dashboard-section"
      >
        <div class="dashboard-section-head">
          <div>
            <p class="dashboard-section-title">{{ section.title }}</p>
            <p class="dashboard-section-desc">{{ section.desc }}</p>
          </div>
        </div>

        <div class="dashboard-grid">
          <button
            v-for="card in section.cards"
            :key="card.path"
            type="button"
            class="dashboard-card touch-target"
            :class="`dashboard-card-${card.variant}`"
            @click="router.push(card.path)"
          >
            <div class="dashboard-card-head">
              <div class="dashboard-card-badge">
                <component :is="card.icon" class="h-5 w-5" />
              </div>
              <span class="dashboard-card-arrow">
                <ArrowRight class="h-4 w-4" />
              </span>
            </div>

            <div class="dashboard-card-body">
              <p class="dashboard-card-kicker">{{ card.kicker }}</p>
              <h3 class="dashboard-card-title">{{ card.title }}</h3>
              <p class="dashboard-card-desc">{{ card.desc }}</p>
            </div>

            <div class="dashboard-card-tags">
              <span v-for="tag in card.tags" :key="tag" class="dashboard-card-tag">{{ tag }}</span>
            </div>

            <div class="dashboard-card-footer">
              <span>{{ t('dashboard.enterAction') }}</span>
              <ArrowRight class="h-4 w-4" />
            </div>
          </button>
        </div>
      </section>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import {
  ArrowRight,
  BarChart3,
  BellRing,
  BookOpen,
  Building2,
  ClipboardList,
  GraduationCap,
  School,
  Settings2,
  ShieldCheck,
  UserRound,
  UsersRound
} from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { getAnnouncementList } from '@/api/announcement'
import { getAttendanceList } from '@/api/attendance'
import { getDashboardOverview } from '@/api/dashboard'
import { getLeaveRequestList, getPendingLeaveRequests } from '@/api/leaveRequest'
import { canRoute } from '@/permission/ability'

const router = useRouter()
const store = useStore()
const { t } = useI18n()

const statistics = ref({
  studentCount: 0,
  teacherCount: 0,
  courseCount: 0,
  classCount: 0
})

const operationOverview = ref({
  pendingApprovalCount: 0,
  abnormalTodayCount: 0,
  lowScoreWarningCount: 0
})
const todoLoading = ref(false)
const todoDraft = ref('')
const systemTodos = ref([])
const customTodos = ref([])
const completedTodoIds = ref([])

const roleLabelMap = {
  SCHOOL_ADMIN: 'roles.schoolAdmin',
  COLLEGE_ADMIN: 'roles.collegeAdmin',
  HOMEROOM_TEACHER: 'roles.homeroomTeacher',
  COURSE_TEACHER: 'roles.courseTeacher',
  STUDENT: 'roles.student'
}

const sectionConfigs = [
  {
    key: 'daily',
    titleKey: 'dashboard.sectionDailyTitle',
    descKey: 'dashboard.sectionDailyDesc',
    variant: 'daily',
    items: [
      {
        path: '/student',
        routeName: 'Student',
        titleKey: 'route.student',
        kickerKey: 'dashboard.cards.student.kicker',
        descKey: 'dashboard.cards.student.desc',
        tagKeys: ['dashboard.cards.student.tagPrimary', 'dashboard.cards.student.tagSecondary'],
        icon: UsersRound
      },
      {
        path: '/teacher',
        routeName: 'Teacher',
        titleKey: 'route.teacher',
        kickerKey: 'dashboard.cards.teacher.kicker',
        descKey: 'dashboard.cards.teacher.desc',
        tagKeys: ['dashboard.cards.teacher.tagPrimary', 'dashboard.cards.teacher.tagSecondary'],
        icon: UserRound
      },
      {
        path: '/class',
        routeName: 'Class',
        titleKey: 'route.class',
        kickerKey: 'dashboard.cards.class.kicker',
        descKey: 'dashboard.cards.class.desc',
        tagKeys: ['dashboard.cards.class.tagPrimary', 'dashboard.cards.class.tagSecondary'],
        icon: School
      },
      {
        path: '/course-center',
        routeName: 'CourseCenter',
        titleKey: 'route.courseCenter',
        kickerKey: 'dashboard.cards.courseCenter.kicker',
        descKey: 'dashboard.cards.courseCenter.desc',
        tagKeys: ['dashboard.cards.courseCenter.tagPrimary', 'dashboard.cards.courseCenter.tagSecondary'],
        icon: BookOpen
      },
      {
        path: '/college',
        routeName: 'College',
        titleKey: 'route.college',
        kickerKey: 'dashboard.cards.college.kicker',
        descKey: 'dashboard.cards.college.desc',
        tagKeys: ['dashboard.cards.college.tagPrimary', 'dashboard.cards.college.tagSecondary'],
        icon: Building2
      }
    ]
  },
  {
    key: 'risk',
    titleKey: 'dashboard.sectionRiskTitle',
    descKey: 'dashboard.sectionRiskDesc',
    variant: 'risk',
    items: [
      {
        path: '/analytics',
        routeName: 'Analytics',
        titleKey: 'route.analytics',
        kickerKey: 'dashboard.cards.analytics.kicker',
        descKey: 'dashboard.cards.analytics.desc',
        tagKeys: ['dashboard.cards.analytics.tagPrimary', 'dashboard.cards.analytics.tagSecondary'],
        icon: BarChart3
      },
      {
        path: '/score',
        routeName: 'Score',
        titleKey: 'route.score',
        kickerKey: 'dashboard.cards.score.kicker',
        descKey: 'dashboard.cards.score.desc',
        tagKeys: ['dashboard.cards.score.tagPrimary', 'dashboard.cards.score.tagSecondary'],
        icon: GraduationCap
      },
      {
        path: '/attendance',
        routeName: 'Attendance',
        titleKey: 'route.attendance',
        kickerKey: 'dashboard.cards.attendance.kicker',
        descKey: 'dashboard.cards.attendance.desc',
        tagKeys: ['dashboard.cards.attendance.tagPrimary', 'dashboard.cards.attendance.tagSecondary'],
        icon: ClipboardList
      },
      {
        path: '/leave-request',
        routeName: 'LeaveRequest',
        titleKey: 'route.leaveRequest',
        kickerKey: 'dashboard.cards.leaveRequest.kicker',
        descKey: 'dashboard.cards.leaveRequest.desc',
        tagKeys: ['dashboard.cards.leaveRequest.tagPrimary', 'dashboard.cards.leaveRequest.tagSecondary'],
        icon: ClipboardList
      },
      {
        path: '/announcement',
        routeName: 'Announcement',
        titleKey: 'route.announcement',
        kickerKey: 'dashboard.cards.announcement.kicker',
        descKey: 'dashboard.cards.announcement.desc',
        tagKeys: ['dashboard.cards.announcement.tagPrimary', 'dashboard.cards.announcement.tagSecondary'],
        icon: BellRing
      }
    ]
  },
  {
    key: 'system',
    titleKey: 'dashboard.sectionSystemTitle',
    descKey: 'dashboard.sectionSystemDesc',
    variant: 'system',
    items: [
      {
        path: '/rbac',
        routeName: 'RBACCenter',
        titleKey: 'route.rbacCenter',
        kickerKey: 'dashboard.cards.rbac.kicker',
        descKey: 'dashboard.cards.rbac.desc',
        tagKeys: ['dashboard.cards.rbac.tagPrimary', 'dashboard.cards.rbac.tagSecondary'],
        icon: ShieldCheck
      },
      {
        path: '/system',
        routeName: 'System',
        titleKey: 'route.system',
        kickerKey: 'dashboard.cards.system.kicker',
        descKey: 'dashboard.cards.system.desc',
        tagKeys: ['dashboard.cards.system.tagPrimary', 'dashboard.cards.system.tagSecondary'],
        icon: Settings2
      },
      {
        path: '/profile',
        routeName: 'Profile',
        titleKey: 'route.profile',
        kickerKey: 'dashboard.cards.profile.kicker',
        descKey: 'dashboard.cards.profile.desc',
        tagKeys: ['dashboard.cards.profile.tagPrimary', 'dashboard.cards.profile.tagSecondary'],
        icon: UserRound
      }
    ]
  }
]

const userInfo = computed(() => store.state.userInfo || {})
const userRole = computed(() => userInfo.value.primaryRole || userInfo.value.role || '')
const userPermissions = computed(() => userInfo.value.permissions || [])
const isStudent = computed(() => userRole.value === 'STUDENT')
const isHomeroomTeacher = computed(() => userRole.value === 'HOMEROOM_TEACHER')
const isCourseTeacher = computed(() => userRole.value === 'COURSE_TEACHER')

const currentRoleLabel = computed(() => t(roleLabelMap[userRole.value] || 'common.user'))

const dashboardTitle = computed(() => {
  if (isStudent.value) return t('dashboard.titleStudent')
  if (isHomeroomTeacher.value || isCourseTeacher.value) return t('dashboard.titleTeacher')
  return t('dashboard.titleAdmin')
})

const dashboardDesc = computed(() => {
  if (isStudent.value) return t('dashboard.descStudent')
  if (isHomeroomTeacher.value || isCourseTeacher.value) return t('dashboard.descTeacher')
  return t('dashboard.descAdmin')
})

const primaryStat = computed(() => {
  if (isStudent.value) {
    return {
      label: t('dashboard.statMyCourseLabel'),
      value: Number(statistics.value.courseCount || 0),
      note: t('dashboard.statMyCourseNote')
    }
  }

  if (isHomeroomTeacher.value) {
    return {
      label: t('dashboard.statTeachingClassLabel'),
      value: Number(statistics.value.classCount || 0),
      note: t('dashboard.statTeachingClassNote')
    }
  }

  if (isCourseTeacher.value) {
    return {
      label: t('dashboard.statTeachingCourseLabel'),
      value: Number(statistics.value.courseCount || 0),
      note: t('dashboard.statTeachingCourseNote')
    }
  }

  return {
    label: t('dashboard.statScopeStudentLabel'),
    value: Number(statistics.value.studentCount || 0),
    note: t('dashboard.statScopeStudentNote')
  }
})

const stats = computed(() => {
  if (isStudent.value) {
    return [
      primaryStat.value,
      {
        label: t('dashboard.statMyClassLabel'),
        value: Number(statistics.value.classCount || 0),
        note: t('dashboard.statMyClassNote')
      },
      {
        label: t('dashboard.statMyLeaveLabel'),
        value: Number(operationOverview.value.pendingApprovalCount || 0),
        note: t('dashboard.statMyLeaveNote')
      },
      {
        label: t('dashboard.statLowScoreLabel'),
        value: Number(operationOverview.value.lowScoreWarningCount || 0),
        note: t('dashboard.statLowScoreNote')
      }
    ]
  }

  return [
    primaryStat.value,
    {
      label: t('dashboard.statPendingLabel'),
      value: Number(operationOverview.value.pendingApprovalCount || 0),
      note: t('dashboard.statPendingNote')
    },
    {
      label: t('dashboard.statAbnormalLabel'),
      value: Number(operationOverview.value.abnormalTodayCount || 0),
      note: t('dashboard.statAbnormalNote')
    },
    {
      label: t('dashboard.statLowScoreLabel'),
      value: Number(operationOverview.value.lowScoreWarningCount || 0),
      note: t('dashboard.statLowScoreNote')
    }
  ]
})

const todoList = computed(() => {
  const completedSet = new Set(completedTodoIds.value)
  return [...systemTodos.value, ...customTodos.value]
    .map((item) => {
      const ts = new Date(item.createdAt).getTime()
      return {
        ...item,
        completed: completedSet.has(item.id),
        timeText: formatDateTime(item.createdAt),
        sortTs: Number.isNaN(ts) ? 0 : ts
      }
    })
    .sort((left, right) => {
      if (left.completed !== right.completed) return left.completed ? 1 : -1
      return right.sortTs - left.sortTs
    })
})

const sections = computed(() =>
  sectionConfigs
    .map((section) => {
      const cards = section.items
        .filter((item) => canRoute(userRole.value, item.routeName, userPermissions.value))
        .map((item) => ({
          path: item.path,
          title: t(item.titleKey),
          kicker: t(item.kickerKey),
          desc: t(item.descKey),
          tags: item.tagKeys.map((key) => t(key)),
          icon: item.icon,
          variant: section.variant
        }))

      return {
        key: section.key,
        title: t(section.titleKey),
        desc: t(section.descKey),
        cards
      }
    })
    .filter((section) => section.cards.length)
)

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
  } catch (_error) {
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

const saveCustomTodos = () => {
  localStorage.setItem(buildTodoStorageKey('custom'), JSON.stringify(customTodos.value))
}

const saveCompletedTodoIds = () => {
  localStorage.setItem(buildTodoStorageKey('completed'), JSON.stringify(completedTodoIds.value))
}

const cleanupCompletedTodos = () => {
  const availableIds = new Set([...systemTodos.value, ...customTodos.value].map((item) => item.id))
  completedTodoIds.value = completedTodoIds.value.filter((id) => availableIds.has(id))
  saveCompletedTodoIds()
}

const loadTodoState = () => {
  const cachedCustom = parseSafe(localStorage.getItem(buildTodoStorageKey('custom')), [])
  const cachedCompleted = parseSafe(localStorage.getItem(buildTodoStorageKey('completed')), [])
  customTodos.value = Array.isArray(cachedCustom) ? cachedCustom : []
  completedTodoIds.value = Array.isArray(cachedCompleted) ? cachedCompleted : []
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
  completedTodoIds.value = completedTodoIds.value.filter((itemId) => itemId !== id)
  saveCustomTodos()
  saveCompletedTodoIds()
}

const toggleTodo = (item, checked) => {
  const ids = new Set(completedTodoIds.value)
  if (checked) ids.add(item.id)
  else ids.delete(item.id)
  completedTodoIds.value = Array.from(ids)
  saveCompletedTodoIds()
}

const openTodoRoute = (item) => {
  if (item.route) {
    router.push(item.route)
  }
}

async function refreshTodos() {
  todoLoading.value = true
  const tasks = []
  const role = userRole.value
  const today = formatDate(new Date())

  try {
    const announcementRes = await getAnnouncementList({ page: 1, size: 6, status: 1 })
    const records = announcementRes.data?.records || []
    records
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
  } catch (_error) {
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
  } catch (_error) {
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
  } catch (_error) {
    // Ignore leave todo source errors.
  }

  systemTodos.value = tasks
  cleanupCompletedTodos()
  todoLoading.value = false
}

async function loadDashboardData() {
  try {
    const response = await getDashboardOverview()
    const payload = response.data || {}

    statistics.value = {
      studentCount: Number(payload.statistics?.studentCount || 0),
      teacherCount: Number(payload.statistics?.teacherCount || 0),
      courseCount: Number(payload.statistics?.courseCount || 0),
      classCount: Number(payload.statistics?.classCount || 0)
    }

    operationOverview.value = {
      pendingApprovalCount: Number(payload.operationOverview?.pendingApprovalCount || 0),
      abnormalTodayCount: Number(payload.operationOverview?.abnormalTodayCount || 0),
      lowScoreWarningCount: Number(payload.operationOverview?.lowScoreWarningCount || 0)
    }
  } catch (_error) {
    statistics.value = {
      studentCount: 0,
      teacherCount: 0,
      courseCount: 0,
      classCount: 0
    }
    operationOverview.value = {
      pendingApprovalCount: 0,
      abnormalTodayCount: 0,
      lowScoreWarningCount: 0
    }
    ElMessage.error(t('dashboard.loadFailed'))
  }
}

onMounted(() => {
  loadTodoState()
  loadDashboardData()
  refreshTodos()
})

onActivated(() => {
  loadTodoState()
  loadDashboardData()
  refreshTodos()
})
</script>

<style scoped>
.dashboard-hero {
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

.dashboard-hero-copy {
  display: grid;
  gap: 10px;
  max-width: 760px;
}

.dashboard-hero-badge {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.dashboard-hero-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.15;
  color: var(--text-primary);
}

.dashboard-hero-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.85;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
}

.dashboard-hero-pill {
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

.dashboard-hero-pill-label {
  color: var(--text-secondary);
}

.dashboard-stats {
  display: grid;
  gap: 12px;
}

.dashboard-stat {
  display: grid;
  gap: 10px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.dashboard-stat-label {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.dashboard-stat-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-stat-value {
  margin: 0;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: var(--text-primary);
}

.dashboard-stat-note {
  font-size: 12px;
  color: color-mix(in srgb, var(--text-primary) 66%, var(--text-secondary));
}

.dashboard-section {
  display: grid;
  gap: 14px;
}

.dashboard-section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.dashboard-section-desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.75;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
}

.dashboard-grid {
  display: grid;
  gap: 14px;
}

.dashboard-todo-panel {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.dashboard-todo-input {
  display: grid;
  gap: 12px;
}

.dashboard-todo-add {
  justify-self: start;
}

.dashboard-todo-empty {
  display: grid;
  place-items: center;
  min-height: 120px;
  border-radius: 16px;
  border: 1px dashed color-mix(in srgb, var(--panel-border) 72%, transparent);
  color: var(--text-secondary);
  font-size: 13px;
}

.dashboard-todo-list {
  display: grid;
  gap: 10px;
}

.dashboard-todo-item {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
}

.dashboard-todo-item.is-completed {
  opacity: 0.66;
}

.dashboard-todo-main {
  display: flex;
  flex: 1;
  min-width: 0;
  align-items: flex-start;
  gap: 10px;
}

.dashboard-todo-copy {
  display: grid;
  gap: 8px;
  min-width: 0;
}

.dashboard-todo-title {
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-primary);
  word-break: break-word;
}

.dashboard-todo-title.is-completed {
  text-decoration: line-through;
}

.dashboard-todo-meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  font-size: 11px;
  color: var(--text-secondary);
}

.dashboard-todo-actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.dashboard-card {
  position: relative;
  display: grid;
  gap: 18px;
  min-height: 248px;
  padding: 22px;
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  text-align: left;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.dashboard-card::before {
  content: '';
  position: absolute;
  inset: auto -40px -48px auto;
  width: 140px;
  height: 140px;
  border-radius: 999px;
  opacity: 0.5;
  filter: blur(9px);
}

.dashboard-card:hover,
.dashboard-card:focus-visible {
  transform: translateY(-3px);
  box-shadow: 0 22px 36px color-mix(in srgb, var(--accent-500) 12%, transparent);
}

.dashboard-card-daily {
  background:
    linear-gradient(135deg, rgba(15, 95, 148, 0.12), rgba(40, 137, 108, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.dashboard-card-daily::before {
  background: linear-gradient(135deg, rgba(40, 137, 108, 0.42), rgba(15, 95, 148, 0.3));
}

.dashboard-card-daily:hover,
.dashboard-card-daily:focus-visible {
  border-color: rgba(15, 95, 148, 0.24);
}

.dashboard-card-risk {
  background:
    linear-gradient(135deg, rgba(182, 88, 32, 0.12), rgba(197, 122, 55, 0.14)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.dashboard-card-risk::before {
  background: linear-gradient(135deg, rgba(197, 122, 55, 0.46), rgba(182, 88, 32, 0.28));
}

.dashboard-card-risk:hover,
.dashboard-card-risk:focus-visible {
  border-color: rgba(182, 88, 32, 0.24);
}

.dashboard-card-system {
  background:
    linear-gradient(135deg, rgba(70, 88, 178, 0.12), rgba(45, 124, 190, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.dashboard-card-system::before {
  background: linear-gradient(135deg, rgba(70, 88, 178, 0.38), rgba(45, 124, 190, 0.28));
}

.dashboard-card-system:hover,
.dashboard-card-system:focus-visible {
  border-color: rgba(70, 88, 178, 0.24);
}

.dashboard-card-head,
.dashboard-card-body,
.dashboard-card-tags,
.dashboard-card-footer {
  position: relative;
  z-index: 1;
}

.dashboard-card-head,
.dashboard-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.dashboard-card-badge,
.dashboard-card-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 78%, transparent);
}

.dashboard-card-badge {
  width: 44px;
  height: 44px;
}

.dashboard-card-arrow {
  width: 34px;
  height: 34px;
  color: var(--text-secondary);
}

.dashboard-card-body {
  display: grid;
  gap: 8px;
}

.dashboard-card-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.dashboard-card-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  color: var(--text-primary);
}

.dashboard-card-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
}

.dashboard-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dashboard-card-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
  border: 1px solid color-mix(in srgb, var(--panel-border) 70%, transparent);
  font-size: 12px;
  color: var(--text-secondary);
}

.dashboard-card-footer {
  margin-top: auto;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

@media (min-width: 768px) {
  .dashboard-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .dashboard-todo-input {
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
  }
}

@media (min-width: 1280px) {
  .dashboard-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .dashboard-stats {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }
}
</style>
