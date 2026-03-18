<template>
  <CrudPageShell :title="t('student.pageTitle')">
    <template #header-actions>
      <AppButton @click="handleAdd">{{ t('student.addStudent') }}</AppButton>
    </template>

    <template #filters>
      <CrudFilterBar :fields="searchFields" :model="searchForm" :update-field="updateSearchField">
        <template #actions>
          <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
        </template>
      </CrudFilterBar>
    </template>

    <template #table>
      <AppDataTable :columns="columns" :rows="studentList" :loading="loading" :density="tableDensity">
        <template #cell-gender="{ row }">{{ row.gender === 'MALE' ? t('student.genderMale') : t('student.genderFemale') }}</template>
        <template #cell-collegeName="{ row }">{{ row.collegeName || getCollegeNameByCode(row.collegeCode) || '-' }}</template>
        <template #cell-majorName="{ row }">{{ row.majorName || getMajorNameByCode(row.majorCode) || '-' }}</template>
        <template #cell-className="{ row }">{{ row.className || getClassNameById(row.classId) || '-' }}</template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ getStatusText(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <AppActionGroup>
            <AppButton variant="secondary" size="sm" @click="handleEdit(row)">{{ t('student.edit') }}</AppButton>
            <AppButton variant="secondary" size="sm" @click="handleViewScore(row)">{{ t('student.viewScore') }}</AppButton>
            <AppButton variant="danger" size="sm" @click="handleDelete(row)">{{ t('student.delete') }}</AppButton>
          </AppActionGroup>
        </template>
      </AppDataTable>
    </template>

    <template #pagination>
      <AppPagination
        :page="page"
        :size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        :show-page-size="true"
        @update:page="handlePageChange"
        @update:size="handleSizeChange"
      />
    </template>

    <template #dialogs>
      <CrudFormDialog
        v-model="dialogVisible"
        :title="dialogTitle"
        :width="'760px'"
        :fields="dialogFields"
        :model="form"
        :rules="rules"
        :update-field="updateFormField"
        :loading="submitting"
        :submit-text="t('common.confirm')"
        :cancel-text="t('common.cancel')"
        @submit="handleSubmit"
      />
    </template>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import CrudFilterBar from '@/components/crud/CrudFilterBar.vue'
import CrudFormDialog from '@/components/crud/CrudFormDialog.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppActionGroup from '@/components/ui/AppActionGroup.vue'
import AppDataTable from '@/components/ui/AppDataTable.vue'
import AppPagination from '@/components/ui/AppPagination.vue'
import { createStudent, deleteStudent, getNextStudentNo, getStudentList, updateStudent } from '@/api/student'
import { getClassList } from '@/api/clazz'
import { getCollegeList } from '@/api/college'
import { getMajorOptions } from '@/api/major'

const router = useRouter()
const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const userInfo = computed(() => store.state.userInfo || {})

const studentList = ref([])
const collegeList = ref([])
const searchMajorList = ref([])
const formMajorList = ref([])
const searchClassList = ref([])
const formClassList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const columns = computed(() => [
  { key: 'studentNo', title: t('student.studentNo'), width: 140 },
  { key: 'name', title: t('student.name'), width: 120 },
  { key: 'gender', title: t('student.gender'), width: 80 },
  { key: 'collegeName', title: t('student.college'), width: 170 },
  { key: 'majorName', title: t('student.major'), width: 170 },
  { key: 'className', title: t('student.class'), width: 160 },
  { key: 'phone', title: t('student.phone'), width: 150 },
  { key: 'email', title: t('student.email') },
  { key: 'status', title: t('student.status'), width: 110 },
  { key: 'actions', title: t('student.actions'), width: 180, align: 'left' }
])

const searchForm = reactive({
  studentNo: '',
  name: '',
  collegeCode: null,
  majorCode: null,
  classId: null,
  status: ''
})

const dialogVisible = ref(false)
const dialogTitleKey = ref('student.dialogAddTitle')
const dialogTitle = computed(() => t(dialogTitleKey.value))
const isEdit = ref(false)
const submitting = ref(false)

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  gender: 'MALE',
  phone: '',
  email: '',
  idCard: '',
  address: '',
  collegeCode: null,
  majorCode: null,
  classId: null,
  enrollmentDate: '',
  password: ''
})

const rules = computed(() => ({
  name: [{ required: true, message: t('student.nameRequired'), trigger: 'blur' }],
  gender: [{ required: true, message: t('student.genderRequired'), trigger: 'change' }],
  collegeCode: [{ required: true, message: t('student.collegeRequired'), trigger: 'change' }],
  majorCode: [{ required: true, message: t('student.majorRequired'), trigger: 'change' }],
  classId: [{ required: true, message: t('student.classRequired'), trigger: 'change' }]
}))

