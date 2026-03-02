<template>
  <div class="page-container">
    <el-card class="workbench-card">
      <template #header>
        <div class="header-row">
          <span>班级管理</span>
          <el-button v-if="!isStudent" type="primary" @click="openCreate">新增班级</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="班级名称">
          <el-input v-model="searchForm.className" clearable />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="searchForm.grade" clearable placeholder="如 2023" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="classCode" label="班级代码" width="120" />
        <el-table-column prop="className" label="班级名称" width="180" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="major" label="专业" />
        <el-table-column prop="teacherId" label="班主任ID" width="110" />
        <el-table-column prop="studentCount" label="人数" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '在读' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="!isStudent" label="操作" width="170" fixed="right">
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑班级' : '新增班级'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="班级名称" prop="className">
              <el-input v-model="form.className" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班级代码" prop="classCode">
              <el-input v-model="form.classCode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="年级" prop="grade">
              <el-input-number v-model="form.grade" :min="2000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="班主任ID">
              <el-input-number v-model="form.teacherId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="专业">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item label="教室">
          <el-input v-model="form.room" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">在读</el-radio>
            <el-radio :label="0">停用</el-radio>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createClass, deleteClass, getClassList, updateClass } from '@/api/clazz'

const store = useStore()
const role = computed(() => store.state.userInfo?.role || '')
const isStudent = computed(() => role.value === 'STUDENT')

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  className: '',
  grade: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  className: '',
  classCode: '',
  grade: 2023,
  major: '',
  teacherId: null,
  room: '',
  status: 1,
  studentCount: 0
})

const rules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  classCode: [{ required: true, message: '请输入班级代码', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    className: '',
    classCode: '',
    grade: 2023,
    major: '',
    teacherId: null,
    room: '',
    status: 1,
    studentCount: 0
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getClassList({
      page: page.value,
      size: size.value,
      className: searchForm.className,
      grade: searchForm.grade
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
  searchForm.className = ''
  searchForm.grade = ''
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
  Object.assign(form, row, {
    grade: row.grade ? Number(row.grade) : 2023
  })
  dialogVisible.value = true
}

async function submit() {
  if (isStudent.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    ...form,
    grade: String(form.grade)
  }

  if (isEdit.value) {
    await updateClass(form.id, payload)
    ElMessage.success('修改成功')
  } else {
    await createClass(payload)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  if (isStudent.value) return
  await ElMessageBox.confirm('确认删除该班级吗？', '提示', { type: 'warning' })
  await deleteClass(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>

<style scoped lang="scss">
.page-container {
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

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}

.header-row > span {
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
  .header-row {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>

