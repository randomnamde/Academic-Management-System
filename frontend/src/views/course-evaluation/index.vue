<template>
  <CrudPageShell :title="t('evaluation.pageTitle')">
    <template #header-actions>
      <AppButton v-if="canSubmitEvaluation" class="ml-2" @click="openCreate">{{ t('evaluation.submit') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-select
          v-model="searchForm.courseArrangementId"
          clearable
          filterable
          :placeholder="t('evaluation.courseArrangement')"
          class="col-span-12 md:col-span-3"
        >
          <el-option
            v-for="item in arrangementOptions"
            :key="item.id"
            :label="`${item.courseName} (${item.semester})`"
            :value="item.id"
          />
        </el-select>
        <el-select
          v-if="!isStudent"
          v-model="searchForm.teacherNo"
          clearable
          filterable
          remote
          reserve-keyword
          :remote-method="handleTeacherSearch"
          :loading="teacherLoading"
          :placeholder="t('evaluation.teacher')"
          class="col-span-12 md:col-span-3"
        >
          <el-option
            v-for="item in teacherOptions"
            :key="item.teacherNo"
            :label="`${item.name} (${item.teacherNo})`"
            :value="item.teacherNo"
          />
        </el-select>
        <el-select v-model="searchForm.status" clearable :placeholder="t('evaluation.status')" class="col-span-12 md:col-span-2">
          <el-option :label="t('evaluation.statusPending')" value="PENDING" />
          <el-option :label="t('evaluation.statusSubmitted')" value="SUBMITTED" />
          <el-option :label="t('evaluation.statusPublished')" value="PUBLISHED" />
        </el-select>
        <div class="app-filter-action-wrap col-span-12 md:col-span-3">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-overallScore="{ row }">
          <el-rate v-model="row.overallScore" disabled :max="5" />
        </template>
        <template #cell-isAnonymous="{ row }">
          <AppBadge :type="row.isAnonymous ? 'info' : 'default'">{{ row.isAnonymous ? t('evaluation.anonymous') : t('evaluation.realName') }}</AppBadge>
        </template>
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">{{ statusLabel(row.status) }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="app-table-actions app-table-actions--start w-full">
            <button class="app-table-action" @click="openEdit(row)">{{ t('common.edit') }}</button>
            <button v-if="canPublishEvaluation && row.status === 'SUBMITTED'" class="app-table-action" @click="handlePublish(row)">{{ t('evaluation.publish') }}</button>
            <button class="app-table-action app-table-action--danger" @click="handleDelete(row)">{{ t('common.delete') }}</button>
          </div>
        </template>
      </AppTable>
    </template>

    <template #pagination>
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </template>

    <el-dialog v-model="dialogVisible" :title="isEdit ? t('evaluation.edit') : t('evaluation.submit')" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item :label="t('evaluation.courseArrangement')" prop="courseArrangementId">
          <el-select v-model="form.courseArrangementId" filterable :placeholder="t('evaluation.selectCourse')">
            <el-option
              v-for="item in arrangementOptions"
              :key="item.id"
              :label="`${item.courseName} - ${item.teacherName} (${item.semester})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('evaluation.teachingScore')" prop="teachingScore">
          <el-rate v-model="form.teachingScore" :max="5" show-text :texts="scoreTexts" />
        </el-form-item>
        <el-form-item :label="t('evaluation.contentScore')" prop="contentScore">
          <el-rate v-model="form.contentScore" :max="5" show-text :texts="scoreTexts" />
        </el-form-item>
        <el-form-item :label="t('evaluation.methodScore')" prop="methodScore">
          <el-rate v-model="form.methodScore" :max="5" show-text :texts="scoreTexts" />
        </el-form-item>
        <el-form-item :label="t('evaluation.comment')" prop="comment">
          <el-input v-model="form.comment" type="textarea" :rows="4" :placeholder="t('evaluation.commentPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('evaluation.anonymous')">
          <el-switch v-model="form.isAnonymous" :true-value="1" :false-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="handleSubmit">{{ t('common.submit') }}</AppButton>
      </template>
    </el-dialog>
  </CrudPageShell>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getEvaluationList, createEvaluation, updateEvaluation, deleteEvaluation, publishEvaluation } from '@/api/evaluation'
import { getCourseArrangementOptions } from '@/api/courseArrangement'
import { getTeacherList } from '@/api/teacher'
import { useUserInfo } from '@/composables/useUser'
import { usePagination } from '@/composables/usePagination'

const { t } = useI18n()
const { userInfo, isStudent, roles } = useUserInfo()
const { page, size, total, loading, tableData, handlePageChange, handleSizeChange } = usePagination()

const canSubmitEvaluation = computed(() => isStudent.value)
const canPublishEvaluation = computed(() => ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'].some(r => roles.value.includes(r)))

const searchForm = reactive({
  courseArrangementId: null,
  teacherNo: null,
  status: null
})

const arrangementOptions = ref([])
const teacherOptions = ref([])
const teacherLoading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const form = reactive({
  id: null,
  courseArrangementId: null,
  teachingScore: 0,
  contentScore: 0,
  methodScore: 0,
  comment: '',
  isAnonymous: 1
})

const scoreTexts = ['1', '2', '3', '4', '5']

const rules = {
  courseArrangementId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  teachingScore: [{ required: true, message: '请评分', trigger: 'change' }],
  contentScore: [{ required: true, message: '请评分', trigger: 'change' }],
  methodScore: [{ required: true, message: '请评分', trigger: 'change' }]
}

const columns = computed(() => {
  const base = [
    { key: 'courseName', title: t('evaluation.course'), width: 180 },
    { key: 'teacherName', title: t('evaluation.teacher'), width: 100 },
    { key: 'studentName', title: isStudent.value ? t('evaluation.student') : t('evaluation.student'), width: 100 },
    { key: 'overallScore', title: t('evaluation.overallScore'), width: 180 },
    { key: 'isAnonymous', title: t('evaluation.anonymous'), width: 100 },
    { key: 'status', title: t('evaluation.status'), width: 100 },
    { key: 'createTime', title: t('evaluation.createTime'), width: 160 }
  ]
  if (!isStudent.value) base.push({ key: 'actions', title: t('evaluation.actions'), width: 180 })
  return base
})

function statusBadgeType(status) {
  const map = { PENDING: 'warning', SUBMITTED: 'info', PUBLISHED: 'success' }
  return map[status] || 'info'
}

function statusLabel(status) {
  const map = { PENDING: t('evaluation.statusPending'), SUBMITTED: t('evaluation.statusSubmitted'), PUBLISHED: t('evaluation.statusPublished') }
  return map[status] || status
}

async function fetchArrangementOptions() {
  const res = await getCourseArrangementOptions()
  arrangementOptions.value = Array.isArray(res.data) ? res.data : []
}

async function handleTeacherSearch(query) {
  if (!query) {
    teacherOptions.value = []
    return
  }
  teacherLoading.value = true
  try {
    const res = await getTeacherList({ name: query, page: 1, size: 20 })
    teacherOptions.value = res.data?.records || []
  } finally {
    teacherLoading.value = false
  }
}

async function fetchData() {
  loading.value = true
  try {
    const res = await getEvaluationList({
      page: page.value,
      size: size.value,
      courseArrangementId: searchForm.courseArrangementId || undefined,
      teacherNo: searchForm.teacherNo || undefined,
      status: searchForm.status || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function handleReset() {
  searchForm.courseArrangementId = null
  searchForm.teacherNo = null
  searchForm.status = null
  handleSearch()
}

function openCreate() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    courseArrangementId: null,
    teachingScore: 0,
    contentScore: 0,
    methodScore: 0,
    comment: '',
    isAnonymous: 1
  })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    courseArrangementId: row.courseArrangementId,
    teachingScore: row.teachingScore,
    contentScore: row.contentScore,
    methodScore: row.methodScore,
    comment: row.comment,
    isAnonymous: row.isAnonymous
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate()
  try {
    if (isEdit.value) {
      await updateEvaluation(form.id, form)
      ElMessage.success(t('common.success'))
    } else {
      await createEvaluation(form)
      ElMessage.success(t('common.success'))
    }
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e.message || t('common.error'))
  }
}

async function handleDelete(row) {
  await ElMessageBox.confirm(t('common.deleteConfirm'), t('common.warning'), { type: 'warning' })
  await deleteEvaluation(row.id)
  ElMessage.success(t('common.success'))
  fetchData()
}

async function handlePublish(row) {
  await publishEvaluation(row.id)
  ElMessage.success(t('common.success'))
  fetchData()
}

const tableDensity = ref('medium')

onMounted(() => {
  fetchArrangementOptions()
  fetchData()
})
</script>
