<template>
  <CrudPageShell :title="t('courseArrangement.pageTitle')">
    <template #header-actions>
      <AppButton @click="openCreate">{{ t('courseArrangement.addArrangement') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('courseArrangement.courseId')">
          <el-input-number v-model="searchForm.courseId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item :label="t('courseArrangement.teacherId')">
          <el-input-number v-model="searchForm.teacherId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item :label="t('courseArrangement.classId')">
          <el-input-number v-model="searchForm.classId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item :label="t('courseArrangement.semester')">
          <el-input v-model="searchForm.semester" clearable :placeholder="t('courseArrangement.semesterPlaceholder')" />
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('courseArrangement.index')" width="60" />
        <el-table-column prop="courseName" :label="t('courseArrangement.course')" min-width="130" />
        <el-table-column prop="teacherName" :label="t('courseArrangement.teacher')" width="120" />
        <el-table-column prop="className" :label="t('courseArrangement.class')" width="120" />
        <el-table-column prop="semester" :label="t('courseArrangement.semester')" width="130" />
        <el-table-column prop="schedule" :label="t('courseArrangement.schedule')" min-width="150" />
        <el-table-column prop="room" :label="t('courseArrangement.room')" width="100" />
        <el-table-column :label="t('courseArrangement.people')" width="120">
          <template #default="{ row }">{{ row.enrolledCount || 0 }}/{{ row.capacity || 0 }}</template>
        </el-table-column>
        <el-table-column :label="t('courseArrangement.status')" width="90">
          <template #default="{ row }">
            <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
          </template>
        </el-table-column>
        <el-table-column :label="t('courseArrangement.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">{{ t('courseArrangement.edit') }}</el-button>
            <el-button link type="danger" @click="handleDelete(row)">{{ t('courseArrangement.delete') }}</el-button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('courseArrangement.dialogEditTitle') : t('courseArrangement.dialogAddTitle')" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item :label="t('courseArrangement.courseId')" prop="courseId">
              <el-input-number v-model="form.courseId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('courseArrangement.teacherId')" prop="teacherId">
              <el-input-number v-model="form.teacherId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('courseArrangement.classId')" prop="classId">
              <el-input-number v-model="form.classId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.semester')" prop="semester">
              <el-input v-model="form.semester" :placeholder="t('courseArrangement.semesterPlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.room')">
              <el-input v-model="form.room" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('courseArrangement.schedule')" prop="schedule">
          <el-input v-model="form.schedule" :placeholder="t('courseArrangement.schedulePlaceholder')" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.capacity')" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.status')">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">{{ t('courseArrangement.statusEnabled') }}</el-radio>
                <el-radio :label="0">{{ t('courseArrangement.statusDisabled') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import {
  createCourseArrangement,
  deleteCourseArrangement,
  getCourseArrangementList,
  updateCourseArrangement
} from '@/api/courseArrangement'

const { t } = useI18n()

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  courseId: null,
  teacherId: null,
  classId: null,
  semester: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  courseId: null,
  teacherId: null,
  classId: null,
  semester: '',
  schedule: '',
  room: '',
  capacity: 50,
  status: 1
})

const rules = computed(() => ({
  courseId: [{ required: true, message: t('courseArrangement.courseIdRequired'), trigger: 'change' }],
  teacherId: [{ required: true, message: t('courseArrangement.teacherIdRequired'), trigger: 'change' }],
  classId: [{ required: true, message: t('courseArrangement.classIdRequired'), trigger: 'change' }],
  semester: [{ required: true, message: t('courseArrangement.semesterRequired'), trigger: 'blur' }],
  schedule: [{ required: true, message: t('courseArrangement.scheduleRequired'), trigger: 'blur' }],
  capacity: [{ required: true, message: t('courseArrangement.capacityRequired'), trigger: 'change' }]
}))

function statusLabel(status) {
  return Number(status) === 1 ? t('courseArrangement.statusEnabled') : t('courseArrangement.statusDisabled')
}

function statusBadgeType(status) {
  return Number(status) === 1 ? 'success' : 'info'
}

function resetForm() {
  Object.assign(form, {
    id: null,
    courseId: null,
    teacherId: null,
    classId: null,
    semester: '',
    schedule: '',
    room: '',
    capacity: 50,
    status: 1
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCourseArrangementList({
      page: page.value,
      size: size.value,
      courseId: searchForm.courseId || undefined,
      teacherId: searchForm.teacherId || undefined,
      classId: searchForm.classId || undefined,
      semester: searchForm.semester || undefined
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
  searchForm.courseId = null
  searchForm.teacherId = null
  searchForm.classId = null
  searchForm.semester = ''
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

  const payload = {
    courseId: form.courseId,
    teacherId: form.teacherId,
    classId: form.classId,
    semester: form.semester,
    schedule: form.schedule,
    room: form.room,
    capacity: form.capacity,
    status: form.status
  }

  if (isEdit.value) {
    await updateCourseArrangement(form.id, payload)
    ElMessage.success(t('courseArrangement.updateSuccess'))
  } else {
    await createCourseArrangement(payload)
    ElMessage.success(t('courseArrangement.createSuccess'))
  }

  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('courseArrangement.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteCourseArrangement(row.id)
  ElMessage.success(t('courseArrangement.deleteSuccess'))
  fetchList()
}

onMounted(fetchList)
</script>

