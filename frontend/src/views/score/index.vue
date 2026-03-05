<template>
  <CrudPageShell :title="t('score.pageTitle')">
    <template #header-actions>
      <el-dropdown @command="handleExport">
        <AppButton variant="secondary">{{ t('score.exportReport') }}</AppButton>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="csv">{{ t('score.exportCsv') }}</el-dropdown-item>
            <el-dropdown-item command="xlsx">{{ t('score.exportXlsx') }}</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
      <AppButton v-if="canEditScore" class="ml-2" @click="openCreate">{{ t('score.addScore') }}</AppButton>
    </template>

    <template #filters>
      <div class="grid grid-cols-12 gap-2">
        <el-input-number v-if="canFilterStudent" v-model="searchForm.studentId" :min="1" class="col-span-12 md:col-span-2" :placeholder="t('score.studentId')" />
        <el-select
          v-model="searchForm.courseArrangementId"
          clearable
          filterable
          :placeholder="t('score.arrangement')"
          class="col-span-12 md:col-span-4"
        >
          <el-option v-for="item in arrangementOptions" :key="item.id" :label="formatArrangementLabel(item)" :value="item.id" />
        </el-select>
        <el-input v-model="searchForm.semester" clearable :placeholder="t('score.semesterPlaceholder')" class="col-span-12 md:col-span-2" />
        <div class="col-span-12 flex items-center justify-end gap-2 md:col-span-4">
          <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
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
            <button class="text-[12px] text-primary-700 hover:text-primary-800" @click="openEdit(row)">{{ t('score.edit') }}</button>
            <button class="text-[12px] text-state-danger hover:opacity-80" @click="handleDelete(row)">{{ t('score.delete') }}</button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('score.dialogEditTitle') : t('score.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('score.studentId')" prop="studentId">
              <el-input-number v-model="form.studentId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('score.arrangementId')" prop="courseArrangementId">
              <el-select v-model="form.courseArrangementId" filterable style="width: 100%" :placeholder="t('score.selectArrangement')">
                <el-option v-for="item in arrangementOptions" :key="item.id" :label="formatArrangementLabel(item)" :value="item.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item :label="t('score.usualScore')">
              <el-input-number v-model="form.usualScore" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('score.midtermScore')">
              <el-input-number v-model="form.midtermScore" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="t('score.finalScore')">
              <el-input-number v-model="form.finalScore" :min="0" :max="100" :precision="2" :controls="false" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="t('score.status')">
          <el-select v-model="form.status" style="width: 100%">
            <el-option :label="t('score.statusNormal')" value="NORMAL" />
            <el-option :label="t('score.statusMakeup')" value="MAKEUP" />
            <el-option :label="t('score.statusRetake')" value="RETAKE" />
          </el-select>
        </el-form-item>

        <el-form-item :label="t('score.remark')">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>

      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="submit">{{ t('common.save') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { createScore, deleteScore, exportScoreReport, getScoreList, updateScore } from '@/api/score'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { canAction } from '@/permission/ability'

const store = useStore()
const { t } = useI18n()
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
    { key: 'studentId', title: t('score.studentId'), width: 100, align: 'left' },
    { key: 'studentName', title: t('score.student'), width: 120, align: 'left' },
    { key: 'className', title: t('score.className'), width: 140, align: 'left' },
    { key: 'courseName', title: t('score.courseName'), width: 140, align: 'left' },
    { key: 'semester', title: t('score.semester'), width: 130, align: 'left' },
    { key: 'usualScore', title: t('score.usual'), width: 80, align: 'left' },
    { key: 'midtermScore', title: t('score.midterm'), width: 80, align: 'left' },
    { key: 'finalScore', title: t('score.final'), width: 80, align: 'left' },
    { key: 'totalScore', title: t('score.total'), width: 80, align: 'left' },
    { key: 'gpa', title: t('score.gpa'), width: 80, align: 'left' },
    { key: 'status', title: t('score.status'), width: 110, align: 'left' }
  ]
  if (canEditScore.value) base.push({ key: 'actions', title: t('score.actions'), width: 140, align: 'left' })
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

const rules = computed(() => ({
  studentId: [{ required: true, message: t('score.studentIdRequired'), trigger: 'change' }],
  courseArrangementId: [{ required: true, message: t('score.arrangementIdRequired'), trigger: 'change' }]
}))

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
  return parts.length ? `${t('score.idLabel')}:${item.id} | ${parts.join(' | ')}` : `${t('score.idLabel')}:${item.id}`
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
  ElMessage.success(t('score.exportStarted'))
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
  if (status === 'NORMAL') return t('score.statusNormal')
  if (status === 'MAKEUP') return t('score.statusMakeup')
  if (status === 'RETAKE') return t('score.statusRetake')
  return '-'
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value) {
    await updateScore(form.id, form)
    ElMessage.success(t('score.updateSuccess'))
  } else {
    await createScore(form)
    ElMessage.success(t('score.createSuccess'))
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('score.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteScore(row.id)
  ElMessage.success(t('score.deleteSuccess'))
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

