<template>
  <CrudPageShell title="班级管理">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">新增班级</AppButton>
    </template>

    <template #filters>
      <div class="grid grid-cols-12 gap-2">
        <el-input v-model="searchForm.className" clearable placeholder="班级名称" class="col-span-12 md:col-span-3" />
        <el-input v-model="searchForm.grade" clearable placeholder="年级，如 2023" class="col-span-12 md:col-span-2" />
        <div class="col-span-12 flex items-center justify-end gap-2 md:col-span-7">
          <AppButton variant="secondary" @click="handleReset">重置</AppButton>
          <AppButton @click="handleSearch">查询</AppButton>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? '在读' : '停用' }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div v-if="!isStudent" class="flex justify-end gap-2">
            <button class="text-[12px] text-primary-700 hover:text-primary-800" @click="openEdit(row)">编辑</button>
            <button class="text-[12px] text-state-danger hover:opacity-80" @click="handleDelete(row)">删除</button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? '编辑班级' : '新增班级'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
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
            <el-form-item label="班主任编号">
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
        <AppButton variant="secondary" @click="dialogVisible = false">取消</AppButton>
        <AppButton @click="submit">保存</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { createClass, deleteClass, getClassList, updateClass } from '@/api/clazz'

const store = useStore()
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
    { key: 'classCode', title: '班级代码', width: 120 },
    { key: 'className', title: '班级名称', width: 160 },
    { key: 'grade', title: '年级', width: 90 },
    { key: 'major', title: '专业', width: 170 },
    { key: 'teacherId', title: '班主任编号', width: 110 },
    { key: 'studentCount', title: '人数', width: 90, align: 'right' },
    { key: 'status', title: '状态', width: 100, align: 'center' }
  ]
  if (!isStudent.value) base.push({ key: 'actions', title: '操作', width: 140, align: 'right' })
  return base
})

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
  Object.assign(form, row, { grade: row.grade ? Number(row.grade) : 2023 })
  dialogVisible.value = true
}

async function submit() {
  if (isStudent.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = { ...form, grade: String(form.grade) }

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

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}
</style>

