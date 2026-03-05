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
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item v-if="canFilterStudent" :label="t('attendance.studentId')">
          <el-input-number v-model="searchForm.studentId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item :label="t('attendance.arrangementId')">
          <el-select
            v-model="searchForm.courseArrangementId"
            clearable
            filterable
            style="width: 260px"
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
        <el-form-item :label="t('attendance.date')">
          <el-date-picker v-model="searchForm.attendanceDate" type="date" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item :label="t('attendance.status')">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option :label="t('attendance.statusPresent')" value="PRESENT" />
            <el-option :label="t('attendance.statusAbsent')" value="ABSENT" />
            <el-option :label="t('attendance.statusLate')" value="LATE" />
            <el-option :label="t('attendance.statusLeave')" value="LEAVE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('attendance.index')" width="60" />
        <el-table-column prop="studentId" :label="t('attendance.studentId')" width="90" />
        <el-table-column prop="studentName" :label="t('attendance.student')" width="120" />
        <el-table-column prop="courseArrangementId" :label="t('attendance.arrangementId')" width="90" />
        <el-table-column prop="courseName" :label="t('attendance.course')" width="140" />
        <el-table-column prop="attendanceDate" :label="t('attendance.date')" width="120" />
        <el-table-column prop="checkInTime" :label="t('attendance.checkIn')" width="100" />
        <el-table-column prop="checkOutTime" :label="t('attendance.checkOut')" width="100" />
        <el-table-column prop="status" :label="t('attendance.status')" width="100" />
        <el-table-column v-if="canEditAttendance" :label="t('attendance.actions')" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">{{ t('attendance.edit') }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">{{ t('attendance.delete') }}</el-button>
          </template>
        </el-table-column>
      </el-table>
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
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('attendance.studentId')" prop="studentId">
              <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
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

const searchForm = reactive({
  studentId: null,
  courseArrangementId: null,
  attendanceDate: '',
  status: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  studentId: null,
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
    studentId: null,
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
  const parts = [item.semester, item.courseName, item.className].filter(Boolean)
  return parts.length ? `${parts.join(' | ')} (${t('attendance.idLabel')}:${item.id})` : `${t('attendance.arrangementId')}:${item.id}`
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
    searchForm.studentId = null
  }
  searchForm.courseArrangementId = null
  searchForm.attendanceDate = ''
  searchForm.status = ''
  handleSearch()
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

