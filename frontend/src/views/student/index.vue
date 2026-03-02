<template>
  <CrudPageShell title="学生管理">
    <template #header-actions>
      <AppButton @click="handleAdd">新增学生</AppButton>
    </template>

    <template #filters>
      <div class="grid grid-cols-12 gap-2">
        <el-input v-model="searchForm.studentNo" clearable placeholder="学号" class="col-span-12 md:col-span-2" />
        <el-input v-model="searchForm.name" clearable placeholder="姓名" class="col-span-12 md:col-span-2" />
        <el-select v-model="searchForm.classId" clearable placeholder="班级" class="col-span-12 md:col-span-2">
          <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
        </el-select>
        <el-select v-model="searchForm.status" clearable placeholder="状态" class="col-span-12 md:col-span-2">
          <el-option label="在读" value="ENROLLED" />
          <el-option label="休学" value="SUSPENDED" />
          <el-option label="毕业" value="GRADUATED" />
          <el-option label="退学" value="DROPPED" />
        </el-select>
        <div class="col-span-12 flex items-center justify-end gap-2 md:col-span-4">
          <AppButton variant="secondary" @click="handleReset">重置</AppButton>
          <AppButton @click="handleSearch">查询</AppButton>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="studentList" :loading="loading" :density="tableDensity">
        <template #cell-gender="{ row }">{{ row.gender === 'MALE' ? '男' : '女' }}</template>
        <template #cell-className="{ row }">{{ row.className || getClassNameById(row.classId) || '-' }}</template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ getStatusText(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex justify-end gap-2">
            <button class="text-[12px] text-primary-700 hover:text-primary-800" @click="handleEdit(row)">编辑</button>
            <button class="text-[12px] text-slatex-600 hover:text-slatex-900" @click="handleViewScore(row)">成绩</button>
            <button class="text-[12px] text-state-danger hover:opacity-80" @click="handleDelete(row)">删除</button>
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
      <el-form :model="form" :rules="rules" ref="formRef" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学号" prop="studentNo">
              <el-input v-model="form.studentNo" disabled placeholder="根据班级与入学日期自动生成" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="form.gender">
                <el-radio label="MALE">男</el-radio>
                <el-radio label="FEMALE">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级" prop="classId">
              <el-select v-model="form.classId" placeholder="请选择班级" style="width: 100%">
                <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="电话" prop="phone">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="身份证号" prop="idCard">
              <el-input v-model="form.idCard" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入学日期" prop="enrollmentDate">
              <el-date-picker v-model="form.enrollmentDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="地址" prop="address">
          <el-input v-model="form.address" type="textarea" :rows="2" />
        </el-form-item>

        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="form.password" type="password" show-password :placeholder="isEdit ? '留空则不修改' : '请输入密码'" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">取消</AppButton>
        <AppButton @click="handleSubmit">确定</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { createStudent, deleteStudent, getNextStudentNo, getStudentList, updateStudent } from '@/api/student'
import { getClassList } from '@/api/clazz'

const router = useRouter()
const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)

const studentList = ref([])
const classList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

const columns = [
  { key: 'studentNo', title: '学号', width: 140 },
  { key: 'name', title: '姓名', width: 120 },
  { key: 'gender', title: '性别', width: 80 },
  { key: 'className', title: '班级', width: 160 },
  { key: 'phone', title: '电话', width: 150 },
  { key: 'email', title: '邮箱' },
  { key: 'status', title: '状态', width: 110 },
  { key: 'actions', title: '操作', width: 180, align: 'right' }
]

const searchForm = reactive({
  studentNo: '',
  name: '',
  classId: null,
  status: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增学生')
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
  classId: null,
  enrollmentDate: '',
  password: ''
})

const rules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  classId: [{ required: true, message: '请选择班级', trigger: 'change' }]
}

async function fetchClassList() {
  const res = await getClassList({ page: 1, size: 500 })
  classList.value = res.data?.records || []
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getStudentList({
      page: page.value,
      size: size.value,
      studentNo: searchForm.studentNo || undefined,
      name: searchForm.name || undefined,
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
    classId: null,
    enrollmentDate: '',
    password: ''
  })
}

async function handleAdd() {
  await fetchClassList()
  isEdit.value = false
  dialogTitle.value = '新增学生'
  resetForm()
  dialogVisible.value = true
}

async function handleEdit(row) {
  await fetchClassList()
  isEdit.value = true
  dialogTitle.value = '编辑学生'
  resetForm()
  Object.assign(form, row, { password: '' })
  dialogVisible.value = true
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确定要删除该学生吗？', '提示', { type: 'warning' })
  await deleteStudent(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

function handleViewScore(row) {
  router.push({ path: '/score', query: { studentId: row.id } })
}

async function handleSubmit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = { ...form }
  if (!payload.password) delete payload.password

  if (!isEdit.value && !payload.password) {
    ElMessage.error('新增学生必须填写密码')
    return
  }

  if (isEdit.value) {
    await updateStudent(form.id, payload)
    ElMessage.success('更新成功')
  } else {
    await createStudent(payload)
    ElMessage.success('添加成功')
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
  const map = { ENROLLED: '在读', SUSPENDED: '休学', GRADUATED: '毕业', DROPPED: '退学' }
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
  const hit = classList.value.find((item) => item.id === classId)
  return hit?.className || ''
}

watch(
  () => [form.classId, form.enrollmentDate, dialogVisible.value, isEdit.value],
  () => {
    refreshStudentNo()
  }
)

onMounted(async () => {
  await fetchClassList()
  await fetchList()
})
</script>
