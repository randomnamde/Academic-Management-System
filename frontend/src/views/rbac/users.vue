<template>
  <CrudPageShell :title="t('rbac.users.pageTitle')">
    <template #header-actions>
      <AppButton variant="secondary" @click="openImportDialog">{{ t('rbac.users.batchImport') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('rbac.users.filterUsername')">
          <el-input v-model="searchForm.username" clearable :placeholder="t('rbac.users.filterUsernamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('rbac.users.filterRole')">
          <el-select v-model="searchForm.role" clearable style="width: 140px">
            <el-option
              v-for="option in roleOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-role="{ row }">
          <AppBadge :type="roleBadgeType(row.role)">{{ roleLabel(row.role) }}</AppBadge>
        </template>
        <template #cell-status="{ row }">
          <div class="flex items-center gap-2">
            <el-switch :model-value="row.status === 1" @change="(val) => handleStatusChange(row, val)" />
            <span class="text-[12px] text-slatex-500">{{ row.status === 1 ? t('rbac.users.statusEnabled') : t('rbac.users.statusDisabled') }}</span>
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

    <AppModal v-model="importDialogVisible" :title="t('rbac.users.importDialogTitle')" width="760px">
      <el-form :model="importForm" label-width="120px">
        <el-form-item :label="t('rbac.users.importRole')">
          <el-select v-model="importForm.roleType" style="width: 100%">
            <el-option
              v-for="option in importRoleOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('rbac.users.fileType')">
          <el-radio-group v-model="importForm.fileType">
            <el-radio label="xlsx">Excel (.xlsx)</el-radio>
            <el-radio label="csv">CSV (.csv)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item :label="t('rbac.users.uploadFile')">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :accept="fileAccept"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :show-file-list="true"
          >
            <AppButton variant="secondary">{{ t('rbac.users.selectFile') }}</AppButton>
            <template #tip>
              <div class="el-upload__tip">{{ t('rbac.users.uploadTip') }}</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <div class="import-action-row">
        <AppButton variant="secondary" :loading="downloadingTemplate" @click="handleDownloadTemplate">{{ t('rbac.users.downloadTemplate') }}</AppButton>
        <AppButton :loading="importing" @click="handleImport">{{ t('rbac.users.startImport') }}</AppButton>
      </div>

      <div v-if="importResult" class="import-result-box">
        <el-alert
          :type="importResult.failedCount ? 'warning' : 'success'"
          :title="t('rbac.users.importFinished', { success: importResult.successCount, failed: importResult.failedCount })"
          show-icon
          :closable="false"
        />

        <el-descriptions :column="3" border class="result-summary">
          <el-descriptions-item :label="t('rbac.users.totalRows')">{{ importResult.totalCount }}</el-descriptions-item>
          <el-descriptions-item :label="t('rbac.users.successRows')">{{ importResult.successCount }}</el-descriptions-item>
          <el-descriptions-item :label="t('rbac.users.failedRows')">{{ importResult.failedCount }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="importResult.failItems?.length" class="fail-block">
          <div class="fail-header">
            <span>{{ t('rbac.users.failDetails') }}</span>
            <AppButton variant="secondary" @click="exportFailItems">{{ t('rbac.users.exportFailCsv') }}</AppButton>
          </div>
          <el-table :data="importResult.failItems" max-height="240" stripe>
            <el-table-column prop="rowNumber" :label="t('rbac.users.colRowNumber')" width="80" />
            <el-table-column prop="message" :label="t('rbac.users.colFailReason')" min-width="220" />
            <el-table-column :label="t('rbac.users.colRawData')" min-width="260">
              <template #default="{ row }">
                <span class="row-data-text">{{ formatRowData(row.rowData) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <AppButton variant="secondary" @click="importDialogVisible = false">{{ t('common.close') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { downloadUserImportTemplate, getUserList, importUsers, updateUserStatus } from '@/api/user'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  username: '',
  role: ''
})

const roleOptions = computed(() => [
  { value: 'SCHOOL_ADMIN', label: t('roles.schoolAdmin') },
  { value: 'COLLEGE_ADMIN', label: t('roles.collegeAdmin') },
  { value: 'HOMEROOM_TEACHER', label: t('roles.homeroomTeacher') },
  { value: 'COURSE_TEACHER', label: t('roles.courseTeacher') },
  { value: 'STUDENT', label: t('roles.student') }
])

const importRoleOptions = computed(() => [
  { value: 'COLLEGE_ADMIN', label: t('roles.collegeAdmin') },
  { value: 'HOMEROOM_TEACHER', label: t('roles.homeroomTeacher') },
  { value: 'COURSE_TEACHER', label: t('roles.courseTeacher') },
  { value: 'STUDENT', label: t('roles.student') }
])

const columns = computed(() => [
  { key: 'username', title: t('rbac.users.colUsername'), width: 180 },
  { key: 'realName', title: t('rbac.users.colRealName'), width: 160 },
  { key: 'role', title: t('rbac.users.colRole'), width: 140 },
  { key: 'phone', title: t('rbac.users.colPhone'), width: 160 },
  { key: 'email', title: t('rbac.users.colEmail') },
  { key: 'status', title: t('rbac.users.colStatus'), width: 140 }
])

const importDialogVisible = ref(false)
const importForm = reactive({
  roleType: 'STUDENT',
  fileType: 'xlsx'
})
const uploadRef = ref()
const selectedFile = ref(null)
const importing = ref(false)
const downloadingTemplate = ref(false)
const importResult = ref(null)

const fileAccept = computed(() => (importForm.fileType === 'csv' ? '.csv' : '.xlsx'))

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: size.value,
      username: searchForm.username || undefined,
      role: searchForm.role || undefined
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
  searchForm.role = ''
  handleSearch()
}

function roleLabel(role) {
  if (role === 'SCHOOL_ADMIN') return t('roles.schoolAdmin')
  if (role === 'COLLEGE_ADMIN') return t('roles.collegeAdmin')
  if (role === 'HOMEROOM_TEACHER') return t('roles.homeroomTeacher')
  if (role === 'COURSE_TEACHER') return t('roles.courseTeacher')
  if (role === 'STUDENT') return t('roles.student')
  return role
}

function roleBadgeType(role) {
  if (role === 'SCHOOL_ADMIN') return 'danger'
  if (role === 'COLLEGE_ADMIN') return 'warning'
  if (role === 'HOMEROOM_TEACHER' || role === 'COURSE_TEACHER') return 'info'
  if (role === 'STUDENT') return 'success'
  return 'info'
}

async function handleStatusChange(row, enabled) {
  await updateUserStatus(row.id, enabled ? 1 : 0)
  ElMessage.success(t('rbac.users.statusUpdated'))
  fetchList()
}

function openImportDialog() {
  importDialogVisible.value = true
  importResult.value = null
  selectedFile.value = null
  uploadRef.value?.clearFiles?.()
}

function handleFileChange(file) {
  selectedFile.value = file?.raw || null
}

function handleFileRemove() {
  selectedFile.value = null
}

async function handleDownloadTemplate() {
  downloadingTemplate.value = true
  try {
    await downloadUserImportTemplate(importForm.roleType, importForm.fileType)
  } finally {
    downloadingTemplate.value = false
  }
}

async function handleImport() {
  if (!selectedFile.value) {
    ElMessage.error(t('rbac.users.selectFileFirst'))
    return
  }
  importing.value = true
  try {
    const res = await importUsers(importForm.roleType, selectedFile.value)
    importResult.value = res.data || null
    const successCount = importResult.value?.successCount || 0
    const failedCount = importResult.value?.failedCount || 0
    ElMessage.success(t('rbac.users.importCompleted', { success: successCount, failed: failedCount }))
    if (successCount > 0) {
      fetchList()
    }
  } finally {
    importing.value = false
  }
}

function formatRowData(rowData) {
  if (!rowData || typeof rowData !== 'object') return '-'
  return Object.entries(rowData)
    .map(([key, value]) => `${key}=${value || ''}`)
    .join('; ')
}

function exportFailItems() {
  const failItems = importResult.value?.failItems || []
  if (!failItems.length) {
    ElMessage.warning(t('rbac.users.noFailData'))
    return
  }

  const headers = ['rowNumber', 'roleType', 'message', 'rowData']
  const lines = [headers.join(',')]
  for (const item of failItems) {
    lines.push(
      [
        csvEscape(String(item.rowNumber || '')),
        csvEscape(String(item.roleType || '')),
        csvEscape(String(item.message || '')),
        csvEscape(formatRowData(item.rowData))
      ].join(',')
    )
  }

  const blob = new Blob(['\uFEFF' + lines.join('\n')], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const anchor = document.createElement('a')
  anchor.href = url
  anchor.download = `import-fail-${Date.now()}.csv`
  document.body.appendChild(anchor)
  anchor.click()
  document.body.removeChild(anchor)
  URL.revokeObjectURL(url)
}

function csvEscape(value) {
  const text = value == null ? '' : String(value)
  if (text.includes(',') || text.includes('"') || text.includes('\n')) {
    return `"${text.replace(/"/g, '""')}"`
  }
  return text
}

watch(
  () => importForm.fileType,
  () => {
    selectedFile.value = null
    uploadRef.value?.clearFiles?.()
  }
)

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}

.import-action-row {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.import-result-box {
  margin-top: 12px;
  display: grid;
  gap: 12px;
}

.result-summary {
  margin-top: 4px;
}

.fail-block {
  display: grid;
  gap: 8px;
}

.fail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.row-data-text {
  color: var(--sms-slatex-600);
  font-size: 12px;
}
</style>
