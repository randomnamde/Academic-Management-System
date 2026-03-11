<template>
  <CrudPageShell :title="t('attendance.pageTitle')">
    <template #header-actions>
      <el-dropdown @command="handleExport">
        <AppButton variant="secondary">{{ t('attendance.exportReport') }}</AppButton>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="csv">{{ t('attendance.exportCsv') }}</el-dropdown-item>
            <el-dropdown-item command="xlsx">{{ t('attendance.exportXlsx') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <AppButton v-if="canEditAttendance" class="ml-2" @click="openCreate">{{ t('attendance.addAttendance') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid attendance-filter-grid">
        <el-input
          v-if="canFilterStudent"
          v-model="searchForm.studentId"
          clearable
          inputmode="numeric"
          class="col-span-12 md:col-span-2 attendance-filter-grid__student"
          :placeholder="t('attendance.studentId')"
        />
        <el-select
          v-model="searchForm.courseArrangementId"
          clearable
          filterable
          class="col-span-12 md:col-span-3 attendance-filter-grid__course"
          :placeholder="t('attendance.course')"
        >
          <el-option
            v-for="item in arrangementOptions"
            :key="item.id"
            :label="formatArrangementLabel(item)"
            :value="item.id"
          />
        </el-select>
        <el-date-picker
          v-model="searchForm.attendanceDate"
          type="date"
          value-format="YYYY-MM-DD"
          class="col-span-12 md:col-span-2 attendance-filter-grid__date"
          :placeholder="t('attendance.date')"
        />
        <el-select
          v-model="searchForm.status"
          clearable
          class="col-span-12 md:col-span-2 attendance-filter-grid__status"
          :placeholder="t('attendance.status')"
        >
          <el-option :label="t('attendance.statusPresent')" value="PRESENT" />
          <el-option :label="t('attendance.statusAbsent')" value="ABSENT" />
          <el-option :label="t('attendance.statusLate')" value="LATE" />
          <el-option :label="t('attendance.statusLeave')" value="LEAVE" />
        </el-select>
        <div class="app-filter-action-wrap col-span-12 md:col-span-3 attendance-filter-grid__actions">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div v-if="canEditAttendance" class="app-table-actions app-table-actions--start w-full">
            <button class="app-table-action" @click="openEdit(row)">{{ t('attendance.edit') }}</button>
            <button class="app-table-action app-table-action--danger" @click="handleDelete(row)">{{ t('attendance.delete') }}</button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('attendance.dialogEditTitle') : t('attendance.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('attendance.studentId')" prop="studentId">
              <el-input v-model="form.studentId" clearable inputmode="numeric" :placeholder="t('attendance.studentId')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('attendance.arrangementId')" prop="courseArrangementId">
              <el-select
                v-model="form.courseArrangementId"
                filterable
                style="width: 100%"
                :placeholder="t('attendance.selectArrangement')"
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
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('attendance.attendanceDate')" prop="attendanceDate">
              <el-date-picker v-model="form.attendanceDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('attendance.status')" prop="status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option :label="t('attendance.statusPresent')" value="PRESENT" />
                <el-option :label="t('attendance.statusAbsent')" value="ABSENT" />
                <el-option :label="t('attendance.statusLate')" value="LATE" />
                <el-option :label="t('attendance.statusLeave')" value="LEAVE" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('attendance.checkInTime')">
              <el-time-picker v-model="form.checkInTime" value-format="HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('attendance.checkOutTime')">
              <el-time-picker v-model="form.checkOutTime" value-format="HH:mm:ss" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('attendance.remark')">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="submit">{{ t('common.save') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import {
  deleteAttendance,
  exportAttendanceReport,
  getAttendanceList,
  recordAttendance,
  updateAttendance
} from '@/api/attendance'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { canAction } from '@/permission/ability'

const store = useStore()
const { t } = useI18n()
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const permissions = computed(() => store.state.userInfo?.permissions || [])
const tableDensity = computed(() => store.getters.tableDensity)
const canEditAttendance = computed(() =>
  canAction(role.value, 'attendance:create', permissions.value)
)
const canFilterStudent = computed(() => role.value !== 'STUDENT')

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const arrangementOptions = ref([])

const columns = computed(() => {
  const base = [
    { key: 'studentId', title: t('attendance.studentId'), width: 96, align: 'left' },
    { key: 'studentName', title: t('attendance.student'), width: 120, align: 'left' },
    { key: 'courseArrangementId', title: t('attendance.arrangementId'), width: 104, align: 'left' },
    { key: 'courseName', title: t('attendance.course'), width: 150, align: 'left' },
    { key: 'attendanceDate', title: t('attendance.date'), width: 124, align: 'left' },
    { key: 'checkInTime', title: t('attendance.checkIn'), width: 108, align: 'left' },
    { key: 'checkOutTime', title: t('attendance.checkOut'), width: 108, align: 'left' },
    { key: 'status', title: t('attendance.status'), width: 110, align: 'left' }
  ]
  if (canEditAttendance.value) base.push({ key: 'actions', title: t('attendance.actions'), width: 148, align: 'left' })
  return base
})

const searchForm = reactive({
  studentId: '',
  courseArrangementId: null,
  attendanceDate: '',
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  studentId: '',
  courseArrangementId: null,
  attendanceDate: '',
  status: 'PRESENT',
  checkInTime: '',
  checkOutTime: '',
  remark: ''
})

const rules = computed(() => ({
  studentId: [{ required: true, message: t('attendance.studentIdRequired'), trigger: 'change' }],
  courseArrangementId: [{ required: true, message: t('attendance.arrangementIdRequired'), trigger: 'change' }],
  attendanceDate: [{ required: true, message: t('attendance.dateRequired'), trigger: 'change' }],
  status: [{ required: true, message: t('attendance.statusRequired'), trigger: 'change' }]
}))

function resetForm() {
  Object.assign(form, {
    id: null,
    studentId: '',
    courseArrangementId: null,
    attendanceDate: '',
    status: 'PRESENT',
    checkInTime: '',
    checkOutTime: '',
    remark: ''
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getAttendanceList({
      page: page.value,
      size: size.value,
      studentId: searchForm.studentId || undefined,
      courseArrangementId: searchForm.courseArrangementId || undefined,
      attendanceDate: searchForm.attendanceDate || undefined,
      status: searchForm.status || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function formatArrangementLabel(item) {
  const title = item.courseName || `${t('attendance.course')} #${item.id}`
  const parts = [item.className, item.semester, item.teacherName].filter(Boolean)
  return parts.length ? `${title} | ${parts.join(' | ')}` : title
}

async function fetchArrangementOptions() {
  const res = await getCourseArrangementOptions({ status: 1 })
  arrangementOptions.value = Array.isArray(res.data) ? res.data : []
}

async function handleExport(format) {
  await exportAttendanceReport({
    studentId: searchForm.studentId || undefined,
    courseArrangementId: searchForm.courseArrangementId || undefined,
    attendanceDate: searchForm.attendanceDate || undefined,
    status: searchForm.status || undefined,
    format
  })
  ElMessage.success(t('attendance.exportStarted'))
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  if (canFilterStudent.value) {
    searchForm.studentId = ''
  }
  searchForm.courseArrangementId = null
  searchForm.attendanceDate = ''
  searchForm.status = ''
  handleSearch()
}

function statusBadgeType(status) {
  if (status === 'PRESENT') return 'success'
  if (status === 'LATE') return 'warning'
  if (status === 'ABSENT') return 'danger'
  return 'info'
}

function statusLabel(status) {
  if (status === 'PRESENT') return t('attendance.statusPresent')
  if (status === 'ABSENT') return t('attendance.statusAbsent')
  if (status === 'LATE') return t('attendance.statusLate')
  if (status === 'LEAVE') return t('attendance.statusLeave')
  return '-'
}

function openCreate() {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value) {
    await updateAttendance(form.id, form)
    ElMessage.success(t('attendance.updateSuccess'))
  } else {
    await recordAttendance(form)
    ElMessage.success(t('attendance.createSuccess'))
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('attendance.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteAttendance(row.id)
  ElMessage.success(t('attendance.deleteSuccess'))
  fetchList()
}

onMounted(async () => {
  await fetchArrangementOptions()
  await fetchList()
})
</script>

<style scoped>
.attendance-filter-grid :deep(.el-date-editor) {
  width: 100%;
  display: flex;
  align-items: center;
  align-self: center;
  margin-top: 0;
}

.attendance-filter-grid__date :deep(.el-input__prefix) {
  color: var(--text-secondary);
}

.attendance-filter-grid__date :deep(.el-input__wrapper) {
  min-height: 38px;
}

@media (min-width: 1024px) {
  .attendance-filter-grid {
    display: flex;
    flex-wrap: nowrap;
    align-items: center;
    gap: 10px;
  }

  .attendance-filter-grid__student,
  .attendance-filter-grid__course,
  .attendance-filter-grid__date,
  .attendance-filter-grid__status,
  .attendance-filter-grid__actions {
    grid-column: auto / auto !important;
    flex: 0 0 auto;
  }

  .attendance-filter-grid__student {
    width: 148px;
  }

  .attendance-filter-grid__course {
    flex: 1 1 360px;
    min-width: 320px;
  }

  .attendance-filter-grid__date {
    width: 156px;
    align-self: center;
  }

  .attendance-filter-grid__status {
    width: 132px;
  }

  .attendance-filter-grid__actions {
    width: 176px;
    justify-self: end;
    justify-content: flex-end;
  }

  .attendance-filter-grid__actions :deep(.app-filter-action-bar) {
    width: 100%;
    min-width: 100%;
    justify-content: space-between;
    gap: 4px;
  }

  .attendance-filter-grid__actions :deep(.app-button) {
    min-width: 78px;
    min-height: 34px;
    padding: 0 10px;
  }
}
</style>

