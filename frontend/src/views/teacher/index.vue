<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>教师管理</span>
          <el-button type="primary" @click="openCreate">新增教师</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="教师编号">
          <el-input v-model="searchForm.teacherNo" clearable />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="searchForm.name" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="teacherNo" label="教师编号" width="130" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="gender" label="性别" width="90">
          <template #default="{ row }">{{ row.gender === 'MALE' ? '男' : '女' }}</template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="160" />
        <el-table-column prop="department" label="院系" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column label="状态" width="110">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑教师' : '新增教师'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="教师编号" prop="teacherNo">
              <el-input v-model="form.teacherNo" />
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
              <el-select v-model="form.gender" style="width: 100%">
                <el-option label="男" value="MALE" />
                <el-option label="女" value="FEMALE" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="form.title" style="width: 100%">
                <el-option label="讲师" value="LECTURER" />
                <el-option label="副教授" value="ASSOCIATE_PROFESSOR" />
                <el-option label="教授" value="PROFESSOR" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="院系">
              <el-input v-model="form.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话">
              <el-input v-model="form.phone" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="密码" :required="!isEdit">
          <el-input v-model="form.password" type="password" show-password placeholder="编辑时留空则不修改" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createTeacher,
  deleteTeacher,
  getTeacherList,
  updateTeacher,
  updateTeacherStatus
} from '@/api/teacher'

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

const rules = {
  teacherNo: [{ required: true, message: '请输入教师编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }]
}

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
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  resetForm()
  Object.assign(form, row, { password: '' })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = { ...form }
  if (!payload.password) {
    delete payload.password
  }

  if (!isEdit.value && !payload.password) {
    ElMessage.error('新增教师必须填写密码')
    return
  }

  if (isEdit.value) {
    await updateTeacher(form.id, payload)
    ElMessage.success('修改成功')
  } else {
    await createTeacher(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该教师吗？', '提示', { type: 'warning' })
  await deleteTeacher(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleStatusChange(row, enabled) {
  await updateTeacherStatus(row.id, enabled ? 1 : 0)
  ElMessage.success('状态已更新')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
