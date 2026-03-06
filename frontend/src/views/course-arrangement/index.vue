<template>
  <CrudPageShell :title="t('courseArrangement.pageTitle')">
    <template #header-actions>
      <AppButton @click="openCreate">{{ t('courseArrangement.addArrangement') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('courseArrangement.college')">
          <el-select
            v-model="searchForm.collegeId"
            clearable
            filterable
            style="width: 180px"
            :disabled="collegeLocked"
            :placeholder="t('courseArrangement.selectCollege')"
            @change="handleSearchCollegeChange"
          >
            <el-option v-for="option in collegeOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.courseId')">
          <el-select
            v-model="searchForm.courseId"
            clearable
            filterable
            style="width: 200px"
            :placeholder="t('courseArrangement.selectCourse')"
          >
            <el-option v-for="option in courseOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.teacherId')">
          <el-select
            v-model="searchForm.teacherId"
            clearable
            filterable
            style="width: 220px"
            :placeholder="t('courseArrangement.selectTeacher')"
          >
            <el-option v-for="option in searchTeacherOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('courseArrangement.classId')">
          <el-select
            v-model="searchForm.classId"
            clearable
            filterable
            style="width: 220px"
            :placeholder="t('courseArrangement.selectClass')"
          >
            <el-option v-for="option in searchClassOptions" :key="option.value" :label="option.label" :value="option.value" />
          </el-select>
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
        <el-table-column prop="arrangementCode" :label="t('courseArrangement.arrangementCode')" width="180" />
        <el-table-column prop="courseName" :label="t('courseArrangement.course')" min-width="150" />
        <el-table-column prop="teacherName" :label="t('courseArrangement.teacher')" width="140" />
        <el-table-column prop="className" :label="t('courseArrangement.class')" width="140" />
        <el-table-column prop="semester" :label="t('courseArrangement.semester')" width="140" />
        <el-table-column prop="schedule" :label="t('courseArrangement.schedule')" min-width="170" />
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('courseArrangement.dialogEditTitle') : t('courseArrangement.dialogAddTitle')" width="760px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.college')" prop="collegeId">
              <el-select
                v-model="form.collegeId"
                filterable
                style="width: 100%"
                :disabled="collegeLocked"
                :placeholder="t('courseArrangement.selectCollege')"
                @change="handleFormCollegeChange"
              >
                <el-option v-for="option in collegeOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.courseId')" prop="courseId">
              <el-select
                v-model="form.courseId"
                filterable
                style="width: 100%"
                :placeholder="t('courseArrangement.selectCourse')"
              >
                <el-option v-for="option in courseOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.teacherId')" prop="teacherId">
              <el-select
                v-model="form.teacherId"
                filterable
                style="width: 100%"
                :placeholder="t('courseArrangement.selectTeacher')"
              >
                <el-option v-for="option in formTeacherOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('courseArrangement.classId')" prop="classId">
              <el-select
                v-model="form.classId"
                filterable
                style="width: 100%"
                :placeholder="t('courseArrangement.selectClass')"
              >
                <el-option v-for="option in formClassOptions" :key="option.value" :label="option.label" :value="option.value" />
              </el-select>
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
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { getCollegeList } from '@/api/college'
import { getCourseList } from '@/api/course'
import {
  createCourseArrangement,
  deleteCourseArrangement,
  getCourseArrangementList,
  updateCourseArrangement
} from '@/api/courseArrangement'
import { getClassList } from '@/api/clazz'
import { getTeacherList } from '@/api/teacher'

const store = useStore()
const { t } = useI18n()
const userInfo = computed(() => store.state.userInfo || {})
const role = computed(() => userInfo.value?.primaryRole || userInfo.value?.role || '')
const isSchoolAdmin = computed(() => role.value === 'SCHOOL_ADMIN')
const isCollegeAdmin = computed(() => role.value === 'COLLEGE_ADMIN')
const isTeacherRole = computed(() => role.value === 'HOMEROOM_TEACHER' || role.value === 'COURSE_TEACHER')
const collegeLocked = computed(() => !isSchoolAdmin.value)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const collegeOptions = ref([])
const courseOptions = ref([])
const searchTeacherOptions = ref([])
const searchClassOptions = ref([])
const formTeacherOptions = ref([])
const formClassOptions = ref([])

const searchForm = reactive({
  collegeId: null,
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
  collegeId: null,
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
  collegeId: [{ required: true, message: t('courseArrangement.collegeRequired'), trigger: 'change' }],
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

function mapCollegeOption(item) {
  return {
    value: item.id,
    label: `${item.collegeName}${item.collegeCode ? ` (${item.collegeCode})` : ''}`
  }
}

function mapCourseOption(item) {
  return {
    value: item.id,
    label: `${item.courseName}${item.courseCode ? ` (${item.courseCode})` : ''}`
  }
}

function mapTeacherOption(item) {
  return {
    value: item.id,
    label: `${item.name}${item.teacherNo ? ` (${item.teacherNo})` : ''}`
  }
}

function mapClassOption(item) {
  return {
    value: item.id,
    label: `${item.className}${item.classCode ? ` (${item.classCode})` : ''}`
  }
}

function getLockedCollegeOption() {
  if (!userInfo.value?.collegeId) return null
  return {
    value: userInfo.value.collegeId,
    label: userInfo.value.collegeName || `${t('courseArrangement.college')} #${userInfo.value.collegeId}`
  }
}

function getDefaultCollegeId() {
  if (collegeLocked.value) {
    return userInfo.value?.collegeId || collegeOptions.value[0]?.value || null
  }
  return null
}

function resetForm() {
  Object.assign(form, {
    id: null,
    collegeId: getDefaultCollegeId(),
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

async function ensureUserScopeInfo() {
  if (!collegeLocked.value || userInfo.value?.collegeId) return
  await store.dispatch('getUserInfo').catch(() => null)
}

async function loadCollegeOptions() {
  if (isSchoolAdmin.value || isCollegeAdmin.value) {
    const res = await getCollegeList({ page: 1, size: 200 })
    collegeOptions.value = (res.data?.records || []).map(mapCollegeOption)
    return
  }
  const currentCollegeOption = getLockedCollegeOption()
  collegeOptions.value = currentCollegeOption ? [currentCollegeOption] : []
}

async function loadCourseOptions() {
  const res = await getCourseList({ page: 1, size: 500 })
  courseOptions.value = (res.data?.records || []).map(mapCourseOption)
}

async function loadTeacherOptions(collegeId, targetRef) {
  if (!collegeId) {
    targetRef.value = []
    return
  }
  const res = await getTeacherList({ page: 1, size: 500, collegeId })
  targetRef.value = (res.data?.records || []).map(mapTeacherOption)
}

async function loadClassOptions(collegeId, targetRef) {
  if (!collegeId) {
    targetRef.value = []
    return
  }
  const res = await getClassList({ page: 1, size: 500, collegeId })
  targetRef.value = (res.data?.records || []).map(mapClassOption)
}

async function syncSearchScopedOptions() {
  await Promise.all([
    loadTeacherOptions(searchForm.collegeId, searchTeacherOptions),
    loadClassOptions(searchForm.collegeId, searchClassOptions)
  ])
}

async function syncFormScopedOptions() {
  await Promise.all([
    loadTeacherOptions(form.collegeId, formTeacherOptions),
    loadClassOptions(form.collegeId, formClassOptions)
  ])
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCourseArrangementList({
      page: page.value,
      size: size.value,
      collegeId: searchForm.collegeId || undefined,
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

async function handleSearchCollegeChange() {
  searchForm.teacherId = null
  searchForm.classId = null
  await syncSearchScopedOptions()
}

async function handleFormCollegeChange() {
  form.teacherId = null
  form.classId = null
  await syncFormScopedOptions()
}

async function handleReset() {
  searchForm.collegeId = getDefaultCollegeId()
  searchForm.courseId = null
  searchForm.teacherId = null
  searchForm.classId = null
  searchForm.semester = ''
  await syncSearchScopedOptions()
  handleSearch()
}

async function openCreate() {
  isEdit.value = false
  resetForm()
  await syncFormScopedOptions()
  dialogVisible.value = true
}

async function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, {
    id: row.id,
    collegeId: row.collegeId || getDefaultCollegeId(),
    courseId: row.courseId,
    teacherId: row.teacherId,
    classId: row.classId,
    semester: row.semester || '',
    schedule: row.schedule || '',
    room: row.room || '',
    capacity: row.capacity || 50,
    status: typeof row.status === 'number' ? row.status : Number(row.status || 1)
  })
  await syncFormScopedOptions()
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    collegeId: form.collegeId,
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

async function initializePage() {
  await ensureUserScopeInfo()
  await loadCollegeOptions()
  await loadCourseOptions()

  const defaultCollegeId = getDefaultCollegeId()
  if (defaultCollegeId) {
    searchForm.collegeId = defaultCollegeId
  }

  await syncSearchScopedOptions()
  resetForm()
  await syncFormScopedOptions()
  await fetchList()
}

onMounted(initializePage)
</script>
