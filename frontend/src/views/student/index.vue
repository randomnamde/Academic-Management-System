<template>
  <div class="student-management">
    <el-card class="workbench-card">
      <template #header>
        <div class="card-header">
          <span>学生管理</span>
          <el-button type="primary" @click="handleAdd">新增学生</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学号">
          <el-input v-model="searchForm.studentNo" placeholder="请输入学号" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" placeholder="请输入姓名" clearable />
        </el-form-item>
        <el-form-item label="班级">
          <el-select v-model="searchForm.classId" placeholder="请选择班级" clearable style="width: 180px">
            <el-option v-for="item in classList" :key="item.id" :label="item.className" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 140px">
            <el-option label="在读" value="ENROLLED" />
            <el-option label="休学" value="SUSPENDED" />
            <el-option label="毕业" value="GRADUATED" />
            <el-option label="退学" value="DROPPED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="studentList" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="studentNo" label="学号" width="130" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">{{ row.gender === 'MALE' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column label="班级" width="150">
          <template #default="{ row }">
            {{ row.className || getClassNameById(row.classId) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="success" link @click="handleViewScore(row)">成绩</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        class="pagination"
      />
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="760px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
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

        <el-row :gutter="20">
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

        <el-row :gutter="20">
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

        <el-row :gutter="20">
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
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createStudent, deleteStudent, getNextStudentNo, getStudentList, updateStudent } from '@/api/student'
import { getClassList } from '@/api/clazz'

const router = useRouter()

const studentList = ref([])
const classList = ref([])
const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)

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
  if (!payload.password) {
    delete payload.password
  }

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
    if (!isEdit.value) {
      form.studentNo = ''
    }
    return
  }
  try {
    const res = await getNextStudentNo({
      classId: form.classId,
      enrollmentDate: form.enrollmentDate || undefined
    })
    form.studentNo = res.data || ''
  } catch (_e) {
    form.studentNo = ''
  }
}

function getStatusType(status) {
  const map = { ENROLLED: 'success', SUSPENDED: 'warning', GRADUATED: 'info', DROPPED: 'danger' }
  return map[status] || ''
}

function getStatusText(status) {
  const map = { ENROLLED: '在读', SUSPENDED: '休学', GRADUATED: '毕业', DROPPED: '退学' }
  return map[status] || status
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

<style scoped lang="scss">
.student-management {
  padding: 4px 0 10px;
}

.workbench-card {
  border-radius: 18px;
  border: 1px solid rgba(21, 88, 102, 0.17);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 248, 0.84));
  box-shadow: 0 10px 24px rgba(20, 68, 80, 0.1);
}

.workbench-card :deep(.el-card__header) {
  border-bottom: 1px solid rgba(19, 87, 98, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.card-header > span {
  font-size: 18px;
  font-weight: 700;
  color: #17384a;
  letter-spacing: 0.01em;
}

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

@media (max-width: 992px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
