<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>课程管理</span>
          <el-button type="primary" @click="openCreate">新增课程</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程代码">
          <el-input v-model="searchForm.courseCode" clearable />
        </el-form-item>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.courseName" clearable />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.category" clearable style="width: 160px">
            <el-option label="必修" value="REQUIRED" />
            <el-option label="选修" value="ELECTIVE" />
            <el-option label="实践" value="PRACTICAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="courseCode" label="课程代码" width="120" />
        <el-table-column prop="courseName" label="课程名称" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="hours" label="学时" width="80" />
        <el-table-column prop="category" label="类型" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-switch
              :model-value="row.status === 1"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="170" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑课程' : '新增课程'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程代码" prop="courseCode">
          <el-input v-model="form.courseCode" />
        </el-form-item>
        <el-form-item label="课程名称" prop="courseName">
          <el-input v-model="form.courseName" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学分" prop="credit">
              <el-input-number v-model="form.credit" :min="0" :max="10" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="学时" prop="hours">
              <el-input-number v-model="form.hours" :min="1" :max="300" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="类型" prop="category">
          <el-select v-model="form.category" style="width: 100%">
            <el-option label="必修" value="REQUIRED" />
            <el-option label="选修" value="ELECTIVE" />
            <el-option label="实践" value="PRACTICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
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
  createCourse,
  deleteCourse,
  getCourseList,
  updateCourse,
  updateCourseStatus
} from '@/api/course'

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  courseCode: '',
  courseName: '',
  category: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  courseCode: '',
  courseName: '',
  credit: 2,
  hours: 32,
  category: 'REQUIRED',
  description: '',
  status: 1
})

const rules = {
  courseCode: [{ required: true, message: '请输入课程代码', trigger: 'blur' }],
  courseName: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'change' }],
  hours: [{ required: true, message: '请输入学时', trigger: 'change' }],
  category: [{ required: true, message: '请选择类型', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    courseCode: '',
    courseName: '',
    credit: 2,
    hours: 32,
    category: 'REQUIRED',
    description: '',
    status: 1
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCourseList({
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
  searchForm.courseCode = ''
  searchForm.courseName = ''
  searchForm.category = ''
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
  Object.assign(form, row)
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value) {
    await updateCourse(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await createCourse(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该课程吗？', '提示', { type: 'warning' })
  await deleteCourse(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

async function handleStatusChange(row, enabled) {
  await updateCourseStatus(row.id, enabled ? 1 : 0)
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
