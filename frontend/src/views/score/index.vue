<template>
  <CrudPageShell title="成绩管理">
    <template #header-actions>
      <el-dropdown @command="handleExport">
        <AppButton variant="secondary">导出报表</AppButton>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="csv">导出逗号分隔文件</el-dropdown-item>
            <el-dropdown-item command="xlsx">导出电子表格文件</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <AppButton v-if="canEditScore" class="ml-2" @click="openCreate">新增成绩</AppButton>
    </template>

    <template #filters>
      <div class="grid grid-cols-12 gap-2">
        <el-input-number v-if="canFilterStudent" v-model="searchForm.studentId" :min="1" class="col-span-12 md:col-span-2" placeholder="学生学号" />
        <el-select
          v-model="searchForm.courseArrangementId"
          clearable
          filterable
          placeholder="排课"
          class="col-span-12 md:col-span-4"
        >
          <el-option v-for="item in arrangementOptions" :key="item.id" :label="formatArrangementLabel(item)" :value="item.id" />
        </el-select>
        <el-input v-model="searchForm.semester" clearable placeholder="学期，如 2024-2025-1" class="col-span-12 md:col-span-2" />
        <div class="col-span-12 flex items-center justify-end gap-2 md:col-span-4">
          <AppButton variant="secondary" @click="handleReset">重置</AppButton>
          <AppButton @click="handleSearch">查询</AppButton>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-className="{ row }">
          {{ resolveClassName(row) }}
        </template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div v-if="canEditScore" class="flex w-full justify-start gap-2">
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

    <AppModal v-model="dialogVisible" :title="isEdit ? '编辑成绩' : '新增成绩'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="学生学号" prop="studentId">
              <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="排课编号" prop="courseArrangementId">
              <el-select v-model="form.courseArrangementId" filterable style="width: 100%" placeholder="请选择排课">
                <el-option v-for="item in arrangementOptions" :key="item.id" :label="formatArrangementLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="平时分">
              <el-input-number v-model="form.usualScore" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期中分">
              <el-input-number v-model="form.midtermScore" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="期末分">
              <el-input-number v-model="form.finalScore" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" />
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
import { createScore, deleteScore, exportScoreReport, getScoreList, updateScore } from '@/api/score'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { canAction } from '@/permission/ability'

const store = useStore()
const role = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const permissions = computed(() => store.state.userInfo?.permissions || [])
const tableDensity = computed(() => store.getters.tableDensity)
const canEditScore = computed(() => canAction(role.value, 'score:create', permissions.value))
const canFilterStudent = computed(() => role.value !== 'STUDENT')

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const arrangementOptions = ref([])
const arrangementClassMap = computed(() => {
  const map = new Map()
  arrangementOptions.value.forEach((item) => {
    if (item?.id != null && item.className) {
      map.set(item.id, item.className)
    }
  })
  return map
})

const columns = computed(() => {
  const base = [
    { key: 'studentId', title: '学生学号', width: 100, align: 'left' },
    { key: 'studentName', title: '学生', width: 120, align: 'left' },
    { key: 'className', title: '班级', width: 140, align: 'left' },
    { key: 'courseName', title: '课程', width: 140, align: 'left' },
    { key: 'semester', title: '学期', width: 130, align: 'left' },
    { key: 'usualScore', title: '平时', width: 80, align: 'left' },
    { key: 'midtermScore', title: '期中', width: 80, align: 'left' },
    { key: 'finalScore', title: '期末', width: 80, align: 'left' },
    { key: 'totalScore', title: '总评', width: 80, align: 'left' },
    { key: 'gpa', title: '绩点', width: 80, align: 'left' },
    { key: 'status', title: '状态', width: 110, align: 'left' }
  ]
  if (canEditScore.value) base.push({ key: 'actions', title: '操作', width: 140, align: 'left' })
  return base
})

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
  studentId: [{ required: true, message: '请输入学生学号', trigger: 'change' }],
  courseArrangementId: [{ required: true, message: '请输入排课编号', trigger: 'change' }]
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

function formatArrangementLabel(item) {
  const parts = [item.semester, item.courseName, item.className].filter(Boolean)
  return parts.length ? `编号:${item.id} | ${parts.join(' | ')}` : `编号:${item.id}`
}

async function fetchArrangementOptions() {
  const res = await getCourseArrangementOptions()
  arrangementOptions.value = Array.isArray(res.data) ? res.data : []
}

function resolveClassName(row) {
  if (row?.className) return row.className
  const arrangementId = row?.courseArrangementId
  if (arrangementId == null) return '-'
  return arrangementClassMap.value.get(arrangementId) || '-'
}

async function handleExport(format) {
  await exportScoreReport({
    studentId: searchForm.studentId || undefined,
    courseArrangementId: searchForm.courseArrangementId || undefined,
    semester: searchForm.semester || undefined,
    format
  })
  ElMessage.success('导出任务已开始')
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  if (canFilterStudent.value) searchForm.studentId = null
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
  Object.assign(form, {
    ...row,
    usualScore: toNullableNumber(row.usualScore),
    midtermScore: toNullableNumber(row.midtermScore),
    finalScore: toNullableNumber(row.finalScore)
  })
  dialogVisible.value = true
}

function toNullableNumber(value) {
  if (value === null || value === undefined || value === '') return null
  const parsed = Number(value)
  return Number.isNaN(parsed) ? null : parsed
}

function statusBadgeType(status) {
  if (status === 'NORMAL') return 'success'
  if (status === 'MAKEUP') return 'warning'
  if (status === 'RETAKE') return 'danger'
  return 'info'
}

function statusLabel(status) {
  if (status === 'NORMAL') return '正常'
  if (status === 'MAKEUP') return '补考'
  if (status === 'RETAKE') return '重修'
  return '-'
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

onMounted(async () => {
  await fetchArrangementOptions()
  await fetchList()
})
</script>

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}
</style>

