<template>
  <CrudPageShell :title="t('teacher.pageTitle')">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('teacher.addTeacher') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('teacher.teacherNo')">
          <el-input v-model="searchForm.teacherNo" clearable />
        </el-form-item>
        <el-form-item :label="t('teacher.name')">
          <el-input v-model="searchForm.name" clearable />
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" :label="t('teacher.index')" width="60" />
        <el-table-column prop="teacherNo" :label="t('teacher.teacherNo')" width="130" />
        <el-table-column prop="name" :label="t('teacher.name')" width="120" />
        <el-table-column prop="gender" :label="t('teacher.gender')" width="90">
          <template #default="{ row }">{{ row.gender === 'MALE' ? t('teacher.genderMale') : t('teacher.genderFemale') }}</template>
        </el-table-column>
        <el-table-column prop="title" :label="t('teacher.title')" width="160" />
        <el-table-column prop="department" :label="t('teacher.department')" />
        <el-table-column prop="phone" :label="t('teacher.phone')" width="140" />
        <el-table-column :label="t('teacher.status')" width="110">
          <template #default="{ row }">
            <el-switch
              v-if="!isStudent"
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
            <el-tag v-else :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? t('teacher.statusEnabled') : t('teacher.statusDisabled') }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isStudent" :label="t('teacher.actions')" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">{{ t('teacher.edit') }}</el-button>
            <el-button type="danger" link @click="handleDelete(row)">{{ t('teacher.delete') }}</el-button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('teacher.dialogEditTitle') : t('teacher.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('teacher.teacherNo')" prop="teacherNo">
              <el-input v-model="form.teacherNo" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('teacher.name')" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('teacher.gender')" prop="gender">
              <el-select v-model="form.gender" style="width: 100%">
                <el-option :label="t('teacher.genderMale')" value="MALE" />
                <el-option :label="t('teacher.genderFemale')" value="FEMALE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('teacher.title')" prop="title">
              <el-select v-model="form.title" style="width: 100%">
                <el-option :label="t('teacher.titleLecturer')" value="LECTURER" />
                <el-option :label="t('teacher.titleAssociateProfessor')" value="ASSOCIATE_PROFESSOR" />
                <el-option :label="t('teacher.titleProfessor')" value="PROFESSOR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('teacher.department')">
              <el-input v-model="form.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('teacher.phone')">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('teacher.email')">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item :label="t('teacher.password')" :required="!isEdit">
          <el-input v-model="form.password" type="password" show-password :placeholder="t('teacher.passwordKeepEmpty')" />
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

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  teacherNo: '',
  name: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
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
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

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
</style>
