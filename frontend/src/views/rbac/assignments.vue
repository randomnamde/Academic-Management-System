<template>
  <CrudPageShell :title="t('rbac.users.assignmentPageTitle')">
    <template #header-actions>
      <AppButton variant="secondary" @click="router.push('/rbac/users')">{{ t('rbac.users.backToHub') }}</AppButton>
    </template>

    <template #filters>
      <div class="grid grid-cols-12 gap-2">
        <el-input
          v-model="searchForm.username"
          clearable
          :placeholder="t('rbac.users.filterUsernamePlaceholder')"
          class="col-span-12 md:col-span-3"
        />
        <el-select
          v-model="searchForm.roleCode"
          clearable
          :placeholder="t('rbac.users.filterRole')"
          class="col-span-12 md:col-span-2"
        >
          <el-option v-for="option in roleOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-select
          v-model="searchForm.collegeId"
          clearable
          :placeholder="t('rbac.users.filterCollege')"
          class="col-span-12 md:col-span-3"
        >
          <el-option v-for="option in collegeOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <el-select
          v-model="searchForm.classId"
          clearable
          :placeholder="t('rbac.users.filterClass')"
          class="col-span-12 md:col-span-2"
        >
          <el-option v-for="option in classOptions" :key="option.value" :label="option.label" :value="option.value" />
        </el-select>
        <div class="col-span-12 flex items-center justify-end gap-2 md:col-span-2">
          <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-role="{ row }">
          <AppBadge :type="roleBadgeType(row.primaryRole)">{{ roleLabel(t, row.primaryRole) }}</AppBadge>
        </template>
        <template #cell-roles="{ row }">
          <div class="flex flex-wrap gap-1.5">
            <AppBadge v-for="code in row.roles || []" :key="`${row.id}-${code}`" :type="roleBadgeType(code)">
              {{ roleLabel(t, code) }}
            </AppBadge>
          </div>
        </template>
        <template #cell-status="{ row }">
          <AppBadge :type="row.status === 1 ? 'success' : 'info'">
            {{ row.status === 1 ? t('rbac.users.statusEnabled') : t('rbac.users.statusDisabled') }}
          </AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex justify-end gap-2">
            <button
              v-if="row.id !== currentUserId"
              class="text-[12px] text-primary-700 hover:text-primary-800"
              @click="openEditRoles(row)"
            >
              {{ t('rbac.users.editRoles') }}
            </button>
            <span v-else class="text-[12px] text-slatex-500">{{ t('rbac.users.currentUserLocked') }}</span>
          </div>
        </template>
      </AppTable>
    </template>

    <template #pagination>
      <el-pagination
        class="pagination"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchList"
        @current-change="fetchList"
      />
    </template>

    <AppModal v-model="dialogVisible" :title="t('rbac.users.roleDialogTitle')" width="680px">
      <el-form label-width="110px">
        <el-form-item :label="t('rbac.users.colUsername')">
          <span>{{ currentRow?.username || '-' }}</span>
        </el-form-item>
        <el-form-item :label="t('rbac.users.colRealName')">
          <span>{{ currentRow?.realName || '-' }}</span>
        </el-form-item>
        <el-form-item :label="t('rbac.users.colCollege')">
          <span>{{ currentRow?.collegeName || '-' }}</span>
        </el-form-item>
        <el-form-item :label="t('rbac.users.colClass')">
          <span>{{ currentRow?.classDisplayName || '-' }}</span>
        </el-form-item>
        <el-form-item :label="t('rbac.users.assignedRoles')">
          <el-select v-model="editingRoles" multiple collapse-tags collapse-tags-tooltip style="width: 100%">
            <el-option v-for="option in roleOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton :loading="saving" @click="handleSaveRoles">{{ t('common.save') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { getUserList, updateUserRoles } from '@/api/user'
import { getCollegeList } from '@/api/college'
import { getClassList } from '@/api/clazz'
import { buildRoleOptions, roleBadgeType, roleLabel } from './roleMeta'

const store = useStore()
const router = useRouter()
const { t } = useI18n()

const tableDensity = computed(() => store.getters.tableDensity)
const currentUserId = computed(() => Number(store.state.userInfo?.id || 0))
const roleOptions = computed(() => buildRoleOptions(t))

const loading = ref(false)
const saving = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const collegeOptions = ref([])
const classOptions = ref([])

const searchForm = reactive({
  username: '',
  roleCode: '',
  collegeId: null,
  classId: null
})

const columns = computed(() => [
  { key: 'username', title: t('rbac.users.colUsername'), width: 150 },
  { key: 'realName', title: t('rbac.users.colRealName'), width: 130 },
  { key: 'role', title: t('rbac.users.colRole'), width: 130 },
  { key: 'roles', title: t('rbac.users.colAssignedRoles'), width: 220 },
  { key: 'collegeName', title: t('rbac.users.colCollege'), width: 160 },
  { key: 'classDisplayName', title: t('rbac.users.colClass'), width: 180 },
  { key: 'status', title: t('rbac.users.colStatus'), width: 100, align: 'center' },
  { key: 'actions', title: t('rbac.users.colActions'), width: 120, align: 'right' }
])

const dialogVisible = ref(false)
const currentRow = ref(null)
const editingRoles = ref([])

async function loadCollegeOptions() {
  const res = await getCollegeList({ page: 1, size: 200 })
  collegeOptions.value = (res.data?.records || []).map((item) => ({
    value: item.id,
    label: item.collegeName
  }))
}

async function loadClassOptions(collegeId) {
  if (!collegeId) {
    classOptions.value = []
    return
  }
  const res = await getClassList({ page: 1, size: 200, collegeId })
  classOptions.value = (res.data?.records || []).map((item) => ({
    value: item.id,
    label: item.className
  }))
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: size.value,
      username: searchForm.username || undefined,
      roleCode: searchForm.roleCode || undefined,
      collegeId: searchForm.collegeId || undefined,
      classId: searchForm.classId || undefined
    })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  searchForm.username = ''
  searchForm.roleCode = ''
  searchForm.collegeId = null
  searchForm.classId = null
  classOptions.value = []
  handleSearch()
}

function openEditRoles(row) {
  currentRow.value = row
  editingRoles.value = Array.isArray(row.roles) ? [...row.roles] : []
  dialogVisible.value = true
}

async function handleSaveRoles() {
  if (!currentRow.value?.id) return
  if (!editingRoles.value.length) {
    ElMessage.error(t('rbac.users.rolesRequired'))
    return
  }
  saving.value = true
  try {
    await updateUserRoles(currentRow.value.id, editingRoles.value)
    ElMessage.success(t('rbac.users.rolesUpdated'))
    dialogVisible.value = false
    await fetchList()
  } finally {
    saving.value = false
  }
}

watch(
  () => searchForm.collegeId,
  async (value, oldValue) => {
    if (value === oldValue) return
    searchForm.classId = null
    await loadClassOptions(value)
  }
)

onMounted(async () => {
  await loadCollegeOptions()
  await fetchList()
})
</script>

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}
</style>
