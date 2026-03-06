import { createRouter, createWebHistory } from 'vue-router'
import Cookies from 'js-cookie'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { canRoute } from '@/permission/ability'

const ALL_ROLES = ['SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT']
const MANAGE_ROLES = ['SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER']

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
        meta: { titleKey: 'route.dashboard', icon: 'HomeFilled', roles: ALL_ROLES, menuGroup: 'overview' }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/analytics/index.vue'),
        meta: { titleKey: 'route.analytics', icon: 'DataAnalysis', roles: ALL_ROLES, menuGroup: 'assessment' }
      },
      {
        path: 'student',
        name: 'Student',
        component: () => import('@/views/student/index.vue'),
        meta: { titleKey: 'route.student', icon: 'UserFilled', roles: MANAGE_ROLES, menuGroup: 'teaching' }
      },
      {
        path: 'teacher',
        name: 'Teacher',
        component: () => import('@/views/teacher/index.vue'),
        meta: {
          titleKey: 'route.teacher',
          icon: 'User',
          roles: MANAGE_ROLES,
          menuGroup: 'teaching'
        }
      },
      {
        path: 'class',
        name: 'Class',
        component: () => import('@/views/class/index.vue'),
        meta: {
          titleKey: 'route.class',
          icon: 'School',
          roles: MANAGE_ROLES,
          menuGroup: 'teaching'
        }
      },
      {
        path: 'course-center',
        name: 'CourseCenter',
        component: () => import('@/views/course-center/index.vue'),
        meta: {
          titleKey: 'route.courseCenter',
          icon: 'Reading',
          roles: MANAGE_ROLES,
          menuGroup: 'teaching'
        }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: {
          titleKey: 'route.course',
          icon: 'Reading',
          roles: MANAGE_ROLES,
          menuGroup: 'teaching',
          hideInMenu: true,
          activeMenu: '/course-center'
        }
      },
      {
        path: 'college',
        name: 'College',
        component: () => import('@/views/college/index.vue'),
        meta: {
          titleKey: 'route.college',
          icon: 'School',
          roles: ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'],
          menuGroup: 'teaching'
        }
      },
      {
        path: 'course-arrangement',
        name: 'CourseArrangement',
        component: () => import('@/views/course-arrangement/index.vue'),
        meta: {
          titleKey: 'route.courseArrangement',
          icon: 'Tickets',
          roles: MANAGE_ROLES,
          menuGroup: 'teaching',
          hideInMenu: true,
          activeMenu: '/course-center'
        }
      },
      {
        path: 'score',
        name: 'Score',
        component: () => import('@/views/score/index.vue'),
        meta: { titleKey: 'route.score', icon: 'TrendCharts', roles: ALL_ROLES, menuGroup: 'assessment' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/index.vue'),
        meta: { titleKey: 'route.attendance', icon: 'Calendar', roles: ALL_ROLES, menuGroup: 'assessment' }
      },
      {
        path: 'leave-request',
        name: 'LeaveRequest',
        component: () => import('@/views/leave-request/index.vue'),
        meta: { titleKey: 'route.leaveRequest', icon: 'DocumentChecked', roles: ALL_ROLES, menuGroup: 'assessment' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/announcement/index.vue'),
        meta: { titleKey: 'route.announcement', icon: 'BellFilled', roles: ALL_ROLES, menuGroup: 'assessment' }
      },
      {
        path: 'rbac',
        name: 'RBACCenter',
        component: () => import('@/views/rbac/center.vue'),
        meta: { titleKey: 'route.rbacCenter', icon: 'Operation', roles: ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'], menuGroup: 'access' }
      },
      {
        path: 'rbac/users',
        name: 'RBACUsers',
        component: () => import('@/views/rbac/hub.vue'),
        meta: { titleKey: 'route.rbacUsers', icon: 'UserFilled', roles: ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'], menuGroup: 'access', hideInMenu: true, activeMenu: '/rbac' }
      },
      {
        path: 'rbac/users/assignments',
        name: 'RBACUserAssignments',
        component: () => import('@/views/rbac/assignments.vue'),
        meta: { titleKey: 'route.rbacUserAssignments', roles: ['SCHOOL_ADMIN'], menuGroup: 'access', hideInMenu: true, activeMenu: '/rbac' }
      },
      {
        path: 'rbac/users/list',
        name: 'RBACUserList',
        component: () => import('@/views/rbac/users.vue'),
        meta: { titleKey: 'route.rbacUserList', roles: ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'], menuGroup: 'access', hideInMenu: true, activeMenu: '/rbac' }
      },
      {
        path: 'rbac/roles',
        name: 'RBACRoles',
        component: () => import('@/views/rbac/roles.vue'),
        meta: { titleKey: 'route.rbacRoles', icon: 'Postcard', roles: ['SCHOOL_ADMIN'], menuGroup: 'access', hideInMenu: true, activeMenu: '/rbac' }
      },
      {
        path: 'rbac/permissions',
        name: 'RBACPermissions',
        component: () => import('@/views/rbac/permissions.vue'),
        meta: { titleKey: 'route.rbacPermissions', icon: 'Operation', roles: ['SCHOOL_ADMIN'], menuGroup: 'access', hideInMenu: true, activeMenu: '/rbac' }
      },
      {
        path: 'rbac/audit',
        name: 'RBACAudit',
        component: () => import('@/views/rbac/audit.vue'),
        meta: { titleKey: 'route.rbacAudit', icon: 'Document', roles: ['SCHOOL_ADMIN'], menuGroup: 'access', hideInMenu: true, activeMenu: '/rbac' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/profile/index.vue'),
        meta: { titleKey: 'route.profile', roles: ALL_ROLES }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('@/views/system/index.vue'),
        meta: { titleKey: 'route.system', icon: 'Setting', roles: ['SCHOOL_ADMIN'], menuGroup: 'access' }
      },
      {
        path: 'semester',
        name: 'Semester',
        component: () => import('@/views/semester/index.vue'),
        meta: { titleKey: 'route.semester', roles: ['SCHOOL_ADMIN'], hideInMenu: true, activeMenu: '/system' }
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

const expandRoles = (roles = []) => {
  const set = new Set()
  roles.forEach((role) => {
    const key = String(role || '').toUpperCase()
    if (!key) return
    set.add(key)
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
