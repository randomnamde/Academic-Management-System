<template>
  <CrudPageShell :title="t('exam.pageTitle')">
    <template #header-actions>
      <AppButton v-if="canManageExam" class="ml-2" @click="openCreate">{{ t('exam.addExam') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-select
          v-model="searchForm.courseArrangementId"
          clearable
          filterable
          :placeholder="t('exam.courseArrangement')"
          class="col-span-12 md:col-span-3"
        >
          <el-option
            v-for="item in arrangementOptions"
            :key="item.id"
            :label="`${item.courseName} (${item.semester})`"
            :value="item.id"
          />
        </el-select>
        <el-select v-model="searchForm.examType" clearable :placeholder="t('exam.examType')" class="col-span-12 md:col-span-2">
          <el-option :label="t('exam.typeMidterm')" value="MIDTERM" />
          <el-option :label="t('exam.typeFinal')" value="FINAL" />
          <el-option :label="t('exam.typeMakeup')" value="MAKEUP" />
          <el-option :label="t('exam.typeRetake')" value="RETAKE" />
        </el-select>
        <el-select v-model="searchForm.status" clearable :placeholder="t('exam.status')" class="col-span-12 md:col-span-2">
          <el-option :label="t('exam.statusScheduled')" value="SCHEDULED" />
          <el-option :label="t('exam.statusOngoing')" value="ONGOING" />
          <el-option :label="t('exam.statusCompleted')" value="COMPLETED" />
          <el-option :label="t('exam.statusCancelled')" value="CANCELLED" />
        </el-select>
        <el-date-picker
          v-model="searchForm.examDate"
          type="date"
          :placeholder="t('exam.examDate')"
          value-format="YYYY-MM-DD"
          class="col-span-12 md:col-span-2"
        />
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
        <template #cell-examType="{ row }">
          <AppBadge :type="examTypeBadge(row.examType)">{{ examTypeLabel(row.examType) }}</AppBadge>
        </template>
        <template #cell-examDate="{ row }">
          {{ row.examDate }} {{ row.startTime }}-{{ row.endTime }}
        </template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
        </template>
        <template #cell-capacity="{ row }">
          {{ row.enrolledCount || 0 }} / {{ row.capacity || '-' }}
        </template>
        <template #cell-actions="{ row }">
          <div v-if="canManageExam" class="app-table-actions app-table-actions--start w-full">
            <button class="app-table-action" @click="openEdit(row)">{{ t('common.edit') }}</button>
            <el-dropdown @command="handleStatusChange($event, row)">
              <button class="app-table-action">{{ t('exam.changeStatus') }}</button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="SCHEDULED">{{ t('exam.statusScheduled') }}</el-dropdown-item>
                  <el-dropdown-item command="ONGOING">{{ t('exam.statusOngoing') }}</el-dropdown-item>
                  <el-dropdown-item command="COMPLETED">{{ t('exam.statusCompleted') }}</el-dropdown-item>
                  <el-dropdown-item command="CANCELLED">{{ t('exam.statusCancelled') }}</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? t('exam.editExam') : t('exam.addExam')" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('exam.examCode')" prop="examCode">
          <el-input v-model="form.examCode" :placeholder="t('exam.examCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('exam.courseArrangement')" prop="courseArrangementId">
          <el-select v-model="form.courseArrangementId" filterable :placeholder="t('exam.selectCourse')">
            <el-option
              v-for="item in arrangementOptions"
              :key="item.id"
              :label="`${item.courseName} - ${item.teacherName} (${item.semester})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('exam.examType')" prop="examType">
          <el-select v-model="form.examType" :placeholder="t('exam.selectExamType')">
            <el-option :label="t('exam.typeMidterm')" value="MIDTERM" />
            <el-option :label="t('exam.typeFinal')" value="FINAL" />
            <el-option :label="t('exam.typeMakeup')" value="MAKEUP" />
            <el-option :label="t('exam.typeRetake')" value="RETAKE" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('exam.examDate')" prop="examDate">
          <el-date-picker v-model="form.examDate" type="date" value-format="YYYY-MM-DD" :placeholder="t('exam.selectDate')" />
        </el-form-item>
        <el-form-item :label="t('exam.time')" prop="timeRange">
          <el-time-picker
            v-model="form.startTime"
            :placeholder="t('exam.startTime')"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 120px"
          />
          <span class="mx-2">-</span>
          <el-time-picker
            v-model="form.endTime"
            :placeholder="t('exam.endTime')"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 120px"
          />
        </el-form-item>
        <el-form-item :label="t('exam.room')" prop="room">
          <el-input v-model="form.room" :placeholder="t('exam.roomPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('exam.capacity')" prop="capacity">
          <el-input-number v-model="form.capacity" :min="1" :max="500" />
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
import { getExamList, createExam, updateExam, deleteExam, updateExamStatus } from '@/api/exam'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { useUserInfo } from '@/composables/useUser'
import { usePagination } from '@/composables/usePagination'

const { t } = useI18n()
const { roles } = useUserInfo()
const { page, size, total, loading, tableData, handlePageChange, handleSizeChange } = usePagination()

const canManageExam = computed(() => ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'].some(r => roles.value.includes(r)))

const searchForm = reactive({
  courseArrangementId: null,
  examType: null,
  status: null,
  examDate: null
})

const arrangementOptions = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  examCode: '',
  courseArrangementId: null,
  examType: null,
  examDate: null,
  startTime: null,
  endTime: null,
  room: '',
  capacity: 50
})

const rules = {
  examCode: [{ required: true, message: '请输入考试编号', trigger: 'blur' }],
  courseArrangementId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  examType: [{ required: true, message: '请选择考试类型', trigger: 'change' }],
  examDate: [{ required: true, message: '请选择考试日期', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  room: [{ required: true, message: '请输入考场', trigger: 'blur' }]
}

const columns = computed(() => [
  { key: 'examCode', title: t('exam.examCode'), width: 120 },
  { key: 'courseName', title: t('exam.course'), width: 180 },
  { key: 'teacherName', title: t('exam.teacher'), width: 100 },
  { key: 'semester', title: t('exam.semester'), width: 100 },
  { key: 'examType', title: t('exam.examType'), width: 100 },
  { key: 'examDate', title: t('exam.examDateTime'), width: 200 },
  { key: 'room', title: t('exam.room'), width: 100 },
  { key: 'capacity', title: t('exam.capacity'), width: 100 },
  { key: 'status', title: t('exam.status'), width: 100 },
  { key: 'actions', title: t('exam.actions'), width: 200 }
])

function examTypeBadge(type) {
  const map = { MIDTERM: 'warning', FINAL: 'success', MAKEUP: 'info', RETAKE: 'danger' }
  return map[type] || 'info'
}

function examTypeLabel(type) {
  const map = { MIDTERM: t('exam.typeMidterm'), FINAL: t('exam.typeFinal'), MAKEUP: t('exam.typeMakeup'), RETAKE: t('exam.typeRetake') }
  return map[type] || type
}

function statusBadgeType(status) {
  const map = { SCHEDULED: 'info', ONGOING: 'warning', COMPLETED: 'success', CANCELLED: 'danger' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { SCHEDULED: t('exam.statusScheduled'), ONGOING: t('exam.statusOngoing'), COMPLETED: t('exam.statusCompleted'), CANCELLED: t('exam.statusCancelled') }
  return map[status] || status
}

async function fetchArrangementOptions() {
  const res = await getCourseArrangementOptions()
  arrangementOptions.value = Array.isArray(res.data) ? res.data : []
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getExamList({
      page: page.value,
      size: size.value,
      courseArrangementId: searchForm.courseArrangementId || undefined,
      examType: searchForm.examType || undefined,
      status: searchForm.status || undefined,
      examDate: searchForm.examDate || undefined
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
  searchForm.courseArrangementId = null
  searchForm.examType = null
  searchForm.status = null
  searchForm.examDate = null
  handleSearch()
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    examCode: '',
    courseArrangementId: null,
    examType: null,
    examDate: null,
    startTime: null,
    endTime: null,
    room: '',
    capacity: 50
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    examCode: row.examCode,
    courseArrangementId: row.courseArrangementId,
    examType: row.examType,
    examDate: row.examDate,
    startTime: row.startTime,
    endTime: row.endTime,
    room: row.room,
    capacity: row.capacity
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateExam(form.id, form)
      ElMessage.success(t('common.success'))
    } else {
      await createExam(form)
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
  await deleteExam(row.id)
  ElMessage.success(t('common.success'))
  fetchData()
}

async function handleStatusChange(status, row) {
  await updateExamStatus(row.id, status)
  ElMessage.success(t('common.success'))
  fetchData()
}

const tableDensity = ref('medium')

onMounted(() => {
  fetchArrangementOptions()
  fetchData()
})
</script>
