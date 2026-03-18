<template>
  <CrudPageShell :title="t('rbac.users.listPageTitle')">
    <template #header-actions>
      <div class="users-header-actions">
        <template v-if="isSchoolAdmin">
          <AppButton variant="secondary" @click="selectCurrentPage">{{ t('rbac.users.selectCurrentPage') }}</AppButton>
          <AppButton variant="secondary" @click="clearSelection">{{ t('rbac.users.clearSelection') }}</AppButton>
          <AppButton
            variant="danger"
            :loading="batchResettingPasswords"
            @click="handleBatchResetPasswords"
          >
            {{ t('rbac.users.batchResetPasswordAction', { count: selectedUserIds.length }) }}
          </AppButton>
        </template>
        <AppButton variant="secondary" @click="openImportDialog">{{ t('rbac.users.batchImport') }}</AppButton>
      </div>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-input
          v-model="searchForm.account"
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
          v-model="searchForm.collegeCode"
          clearable
          :placeholder="t('rbac.users.filterCollege')"
          :disabled="isCollegeAdmin"
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
        <div class="app-filter-action-wrap col-span-12 md:col-span-2">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <div v-if="isSchoolAdmin && selectedUserIds.length" class="selection-summary-bar">
        <div class="selection-summary-copy">
          <span class="selection-summary-title">{{ t('rbac.users.selectionSummaryTitle', { count: selectedUserIds.length }) }}</span>
          <span class="selection-summary-desc">{{ t('rbac.users.selectionSummaryDesc') }}</span>
        </div>
        <div class="selection-summary-actions">
          <AppButton variant="secondary" size="sm" @click="selectionDrawerVisible = true">{{ t('rbac.users.selectionPreviewAction') }}</AppButton>
          <AppButton variant="secondary" size="sm" @click="selectCurrentPage">{{ t('rbac.users.selectCurrentPage') }}</AppButton>
          <AppButton variant="secondary" size="sm" @click="clearSelection">{{ t('rbac.users.clearSelection') }}</AppButton>
          <AppButton
            variant="danger"
            size="sm"
            :loading="batchResettingPasswords"
            @click="handleBatchResetPasswords"
          >
            {{ t('rbac.users.batchResetPasswordAction', { count: selectedUserIds.length }) }}
          </AppButton>
        </div>
      </div>

      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #header-select>
          <el-checkbox
            :model-value="isCurrentPageFullySelected"
            :indeterminate="isCurrentPagePartiallySelected"
            @change="toggleCurrentPageSelection"
          />
        </template>
        <template #cell-select="{ row }">
          <el-checkbox
            :model-value="selectedUserIds.includes(row.id)"
            @change="(checked) => toggleRowSelection(row, checked)"
          />
        </template>
        <template #cell-account="{ row }">
          <span class="text-[13px] font-medium text-slatex-800">{{ row.account || '-' }}</span>
        </template>
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
          <div class="flex items-center gap-2">
            <el-switch :model-value="row.status === 1" @change="(val) => handleStatusChange(row, val)" />
            <span class="text-[12px] text-slatex-500">{{ row.status === 1 ? t('rbac.users.statusEnabled') : t('rbac.users.statusDisabled') }}</span>
          </div>
        </template>
        <template #cell-actions="{ row }">
          <div class="app-table-actions">
            <AppButton
              size="sm"
              variant="secondary"
              :loading="resettingUserId === row.id"
              @click="handleResetUserPassword(row)"
            >
              {{ t('rbac.users.resetPasswordAction') }}
            </AppButton>
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
      <el-form :model="importForm" label-position="top">
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

    <AppModal
      v-model="verifyPasswordDialogVisible"
      :title="verifyPasswordDialogTitle"
      width="520px"
    >
      <div class="verify-password-dialog">
        <p class="verify-password-desc">{{ verifyPasswordDialogMessage }}</p>
        <el-form class="verify-password-form" label-position="top" @submit.prevent>
          <el-form-item class="verify-password-form-item" :label="t('rbac.users.verifyPasswordLabel')">
            <el-input
              v-model="verifyPasswordForm.operatorPassword"
              type="password"
              show-password
              :placeholder="t('rbac.users.verifyPasswordPlaceholder')"
              @keyup.enter="submitVerifiedReset"
            />
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <AppButton variant="secondary" @click="closeVerifyPasswordDialog">{{ t('common.cancel') }}</AppButton>
        <AppButton
          variant="danger"
          :loading="verifySubmitting"
          @click="submitVerifiedReset"
        >
          {{ verifyPasswordConfirmButtonLabel }}
        </AppButton>
      </template>
    </AppModal>

    <el-drawer
      v-model="selectionDrawerVisible"
      :title="t('rbac.users.selectionDrawerTitle', { count: selectedUserIds.length })"
      size="420px"
      append-to-body
    >
      <div class="selection-drawer">
        <div class="selection-drawer-head">
          <p class="selection-drawer-desc">{{ t('rbac.users.selectionDrawerDesc') }}</p>
          <AppButton variant="secondary" size="sm" @click="clearSelection">{{ t('rbac.users.clearSelection') }}</AppButton>
        </div>

        <el-input
          v-model="selectionSearchKeyword"
          clearable
          :placeholder="t('rbac.users.selectionSearchPlaceholder')"
        />

        <div v-if="groupedSelectedUsers.length" class="selection-drawer-groups">
          <section v-for="group in groupedSelectedUsers" :key="group.roleKey" class="selection-group">
            <div class="selection-group-head">
              <p class="selection-group-title">{{ group.roleLabel }}</p>
              <span class="selection-group-count">{{ t('rbac.users.selectionGroupCount', { count: group.users.length }) }}</span>
            </div>

            <div class="selection-drawer-list">
              <div v-for="user in group.users" :key="user.id" class="selection-user-card">
                <div class="selection-user-main">
                  <p class="selection-user-account">{{ user.account || '-' }}</p>
                  <p class="selection-user-name">{{ user.realName || '-' }}</p>
                </div>
                <AppButton variant="text" size="sm" @click="removeSelectedUser(user.id)">{{ t('rbac.users.selectionRemoveAction') }}</AppButton>
                <div class="selection-user-meta">
                  <span>{{ t('rbac.users.selectionMetaRole', { role: user.primaryRoleLabel }) }}</span>
                  <span>{{ t('rbac.users.selectionMetaCollege', { college: user.collegeName || '-' }) }}</span>
                  <span>{{ t('rbac.users.selectionMetaClass', { className: user.classDisplayName || '-' }) }}</span>
                </div>
              </div>
            </div>
          </section>
        </div>

        <div v-else class="selection-drawer-empty">
          {{ selectionSearchKeyword ? t('rbac.users.selectionSearchEmpty') : t('rbac.users.selectionDrawerEmpty') }}
        </div>
      </div>
    </el-drawer>
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
import { downloadUserImportTemplate, getUserList, importUsers, resetBatchUserPasswords, resetUserPassword, updateUserStatus } from '@/api/user'
import { getCollegeList } from '@/api/college'
import { getClassList } from '@/api/clazz'
import { buildRoleOptions, roleBadgeType, roleLabel } from './roleMeta'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const currentRole = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const isCollegeAdmin = computed(() => currentRole.value === 'COLLEGE_ADMIN')
const isSchoolAdmin = computed(() => currentRole.value === 'SCHOOL_ADMIN')

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const collegeOptions = ref([])
const classOptions = ref([])

