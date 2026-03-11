<template>
  <CrudPageShell :title="t('student.pageTitle')">
    <template #header-actions>
      <AppButton @click="handleAdd">{{ t('student.addStudent') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-input v-model="searchForm.studentNo" clearable :placeholder="t('student.studentNo')" class="col-span-12 md:col-span-2" />
        <el-input v-model="searchForm.name" clearable :placeholder="t('student.name')" class="col-span-12 md:col-span-2" />
        <el-select v-model="searchForm.collegeId" clearable :placeholder="t('student.college')" class="col-span-12 md:col-span-2">
          <el-option v-for="item in collegeList" :key="item.id" :label="item.collegeName" :value="item.id" />
        </el-select>
        <el-select v-model="searchForm.majorCode" clearable :placeholder="t('student.major')" class="col-span-12 md:col-span-2">
          <el-option v-for="item in searchMajorList" :key="item.majorCode" :label="item.majorName" :value="item.majorCode" />
        </el-select>
        <el-select v-model="searchForm.classId" clearable :placeholder="t('student.class')" class="col-span-12 md:col-span-2">
          <el-option v-for="item in searchClassList" :key="item.classCode" :label="item.className" :value="item.classCode" />
        </el-select>
        <el-select v-model="searchForm.status" clearable :placeholder="t('student.status')" class="col-span-12 md:col-span-2">
          <el-option :label="t('student.statusEnrolled')" value="ENROLLED" />
          <el-option :label="t('student.statusSuspended')" value="SUSPENDED" />
          <el-option :label="t('student.statusGraduated')" value="GRADUATED" />
          <el-option :label="t('student.statusDropped')" value="DROPPED" />
        </el-select>
        <div class="app-filter-action-wrap col-span-12 md:col-span-12">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="studentList" :loading="loading" :density="tableDensity">
        <template #cell-gender="{ row }">{{ row.gender === 'MALE' ? t('student.genderMale') : t('student.genderFemale') }}</template>
        <template #cell-collegeName="{ row }">{{ row.collegeName || getCollegeNameById(row.collegeId) || '-' }}</template>
        <template #cell-majorName="{ row }">{{ row.majorName || getMajorNameByCode(row.majorCode) || '-' }}</template>
        <template #cell-className="{ row }">{{ row.className || getClassNameById(row.classId) || '-' }}</template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ getStatusText(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="app-table-actions">
            <button class="app-table-action" @click="handleEdit(row)">{{ t('student.edit') }}</button>
            <button class="app-table-action app-table-action--muted" @click="handleViewScore(row)">{{ t('student.viewScore') }}</button>
            <button class="app-table-action app-table-action--danger" @click="handleDelete(row)">{{ t('student.delete') }}</button>
          </div>
        </template>
      </AppTable>
    </template>

    <template #pagination>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </template>

    <AppModal v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="form" :rules="rules" ref="formRef" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('student.studentNo')" prop="studentNo">
              <el-input v-model="form.studentNo" disabled :placeholder="t('student.autoGeneratePlaceholder')" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('student.name')" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('student.gender')" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio label="MALE">{{ t('student.genderMale') }}</el-radio>
                <el-radio label="FEMALE">{{ t('student.genderFemale') }}</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('student.college')" prop="collegeId">
              <el-select v-model="form.collegeId" :placeholder="t('student.selectCollege')" style="width: 100%">
                <el-option v-for="item in collegeList" :key="item.id" :label="item.collegeName" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('student.major')" prop="majorCode">
              <el-select v-model="form.majorCode" :placeholder="t('student.selectMajor')" style="width: 100%" :disabled="!form.collegeId">
                <el-option v-for="item in formMajorList" :key="item.majorCode" :label="item.majorName" :value="item.majorCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('student.class')" prop="classId">
              <el-select v-model="form.classId" :placeholder="t('student.selectClass')" style="width: 100%" :disabled="!form.majorCode">
                <el-option v-for="item in formClassList" :key="item.classCode" :label="item.className" :value="item.classCode" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('student.phone')" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('student.email')" prop="email">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('student.idCard')" prop="idCard">
              <el-input v-model="form.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('student.enrollmentDate')" prop="enrollmentDate">
              <el-date-picker v-model="form.enrollmentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('student.address')" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item :label="t('student.password')" :required="!isEdit">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? t('student.passwordKeepEmpty') : t('student.passwordPlaceholder')" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="handleSubmit">{{ t('common.confirm') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { createStudent, deleteStudent, getNextStudentNo, getStudentList, updateStudent } from '@/api/student'
import { getClassList } from '@/api/clazz'
import { getCollegeList } from '@/api/college'
import { getMajorOptions } from '@/api/major'

const router = useRouter()
const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
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
  collegeId: null,
  majorCode: null,
  classId: null,
  status: ''
})

const dialogVisible = ref(false)
const dialogTitleKey = ref('student.dialogAddTitle')
const dialogTitle = computed(() => t(dialogTitleKey.value))
const formRef = ref()
const isEdit = ref(false)

