<template>
  <CrudPageShell :title="pageTitle">
    <template #header-actions>
      <el-dropdown @command="handleExport">
        <AppButton variant="secondary">导出报表</AppButton>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="csv">导出逗号分隔文件</el-dropdown-item>
            <el-dropdown-item command="xlsx">导出电子表格文件</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <AppButton v-if="isStudent" class="ml-2" @click="openCreate">发起请假</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable style="width: 140px">
            <el-option label="待审批" value="PENDING" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">查询</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">重置</AppButton>
        </el-form-item>
      </el-form>

      <el-tabs v-if="canApprove" v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="待审批" name="pending" />
        <el-tab-pane label="全部记录" name="all" />
        <el-tab-pane v-if="canViewCc" label="抄送给我" name="cc" />
      </el-tabs>
    </template>

    <template #table>
      <el-table v-if="activeTab !== 'cc'" :data="currentRows" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column v-if="!isStudent" prop="studentName" label="学生" width="120" />
        <el-table-column v-if="!isStudent" prop="className" label="班级" width="120" />
        <el-table-column prop="courseName" label="课程" width="150" />
        <el-table-column prop="courseArrangementId" label="排课编号" width="100" />
        <el-table-column prop="leaveType" label="请假类型" width="100">
          <template #default="{ row }">{{ getLeaveTypeText(row.leaveType) }}</template>
        </el-table-column>
        <el-table-column label="请假时段" min-width="260">
          <template #default="{ row }">{{ formatRange(row) }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="请假事由" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTagType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="workflowType" label="流程" width="100">
          <template #default="{ row }">{{ getWorkflowTypeText(row.workflowType) }}</template>
        </el-table-column>
        <el-table-column prop="currentNode" label="当前节点" width="160">
          <template #default="{ row }">{{ getNodeText(row.currentNode) }}</template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" width="170">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="320" fixed="right" class-name="op-cell" label-class-name="op-header-cell">
          <template #header>
            <span class="op-header-badge">操作</span>
          </template>
          <template #default="{ row }">
            <div class="op-actions">
              <el-button link type="primary" @click="openDetail(row)">详情</el-button>
              <el-dropdown v-if="hasMoreAction(row)" @command="(cmd) => handleRowCommand(cmd, row)">
                <el-button link type="primary">
                  更多
                  <el-icon><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="isStudent && row.status === 'PENDING'" command="edit">编辑</el-dropdown-item>
                    <el-dropdown-item v-if="isStudent && row.status === 'PENDING'" command="cancel">撤销</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
              <el-button
                v-if="canApprove && row.status === 'PENDING'"
                link
                type="success"
                @click="handleApprove(row, true)"
              >
                通过
              </el-button>
              <el-button
                v-if="canApprove && row.status === 'PENDING'"
                link
                type="danger"
                @click="handleApprove(row, false)"
              >
                驳回
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <el-table v-else :data="ccData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="leaveRequestId" label="请假单ID" width="120" />
        <el-table-column prop="remark" label="抄送说明" min-width="260" />
        <el-table-column prop="createTime" label="抄送时间" width="180">
          <template #default="{ row }">{{ formatDateTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column prop="readFlag" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="Number(row.readFlag) === 1 ? 'success' : 'warning'">
              {{ Number(row.readFlag) === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetailById(row.leaveRequestId)">查看请假详情</el-button>
            <el-button v-if="Number(row.readFlag) !== 1" link type="success" @click="markCcReadRow(row)">标记已读</el-button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? '编辑请假申请' : '发起请假申请'" width="720px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="排课编号（可选）" prop="courseArrangementId">
              <el-select
                v-model="form.courseArrangementId"
                clearable
                filterable
                style="width: 100%"
                placeholder="可选：不选则按请假时段与班级流程处理"
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
            <el-form-item label="请假类型" prop="leaveType">
              <el-select v-model="form.leaveType" style="width: 100%">
                <el-option label="病假" value="SICK" />
                <el-option label="事假" value="PERSONAL" />
                <el-option label="公假" value="OFFICIAL" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始时间" prop="startTime">
              <el-date-picker
                v-model="form.startTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束时间" prop="endTime">
              <el-date-picker
                v-model="form.endTime"
                type="datetime"
                value-format="YYYY-MM-DDTHH:mm:ss"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="请假事由" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="4" maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="附件链接">
          <el-input v-model="form.attachment" placeholder="可选：填写附件 URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">取消</AppButton>
        <AppButton @click="submitForm">提交</AppButton>
      </template>
    </AppModal>

    <AppModal v-model="detailVisible" title="请假详情" width="780px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="学生">{{ detail.studentName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="班级">{{ detail.className || '-' }}</el-descriptions-item>
        <el-descriptions-item label="课程">{{ detail.courseName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="排课编号">{{ detail.courseArrangementId || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请假类型">{{ getLeaveTypeText(detail.leaveType) }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTagType(detail.status)">{{ getStatusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="流程类型">{{ getWorkflowTypeText(detail.workflowType) }}</el-descriptions-item>
        <el-descriptions-item label="当前节点">{{ getNodeText(detail.currentNode) }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ formatDateTime(detail.startTime) }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ formatDateTime(detail.endTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ detail.approverName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批时间">{{ formatDateTime(detail.approveTime) }}</el-descriptions-item>
        <el-descriptions-item label="审批备注" :span="2">{{ detail.approveRemark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="请假事由" :span="2">{{ detail.reason || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <AppButton variant="secondary" @click="detailVisible = false">关闭</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown } from '@element-plus/icons-vue'
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

const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const permissions = computed(() => store.state.userInfo?.permissions || [])
const isStudent = computed(() => role.value === 'STUDENT')
const canApprove = computed(() => canAction(role.value, 'leave:approve', permissions.value))
const canViewCc = computed(() => !isStudent.value)
const pageTitle = computed(() => {
  if (isStudent.value) return '请假申请'
  if (canApprove.value) return '请假审批'
  return '请假管理'
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

const rules = {
  leaveType: [{ required: true, message: '请选择请假类型', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  reason: [{ required: true, message: '请输入请假事由', trigger: 'blur' }]
}

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
    SICK: '病假',
    PERSONAL: '事假',
    OFFICIAL: '公假',
    OTHER: '其他'
  }
  return map[type] || type || '-'
}

const getStatusText = (status) => {
  const map = {
    PENDING: '待审批',
    APPROVED: '已通过',
    REJECTED: '已驳回'
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
    SHORT: '短假',
    LONG: '长假'
  }
  return map[type] || type || '-'
}

const getNodeText = (node) => {
  const map = {
    PENDING_HOMEROOM_REVIEW: '待班主任审批',
    PENDING_COLLEGE_REVIEW: '待学院审批',
    COMPLETED: '流程完成'
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
  return parts.length ? `${parts.join(' | ')} (编号:${item.id})` : `排课编号:${item.id}`
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
  ElMessage.success('导出任务已开始')
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
    ElMessage.warning('结束时间必须晚于开始时间')
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
    ElMessage.success('修改成功')
  } else {
    await submitLeaveRequest(payload)
    ElMessage.success('提交成功')
  }

  dialogVisible.value = false
  fetchList()
}

const handleCancel = async (row) => {
  await ElMessageBox.confirm('确认撤销该请假申请吗？', '提示', { type: 'warning' })
  await cancelLeaveRequest(row.id)
  ElMessage.success('撤销成功')
  fetchList()
}

const handleApprove = async (row, approved) => {
  const actionText = approved ? '通过' : '驳回'
  const { value } = await ElMessageBox.prompt(`请输入${actionText}备注（可选）`, `${actionText}请假申请`, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入备注'
  }).catch(() => ({ value: null }))

  if (value === null) return
  await approveLeaveRequest(row.id, approved, value || '')
  ElMessage.success(`${actionText}成功`)
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
  ElMessage.success('已标记为已读')
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