const searchForm = reactive({
  account: '',
  roleCode: '',
  collegeCode: null,
  classId: null
})

const roleOptions = computed(() => buildRoleOptions(t))
const importRoleOptions = computed(() => buildRoleOptions(t).filter((item) => item.value !== 'SCHOOL_ADMIN'))
const selectedUserIds = ref([])
const selectedUserSnapshots = ref({})
const batchResettingPasswords = ref(false)
const selectionDrawerVisible = ref(false)
const selectionSearchKeyword = ref('')
const currentPageSelectableIds = computed(() => tableData.value.map((item) => item.id).filter(Boolean))
const selectedUserIdSet = computed(() => new Set(selectedUserIds.value))
const currentPageSelectedCount = computed(() => currentPageSelectableIds.value.filter((id) => selectedUserIdSet.value.has(id)).length)
const isCurrentPageFullySelected = computed(() => currentPageSelectableIds.value.length > 0 && currentPageSelectedCount.value === currentPageSelectableIds.value.length)
const isCurrentPagePartiallySelected = computed(() => currentPageSelectedCount.value > 0 && currentPageSelectedCount.value < currentPageSelectableIds.value.length)
const selectedUsersPreview = computed(() => selectedUserIds.value.map((id) => selectedUserSnapshots.value[id]).filter(Boolean))
const filteredSelectedUsers = computed(() => {
  const keyword = selectionSearchKeyword.value.trim().toLowerCase()
  if (!keyword) {
    return selectedUsersPreview.value
  }
  return selectedUsersPreview.value.filter((user) => {
    const haystack = [
      user.account,
      user.realName,
      user.primaryRoleLabel,
      user.collegeName,
      user.classDisplayName
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()
    return haystack.includes(keyword)
  })
})
const groupedSelectedUsers = computed(() => {
  const groups = new Map()
  filteredSelectedUsers.value.forEach((user) => {
    const roleKey = user.primaryRole || 'UNKNOWN'
    if (!groups.has(roleKey)) {
      groups.set(roleKey, {
        roleKey,
        roleLabel: user.primaryRoleLabel || t('rbac.users.selectionUnknownRole'),
        users: []
      })
    }
    groups.get(roleKey).users.push(user)
  })
  return [...groups.values()]
})

const columns = computed(() => {
  const base = [
    ...(isSchoolAdmin.value ? [{ key: 'select', title: t('rbac.users.selectionColumn'), width: 64, align: 'center' }] : []),
    { key: 'account', title: t('rbac.users.colUsername'), width: 190 },
    { key: 'realName', title: t('rbac.users.colRealName'), width: 120 },
    { key: 'role', title: t('rbac.users.colRole'), width: 156 },
    { key: 'roles', title: t('rbac.users.colAssignedRoles'), width: 220 },
    { key: 'collegeName', title: t('rbac.users.colCollege'), width: 160 },
    { key: 'classDisplayName', title: t('rbac.users.colClass'), width: 180 },
    { key: 'phone', title: t('rbac.users.colPhone'), width: 150 },
    { key: 'email', title: t('rbac.users.colEmail') },
    { key: 'status', title: t('rbac.users.colStatus'), width: 140 }
  ]
  if (isSchoolAdmin.value) {
    base.push({ key: 'actions', title: t('rbac.users.colActions'), width: 148, align: 'left' })
  }
  return base
})

const importDialogVisible = ref(false)
const resettingUserId = ref(null)
const verifyPasswordDialogVisible = ref(false)
const verifySubmitting = ref(false)
const verifyPasswordMode = ref('single')
const verifyTargetUser = ref(null)
const verifyPasswordForm = reactive({
  operatorPassword: ''
})
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
const verifyPasswordDialogTitle = computed(() => (
  verifyPasswordMode.value === 'batch'
    ? t('rbac.users.batchResetPasswordVerifyTitle')
    : t('rbac.users.resetPasswordVerifyTitle')
))
const verifyPasswordDialogMessage = computed(() => {
  if (verifyPasswordMode.value === 'batch') {
    return t('rbac.users.batchResetPasswordVerifyMessage', { count: selectedUserIds.value.length })
  }
  return t('rbac.users.resetPasswordVerifyMessage', { account: verifyTargetUser.value?.account || '-' })
})
const verifyPasswordConfirmButtonLabel = computed(() => (
  verifyPasswordMode.value === 'batch'
    ? t('rbac.users.batchResetPasswordAction', { count: selectedUserIds.value.length })
    : t('rbac.users.resetPasswordAction')
))

async function loadCollegeOptions() {
  const res = await getCollegeList({ page: 1, size: 200 })
  collegeOptions.value = (res.data?.records || []).map((item) => ({
    value: item.id,
    label: item.collegeName
  }))
  if (isCollegeAdmin.value && collegeOptions.value.length) {
    searchForm.collegeCode = searchForm.collegeCode || collegeOptions.value[0].value
  }
}

async function loadClassOptions(collegeCode) {
  if (!collegeCode) {
    classOptions.value = []
    return
  }
  const res = await getClassList({ page: 1, size: 200, collegeCode })
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
      account: searchForm.account || undefined,
      roleCode: searchForm.roleCode || undefined,
      collegeCode: searchForm.collegeCode || undefined,
      classId: searchForm.classId || undefined
    })
    tableData.value = res.data?.records || []
    tableData.value.forEach((item) => {
      if (item?.id && selectedUserIdSet.value.has(item.id)) {
        selectedUserSnapshots.value[item.id] = buildUserSnapshot(item)
      }
    })
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  clearSelection()
  fetchList()
}

