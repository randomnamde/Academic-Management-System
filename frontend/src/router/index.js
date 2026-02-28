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
        meta: { title: '首页', icon: 'HomeFilled', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'ops' }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/analytics/index.vue'),
        meta: { title: '分析中心', icon: 'DataAnalysis', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'ops' }
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
        meta: { title: '教师管理', icon: 'User', roles: ['ADMIN'], menuGroup: 'teaching' }
      },
      {
        path: 'class',
        name: 'Class',
        component: () => import('@/views/class/index.vue'),
        meta: { title: '班级管理', icon: 'School', roles: ['ADMIN', 'TEACHER'], menuGroup: 'teaching' }
      },
      {
        path: 'course',
        name: 'Course',
        component: () => import('@/views/course/index.vue'),
        meta: { title: '课程管理', icon: 'Reading', roles: ['ADMIN', 'TEACHER'], menuGroup: 'teaching' }
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
        meta: { title: '成绩管理', icon: 'TrendCharts', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'process' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/attendance/index.vue'),
        meta: { title: '考勤管理', icon: 'Calendar', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'process' }
      },
      {
        path: 'leave-request',
        name: 'LeaveRequest',
        component: () => import('@/views/leave-request/index.vue'),
        meta: { title: '请假审批', icon: 'DocumentChecked', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'process' }
      },
      {
        path: 'announcement',
        name: 'Announcement',
        component: () => import('@/views/announcement/index.vue'),
        meta: { title: '通知公告', icon: 'BellFilled', roles: ['ADMIN', 'TEACHER', 'STUDENT'], menuGroup: 'process' }
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
        meta: { title: '系统设置', icon: 'Setting', roles: ['ADMIN'], menuGroup: 'system' }
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

router.beforeEach((to, from, next) => {
  NProgress.start()

  const token = Cookies.get('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const userRole = userInfo?.role

  if (to.meta.public) {
    next()
  } else if (!token) {
    next('/login')
  } else if (to.name && !['Layout', 'NotFound'].includes(String(to.name)) && !canRoute(userRole, String(to.name), userInfo?.permissions || [])) {
    next('/403')
  } else if (to.meta.roles && !to.meta.roles.includes(userRole)) {
    next('/403')
  } else {
    next()
  }
})

router.afterEach(() => {
  NProgress.done()
})

export default router
