<template>
  <div class="page-container">
    <el-card>
      <template #header>
        <div class="header-row">
          <span>成绩管理</span>
          <el-button type="primary" @click="openCreate">新增成绩</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="学生ID">
          <el-input-number v-model="searchForm.studentId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item label="排课ID">
          <el-input-number v-model="searchForm.courseArrangementId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="searchForm.semester" clearable placeholder="如 2024-2025-1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="studentId" label="学生ID" width="90" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="courseArrangementId" label="排课ID" width="90" />
        <el-table-column prop="courseName" label="课程" width="140" />
        <el-table-column prop="usualScore" label="平时" width="80" />
        <el-table-column prop="midtermScore" label="期中" width="80" />
        <el-table-column prop="finalScore" label="期末" width="80" />
        <el-table-column prop="totalScore" label="总评" width="80" />
        <el-table-column prop="gpa" label="GPA" width="80" />
        <el-table-column prop="status" label="状态" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑成绩' : '新增成绩'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学生ID" prop="studentId">
              <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排课ID" prop="courseArrangementId">
              <el-input-number v-model="form.courseArrangementId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="平时分">
              <el-input-number v-model="form.usualScore" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期中分">
              <el-input-number v-model="form.midtermScore" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期末分">
              <el-input-number v-model="form.finalScore" :min="0" :max="100" :precision="2" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 100%">
            <el-option label="正常" value="NORMAL" />
            <el-option label="补考" value="MAKEUP" />
            <el-option label="重修" value="RETAKE" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
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
import { createScore, deleteScore, getScoreList, updateScore } from '@/api/score'

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  studentId: null,
  courseArrangementId: null,
  semester: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  studentId: null,
  courseArrangementId: null,
  usualScore: null,
  midtermScore: null,
  finalScore: null,
  status: 'NORMAL',
  remark: ''
})

const rules = {
  studentId: [{ required: true, message: '请输入学生ID', trigger: 'change' }],
  courseArrangementId: [{ required: true, message: '请输入排课ID', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    studentId: null,
    courseArrangementId: null,
    usualScore: null,
    midtermScore: null,
    finalScore: null,
    status: 'NORMAL',
    remark: ''
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getScoreList({
      page: page.value,
      size: size.value,
      studentId: searchForm.studentId || undefined,
      courseArrangementId: searchForm.courseArrangementId || undefined,
      semester: searchForm.semester || undefined
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
  searchForm.studentId = null
  searchForm.courseArrangementId = null
  searchForm.semester = ''
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
    await updateScore(form.id, form)
    ElMessage.success('修改成功')
  } else {
    await createScore(form)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该成绩记录吗？', '提示', { type: 'warning' })
  await deleteScore(row.id)
  ElMessage.success('删除成功')
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
