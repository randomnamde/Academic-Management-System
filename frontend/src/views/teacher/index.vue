<template>
  <CrudPageShell :title="t('teacher.pageTitle')">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('teacher.addTeacher') }}</AppButton>
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
        <template #cell-gender="{ row }">
          {{ row.gender === 'MALE' ? t('teacher.genderMale') : t('teacher.genderFemale') }}
        </template>
        <template #cell-status="{ row }">
          <div class="teacher-status-cell">
            <AppBadge :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? t('teacher.statusEnabled') : t('teacher.statusDisabled') }}
            </AppBadge>
            <AppSwitch
              v-if="!isStudent"
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </div>
        </template>
        <template #cell-actions="{ row }">
          <div v-if="!isStudent" class="app-table-actions">
            <AppActionGroup>
              <AppButton variant="secondary" size="sm" @click="openEdit(row)">{{ t('teacher.edit') }}</AppButton>
              <AppButton variant="danger" size="sm" @click="handleDelete(row)">{{ t('teacher.delete') }}</AppButton>
            </AppActionGroup>
          </div>
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
        :title="isEdit ? t('teacher.dialogEditTitle') : t('teacher.dialogAddTitle')"
        width="640px"
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
import { computed, onMounted, reactive, ref } from 'vue'
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
import AppSwitch from '@/components/ui/AppSwitch.vue'
import {
  createTeacher,
  deleteTeacher,
  getTeacherList,
  updateTeacher,
  updateTeacherStatus
} from '@/api/teacher'

const store = useStore()
const { t } = useI18n()
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const isStudent = computed(() => role.value === 'STUDENT')
const tableDensity = computed(() => store.getters.tableDensity)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const columns = computed(() => {
  const base = [
    { key: 'teacherNo', title: t('teacher.teacherNo'), width: 136, align: 'left' },
    { key: 'name', title: t('teacher.name'), width: 124, align: 'left' },
    { key: 'gender', title: t('teacher.gender'), width: 92, align: 'left' },
    { key: 'title', title: t('teacher.title'), width: 164, align: 'left' },
    { key: 'department', title: t('teacher.department'), align: 'left' },
    { key: 'phone', title: t('teacher.phone'), width: 144, align: 'left' },
    { key: 'status', title: t('teacher.status'), width: 188, align: 'left' }
  ]
  if (!isStudent.value) base.push({ key: 'actions', title: t('teacher.actions'), width: 148, align: 'left' })
  return base
})

const searchForm = reactive({
  teacherNo: '',
  name: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const form = reactive({
  id: null,
  teacherNo: '',
  name: '',
  gender: 'MALE',
  title: 'LECTURER',
  department: '',
  phone: '',
  email: '',
  password: ''
})

const rules = computed(() => ({
  teacherNo: [{ required: true, message: t('teacher.teacherNoRequired'), trigger: 'blur' }],
  name: [{ required: true, message: t('teacher.nameRequired'), trigger: 'blur' }],
  gender: [{ required: true, message: t('teacher.genderRequired'), trigger: 'change' }]
}))

const searchFields = computed(() => [
  {
    model: 'teacherNo',
    label: t('teacher.teacherNo'),
    type: 'input',
    span: 3,
    clearable: true,
    placeholder: t('teacher.teacherNo')
  },
  {
    model: 'name',
    label: t('teacher.name'),
    type: 'input',
    span: 3,
    clearable: true,
    placeholder: t('teacher.name')
  }
])

const dialogFields = computed(() => [
  {
    model: 'teacherNo',
    label: t('teacher.teacherNo'),
    type: 'input',
    span: 1
  },
  {
    model: 'name',
    label: t('teacher.name'),
    type: 'input',
    span: 1
  },
  {
    model: 'gender',
    label: t('teacher.gender'),
    type: 'radio',
    span: 1,
    options: [
      { label: t('teacher.genderMale'), value: 'MALE' },
      { label: t('teacher.genderFemale'), value: 'FEMALE' }
    ]
  },
  {
    model: 'title',
    label: t('teacher.title'),
    type: 'select',
    span: 1,
    options: [
      { label: t('teacher.titleLecturer'), value: 'LECTURER' },
      { label: t('teacher.titleAssociateProfessor'), value: 'ASSOCIATE_PROFESSOR' },
      { label: t('teacher.titleProfessor'), value: 'PROFESSOR' }
    ]
  },
  {
    model: 'department',
    label: t('teacher.department'),
    type: 'input',
    span: 1
  },
  {
    model: 'phone',
    label: t('teacher.phone'),
    type: 'input',
    span: 1
  },
  {
    model: 'email',
    label: t('teacher.email'),
    type: 'input',
    span: 1
  },
  {
    model: 'password',
    label: t('teacher.password'),
    type: 'input',
    span: 2,
    showPassword: true,
    placeholder: t('teacher.passwordKeepEmpty')
  }
])

function resetForm() {
  Object.assign(form, {
    id: null,
    teacherNo: '',
    name: '',
    gender: 'MALE',
    title: 'LECTURER',
    department: '',
    phone: '',
    email: '',
    password: ''
  })
}

function updateFormField(key, value) {
  form[key] = value
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getTeacherList({
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
  searchForm.teacherNo = ''
  searchForm.name = ''
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
  dialogVisible.value = true
}

function openEdit(row) {
  if (isStudent.value) return
  isEdit.value = true
  resetForm()
  Object.assign(form, row, { password: '' })
  dialogVisible.value = true
}

async function submit() {
  if (isStudent.value) return
  submitting.value = true
  try {
    const payload = { ...form }
    if (!payload.password) {
      delete payload.password
    }

    if (!isEdit.value && !payload.password) {
      ElMessage.error(t('teacher.passwordRequiredOnCreate'))
      return
    }

    if (isEdit.value) {
      await updateTeacher(form.id, payload)
      ElMessage.success(t('teacher.updateSuccess'))
    } else {
      await createTeacher(payload)
      ElMessage.success(t('teacher.createSuccess'))
    }
    dialogVisible.value = false
    fetchList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  if (isStudent.value) return
  await ElMessageBox.confirm(t('teacher.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteTeacher(row.id)
  ElMessage.success(t('teacher.deleteSuccess'))
  fetchList()
}

async function handleStatusChange(row, enabled) {
  if (isStudent.value) return
  await updateTeacherStatus(row.id, enabled ? 1 : 0)
  ElMessage.success(t('teacher.statusUpdated'))
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}

.teacher-status-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
</style>
