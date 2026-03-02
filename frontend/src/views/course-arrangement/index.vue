<template>
  <CrudPageShell title="排课管理">
    <template #header-actions>
      <AppButton @click="openCreate">新增排课</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="课程ID">
          <el-input-number v-model="searchForm.courseId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item label="教师ID">
          <el-input-number v-model="searchForm.teacherId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item label="班级ID">
          <el-input-number v-model="searchForm.classId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="searchForm.semester" clearable placeholder="如 2024-2025-1" />
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">查询</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">重置</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column type="index" label="序号" width="60" />
        <el-table-column prop="courseName" label="课程" min-width="130" />
        <el-table-column prop="teacherName" label="教师" width="120" />
        <el-table-column prop="className" label="班级" width="120" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="schedule" label="时间安排" min-width="150" />
        <el-table-column prop="room" label="教室" width="100" />
        <el-table-column label="人数" width="120">
          <template #default="{ row }">{{ row.enrolledCount || 0 }}/{{ row.capacity || 0 }}</template>
        </el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="{ row }">{{ row.status === 1 ? '启用' : '停用' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? '编辑排课' : '新增排课'" width="700px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="课程ID" prop="courseId">
              <el-input-number v-model="form.courseId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="教师ID" prop="teacherId">
              <el-input-number v-model="form.teacherId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="班级ID" prop="classId">
              <el-input-number v-model="form.classId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学期" prop="semester">
              <el-input v-model="form.semester" placeholder="如 2024-2025-1" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="教室">
              <el-input v-model="form.room" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="时间安排" prop="schedule">
          <el-input v-model="form.schedule" placeholder="如 周一 08:00-09:40" />
        </el-form-item>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="容量" prop="capacity">
              <el-input-number v-model="form.capacity" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-radio-group v-model="form.status">
                <el-radio :label="1">启用</el-radio>
                <el-radio :label="0">停用</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">取消</AppButton>
        <AppButton @click="submit">保存</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import {
  createCourseArrangement,
  deleteCourseArrangement,
  getCourseArrangementList,
  updateCourseArrangement
} from '@/api/courseArrangement'

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  courseId: null,
  teacherId: null,
  classId: null,
  semester: ''
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  id: null,
  courseId: null,
  teacherId: null,
  classId: null,
  semester: '',
  schedule: '',
  room: '',
  capacity: 50,
  status: 1
})

const rules = {
  courseId: [{ required: true, message: '请输入课程ID', trigger: 'change' }],
  teacherId: [{ required: true, message: '请输入教师ID', trigger: 'change' }],
  classId: [{ required: true, message: '请输入班级ID', trigger: 'change' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }],
  schedule: [{ required: true, message: '请输入时间安排', trigger: 'blur' }],
  capacity: [{ required: true, message: '请输入容量', trigger: 'change' }]
}

function resetForm() {
  Object.assign(form, {
    id: null,
    courseId: null,
    teacherId: null,
    classId: null,
    semester: '',
    schedule: '',
    room: '',
    capacity: 50,
    status: 1
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCourseArrangementList({
      page: page.value,
      size: size.value,
      courseId: searchForm.courseId || undefined,
      teacherId: searchForm.teacherId || undefined,
      classId: searchForm.classId || undefined,
      semester: searchForm.semester || undefined
    })
    tableData.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  searchForm.courseId = null
  searchForm.teacherId = null
  searchForm.classId = null
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

  const payload = {
    courseId: form.courseId,
    teacherId: form.teacherId,
    classId: form.classId,
    semester: form.semester,
    schedule: form.schedule,
    room: form.room,
    capacity: form.capacity,
    status: form.status
  }

  if (isEdit.value) {
    await updateCourseArrangement(form.id, payload)
    ElMessage.success('修改成功')
  } else {
    await createCourseArrangement(payload)
    ElMessage.success('新增成功')
  }

  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm('确认删除该排课吗？', '提示', { type: 'warning' })
  await deleteCourseArrangement(row.id)
  ElMessage.success('删除成功')
  fetchList()
}

onMounted(fetchList)
</script>
