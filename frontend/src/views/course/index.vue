<template>
  <CrudPageShell :title="t('course.pageTitle')">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('course.addCourse') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('course.courseCode')">
          <el-input v-model="searchForm.courseCode" clearable />
        </el-form-item>
        <el-form-item :label="t('course.courseName')">
          <el-input v-model="searchForm.courseName" clearable />
        </el-form-item>
        <el-form-item :label="t('course.category')">
          <el-select v-model="searchForm.category" clearable style="width: 160px">
            <el-option :label="t('course.categoryRequired')" value="REQUIRED" />
            <el-option :label="t('course.categoryElective')" value="ELECTIVE" />
            <el-option :label="t('course.categoryPractical')" value="PRACTICAL" />
          </el-select>
        </el-form-item>
        <el-form-item class="search-form__actions">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('course.index')" width="60" />
        <el-table-column prop="courseCode" :label="t('course.courseCode')" width="120" />
        <el-table-column prop="courseName" :label="t('course.courseName')" />
        <el-table-column prop="credit" :label="t('course.credit')" width="80" />
        <el-table-column prop="hours" :label="t('course.hours')" width="80" />
        <el-table-column prop="category" :label="t('course.category')" width="100" />
        <el-table-column :label="t('course.status')" width="100">
          <template #default="{ row }">
            <el-switch
              v-if="!isStudent"
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
            <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? t('course.statusEnabled') : t('course.statusDisabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isStudent" :label="t('course.actions')" width="170" fixed="right">
          <template #default="{ row }">
            <div class="app-table-inline-actions">
              <el-button link type="primary" @click="openEdit(row)">{{ t('course.edit') }}</el-button>
              <el-button link type="danger" @click="handleDelete(row)">{{ t('course.delete') }}</el-button>
            </div>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('course.dialogEditTitle') : t('course.dialogAddTitle')" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="t('course.courseCode')" prop="courseCode">
          <el-input v-model="form.courseCode" />
        </el-form-item>
        <el-form-item :label="t('course.courseName')" prop="courseName">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('course.credit')" prop="credit">
              <el-input-number v-model="form.credit" :min="0" :max="10" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('course.hours')" prop="hours">
              <el-input-number v-model="form.hours" :min="1" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('course.category')" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option :label="t('course.categoryRequired')" value="REQUIRED" />
            <el-option :label="t('course.categoryElective')" value="ELECTIVE" />
            <el-option :label="t('course.categoryPractical')" value="PRACTICAL" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('course.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item :label="t('course.status')" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ t('course.statusEnabled') }}</el-radio>
            <el-radio :label="0">{{ t('course.statusDisabled') }}</el-radio>
          </el-radio-group>
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
  createCourse,
  deleteCourse,
  getCourseList,
  updateCourse,
  updateCourseStatus
} from '@/api/course'

const store = useStore()
const { t } = useI18n()
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const isStudent = computed(() => role.value === 'STUDENT')

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  courseCode: '',
  courseName: '',
  category: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  courseCode: '',
  courseName: '',
  credit: 2,
  hours: 32,
  category: 'REQUIRED',
  description: '',
  status: 1
})

const rules = computed(() => ({
  courseCode: [{ required: true, message: t('course.courseCodeRequired'), trigger: 'blur' }],
  courseName: [{ required: true, message: t('course.courseNameRequired'), trigger: 'blur' }],
  credit: [{ required: true, message: t('course.creditRequired'), trigger: 'change' }],
  hours: [{ required: true, message: t('course.hoursRequired'), trigger: 'change' }],
  category: [{ required: true, message: t('course.categoryRequiredMsg'), trigger: 'change' }]
}))

function resetForm() {
  Object.assign(form, {
    id: null,
    courseCode: '',
    courseName: '',
    credit: 2,
    hours: 32,
    category: 'REQUIRED',
    description: '',
    status: 1
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCourseList({
      page: page.value,
      size: size.value,
      ...searchForm
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  searchForm.courseCode = ''
  searchForm.courseName = ''
  searchForm.category = ''
  handleSearch()
}

function openCreate() {
  if (isStudent.value) return
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  if (isStudent.value) return
  isEdit.value = true
  resetForm()
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  if (isStudent.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value) {
    await updateCourse(form.id, form)
    ElMessage.success(t('course.updateSuccess'))
  } else {
    await createCourse(form)
    ElMessage.success(t('course.createSuccess'))
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  if (isStudent.value) return
  await ElMessageBox.confirm(t('course.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success(t('course.deleteSuccess'))
  fetchList()
}

async function handleStatusChange(row, enabled) {
  if (isStudent.value) return
  await updateCourseStatus(row.id, enabled ? 1 : 0)
  ElMessage.success(t('course.statusUpdated'))
  fetchList()
}

onMounted(fetchList)
</script>
