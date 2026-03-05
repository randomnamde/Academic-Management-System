<template>
  <CrudPageShell :title="t('class.pageTitle')">
    <template #header-actions>
      <AppButton v-if="!isStudent" @click="openCreate">{{ t('class.addClass') }}</AppButton>
    </template>

    <template #filters>
      <div class="grid grid-cols-12 gap-2">
        <el-input v-model="searchForm.className" clearable :placeholder="t('class.className')" class="col-span-12 md:col-span-3" />
        <el-input v-model="searchForm.grade" clearable :placeholder="t('class.gradePlaceholder')" class="col-span-12 md:col-span-2" />
        <div class="col-span-12 flex items-center justify-end gap-2 md:col-span-7">
          <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? t('class.statusActive') : t('class.statusDisabled') }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div v-if="!isStudent" class="flex justify-end gap-2">
            <button class="text-[12px] text-primary-700 hover:text-primary-800" @click="openEdit(row)">{{ t('class.edit') }}</button>
            <button class="text-[12px] text-state-danger hover:opacity-80" @click="handleDelete(row)">{{ t('class.delete') }}</button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('class.dialogEditTitle') : t('class.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="96px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('class.className')" prop="className">
              <el-input v-model="form.className" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('class.classCode')" prop="classCode">
              <el-input v-model="form.classCode" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('class.grade')" prop="grade">
              <el-input-number v-model="form.grade" :min="2000" :max="2100" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('class.homeroomTeacherId')">
              <el-input-number v-model="form.teacherId" :min="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('class.major')">
          <el-input v-model="form.major" />
        </el-form-item>
        <el-form-item :label="t('class.room')">
          <el-input v-model="form.room" />
        </el-form-item>
        <el-form-item :label="t('class.status')">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">{{ t('class.statusActive') }}</el-radio>
            <el-radio :label="0">{{ t('class.statusDisabled') }}</el-radio>
          </el-radio-group>
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
import { createClass, deleteClass, getClassList, updateClass } from '@/api/clazz'

const store = useStore()
const { t } = useI18n()
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
    { key: 'classCode', title: t('class.classCode'), width: 120 },
    { key: 'className', title: t('class.className'), width: 160 },
    { key: 'grade', title: t('class.grade'), width: 90 },
    { key: 'major', title: t('class.major'), width: 170 },
    { key: 'teacherId', title: t('class.homeroomTeacherId'), width: 110 },
    { key: 'studentCount', title: t('class.studentCount'), width: 90, align: 'right' },
    { key: 'status', title: t('class.status'), width: 100, align: 'center' }
  ]
  if (!isStudent.value) base.push({ key: 'actions', title: t('class.actions'), width: 140, align: 'right' })
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

const rules = computed(() => ({
  className: [{ required: true, message: t('class.classNameRequired'), trigger: 'blur' }],
  classCode: [{ required: true, message: t('class.classCodeRequired'), trigger: 'blur' }],
  grade: [{ required: true, message: t('class.gradeRequired'), trigger: 'change' }]
}))

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
    ElMessage.success(t('class.updateSuccess'))
  } else {
    await createClass(payload)
    ElMessage.success(t('class.createSuccess'))
  }

  dialogVisible.value = false
  fetchList()
}

async function handleDelete(row) {
  if (isStudent.value) return
  await ElMessageBox.confirm(t('class.deleteConfirm'), t('common.tip'), { type: 'warning' })
  await deleteClass(row.id)
  ElMessage.success(t('class.deleteSuccess'))
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

