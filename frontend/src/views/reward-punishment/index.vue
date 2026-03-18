<template>
  <CrudPageShell :title="t('rewardPunishment.pageTitle')">
    <template #header-actions>
      <AppButton v-if="canManageRewardPunishment" class="ml-2" @click="openCreate">{{ t('rewardPunishment.add') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-select
          v-if="canManageRewardPunishment"
          v-model="searchForm.studentId"
          clearable
          filterable
          remote
          reserve-keyword
          :remote-method="handleStudentSearch"
          :loading="studentLoading"
          :placeholder="t('rewardPunishment.student')"
          class="col-span-12 md:col-span-2"
        >
          <el-option
            v-for="item in studentOptions"
            :key="item.studentNo"
            :label="`${item.name} (${item.studentNo})`"
            :value="item.studentNo"
          />
        </el-select>
        <el-select v-model="searchForm.type" clearable :placeholder="t('rewardPunishment.type')" class="col-span-12 md:col-span-2">
          <el-option :label="t('rewardPunishment.typeReward')" value="REWARD" />
          <el-option :label="t('rewardPunishment.typePunishment')" value="PUNISHMENT" />
        </el-select>
        <el-select v-model="searchForm.status" clearable :placeholder="t('rewardPunishment.status')" class="col-span-12 md:col-span-2">
          <el-option :label="t('rewardPunishment.statusPending')" value="PENDING" />
          <el-option :label="t('rewardPunishment.statusApproved')" value="APPROVED" />
          <el-option :label="t('rewardPunishment.statusRejected')" value="REJECTED" />
        </el-select>
        <el-input v-model="searchForm.category" clearable :placeholder="t('rewardPunishment.category')" class="col-span-12 md:col-span-2" />
        <div class="app-filter-action-wrap col-span-12 md:col-span-2">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-studentName="{ row }">
          {{ row.studentName || row.studentId }}
        </template>
        <template #cell-type="{ row }">
          <AppBadge :type="typeBadge(row.type)">{{ typeLabel(row.type) }}</AppBadge>
        </template>
        <template #cell-amount="{ row }">
          {{ row.amount ? `¥${row.amount}` : '-' }}
        </template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="app-table-actions app-table-actions--start w-full">
            <button class="app-table-action" @click="openEdit(row)">{{ t('common.edit') }}</button>
            <button v-if="canApprove && row.status === 'PENDING'" class="app-table-action" @click="handleApprove(row)">{{ t('rewardPunishment.approve') }}</button>
            <button v-if="canApprove && row.status === 'PENDING'" class="app-table-action" @click="handleReject(row)">{{ t('rewardPunishment.reject') }}</button>
            <button class="app-table-action app-table-action--danger" @click="handleDelete(row)">{{ t('common.delete') }}</button>
          </div>
        </template>
      </AppTable>
    </template>

    <template #pagination>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </template>

    <el-dialog v-model="dialogVisible" :title="isEdit ? t('rewardPunishment.edit') : t('rewardPunishment.add')" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('rewardPunishment.student')" prop="studentId">
          <el-select
            v-model="form.studentId"
            filterable
            remote
            reserve-keyword
            :remote-method="handleStudentSearch"
            :loading="studentLoading"
            :placeholder="t('rewardPunishment.selectStudent')"
          >
            <el-option
              v-for="item in studentOptions"
              :key="item.studentNo"
              :label="`${item.name} (${item.studentNo})`"
              :value="item.studentNo"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('rewardPunishment.type')" prop="type">
          <el-select v-model="form.type" :placeholder="t('rewardPunishment.selectType')">
            <el-option :label="t('rewardPunishment.typeReward')" value="REWARD" />
            <el-option :label="t('rewardPunishment.typePunishment')" value="PUNISHMENT" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('rewardPunishment.category')" prop="category">
          <el-input v-model="form.category" :placeholder="t('rewardPunishment.categoryPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('rewardPunishment.reason')" prop="reason">
          <el-input v-model="form.reason" type="textarea" :rows="3" :placeholder="t('rewardPunishment.reasonPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('rewardPunishment.amount')" prop="amount">
          <el-input-number v-model="form.amount" :min="0" :precision="2" :placeholder="t('rewardPunishment.amountPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="handleSubmit">{{ t('common.submit') }}</AppButton>
      </template>
    </el-dialog>
  </CrudPageShell>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRewardPunishmentList, createRewardPunishment, updateRewardPunishment, deleteRewardPunishment, approveRewardPunishment, rejectRewardPunishment } from '@/api/reward-punishment'
import { getStudentList } from '@/api/student'
import { useUserInfo } from '@/composables/useUser'
import { usePagination } from '@/composables/usePagination'

const { t } = useI18n()
const { userInfo, isStudent, roles } = useUserInfo()
const { page, size, total, loading, tableData, handlePageChange, handleSizeChange } = usePagination()

const canManageRewardPunishment = computed(() => ['SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER'].some(r => roles.value.includes(r)))
const canApprove = computed(() => ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'].some(r => roles.value.includes(r)))

const searchForm = reactive({
  studentId: null,
  type: null,
  status: null,
  category: null
})

const studentOptions = ref([])
const studentLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  studentId: null,
  type: null,
  category: '',
  reason: '',
  amount: null
})

