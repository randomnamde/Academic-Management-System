<template>
  <CrudPageShell :title="t('class.pageTitle')">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('class.addClass') }}</AppButton>
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
      <AppDataTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? t('class.statusActive') : t('class.statusDisabled') }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <AppActionGroup v-if="!isStudent">
            <AppButton variant="secondary" size="sm" @click="openEdit(row)">{{ t('class.edit') }}</AppButton>
            <AppButton variant="danger" size="sm" @click="handleDelete(row)">{{ t('class.delete') }}</AppButton>
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
        :title="isEdit ? t('class.dialogEditTitle') : t('class.dialogAddTitle')"
        width="680px"
        :fields="dialogFields"
        :model="form"
        :rules="rules"
        :update-field="updateFormField"
        :loading="submitting"
        :submit-text="t('common.save')"
        :cancel-text="t('common.cancel')"
        @submit="submit"
      />
    </template>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
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
import { createClass, deleteClass, getClassList, updateClass } from '@/api/clazz'
import { getCollegeList } from '@/api/college'
import { getMajorOptions } from '@/api/major'

const store = useStore()
const { t } = useI18n()
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const userInfo = computed(() => store.state.userInfo || {})
const isStudent = computed(() => role.value === 'STUDENT')
const tableDensity = computed(() => store.getters.tableDensity)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const collegeOptions = ref([])
const searchMajorOptions = ref([])
const formMajorOptions = ref([])

const columns = computed(() => {
  const base = [
    { key: 'classCode', title: t('class.classCode'), width: 120 },
    { key: 'className', title: t('class.className'), width: 160 },
    { key: 'grade', title: t('class.grade'), width: 90 },
    { key: 'majorName', title: t('class.major'), width: 180 },
    { key: 'teacherName', title: t('class.teacherName'), width: 110 },
    { key: 'studentCount', title: t('class.studentCount'), width: 90, align: 'left' },
    { key: 'status', title: t('class.status'), width: 100, align: 'left' }
  ]
  if (!isStudent.value) base.push({ key: 'actions', title: t('class.actions'), width: 148, align: 'left' })
  return base
})

