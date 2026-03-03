<template>
  <div class="app-page space-y-3">
    <AppCard title="权限中心 / 权限矩阵" content-class="p-4 space-y-3">
      <div class="grid gap-2 md:grid-cols-[1fr_auto]">
        <el-input v-model="keyword" clearable placeholder="按路由名称过滤，例如 成绩管理" />
        <AppButton variant="secondary" @click="keyword = ''">清空过滤</AppButton>
      </div>

      <AppTable :columns="columns" :rows="filteredRows" :density="tableDensity">
        <template #cell-admin="{ row }">
          <span :class="row.admin ? 'app-tag-success' : 'app-tag-info'">{{ row.admin ? '允许' : '禁止' }}</span>
        </template>
        <template #cell-teacher="{ row }">
          <span :class="row.teacher ? 'app-tag-success' : 'app-tag-info'">{{ row.teacher ? '允许' : '禁止' }}</span>
        </template>
        <template #cell-student="{ row }">
          <span :class="row.student ? 'app-tag-success' : 'app-tag-info'">{{ row.student ? '允许' : '禁止' }}</span>
        </template>
      </AppTable>
    </AppCard>

    <AppCard title="操作权限（权限编码）" content-class="p-4">
      <div class="flex flex-wrap gap-1.5">
        <span v-for="action in actionCodes" :key="action" class="app-tag-warning">{{ action }}</span>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import AppCard from '@/components/ui/AppCard.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppButton from '@/components/ui/AppButton.vue'

const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)
const keyword = ref('')

const matrixRows = [
  { route: '首页总览', routeCode: 'Dashboard', admin: true, teacher: true, student: true },
  { route: '学生管理', routeCode: 'Student', admin: true, teacher: true, student: false },
  { route: '班级管理', routeCode: 'Class', admin: true, teacher: true, student: false },
  { route: '成绩管理', routeCode: 'Score', admin: true, teacher: true, student: true },
  { route: '统计分析', routeCode: 'Analytics', admin: true, teacher: true, student: true },
  { route: '权限用户', routeCode: 'RBACUsers', admin: true, teacher: false, student: false },
  { route: '角色模板', routeCode: 'RBACRoles', admin: true, teacher: false, student: false },
  { route: '权限矩阵', routeCode: 'RBACPermissions', admin: true, teacher: false, student: false },
  { route: '审计日志', routeCode: 'RBACAudit', admin: true, teacher: false, student: false },
  { route: '系统偏好', routeCode: 'System', admin: true, teacher: false, student: false }
]

const actionCodes = [
  'score:create',
  'score:update',
  'score:delete',
  'attendance:create',
  'attendance:update',
  'attendance:delete',
  'announcement:create',
  'announcement:update',
  'announcement:delete',
  'announcement:publish',
  'leave:approve'
]

const columns = [
  { key: 'route', title: '路由名称' },
  { key: 'admin', title: '管理员', width: 120, align: 'center' },
  { key: 'teacher', title: '教师', width: 120, align: 'center' },
  { key: 'student', title: '学生', width: 120, align: 'center' }
]

const filteredRows = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return matrixRows
  return matrixRows.filter((row) => row.route.toLowerCase().includes(key) || row.routeCode.toLowerCase().includes(key))
})
</script>
