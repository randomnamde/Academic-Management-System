<template>
  <CrudPageShell :title="t('college.pageTitle')">
    <template #header-actions>
      <AppButton @click="openCreate">{{ t('college.addCollege') }}</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('college.keyword')">
          <el-input v-model="searchForm.keyword" clearable :placeholder="t('college.keywordPlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('college.status')">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option :label="t('college.statusEnabled')" :value="1" />
            <el-option :label="t('college.statusDisabled')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">{{ t('common.reset') }}</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? t('college.statusEnabled') : t('college.statusDisabled') }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex justify-end gap-2">
            <button class="text-[12px] text-primary-700 hover:text-primary-800" @click="openEdit(row)">{{ t('college.edit') }}</button>
            <button class="text-[12px] text-slatex-600 hover:text-slatex-900" @click="openBindAdmin(row)">{{ t('college.bindAdmin') }}</button>
            <button class="text-[12px] text-state-danger hover:opacity-80" @click="toggleStatus(row)">
              {{ Number(row.status) === 1 ? t('college.disable') : t('college.enable') }}
            </button>
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('college.dialogEditTitle') : t('college.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item :label="t('college.collegeCode')" prop="collegeCode">
          <el-input v-model="form.collegeCode" />
        </el-form-item>
        <el-form-item :label="t('college.collegeName')" prop="collegeName">
          <el-input v-model="form.collegeName" />
        </el-form-item>
        <el-form-item :label="t('college.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="submit">{{ t('common.save') }}</AppButton>
      </template>
    </AppModal>

    <AppModal v-model="bindVisible" :title="t('college.bindAdminTitle')" width="520px">
      <el-form label-width="130px">
        <el-form-item :label="t('college.adminUserId')">
          <el-input-number v-model="bindAdminUserId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="bindVisible = false">{{ t('common.cancel') }}</AppButton>
        <AppButton @click="submitBindAdmin">{{ t('common.confirm') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { bindCollegeAdmin, createCollege, getCollegeList, updateCollege, updateCollegeStatus } from '@/api/college'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const currentCollegeId = ref(null)

const searchForm = reactive({
  keyword: '',
  status: null
})

const columns = computed(() => [
  { key: 'collegeCode', title: t('college.collegeCode'), width: 160 },
  { key: 'collegeName', title: t('college.collegeName'), width: 220 },
  { key: 'description', title: t('college.description') },
  { key: 'status', title: t('college.status'), width: 100 },
  { key: 'actions', title: t('college.actions'), width: 250, align: 'right' }
])

const dialogVisible = ref(false)
const bindVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const bindAdminUserId = ref(null)
const form = reactive({
  id: null,
  collegeCode: '',
  collegeName: '',
  description: ''
})

const rules = computed(() => ({
  collegeCode: [{ required: true, message: t('college.collegeCodeRequired'), trigger: 'blur' }],
  collegeName: [{ required: true, message: t('college.collegeNameRequired'), trigger: 'blur' }]
}))

function resetForm() {
  Object.assign(form, {
    id: null,
    collegeCode: '',
    collegeName: '',
    description: ''
  })
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getCollegeList({
      page: page.value,
      size: size.value,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status ?? undefined
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
  searchForm.keyword = ''
  searchForm.status = null
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

function openBindAdmin(row) {
  currentCollegeId.value = row.id
  bindAdminUserId.value = row.adminUserId || null
  bindVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  if (isEdit.value) {
    await updateCollege(form.id, {
      collegeCode: form.collegeCode,
      collegeName: form.collegeName,
      description: form.description
    })
    ElMessage.success(t('college.updateSuccess'))
  } else {
    await createCollege({
      collegeCode: form.collegeCode,
      collegeName: form.collegeName,
      description: form.description
    })
    ElMessage.success(t('college.createSuccess'))
  }
  dialogVisible.value = false
  fetchList()
}

async function submitBindAdmin() {
  if (!currentCollegeId.value || !bindAdminUserId.value) return
  await bindCollegeAdmin(currentCollegeId.value, bindAdminUserId.value)
  ElMessage.success(t('college.bindSuccess'))
  bindVisible.value = false
  fetchList()
}

async function toggleStatus(row) {
  const next = Number(row.status) === 1 ? 0 : 1
  await updateCollegeStatus(row.id, next)
  ElMessage.success(t('college.statusUpdated'))
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
