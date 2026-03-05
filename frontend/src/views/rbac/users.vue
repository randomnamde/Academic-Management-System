<template>
  <CrudPageShell title="权限中心 / 用户管理">
    <template #header-actions>
      <AppButton variant="secondary" @click="openImportDialog">批量导入</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" clearable placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" clearable style="width: 140px">
            <el-option label="学校管理员" value="SCHOOL_ADMIN" />
            <el-option label="学院管理员" value="COLLEGE_ADMIN" />
            <el-option label="班主任" value="HOMEROOM_TEACHER" />
            <el-option label="任课教师" value="COURSE_TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">查询</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">重置</AppButton>
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
            <span class="text-[12px] text-slatex-500">{{ row.status === 1 ? '启用' : '停用' }}</span>
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

    <AppModal v-model="importDialogVisible" title="批量导入用户" width="760px">
      <el-form :model="importForm" label-width="110px">
        <el-form-item label="导入角色">
          <el-select v-model="importForm.roleType" style="width: 100%">
            <el-option label="学院管理员" value="COLLEGE_ADMIN" />
            <el-option label="班主任" value="HOMEROOM_TEACHER" />
            <el-option label="任课教师" value="COURSE_TEACHER" />
            <el-option label="学生" value="STUDENT" />
          </el-select>
        </el-form-item>

        <el-form-item label="文件类型">
          <el-radio-group v-model="importForm.fileType">
            <el-radio label="xlsx">Excel (.xlsx)</el-radio>
            <el-radio label="csv">CSV (.csv)</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="上传文件">
          <el-upload
            ref="uploadRef"
            :auto-upload="false"
            :limit="1"
            :accept="fileAccept"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :show-file-list="true"
          >
            <AppButton variant="secondary">选择文件</AppButton>
            <template #tip>
              <div class="el-upload__tip">请先下载模板并按模板字段填写，单次最多 2000 行。</div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>

      <div class="import-action-row">
        <AppButton variant="secondary" :loading="downloadingTemplate" @click="handleDownloadTemplate">下载模板</AppButton>
        <AppButton :loading="importing" @click="handleImport">开始导入</AppButton>
      </div>

      <div v-if="importResult" class="import-result-box">
        <el-alert
          :type="importResult.failedCount ? 'warning' : 'success'"
          :title="`导入完成：成功 ${importResult.successCount} 条，失败 ${importResult.failedCount} 条`"
          show-icon
          :closable="false"
        />

        <el-descriptions :column="3" border class="result-summary">
          <el-descriptions-item label="总行数">{{ importResult.totalCount }}</el-descriptions-item>
          <el-descriptions-item label="成功">{{ importResult.successCount }}</el-descriptions-item>
          <el-descriptions-item label="失败">{{ importResult.failedCount }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="importResult.failItems?.length" class="fail-block">
          <div class="fail-header">
            <span>失败明细</span>
            <AppButton variant="secondary" @click="exportFailItems">导出失败CSV</AppButton>
          </div>
          <el-table :data="importResult.failItems" max-height="240" stripe>
            <el-table-column prop="rowNumber" label="行号" width="80" />
            <el-table-column prop="message" label="失败原因" min-width="220" />
            <el-table-column label="原始数据" min-width="260">
              <template #default="{ row }">
                <span class="row-data-text">{{ formatRowData(row.rowData) }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>

      <template #footer>
        <AppButton variant="secondary" @click="importDialogVisible = false">关闭</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { downloadUserImportTemplate, getUserList, importUsers, updateUserStatus } from '@/api/user'

const store = useStore()
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

const columns = [
  { key: 'username', title: '用户名', width: 180 },
  { key: 'realName', title: '姓名', width: 160 },
  { key: 'role', title: '角色', width: 140 },
  { key: 'phone', title: '电话', width: 160 },
  { key: 'email', title: '邮箱' },
  { key: 'status', title: '状态', width: 140 }
]

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
  if (role === 'SCHOOL_ADMIN') return '学校管理员'
  if (role === 'COLLEGE_ADMIN') return '学院管理员'
  if (role === 'HOMEROOM_TEACHER') return '班主任'
  if (role === 'COURSE_TEACHER') return '任课教师'
  if (role === 'STUDENT') return '学生'
  return role
}

function roleBadgeType(role) {
  if (role === 'SCHOOL_ADMIN') return 'danger'
  if (role === 'COLLEGE_ADMIN') return 'warning'
  if (role === 'HOMEROOM_TEACHER') return 'info'
  if (role === 'COURSE_TEACHER') return 'info'
  if (role === 'STUDENT') return 'success'
  return 'info'
}

async function handleStatusChange(row, enabled) {
  await updateUserStatus(row.id, enabled ? 1 : 0)
  ElMessage.success('状态已更新')
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
    ElMessage.error('请先选择导入文件')
    return
  }
  importing.value = true
  try {
    const res = await importUsers(importForm.roleType, selectedFile.value)
    importResult.value = res.data || null
    const successCount = importResult.value?.successCount || 0
    const failedCount = importResult.value?.failedCount || 0
    ElMessage.success(`导入完成：成功 ${successCount} 条，失败 ${failedCount} 条`)
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
    ElMessage.warning('暂无失败数据')
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