const searchFields = computed(() => [
  {
    model: 'studentNo',
    label: t('student.studentNo'),
    type: 'input',
    span: 2,
    clearable: true,
    placeholder: t('student.studentNo')
  },
  {
    model: 'name',
    label: t('student.name'),
    type: 'input',
    span: 2,
    clearable: true,
    placeholder: t('student.name')
  },
  {
    model: 'collegeCode',
    label: t('student.college'),
    type: 'select',
    span: 2,
    clearable: true,
    placeholder: t('student.college'),
    options: collegeList.value.map((item) => ({ label: item.collegeName, value: item.collegeCode }))
  },
  {
    model: 'majorCode',
    label: t('student.major'),
    type: 'select',
    span: 2,
    clearable: true,
    placeholder: t('student.major'),
    options: searchMajorList.value.map((item) => ({ label: item.majorName, value: item.majorCode }))
  },
  {
    model: 'classId',
    label: t('student.class'),
    type: 'select',
    span: 2,
    clearable: true,
    placeholder: t('student.class'),
    options: searchClassList.value.map((item) => ({ label: item.className, value: item.classCode }))
  },
  {
    model: 'status',
    label: t('student.status'),
    type: 'select',
    span: 2,
    clearable: true,
    placeholder: t('student.status'),
    options: [
      { label: t('student.statusEnrolled'), value: 'ENROLLED' },
      { label: t('student.statusSuspended'), value: 'SUSPENDED' },
      { label: t('student.statusGraduated'), value: 'GRADUATED' },
      { label: t('student.statusDropped'), value: 'DROPPED' }
    ]
  }
])

const dialogFields = computed(() => [
  {
    model: 'studentNo',
    label: t('student.studentNo'),
    type: 'input',
    span: 1,
    disabled: true,
    placeholder: t('student.autoGeneratePlaceholder')
  },
  {
    model: 'name',
    label: t('student.name'),
    type: 'input',
    span: 1
  },
  {
    model: 'gender',
    label: t('student.gender'),
    type: 'radio',
    span: 1,
    options: [
      { label: t('student.genderMale'), value: 'MALE' },
      { label: t('student.genderFemale'), value: 'FEMALE' }
    ]
  },
  {
    model: 'collegeCode',
    label: t('student.college'),
    type: 'select',
    span: 1,
    placeholder: t('student.selectCollege'),
    options: collegeList.value.map((item) => ({ label: item.collegeName, value: item.collegeCode }))
  },
  {
    model: 'majorCode',
    label: t('student.major'),
    type: 'select',
    span: 1,
    placeholder: t('student.selectMajor'),
    disabled: (model) => !model.collegeCode,
    options: formMajorList.value.map((item) => ({ label: item.majorName, value: item.majorCode }))
  },
  {
    model: 'classId',
    label: t('student.class'),
    type: 'select',
    span: 1,
    placeholder: t('student.selectClass'),
    disabled: (model) => !model.majorCode,
    options: formClassList.value.map((item) => ({ label: item.className, value: item.classCode }))
  },
  {
    model: 'phone',
    label: t('student.phone'),
    type: 'input',
    span: 1
  },
  {
    model: 'email',
    label: t('student.email'),
    type: 'input',
    span: 1
  },
  {
    model: 'idCard',
    label: t('student.idCard'),
    type: 'input',
    span: 1
  },
  {
    model: 'enrollmentDate',
    label: t('student.enrollmentDate'),
    type: 'date',
    span: 1
  },
  {
    model: 'address',
    label: t('student.address'),
    type: 'textarea',
    span: 2,
    rows: 2
  },
  {
    model: 'password',
    label: t('student.password'),
    type: 'input',
    span: 2,
    showPassword: true,
    placeholder: () => (isEdit.value ? t('student.passwordKeepEmpty') : t('student.passwordPlaceholder'))
  }
])

function getDefaultCollegeCode() {
  return userInfo.value?.collegeCode || null
}

async function fetchCollegeList() {
  const res = await getCollegeList({ page: 1, size: 500 })
  collegeList.value = res.data?.records || []
  if (!searchForm.collegeCode && getDefaultCollegeCode()) {
    searchForm.collegeCode = getDefaultCollegeCode()
  }
}

async function fetchMajorList(collegeCode, targetRef) {
  if (!collegeCode) {
    targetRef.value = []
    return
  }
  const res = await getMajorOptions({ collegeCode, status: 1 })
  targetRef.value = res.data || []
}

