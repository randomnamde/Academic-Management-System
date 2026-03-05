<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('rbac.permissions.pageTitle')" content-class="p-4 space-y-3">
      <div class="grid gap-2 md:grid-cols-[1fr_auto]">
        <el-input v-model="keyword" clearable :placeholder="t('rbac.permissions.keywordPlaceholder')" />
        <AppButton variant="secondary" @click="keyword = ''">{{ t('rbac.permissions.clearFilter') }}</AppButton>
      </div>

      <AppTable :columns="columns" :rows="filteredRows" :density="tableDensity">
        <template #cell-admin="{ row }">
          <span :class="row.admin ? 'app-tag-success' : 'app-tag-info'">{{ row.admin ? t('rbac.common.allowed') : t('rbac.common.denied') }}</span>
        </template>
        <template #cell-teacher="{ row }">
          <span :class="row.teacher ? 'app-tag-success' : 'app-tag-info'">{{ row.teacher ? t('rbac.common.allowed') : t('rbac.common.denied') }}</span>
        </template>
        <template #cell-student="{ row }">
          <span :class="row.student ? 'app-tag-success' : 'app-tag-info'">{{ row.student ? t('rbac.common.allowed') : t('rbac.common.denied') }}</span>
        </template>
      </AppTable>
    </AppCard>

    <AppCard :title="t('rbac.permissions.actionTitle')" content-class="p-4">
      <div class="flex flex-wrap gap-1.5">
        <span v-for="action in actionCodes" :key="action" class="app-tag-warning">{{ action }}</span>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import AppCard from '@/components/ui/AppCard.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppButton from '@/components/ui/AppButton.vue'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const keyword = ref('')

const matrixRows = [
  { routeKey: 'route.dashboard', routeCode: 'Dashboard', admin: true, teacher: true, student: true },
  { routeKey: 'route.student', routeCode: 'Student', admin: true, teacher: true, student: false },
  { routeKey: 'route.class', routeCode: 'Class', admin: true, teacher: true, student: false },
  { routeKey: 'route.score', routeCode: 'Score', admin: true, teacher: true, student: true },
  { routeKey: 'route.analytics', routeCode: 'Analytics', admin: true, teacher: true, student: true },
  { routeKey: 'route.rbacUsers', routeCode: 'RBACUsers', admin: true, teacher: false, student: false },
  { routeKey: 'route.rbacRoles', routeCode: 'RBACRoles', admin: true, teacher: false, student: false },
  { routeKey: 'route.rbacPermissions', routeCode: 'RBACPermissions', admin: true, teacher: false, student: false },
  { routeKey: 'route.rbacAudit', routeCode: 'RBACAudit', admin: true, teacher: false, student: false },
  { routeKey: 'route.system', routeCode: 'System', admin: true, teacher: false, student: false }
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

const columns = computed(() => [
  { key: 'route', title: t('rbac.permissions.colRoute') },
  { key: 'admin', title: t('rbac.permissions.colAdmin'), width: 120, align: 'center' },
  { key: 'teacher', title: t('rbac.permissions.colTeacher'), width: 120, align: 'center' },
  { key: 'student', title: t('rbac.permissions.colStudent'), width: 120, align: 'center' }
])

const filteredRows = computed(() => {
  const key = keyword.value.trim().toLowerCase()
  const rows = matrixRows.map((item) => ({ ...item, route: t(item.routeKey) }))
  if (!key) return rows
  return rows.filter((row) => row.route.toLowerCase().includes(key) || row.routeCode.toLowerCase().includes(key))
})
</script>
