<template>
  <CrudPageShell :title="pageTitle">
    <template #header-actions>
      <el-dropdown @command="handleExport">
        <AppButton variant="secondary">{{ t('leaveRequest.exportReport') }}</AppButton>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="csv">{{ t('leaveRequest.exportCsv') }}</el-dropdown-item>
            <el-dropdown-item command="xlsx">{{ t('leaveRequest.exportXlsx') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <AppButton v-if="isStudent" class="ml-2" @click="openCreate">{{ t('leaveRequest.submitLeave') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('leaveRequest.status')">
          <el-select v-model="searchForm.status" clearable style="width: 140px">
            <el-option :label="t('leaveRequest.statusPending')" value="PENDING" />
            <el-option :label="t('leaveRequest.statusApproved')" value="APPROVED" />
            <el-option :label="t('leaveRequest.statusRejected')" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>

      <el-tabs v-if="canApprove" v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane :label="t('leaveRequest.tabPending')" name="pending" />
        <el-tab-pane :label="t('leaveRequest.tabAll')" name="all" />
        <el-tab-pane v-if="canViewCc" :label="t('leaveRequest.tabCc')" name="cc" />
      </el-tabs>
    </template>

    <template #table>
      <el-table v-if="activeTab !== 'cc'" :data="currentRows" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('leaveRequest.index')" width="60" />
        <el-table-column v-if="!isStudent" prop="studentName" :label="t('leaveRequest.student')" width="120" />
        <el-table-column v-if="!isStudent" prop="className" :label="t('leaveRequest.class')" width="120" />
        <el-table-column prop="courseName" :label="t('leaveRequest.course')" width="150" />
        <el-table-column prop="courseArrangementId" :label="t('leaveRequest.arrangementId')" width="100" />
        <el-table-column prop="leaveType" :label="t('leaveRequest.leaveType')" width="100">
          <template #default="{ row }">{{ getLeaveTypeText(row.leaveType) }}</template>
        </el-table-column>
        <el-table-column :label="t('leaveRequest.leaveRange')" min-width="260">
          <template #default="{ row }">{{ formatRange(row) }}</template>
        </el-table-column>
        <el-table-column prop="reason" :label="t('leaveRequest.reason')" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" :label="t('leaveRequest.status')" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workflowType" :label="t('leaveRequest.workflow')" width="100">
          <template #default="{ row }">{{ getWorkflowTypeText(row.workflowType) }}</template>
        </el-table-column>
        <el-table-column prop="currentNode" :label="t('leaveRequest.currentNode')" width="160">
          <template #default="{ row }">{{ getNodeText(row.currentNode) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" :label="t('leaveRequest.submitTime')" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column :label="t('leaveRequest.actions')" width="320" fixed="right" class-name="op-cell" label-class-name="op-header-cell">
          <template #header>
            <span class="op-header-badge">{{ t('leaveRequest.actions') }}</span>
          </template>
          <template #default="{ row }">
            <div class="op-actions">
              <el-button link type="primary" @click="openDetail(row)">{{ t('leaveRequest.detail') }}</el-button>
              <el-dropdown v-if="hasMoreAction(row)" @command="(cmd) => handleRowCommand(cmd, row)">
                <el-button link type="primary">
                  {{ t('leaveRequest.more') }}
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isStudent && row.status === 'PENDING'" command="edit">{{ t('leaveRequest.edit') }}</el-dropdown-item>
                    <el-dropdown-item v-if="isStudent && row.status === 'PENDING'" command="cancel">{{ t('leaveRequest.cancel') }}</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button
                v-if="canApprove && row.status === 'PENDING'"
                link
                type="success"
                @click="handleApprove(row, true)"
              >
                {{ t('leaveRequest.approve') }}
              </el-button>
              <el-button
                v-if="canApprove && row.status === 'PENDING'"
                link
                type="danger"
                @click="handleApprove(row, false)"
              >
                {{ t('leaveRequest.reject') }}
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="ccData" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('leaveRequest.index')" width="60" />
        <el-table-column prop="leaveRequestId" :label="t('leaveRequest.leaveId')" width="120" />
        <el-table-column prop="remark" :label="t('leaveRequest.ccRemark')" min-width="260" />
        <el-table-column prop="createTime" :label="t('leaveRequest.ccTime')" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="readFlag" :label="t('leaveRequest.status')" width="120">
          <template #default="{ row }">
            <el-tag :type="Number(row.readFlag) === 1 ? 'success' : 'warning'">
              {{ Number(row.readFlag) === 1 ? t('leaveRequest.read') : t('leaveRequest.unread') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column :label="t('leaveRequest.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailById(row.leaveRequestId)">{{ t('leaveRequest.viewLeaveDetail') }}</el-button>
            <el-button v-if="Number(row.readFlag) !== 1" link type="success" @click="markCcReadRow(row)">{{ t('leaveRequest.markRead') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </template>

    <template #pagination>
      <el-pagination
        v-if="showPagination"
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('leaveRequest.dialogEditTitle') : t('leaveRequest.dialogCreateTitle')" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('leaveRequest.arrangementOptional')" prop="courseArrangementId">
              <el-select
                v-model="form.courseArrangementId"
                clearable
                filterable
                style="width: 100%"
                :placeholder="t('leaveRequest.arrangementOptionalPlaceholder')"
              >
                <el-option
                  v-for="item in arrangementOptions"
                  :key="item.id"
                  :label="formatArrangementLabel(item)"
                  :value="item.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('leaveRequest.leaveType')" prop="leaveType">
              <el-select v-model="form.leaveType" style="width: 100%">
                <el-option :label="t('leaveRequest.typeSick')" value="SICK" />
                <el-option :label="t('leaveRequest.typePersonal')" value="PERSONAL" />
                <el-option :label="t('leaveRequest.typeOfficial')" value="OFFICIAL" />
                <el-option :label="t('leaveRequest.typeOther')" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('leaveRequest.startTime')" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('leaveRequest.endTime')" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('leaveRequest.reason')" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item :label="t('leaveRequest.attachment')">
          <el-input v-model="form.attachment" :placeholder="t('leaveRequest.attachmentPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="submitForm">{{ t('leaveRequest.submit') }}</AppButton>
      </template>
    </AppModal>

    <AppModal v-model="detailVisible" :title="t('leaveRequest.detailTitle')" width="780px">
      <el-descriptions :column="2" border>
        <el-descriptions-item :label="t('leaveRequest.student')">{{ detail.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.class')">{{ detail.className || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.course')">{{ detail.courseName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.arrangementId')">{{ detail.courseArrangementId || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.leaveType')">{{ getLeaveTypeText(detail.leaveType) }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.status')">
          <el-tag :type="getStatusTagType(detail.status)">{{ getStatusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.workflowType')">{{ getWorkflowTypeText(detail.workflowType) }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.currentNode')">{{ getNodeText(detail.currentNode) }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.startTime')">{{ formatDateTime(detail.startTime) }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.endTime')">{{ formatDateTime(detail.endTime) }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.approver')">{{ detail.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.approveTime')">{{ formatDateTime(detail.approveTime) }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.approveRemark')" :span="2">{{ detail.approveRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item :label="t('leaveRequest.reason')" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <AppButton variant="secondary" @click="detailVisible = false">{{ t('common.close') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { canAction } from '@/permission/ability'
import {
  approveLeaveRequest,
  cancelLeaveRequest,
  exportLeaveRequestReport,
  getLeaveCcList,
  getLeaveRequestDetail,
  getLeaveRequestList,
  getPendingLeaveRequests,
  markLeaveCcRead,
  submitLeaveRequest,
  updateLeaveRequest
} from '@/api/leaveRequest'

const store = useStore()
const { t } = useI18n()

const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const permissions = computed(() => store.state.userInfo?.permissions || [])
const isStudent = computed(() => role.value === 'STUDENT')
const canApprove = computed(() => canAction(role.value, 'leave:approve', permissions.value))
const canViewCc = computed(() => !isStudent.value)
const pageTitle = computed(() => {
  if (isStudent.value) return t('leaveRequest.pageApply')
  if (canApprove.value) return t('leaveRequest.pageApprove')
  return t('leaveRequest.pageManage')
})

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const pendingData = ref([])
const ccData = ref([])
const arrangementOptions = ref([])
const activeTab = ref('pending')

const searchForm = reactive({
  status: ''
})

const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)
const detail = ref({})
const form = reactive({
  id: null,
  courseArrangementId: null,
  leaveType: 'SICK',
  startTime: '',
  endTime: '',
  reason: '',
  attachment: ''
})

const rules = computed(() => ({
  leaveType: [{ required: true, message: t('leaveRequest.leaveTypeRequired'), trigger: 'change' }],
  startTime: [{ required: true, message: t('leaveRequest.startTimeRequired'), trigger: 'change' }],
  endTime: [{ required: true, message: t('leaveRequest.endTimeRequired'), trigger: 'change' }],
  reason: [{ required: true, message: t('leaveRequest.reasonRequired'), trigger: 'blur' }]
}))

const usePendingShortcut = computed(() => canApprove.value && activeTab.value === 'pending' && !searchForm.status)

const showPagination = computed(() => !usePendingShortcut.value)
const currentRows = computed(() => {
  if (activeTab.value === 'cc') return ccData.value
  if (usePendingShortcut.value) return pendingData.value
  return tableData.value
})
const hasMoreAction = (row) => isStudent.value && row.status === 'PENDING'

const getLeaveTypeText = (type) => {
  const map = {
    SICK: t('leaveRequest.typeSick'),
    PERSONAL: t('leaveRequest.typePersonal'),
    OFFICIAL: t('leaveRequest.typeOfficial'),
    OTHER: t('leaveRequest.typeOther')
  }
  return map[type] || type || '-'
}

const getStatusText = (status) => {
  const map = {
    PENDING: t('leaveRequest.statusPending'),
    APPROVED: t('leaveRequest.statusApproved'),
    REJECTED: t('leaveRequest.statusRejected')
  }
  return map[status] || status || '-'
}

const getStatusTagType = (status) => {
  const map = {
    PENDING: 'warning',
    APPROVED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || 'info'
}

const getWorkflowTypeText = (type) => {
  const map = {
    SHORT: t('leaveRequest.workflowShort'),
    LONG: t('leaveRequest.workflowLong')
  }
  return map[type] || type || '-'
}

const getNodeText = (node) => {
  const map = {
    PENDING_HOMEROOM_REVIEW: t('leaveRequest.nodeHomeroom'),
    PENDING_COLLEGE_REVIEW: t('leaveRequest.nodeCollege'),
    COMPLETED: t('leaveRequest.nodeCompleted')
  }
  return map[node] || node || '-'
}

const formatDateTime = (value) => {
  if (!value) return '-'
  return String(value).replace('T', ' ')
}

const formatRange = (row) => `${formatDateTime(row.startTime)} ~ ${formatDateTime(row.endTime)}`

const formatArrangementLabel = (item) => {
  const parts = [item.semester, item.courseName, item.className].filter(Boolean)
  return parts.length ? `${parts.join(' | ')} (${t('leaveRequest.idLabel')}:${item.id})` : `${t('leaveRequest.arrangementId')}:${item.id}`
}

const resetForm = () => {
  Object.assign(form, {
    id: null,
    courseArrangementId: null,
    leaveType: 'SICK',
    startTime: '',
    endTime: '',
    reason: '',
    attachment: ''
  })
}

const fetchArrangementOptions = async () => {
  const res = await getCourseArrangementOptions({ status: 1 })
  arrangementOptions.value = Array.isArray(res.data) ? res.data : []
}

const handleExport = async (format) => {
  await exportLeaveRequestReport({
    status: searchForm.status || undefined,
    format
  })
  ElMessage.success(t('leaveRequest.exportStarted'))
}

const fetchList = async () => {
  loading.value = true
  try {
    if (activeTab.value === 'cc') {
      const res = await getLeaveCcList()
      ccData.value = res.data || []
      return
    }

    if (usePendingShortcut.value) {
      const res = await getPendingLeaveRequests()
      pendingData.value = res.data || []
      return
    }

    const res = await getLeaveRequestList({
      page: page.value,
      size: size.value,
      status: searchForm.status || undefined
    })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchList()
}

const handleReset = () => {
  searchForm.status = ''
  handleSearch()
}

const handleTabChange = () => {
  page.value = 1
  fetchList()
}

const openCreate = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

const openEdit = (row) => {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    id: row.id,
    courseArrangementId: row.courseArrangementId,
    leaveType: row.leaveType,
    startTime: row.startTime,
    endTime: row.endTime,
    reason: row.reason,
    attachment: row.attachment || ''
  })
  dialogVisible.value = true
}

const submitForm = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  if (new Date(form.startTime).getTime() >= new Date(form.endTime).getTime()) {
    ElMessage.warning(t('leaveRequest.endTimeAfterStart'))
    return
  }

  const payload = {
    courseArrangementId: form.courseArrangementId,
    leaveType: form.leaveType,
    startTime: form.startTime,
    endTime: form.endTime,
    reason: form.reason,
    attachment: form.attachment || ''
  }

  if (isEdit.value) {
    await updateLeaveRequest(form.id, payload)
    ElMessage.success(t('leaveRequest.updateSuccess'))
  } else {
    await submitLeaveRequest(payload)
    ElMessage.success(t('leaveRequest.submitSuccess'))
  }

  dialogVisible.value = false
  fetchList()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm(t('leaveRequest.cancelConfirm'), t('common.tip'), { type: 'warning' })
  await cancelLeaveRequest(row.id)
  ElMessage.success(t('leaveRequest.cancelSuccess'))
  fetchList()
}

const handleApprove = async (row, approved) => {
  const actionText = approved ? t('leaveRequest.approve') : t('leaveRequest.reject')
  const { value } = await ElMessageBox.prompt(t('leaveRequest.approveRemarkPrompt', { action: actionText }), t('leaveRequest.approveDialogTitle', { action: actionText }), {
    confirmButtonText: t('common.confirm'),
    cancelButtonText: t('common.cancel'),
    inputPlaceholder: t('leaveRequest.remarkPlaceholder')
  }).catch(() => ({ value: null }))

  if (value === null) return
  await approveLeaveRequest(row.id, approved, value || '')
  ElMessage.success(t('leaveRequest.approveSuccess', { action: actionText }))
  fetchList()
}

const handleRowCommand = async (command, row) => {
  if (command === 'edit') {
    openEdit(row)
    return
  }
  if (command === 'cancel') {
    await handleCancel(row)
  }
}

const openDetail = async (row) => {
  const res = await getLeaveRequestDetail(row.id)
  detail.value = res.data || {}
  detailVisible.value = true
}

const openDetailById = async (id) => {
  const res = await getLeaveRequestDetail(id)
  detail.value = res.data || {}
  detailVisible.value = true
}

const markCcReadRow = async (row) => {
  await markLeaveCcRead(row.id)
  ElMessage.success(t('leaveRequest.markReadSuccess'))
  fetchList()
}

onMounted(() => {
  if (!canApprove.value && !canViewCc.value) {
    activeTab.value = 'all'
  } else if (!canApprove.value && canViewCc.value) {
    activeTab.value = 'cc'
  }
  fetchArrangementOptions()
  fetchList()
})
</script>

<style scoped lang="scss">
.op-actions {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: nowrap;
}

:deep(.op-cell .cell) {
  white-space: normal;
  line-height: 1.6;
  padding-top: 6px;
  padding-bottom: 6px;
}

:deep(.op-cell) {
  background-color: var(--el-table-tr-bg-color, color-mix(in srgb, var(--surface-base) 88%, transparent)) !important;
}

:deep(.el-table__fixed-right .op-cell) {
  background-color: var(--el-table-tr-bg-color, color-mix(in srgb, var(--surface-base) 88%, transparent)) !important;
}

:deep(.op-header-cell) {
  background-color: var(--el-table-header-bg-color, color-mix(in srgb, var(--surface-elevated) 78%, transparent)) !important;
  color: var(--text-secondary);
}

:deep(.el-table__fixed-right .op-header-cell) {
  background-color: var(--el-table-header-bg-color, color-mix(in srgb, var(--surface-elevated) 78%, transparent)) !important;
  color: var(--text-secondary);
}

:deep(.el-table__fixed-right-patch) {
  background-color: var(--el-table-header-bg-color, color-mix(in srgb, var(--surface-elevated) 78%, transparent)) !important;
}

:deep(.op-actions .el-button + .el-button) {
  margin-left: 0;
}

.op-header-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 6px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 84%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 84%, transparent);
  color: var(--text-secondary);
  font-weight: 700;
}
</style>

