<template>
  <div class="app-page space-y-3">
    <AppCard title="Role & Permission / 角色模板" content-class="p-4">
      <p class="text-[13px] text-slatex-600">
        角色模板采用最小授权原则。当前界面用于查看权限边界，后续可扩展为可编辑模板并对接后端策略引擎。
      </p>
    </AppCard>

    <section class="grid gap-3 md:grid-cols-3">
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
  ADMIN: { title: '管理员' },
  TEACHER: { title: '教师' },
  STUDENT: { title: '学生' }
}

const routeMap = {
  ADMIN: ['Dashboard', 'Student', 'Class', 'Score', 'Statistics', 'RBAC', 'System'],
  TEACHER: ['Dashboard', 'Student', 'Class', 'Score', 'Statistics'],
  STUDENT: ['Dashboard', 'Score', 'Statistics']
}

const actionMap = {
  ADMIN: ['score:create', 'attendance:update', 'announcement:publish', 'leave:approve'],
  TEACHER: ['score:create', 'attendance:update', 'announcement:publish', 'leave:approve'],
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