async function fetchClassList(collegeCode, majorCode, targetRef, allowAll = false) {
  if ((!collegeCode || !majorCode) && !allowAll) {
    targetRef.value = []
    return
  }
  const res = await getClassList({
    page: 1,
    size: 500,
    collegeCode: collegeCode || undefined,
    majorCode: majorCode || undefined
  })
  targetRef.value = res.data?.records || []
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudentList({
      page: page.value,
      size: size.value,
      studentNo: searchForm.studentNo || undefined,
      name: searchForm.name || undefined,
      collegeCode: searchForm.collegeCode || undefined,
      majorCode: searchForm.majorCode || undefined,
      classId: searchForm.classId || undefined,
      status: searchForm.status || undefined
    })
    studentList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function updateSearchField(key, value) {
  searchForm[key] = value
}

function handleReset() {
  searchForm.studentNo = ''
  searchForm.name = ''
  searchForm.collegeCode = getDefaultCollegeCode()
  searchForm.majorCode = null
  searchForm.classId = null
  searchForm.status = ''
  handleSearch()
}

function handleSizeChange(val) {
  size.value = val
  fetchList()
}

function handlePageChange(val) {
  page.value = val
  fetchList()
}

function resetForm() {
  Object.assign(form, {
    id: null,
    studentNo: '',
    name: '',
    gender: 'MALE',
    phone: '',
    email: '',
    idCard: '',
    address: '',
    collegeCode: getDefaultCollegeCode(),
    majorCode: null,
    classId: null,
    enrollmentDate: '',
    password: ''
  })
}

function updateFormField(key, value) {
  form[key] = value
}

async function handleAdd() {
  isEdit.value = false
  dialogTitleKey.value = 'student.dialogAddTitle'
  resetForm()
  await fetchMajorList(form.collegeCode, formMajorList)
  await fetchClassList(form.collegeCode, form.majorCode, formClassList)
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  dialogTitleKey.value = 'student.dialogEditTitle'
  resetForm()
  Object.assign(form, row, {
    collegeCode: row.collegeCode || getDefaultCollegeCode(),
    majorCode: row.majorCode || null,
    password: ''
  })
  await fetchMajorList(form.collegeCode, formMajorList)
  await fetchClassList(form.collegeCode, form.majorCode, formClassList, true)
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('student.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteStudent(row.studentNo)
  ElMessage.success(t('student.deleteSuccess'))
  fetchList()
}

function handleViewScore(row) {
  router.push({ path: '/score', query: { studentId: row.studentNo } })
}

async function handleSubmit() {
  submitting.value = true
  try {
    const payload = { ...form }
    delete payload.collegeCode
    delete payload.majorCode
    if (!payload.password) delete payload.password

    if (!isEdit.value && !payload.password) {
      ElMessage.error(t('student.passwordRequiredOnCreate'))
      return
    }

    if (isEdit.value) {
      await updateStudent(form.studentNo, payload)
      ElMessage.success(t('student.updateSuccess'))
    } else {
      await createStudent(payload)
      ElMessage.success(t('student.createSuccess'))
    }

    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function refreshStudentNo() {
  if (!dialogVisible.value || isEdit.value || !form.classId) {
    if (!isEdit.value) form.studentNo = ''
    return
  }

  try {
    const res = await getNextStudentNo({ classId: form.classId, enrollmentDate: form.enrollmentDate || undefined })
    form.studentNo = res.data || ''
  } catch (_e) {
    form.studentNo = ''
  }
}

function getStatusText(status) {
  const map = {
    ENROLLED: t('student.statusEnrolled'),
    SUSPENDED: t('student.statusSuspended'),
    GRADUATED: t('student.statusGraduated'),
    DROPPED: t('student.statusDropped')
  }
  return map[status] || status
}

function statusBadgeType(status) {
  if (status === 'ENROLLED') return 'success'
  if (status === 'SUSPENDED') return 'warning'
  if (status === 'DROPPED') return 'danger'
  return 'info'
}

function getClassNameById(classId) {
  if (!classId) return ''
  const hit = [...searchClassList.value, ...formClassList.value].find((item) => item.classCode === classId)
  return hit?.className || ''
}

function getCollegeNameByCode(collegeCode) {
  if (!collegeCode) return ''
  const hit = collegeList.value.find((item) => item.collegeCode === collegeCode)
  return hit?.collegeName || ''
}

function getMajorNameByCode(majorCode) {
  if (!majorCode) return ''
  const hit = [...searchMajorList.value, ...formMajorList.value].find((item) => item.majorCode === majorCode)
  return hit?.majorName || ''
}

watch(
  () => searchForm.collegeCode,
  async (collegeCode) => {
    searchForm.majorCode = null
    searchForm.classId = null
    await fetchMajorList(collegeCode, searchMajorList)
    await fetchClassList(collegeCode, null, searchClassList, true)
  }
)

watch(
  () => searchForm.majorCode,
  async (majorCode) => {
    searchForm.classId = null
    await fetchClassList(searchForm.collegeCode, majorCode, searchClassList, true)
  }
)

watch(
  () => form.collegeCode,
  async (collegeCode) => {
    form.majorCode = null
    form.classId = null
    if (!dialogVisible.value) return
    await fetchMajorList(collegeCode, formMajorList)
    await fetchClassList(collegeCode, null, formClassList)
  }
)

watch(
  () => form.majorCode,
  async (majorCode) => {
    form.classId = null
    if (!dialogVisible.value) return
    await fetchClassList(form.collegeCode, majorCode, formClassList)
  }
)

watch(
  () => [form.classId, form.enrollmentDate, dialogVisible.value, isEdit.value],
  () => {
    refreshStudentNo()
  }
)

onMounted(async () => {
  await fetchCollegeList()
  await fetchMajorList(searchForm.collegeCode, searchMajorList)
  await fetchClassList(searchForm.collegeCode, searchForm.majorCode, searchClassList, true)
  await fetchList()
})
</script>