function handleReset() {
  searchForm.account = ''
  searchForm.roleCode = ''
  searchForm.classId = null
  if (!isCollegeAdmin.value) {
    searchForm.collegeCode = null
  }
  clearSelection()
  loadClassOptions(searchForm.collegeCode)
  handleSearch()
}

function buildUserSnapshot(row) {
  if (!row?.id) return null
  return {
    id: row.id,
    account: row.account,
    realName: row.realName,
    primaryRole: row.primaryRole,
    primaryRoleLabel: roleLabel(t, row.primaryRole),
    collegeName: row.collegeName,
    classDisplayName: row.classDisplayName
  }
}

function toggleRowSelection(row, checked) {
  const userId = row?.id
  if (!userId) return
  if (checked) {
    if (!selectedUserIds.value.includes(userId)) {
      selectedUserIds.value = [...selectedUserIds.value, userId]
    }
    selectedUserSnapshots.value[userId] = buildUserSnapshot(row)
    return
  }
  selectedUserIds.value = selectedUserIds.value.filter((id) => id !== userId)
  delete selectedUserSnapshots.value[userId]
}

function selectCurrentPage() {
  const merged = new Set(selectedUserIds.value)
  tableData.value.forEach((row) => {
    if (!row?.id) return
    merged.add(row.id)
    selectedUserSnapshots.value[row.id] = buildUserSnapshot(row)
  })
  selectedUserIds.value = [...merged]
}

