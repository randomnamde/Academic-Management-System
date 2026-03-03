import { createRouter, createWebHistory } from 'vue-router'
import Cookies from 'js-cookie'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { canRoute } from '@/permission/ability'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'overview' }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/analytics/index.vue'),
        meta: { title: '统计分析', icon: 'DataAnalysis', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'assessment' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/index.vue'),
        meta: { title: '学生管理', icon: 'UserFilled', roles: ['ADMIN', 'TEACHER'], menuGroup: 'teaching' }
      },
      {
        path: 'teacher',
        name: 'Teacher',
        component: () => import('@/views/teacher/index.vue'),
        meta: {
          title: '教师管理',
          icon: 'User',
          roles: ['ADMIN', 'STUDENT'],
          menuGroup: 'teaching',
          hideInMenuForRoles: ['STUDENT']
        }
      },
      {
        path: 'class',
        name: 'Class',
        component: () => import('@/views/class/index.vue'),
        meta: {
          title: '班级管理',
          icon: 'School',
          roles: ['ADMIN', 'TEACHER', 'STUDENT'],
          menuGroup: 'teaching',
          hideInMenuForRoles: ['STUDENT']
        }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: {
          title: '课程管理',
          icon: 'Reading',
          roles: ['ADMIN', 'TEACHER', 'STUDENT'],
          menuGroup: 'teaching',
          hideInMenuForRoles: ['STUDENT']
        }
      },
      {
        path: 'college',
        name: 'College',
        component: () => import('@/views/college/index.vue'),
        meta: {
          title: '学院管理',
          icon: 'School',
          roles: ['SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'ADMIN'],
          menuGroup: 'teaching'
        }
      },
      {
        path: 'course-arrangement',
        name: 'CourseArrangement',
        component: () => import('@/views/course-arrangement/index.vue'),
        meta: { title: '排课管理', icon: 'Tickets', roles: ['ADMIN', 'TEACHER'], menuGroup: 'teaching' }
      },
      {
        path: 'score',
        name: 'Score',
        component: () => import('@/views/score/index.vue'),
        meta: { title: '成绩管理', icon: 'TrendCharts', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'assessment' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/index.vue'),
        meta: { title: '考勤管理', icon: 'Calendar', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'assessment' }
      },
      {
        path: 'leave-request',
        name: 'LeaveRequest',
        component: () => import('@/views/leave-request/index.vue'),
        meta: { title: '请假审批', icon: 'DocumentChecked', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'assessment' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/announcement/index.vue'),
        meta: { title: '通知公告', icon: 'BellFilled', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'assessment' }
      },
      {
        path: 'rbac/users',
        name: 'RBACUsers',
        component: () => import('@/views/rbac/users.vue'),
        meta: { title: '权限用户', icon: 'UserFilled', roles: ['ADMIN'], menuGroup: 'access' }
      },
      {
        path: 'rbac/roles',
        name: 'RBACRoles',
        component: () => import('@/views/rbac/roles.vue'),
        meta: { title: '角色模板', icon: 'Postcard', roles: ['ADMIN'], menuGroup: 'access' }
      },
      {
        path: 'rbac/permissions',
        name: 'RBACPermissions',
        component: () => import('@/views/rbac/permissions.vue'),
        meta: { title: '权限矩阵', icon: 'Operation', roles: ['ADMIN'], menuGroup: 'access' }
      },
      {
        path: 'rbac/audit',
        name: 'RBACAudit',
        component: () => import('@/views/rbac/audit.vue'),
        meta: { title: '审计日志', icon: 'Document', roles: ['ADMIN'], menuGroup: 'access' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { title: '个人中心', roles: ['ADMIN', 'TEACHER', 'STUDENT'] }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/system/index.vue'),
        meta: { title: '系统偏好', icon: 'Setting', roles: ['ADMIN'], menuGroup: 'access' }
      }
    ]
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('@/views/error/403.vue'),
    meta: { public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/error/404.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const ROLE_ALIAS = {
  ADMIN: ['ADMIN', 'SCHOOL_ADMIN', 'COLLEGE_ADMIN'],
  SCHOOL_ADMIN: ['SCHOOL_ADMIN', 'ADMIN'],
  COLLEGE_ADMIN: ['COLLEGE_ADMIN', 'ADMIN'],
  TEACHER: ['TEACHER', 'HOMEROOM_TEACHER', 'COURSE_TEACHER'],
  HOMEROOM_TEACHER: ['HOMEROOM_TEACHER', 'TEACHER'],
  COURSE_TEACHER: ['COURSE_TEACHER', 'TEACHER'],
  STUDENT: ['STUDENT']
}

const expandRoles = (roles = []) => {
  const set = new Set()
  roles.forEach((role) => {
    const key = String(role || '').toUpperCase()
    if (!key) return
    ;(ROLE_ALIAS[key] || [key]).forEach((item) => set.add(item))
  })
  return set
}

router.beforeEach((to, from, next) => {
  NProgress.start()

  const token = Cookies.get('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const userRole = userInfo?.primaryRole || userInfo?.role
  const roleList = []
  if (Array.isArray(userInfo?.roles) && userInfo.roles.length) {
    roleList.push(...userInfo.roles)
  }
  if (userRole) {
    roleList.push(userRole)
  }
  if (userInfo?.role) {
    roleList.push(userInfo.role)
  }
  const expandedRoles = expandRoles(roleList)

  if (to.meta.public) {
    next()
  } else if (!token) {
    next('/login')
  } else if (to.name && !['Layout', 'NotFound'].includes(String(to.name)) && !canRoute(userRole, String(to.name), userInfo?.permissions || [])) {
    next('/403')
  } else if (to.meta.roles && !to.meta.roles.some((allowed) => expandedRoles.has(String(allowed || '').toUpperCase()))) {
    next('/403')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
