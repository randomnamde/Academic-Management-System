<template>
  <div class="app-page space-y-3">
    <AppCard title="Role & Permission / 权限矩阵" content-class="p-4 space-y-3">
      <div class="grid gap-2 md:grid-cols-[1fr_auto]">
        <el-input v-model="keyword" clearable placeholder="按路由名过滤，例如 Score" />
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

    <AppCard title="动作权限（Action Codes）" content-class="p-4">
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
  { route: 'Dashboard', admin: true, teacher: true, student: true },
  { route: 'Student', admin: true, teacher: true, student: false },
  { route: 'Class', admin: true, teacher: true, student: false },
  { route: 'Score', admin: true, teacher: true, student: true },
  { route: 'Analytics', admin: true, teacher: true, student: true },
  { route: 'RBACUsers', admin: true, teacher: false, student: false },
  { route: 'RBACRoles', admin: true, teacher: false, student: false },
  { route: 'RBACPermissions', admin: true, teacher: false, student: false },
  { route: 'RBACAudit', admin: true, teacher: false, student: false },
  { route: 'System', admin: true, teacher: false, student: false }
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
  { key: 'route', title: 'Route Name' },
  { key: 'admin', title: 'Admin', width: 120, align: 'center' },
  { key: 'teacher', title: 'Teacher', width: 120, align: 'center' },
  { key: 'student', title: 'Student', width: 120, align: 'center' }
]

const filteredRows = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  if (!key) return matrixRows
  return matrixRows.filter((row) => row.route.toLowerCase().includes(key))
})
</script>