const searchForm = reactive({
  className: '',
  grade: '',
  collegeCode: null,
  majorCode: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const form = reactive({
  id: null,
  className: '',
  classCode: '',
  grade: 2023,
  collegeCode: null,
  majorCode: null,
  teacherNo: null,
  room: '',
  status: 1,
  studentCount: 0
})

const rules = computed(() => ({
  className: [{ required: true, message: t('class.classNameRequired'), trigger: 'blur' }],
  grade: [{ required: true, message: t('class.gradeRequired'), trigger: 'change' }],
  collegeCode: [{ required: true, message: t('class.collegeRequired'), trigger: 'change' }],
  majorCode: [{ required: true, message: t('class.majorRequired'), trigger: 'change' }]
}))

const searchFields = computed(() => [
  {
    model: 'className',
    label: t('class.className'),
    type: 'input',
    span: 3,
    clearable: true,
    placeholder: t('class.className')
  },
  {
    model: 'grade',
    label: t('class.grade'),
    type: 'input',
    span: 2,
    clearable: true,
    placeholder: t('class.gradePlaceholder')
  },
  {
    model: 'collegeCode',
    label: t('class.college'),
    type: 'select',
    span: 3,
    clearable: true,
    placeholder: t('class.college'),
    options: collegeOptions.value.map((item) => ({ label: item.collegeName, value: item.collegeCode }))
  },
  {
    model: 'majorCode',
    label: t('class.major'),
    type: 'select',
    span: 2,
    clearable: true,
    placeholder: t('class.major'),
    options: searchMajorOptions.value.map((item) => ({ label: item.majorName, value: item.majorCode }))
  }
])

const dialogFields = computed(() => [
  {
    model: 'className',
    label: t('class.className'),
    type: 'input',
    span: 1
  },
  {
    model: 'classCode',
    label: t('class.classCode'),
    type: 'input',
    span: 1,
    disabled: true,
    placeholder: t('class.autoGenerateHint')
  },
  {
    model: 'grade',
    label: t('class.grade'),
    type: 'number',
    span: 1,
    min: 2000,
    max: 2100
  },
  {
    model: 'teacherNo',
    label: t('class.teacherNo'),
    type: 'number',
    span: 1,
    min: 1
  },
  {
    model: 'collegeCode',
    label: t('class.college'),
    type: 'select',
    span: 1,
    options: collegeOptions.value.map((item) => ({ label: item.collegeName, value: item.collegeCode }))
  },
  {
    model: 'majorCode',
    label: t('class.major'),
    type: 'select',
    span: 1,
    disabled: (model) => !model.collegeCode,
    options: formMajorOptions.value.map((item) => ({ label: item.majorName, value: item.majorCode }))
  },
  {
    model: 'room',
    label: t('class.room'),
    type: 'input',
    span: 1
  },
  {
    model: 'status',
    label: t('class.status'),
    type: 'radio',
    span: 1,
    options: [
      { label: t('class.statusActive'), value: 1 },
      { label: t('class.statusDisabled'), value: 0 }
    ]
  }
])

function getDefaultCollegeCode() {
  return userInfo.value?.collegeCode || null
}

function resetForm() {
  Object.assign(form, {
    id: null,
    className: '',
    classCode: '',
    grade: 2023,
    collegeCode: getDefaultCollegeCode(),
    majorCode: null,
    teacherNo: null,
    room: '',
    status: 1,
    studentCount: 0
  })
}

function updateFormField(key, value) {
  form[key] = value
}

async function loadCollegeOptions() {
  const res = await getCollegeList({ page: 1, size: 500 })
  collegeOptions.value = res.data?.records || []
  if (!searchForm.collegeCode && getDefaultCollegeCode()) {
    searchForm.collegeCode = getDefaultCollegeCode()
  }
}

async function loadMajorOptions(collegeCode, targetRef) {
  if (!collegeCode) {
    targetRef.value = []
    return
  }
  const res = await getMajorOptions({ collegeCode, status: 1 })
  targetRef.value = res.data || []
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getClassList({
      page: page.value,
      size: size.value,
      className: searchForm.className || undefined,
      grade: searchForm.grade || undefined,
      collegeCode: searchForm.collegeCode || undefined,
      majorCode: searchForm.majorCode || undefined
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
  searchForm.className = ''
  searchForm.grade = ''
  searchForm.collegeCode = getDefaultCollegeCode()
  searchForm.majorCode = null
  handleSearch()
}

function updateSearchField(key, value) {
  searchForm[key] = value
}

function handleSizeChange(val) {
  size.value = val
  fetchList()
}

function handlePageChange(val) {
  page.value = val
  fetchList()
}

function openCreate() {
  if (isStudent.value) return
  isEdit.value = false
  resetForm()
  loadMajorOptions(form.collegeCode, formMajorOptions)
  dialogVisible.value = true
}

async function openEdit(row) {
  if (isStudent.value) return
  isEdit.value = true
  resetForm()
  Object.assign(form, row, {
    grade: row.grade ? Number(row.grade) : 2023,
    collegeCode: row.collegeCode || getDefaultCollegeCode()
  })
  await loadMajorOptions(form.collegeCode, formMajorOptions)
  dialogVisible.value = true
}

async function submit() {
  if (isStudent.value) return
  submitting.value = true
  try {
    const payload = {
      className: form.className,
      grade: String(form.grade),
      majorCode: form.majorCode,
      teacherNo: form.teacherNo,
      room: form.room,
      status: form.status,
      studentCount: form.studentCount
    }

    if (isEdit.value) {
      await updateClass(form.classCode, payload)
      ElMessage.success(t('class.updateSuccess'))
    } else {
      await createClass(payload)
      ElMessage.success(t('class.createSuccess'))
    }

    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  if (isStudent.value) return
  await ElMessageBox.confirm(t('class.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteClass(row.classCode)
  ElMessage.success(t('class.deleteSuccess'))
  fetchList()
}

watch(
  () => searchForm.collegeCode,
  async (collegeCode) => {
    searchForm.majorCode = null
    await loadMajorOptions(collegeCode, searchMajorOptions)
  }
)

watch(
  () => form.collegeCode,
  async (collegeCode) => {
    form.majorCode = null
    if (!dialogVisible.value) return
    await loadMajorOptions(collegeCode, formMajorOptions)
  }
)

onMounted(async () => {
  await loadCollegeOptions()
  await loadMajorOptions(searchForm.collegeCode, searchMajorOptions)
  await fetchList()
})
</script>

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}
</style>