function clearSelection() {
  selectedUserIds.value = []
  selectedUserSnapshots.value = {}
  selectionDrawerVisible.value = false
  selectionSearchKeyword.value = ''
}

function toggleCurrentPageSelection(checked) {
  if (checked) {
    selectCurrentPage()
    return
  }
  tableData.value.forEach((row) => {
    if (row?.id) {
      delete selectedUserSnapshots.value[row.id]
    }
  })
  const currentPageIdSet = new Set(currentPageSelectableIds.value)
  selectedUserIds.value = selectedUserIds.value.filter((id) => !currentPageIdSet.has(id))
}

function removeSelectedUser(userId) {
  if (!userId) return
  selectedUserIds.value = selectedUserIds.value.filter((id) => id !== userId)
  delete selectedUserSnapshots.value[userId]
  if (!selectedUserIds.value.length) {
    selectionDrawerVisible.value = false
  }
}

async function handleStatusChange(row, enabled) {
  await updateUserStatus(row.id, enabled ? 1 : 0)
  ElMessage.success(t('rbac.users.statusUpdated'))
  fetchList()
}

async function handleResetUserPassword(row) {
  verifyPasswordMode.value = 'single'
  verifyTargetUser.value = row
  verifyPasswordForm.operatorPassword = ''
  verifyPasswordDialogVisible.value = true
}

async function handleBatchResetPasswords() {
  if (!selectedUserIds.value.length) {
    ElMessage.warning(t('rbac.users.batchResetNoSelection'))
    return
  }

  verifyPasswordMode.value = 'batch'
  verifyTargetUser.value = null
  verifyPasswordForm.operatorPassword = ''
  verifyPasswordDialogVisible.value = true
}

function closeVerifyPasswordDialog() {
  verifyPasswordDialogVisible.value = false
  verifySubmitting.value = false
  verifyPasswordForm.operatorPassword = ''
  verifyTargetUser.value = null
}

async function submitVerifiedReset() {
  const operatorPassword = verifyPasswordForm.operatorPassword.trim()
  if (!operatorPassword) {
    ElMessage.warning(t('rbac.users.verifyPasswordRequired'))
    return
  }

  verifySubmitting.value = true
  if (verifyPasswordMode.value === 'single') {
    const target = verifyTargetUser.value
    if (!target?.id) {
      verifySubmitting.value = false
      return
    }
    resettingUserId.value = target.id
    try {
      await resetUserPassword(target.id, operatorPassword)
      ElMessage.success(t('rbac.users.resetPasswordSuccess', { account: target.account || '-' }))
      closeVerifyPasswordDialog()
    } finally {
      resettingUserId.value = null
      verifySubmitting.value = false
    }
    return
  }

  batchResettingPasswords.value = true
  try {
    const res = await resetBatchUserPasswords(selectedUserIds.value, operatorPassword)
    ElMessage.success(t('rbac.users.batchResetPasswordSuccess', { count: res.data ?? 0 }))
    clearSelection()
    closeVerifyPasswordDialog()
  } finally {
    batchResettingPasswords.value = false
    verifySubmitting.value = false
  }
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
  () => searchForm.collegeCode,
  async (value, oldValue) => {
    if (value === oldValue) return
    searchForm.classId = null
    await loadClassOptions(value)
  }
)