const form = reactive({
  id: null,
  studentNo: '',
  name: '',
  gender: 'MALE',
  phone: '',
  email: '',
  idCard: '',
  address: '',
  collegeId: null,
  majorCode: null,
  classId: null,
  enrollmentDate: '',
  password: ''
})

const rules = computed(() => ({
  name: [{ required: true, message: t('student.nameRequired'), trigger: 'blur' }],
  gender: [{ required: true, message: t('student.genderRequired'), trigger: 'change' }],
  collegeId: [{ required: true, message: t('student.collegeRequired'), trigger: 'change' }],
  majorCode: [{ required: true, message: t('student.majorRequired'), trigger: 'change' }],
  classId: [{ required: true, message: t('student.classRequired'), trigger: 'change' }]
}))

function getDefaultCollegeId() {
  return userInfo.value?.collegeId || null
}

async function fetchCollegeList() {
  const res = await getCollegeList({ page: 1, size: 500 })
  collegeList.value = res.data?.records || []
  if (!searchForm.collegeId && getDefaultCollegeId()) {
    searchForm.collegeId = getDefaultCollegeId()
  }
}

async function fetchMajorList(collegeId, targetRef) {
  if (!collegeId) {
    targetRef.value = []
    return
  }
  const res = await getMajorOptions({ collegeId, status: 1 })
  targetRef.value = res.data || []
}

async function fetchClassList(collegeId, majorCode, targetRef, allowAll = false) {
  if ((!collegeId || !majorCode) && !allowAll) {
    targetRef.value = []
    return
  }
  const res = await getClassList({
    page: 1,
    size: 500,
    collegeId: collegeId || undefined,
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
      collegeId: searchForm.collegeId || undefined,
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

function handleReset() {
  searchForm.studentNo = ''
  searchForm.name = ''
  searchForm.collegeId = getDefaultCollegeId()
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
    collegeId: getDefaultCollegeId(),
    majorCode: null,
    classId: null,
    enrollmentDate: '',
    password: ''
  })
}

async function handleAdd() {
  isEdit.value = false
  dialogTitleKey.value = 'student.dialogAddTitle'
  resetForm()
  await fetchMajorList(form.collegeId, formMajorList)
  await fetchClassList(form.collegeId, form.majorCode, formClassList)
  dialogVisible.value = true
}

async function handleEdit(row) {
  isEdit.value = true
  dialogTitleKey.value = 'student.dialogEditTitle'
  resetForm()
  Object.assign(form, row, {
    collegeId: row.collegeId || getDefaultCollegeId(),
    majorCode: row.majorCode || null,
    password: ''
  })
  await fetchMajorList(form.collegeId, formMajorList)
  await fetchClassList(form.collegeId, form.majorCode, formClassList, true)
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('student.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success(t('student.deleteSuccess'))
  fetchList()
}

function handleViewScore(row) {
  router.push({ path: '/score', query: { studentId: row.id } })
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = { ...form }
  delete payload.collegeId
  delete payload.majorCode
  if (!payload.password) delete payload.password

  if (!isEdit.value && !payload.password) {
    ElMessage.error(t('student.passwordRequiredOnCreate'))
    return
  }

  if (isEdit.value) {
    await updateStudent(form.id, payload)
    ElMessage.success(t('student.updateSuccess'))
  } else {
    await createStudent(payload)
    ElMessage.success(t('student.createSuccess'))
  }

  dialogVisible.value = false
  fetchList()
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

function getCollegeNameById(collegeId) {
  if (!collegeId) return ''
  const hit = collegeList.value.find((item) => item.id === collegeId)
  return hit?.collegeName || ''
}

function getMajorNameByCode(majorCode) {
  if (!majorCode) return ''
  const hit = [...searchMajorList.value, ...formMajorList.value].find((item) => item.majorCode === majorCode)
  return hit?.majorName || ''
}

watch(
  () => searchForm.collegeId,
  async (collegeId) => {
    searchForm.majorCode = null
    searchForm.classId = null
    await fetchMajorList(collegeId, searchMajorList)
    await fetchClassList(collegeId, null, searchClassList, true)
  }
)

watch(
  () => searchForm.majorCode,
  async (majorCode) => {
    searchForm.classId = null
    await fetchClassList(searchForm.collegeId, majorCode, searchClassList, true)
  }
)

watch(
  () => form.collegeId,
  async (collegeId) => {
    form.majorCode = null
    form.classId = null
    if (!dialogVisible.value) return
    await fetchMajorList(collegeId, formMajorList)
    await fetchClassList(collegeId, null, formClassList)
  }
)

watch(
  () => form.majorCode,
  async (majorCode) => {
    form.classId = null
    if (!dialogVisible.value) return
    await fetchClassList(form.collegeId, majorCode, formClassList)
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
  await fetchMajorList(searchForm.collegeId, searchMajorList)
  await fetchClassList(searchForm.collegeId, searchForm.majorCode, searchClassList, true)
  await fetchList()
})
</script>
