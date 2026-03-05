<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('rbac.roles.pageTitle')" content-class="p-4">
      <p class="text-[13px] text-slatex-600">
        {{ t('rbac.roles.desc') }}
      </p>
    </AppCard>

    <section class="grid gap-3 md:grid-cols-2 xl:grid-cols-3">
      <AppCard v-for="card in roleCards" :key="card.key" :title="card.title" content-class="p-4 space-y-3">
        <div class="space-y-2">
          <div class="text-[12px] text-slatex-500">{{ t('rbac.roles.accessibleModules') }}</div>
          <div class="flex flex-wrap gap-1.5">
            <span v-for="item in card.routes" :key="item" class="app-tag-info">{{ item }}</span>
          </div>
        </div>
        <div class="space-y-2">
          <div class="text-[12px] text-slatex-500">{{ t('rbac.roles.actionPermissions') }}</div>
          <div class="flex flex-wrap gap-1.5">
            <span v-for="action in card.actions" :key="action" class="app-tag-success">{{ action }}</span>
            <span v-if="!card.actions.length" class="app-tag-info">{{ t('rbac.roles.readOnly') }}</span>
          </div>
        </div>
      </AppCard>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'
import AppCard from '@/components/ui/AppCard.vue'

const { t } = useI18n()

const roleMeta = {
  SCHOOL_ADMIN: { titleKey: 'roles.schoolAdmin' },
  COLLEGE_ADMIN: { titleKey: 'roles.collegeAdmin' },
  HOMEROOM_TEACHER: { titleKey: 'roles.homeroomTeacher' },
  COURSE_TEACHER: { titleKey: 'roles.courseTeacher' },
  STUDENT: { titleKey: 'roles.student' }
}

const routeMap = {
  SCHOOL_ADMIN: ['route.dashboard', 'route.analytics', 'route.student', 'route.teacher', 'route.class', 'route.course', 'route.college', 'route.courseArrangement', 'route.score', 'route.attendance', 'route.leaveRequest', 'route.announcement', 'route.profile', 'route.system', 'route.rbacUsers', 'route.rbacRoles', 'route.rbacPermissions', 'route.rbacAudit'],
  COLLEGE_ADMIN: ['route.dashboard', 'route.analytics', 'route.student', 'route.teacher', 'route.class', 'route.course', 'route.college', 'route.courseArrangement', 'route.score', 'route.attendance', 'route.leaveRequest', 'route.announcement', 'route.profile'],
  HOMEROOM_TEACHER: ['route.dashboard', 'route.analytics', 'route.student', 'route.class', 'route.course', 'route.courseArrangement', 'route.score', 'route.attendance', 'route.leaveRequest', 'route.announcement', 'route.profile'],
  COURSE_TEACHER: ['route.dashboard', 'route.analytics', 'route.student', 'route.class', 'route.course', 'route.courseArrangement', 'route.score', 'route.attendance', 'route.leaveRequest', 'route.announcement', 'route.profile'],
  STUDENT: ['route.dashboard', 'route.analytics', 'route.score', 'route.attendance', 'route.leaveRequest', 'route.announcement', 'route.profile']
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
    title: t(roleMeta[key].titleKey),
    routes: (routeMap[key] || []).map((routeKey) => t(routeKey)),
    actions: actionMap[key] || []
  }))
)
</script>