watch(
  () => importForm.fileType,
  () => {
    selectedFile.value = null
    uploadRef.value?.clearFiles?.()
  }
)

onMounted(async () => {
  await loadCollegeOptions()
  await loadClassOptions(searchForm.collegeCode)
  await fetchList()
})
</script>

<style scoped>
.users-header-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.selection-summary-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  margin-bottom: 12px;
  padding: 14px 16px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--accent-500) 20%, transparent);
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 10%, transparent), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 84%, transparent));
}

.selection-summary-copy {
  display: grid;
  gap: 4px;
}

.selection-summary-title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.selection-summary-desc {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.selection-summary-actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 8px;
}

.selection-drawer {
  display: grid;
  gap: 16px;
}

.verify-password-dialog {
  display: grid;
  gap: 14px;
  width: 100%;
}

.verify-password-desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.8;
  color: var(--text-secondary);
}

.verify-password-form {
  width: 100%;
}

.verify-password-form-item {
  margin-bottom: 0;
}

.verify-password-form-item :deep(.el-form-item__content),
.verify-password-form-item :deep(.el-input) {
  width: 100%;
}

.selection-drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.selection-drawer-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.75;
  color: var(--text-secondary);
}

.selection-drawer-list {
  display: grid;
  gap: 12px;
}

.selection-drawer-groups {
  display: grid;
  gap: 18px;
}

.selection-group {
  display: grid;
  gap: 12px;
}

.selection-group-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.selection-group-title,
.selection-group-count {
  margin: 0;
}

.selection-group-title {
  font-size: 13px;
  font-weight: 700;
  color: var(--text-primary);
}

.selection-group-count {
  font-size: 12px;
  color: var(--text-secondary);
}

.selection-user-card {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px 12px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 74%, transparent);
  background: color-mix(in srgb, var(--surface-base) 86%, transparent);
}

.selection-user-main {
  display: grid;
  gap: 4px;
}

.selection-user-account,
.selection-user-name {
  margin: 0;
  color: var(--text-primary);
}

.selection-user-account {
  font-size: 14px;
  font-weight: 700;
}

.selection-user-name {
  font-size: 12px;
  color: var(--text-secondary);
}

.selection-user-meta {
  grid-column: 1 / -1;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.selection-user-meta span {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-elevated) 88%, transparent);
  color: var(--text-secondary);
  font-size: 12px;
}

.selection-drawer-empty {
  display: grid;
  place-items: center;
  min-height: 180px;
  border-radius: 18px;
  border: 1px dashed color-mix(in srgb, var(--panel-border) 74%, transparent);
  color: var(--text-secondary);
  font-size: 13px;
}

.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}

.import-action-row {
  margin-top: 14px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 14px;
  border-top: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
}

.import-result-box {
  margin-top: 16px;
  display: grid;
  gap: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  border-radius: 18px;
  padding: 14px;
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 88%, transparent), color-mix(in srgb, var(--surface-elevated) 82%, transparent));
}

.result-summary {
  margin-top: 4px;
}

.fail-block {
  display: grid;
  gap: 10px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 68%, transparent);
  border-radius: 16px;
  padding: 12px;
  background: color-mix(in srgb, var(--surface-elevated) 74%, transparent);
}

.fail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 10px;
}

.fail-header span {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.row-data-text {
  color: var(--text-secondary);
  font-size: 12px;
  line-height: 1.7;
}

@media (max-width: 767px) {
  .users-header-actions {
    justify-content: stretch;
  }

  .selection-summary-bar {
    flex-direction: column;
    align-items: stretch;
  }

  .selection-summary-actions {
    justify-content: stretch;
  }

  .selection-summary-actions :deep(.app-button) {
    width: 100%;
  }

  .selection-drawer-head {
    flex-direction: column;
  }

  .users-header-actions :deep(.app-button) {
    width: 100%;
  }
}
</style>