const rules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  category: [{ required: true, message: '请输入类别', trigger: 'blur' }],
  reason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
}

const columns = computed(() => [
  { key: 'studentName', title: t('rewardPunishment.student'), width: 120 },
  { key: 'className', title: t('rewardPunishment.class'), width: 150 },
  { key: 'type', title: t('rewardPunishment.type'), width: 120 },
  { key: 'category', title: t('rewardPunishment.category'), width: 150 },
  { key: 'reason', title: t('rewardPunishment.reason'), minWidth: 200 },
  { key: 'amount', title: t('rewardPunishment.amount'), width: 100 },
  { key: 'status', title: t('rewardPunishment.status'), width: 100 },
  { key: 'createTime', title: t('rewardPunishment.createTime'), width: 160 },
  { key: 'actions', title: t('rewardPunishment.actions'), width: 220 }
])

function typeBadge(type) {
  return type === 'REWARD' ? 'success' : 'danger'
}

function typeLabel(type) {
  return type === 'REWARD' ? t('rewardPunishment.typeReward') : t('rewardPunishment.typePunishment')
}

function statusBadgeType(status) {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { PENDING: t('rewardPunishment.statusPending'), APPROVED: t('rewardPunishment.statusApproved'), REJECTED: t('rewardPunishment.statusRejected') }
  return map[status] || status
}

async function handleStudentSearch(query) {
  if (!query) {
    studentOptions.value = []
    return
  }
  studentLoading.value = true
  try {
    const res = await getStudentList({ name: query, page: 1, size: 20 })
    studentOptions.value = res.data?.records || []
  } finally {
    studentLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getRewardPunishmentList({
      page: page.value,
      size: size.value,
      studentId: searchForm.studentId || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined,
      category: searchForm.category || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.studentId = null
  searchForm.type = null
  searchForm.status = null
  searchForm.category = null
  handleSearch()
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    studentId: null,
    type: null,
    category: '',
    reason: '',
    amount: null
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    studentId: row.studentId,
    type: row.type,
    category: row.category,
    reason: row.reason,
    amount: row.amount
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateRewardPunishment(form.id, form)
      ElMessage.success(t('common.success'))
    } else {
      await createRewardPunishment(form)
      ElMessage.success(t('common.success'))
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || t('common.error'))
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.warning'), { type: 'warning' })
  await deleteRewardPunishment(row.id)
  ElMessage.success(t('common.success'))
  fetchData()
}

async function handleApprove(row) {
  await ElMessageBox.confirm(t('rewardPunishment.approveConfirm'), t('common.warning'), { type: 'info' })
  await approveRewardPunishment(row.id)
  ElMessage.success(t('common.success'))
  fetchData()
}

async function handleReject(row) {
  const { value: reason } = await ElMessageBox.prompt(t('rewardPunishment.rejectReason'), t('rewardPunishment.reject'), {
    confirmButtonText: t('common.submit'),
    cancelButtonText: t('common.cancel'),
    inputType: 'textarea'
  })
  await rejectRewardPunishment(row.id, reason)
  ElMessage.success(t('common.success'))
  fetchData()
}

const tableDensity = ref('medium')

onMounted(() => {
  fetchData()
})
</script>
