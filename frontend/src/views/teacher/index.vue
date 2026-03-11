<template>
  <CrudPageShell :title="t('teacher.pageTitle')">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('teacher.addTeacher') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-input v-model="searchForm.teacherNo" clearable :placeholder="t('teacher.teacherNo')" class="col-span-12 md:col-span-3" />
        <el-input v-model="searchForm.name" clearable :placeholder="t('teacher.name')" class="col-span-12 md:col-span-3" />
        <div class="app-filter-action-wrap col-span-12 md:col-span-6">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-gender="{ row }">
          {{ row.gender === 'MALE' ? t('teacher.genderMale') : t('teacher.genderFemale') }}
        </template>
        <template #cell-status="{ row }">
          <div class="teacher-status-cell">
            <AppBadge :type="row.status === 1 ? 'success' : 'info'">
              {{ row.status === 1 ? t('teacher.statusEnabled') : t('teacher.statusDisabled') }}
            </AppBadge>
            <el-switch
              v-if="!isStudent"
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </div>
        </template>
        <template #cell-actions="{ row }">
          <div v-if="!isStudent" class="app-table-actions">
            <button class="app-table-action" @click="openEdit(row)">{{ t('teacher.edit') }}</button>
            <button class="app-table-action app-table-action--danger" @click="handleDelete(row)">{{ t('teacher.delete') }}</button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('teacher.dialogEditTitle') : t('teacher.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
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
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
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

.teacher-status-cell {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}
</style>
