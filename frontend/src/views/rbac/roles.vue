<template>
  <div class="app-page space-y-3">
    <AppCard title="权限中心 / 角色模板" content-class="p-4">
      <p class="text-[13px] text-slatex-600">
        角色模板采用最小授权原则。当前界面用于查看权限边界，后续可扩展为可编辑模板并对接后端策略引擎。
      </p>
    </AppCard>

    <section class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
      <AppCard v-for="card in roleCards" :key="card.key" :title="card.title" content-class="p-4 space-y-3">
        <div class="space-y-2">
          <div class="text-[12px] text-slatex-500">可访问模块</div>
          <div class="flex flex-wrap gap-1.5">
            <span v-for="item in card.routes" :key="item" class="app-tag-info">{{ item }}</span>
          </div>
        </div>
        <div class="space-y-2">
          <div class="text-[12px] text-slatex-500">动作权限</div>
          <div class="flex flex-wrap gap-1.5">
            <span v-for="action in card.actions" :key="action" class="app-tag-success">{{ action }}</span>
            <span v-if="!card.actions.length" class="app-tag-info">只读</span>
          </div>
        </div>
      </AppCard>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import AppCard from '@/components/ui/AppCard.vue'

const roleMeta = {
  SCHOOL_ADMIN: { title: '学校管理员' },
  COLLEGE_ADMIN: { title: '学院管理员' },
  HOMEROOM_TEACHER: { title: '班主任' },
  COURSE_TEACHER: { title: '任课教师' },
  STUDENT: { title: '学生' }
}

const routeMap = {
  SCHOOL_ADMIN: ['首页', '统计分析', '学生管理', '教师管理', '班级管理', '课程管理', '学院管理', '排课管理', '成绩管理', '考勤管理', '请假审批', '通知公告', '个人中心', '系统偏好', '权限用户', '角色模板', '权限矩阵', '审计日志'],
  COLLEGE_ADMIN: ['首页', '统计分析', '学生管理', '教师管理', '班级管理', '课程管理', '学院管理', '排课管理', '成绩管理', '考勤管理', '请假审批', '通知公告', '个人中心'],
  HOMEROOM_TEACHER: ['首页', '统计分析', '学生管理', '班级管理', '课程管理', '排课管理', '成绩管理', '考勤管理', '请假审批', '通知公告', '个人中心'],
  COURSE_TEACHER: ['首页', '统计分析', '学生管理', '班级管理', '课程管理', '排课管理', '成绩管理', '考勤管理', '请假审批', '通知公告', '个人中心'],
  STUDENT: ['首页', '统计分析', '成绩管理', '考勤管理', '请假审批', '通知公告', '个人中心']
}

const actionMap = {
  SCHOOL_ADMIN: ['*:*', '*'],
  COLLEGE_ADMIN: ['score:create', 'score:update', 'score:delete', 'attendance:create', 'attendance:update', 'attendance:delete', 'announcement:create', 'announcement:update', 'announcement:delete', 'announcement:publish', 'leave:approve'],
  HOMEROOM_TEACHER: ['score:create', 'score:update', 'score:delete', 'attendance:create', 'attendance:update', 'attendance:delete', 'announcement:create', 'announcement:update', 'announcement:delete', 'announcement:publish', 'leave:approve'],
  COURSE_TEACHER: ['score:create', 'score:update', 'score:delete', 'attendance:create', 'attendance:update', 'attendance:delete', 'announcement:create', 'announcement:update', 'announcement:delete', 'announcement:publish'],
  STUDENT: []
}

const roleCards = computed(() =>
  Object.keys(roleMeta).map((key) => ({
    key,
    title: roleMeta[key].title,
    routes: routeMap[key] || [],
    actions: actionMap[key] || []
  }))
)
</script>
