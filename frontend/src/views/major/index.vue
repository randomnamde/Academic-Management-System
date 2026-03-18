<template>
  <CrudPageShell :title="t('major.pageTitle')">
    <template #header-actions>
      <AppButton @click="openCreate">{{ t('major.addMajor') }}</AppButton>
    </template>

    <template #filters>
      <div class="app-filter-grid">
        <el-input v-model="searchForm.keyword" clearable :placeholder="t('major.keywordPlaceholder')" class="col-span-12 md:col-span-3" />
        <el-select v-model="searchForm.collegeCode" clearable :placeholder="t('major.college')" class="col-span-12 md:col-span-3">
          <el-option v-for="item in collegeOptions" :key="item.collegeCode" :label="item.collegeName" :value="item.collegeCode" />
        </el-select>
        <el-select v-model="searchForm.status" clearable :placeholder="t('major.status')" class="col-span-12 md:col-span-2">
          <el-option :label="t('major.statusEnabled')" :value="1" />
          <el-option :label="t('major.statusDisabled')" :value="0" />
        </el-select>
        <div class="app-filter-action-wrap col-span-12 md:col-span-4">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </div>
      </div>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'info'">
            {{ Number(row.status) === 1 ? t('major.statusEnabled') : t('major.statusDisabled') }}
          </AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="app-table-actions">
            <button class="app-table-action" @click="openEdit(row)">{{ t('major.edit') }}</button>
            <button class="app-table-action app-table-action--danger" @click="toggleStatus(row)">
              {{ Number(row.status) === 1 ? t('major.disable') : t('major.enable') }}
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

    <AppModal v-model="dialogVisible" :title="isEdit ? t('major.dialogEditTitle') : t('major.dialogAddTitle')" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('major.majorName')" prop="majorName">
              <el-input v-model="form.majorName" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('major.majorAbbreviation')" prop="majorAbbreviation">
              <el-input v-model="form.majorAbbreviation" maxlength="4" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item :label="t('major.college')" prop="collegeCode">
              <el-select v-model="form.collegeCode" style="width: 100%">
                <el-option v-for="item in collegeOptions" :key="item.collegeCode" :label="item.collegeName" :value="item.collegeCode" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item :label="t('major.majorCode')">
              <el-input :model-value="form.majorCode || t('major.autoGenerateHint')" disabled />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="t('major.description')">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { getCollegeList } from '@/api/college'
import { createMajor, getMajorList, updateMajor, updateMajorStatus } from '@/api/major'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const userInfo = computed(() => store.state.userInfo || {})

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const collegeOptions = ref([])

const searchForm = reactive({
  keyword: '',
  collegeCode: null,
  status: null
})

const columns = computed(() => [
  { key: 'majorCode', title: t('major.majorCode'), width: 130 },
  { key: 'majorName', title: t('major.majorName'), width: 180 },
  { key: 'majorAbbreviation', title: t('major.majorAbbreviation'), width: 110 },
  { key: 'collegeName', title: t('major.college'), width: 180 },
  { key: 'description', title: t('major.description') },
  { key: 'status', title: t('major.status'), width: 100 },
  { key: 'actions', title: t('major.actions'), width: 180, align: 'left' }
])

const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref()
const form = reactive({
  majorCode: '',
  majorName: '',
  majorAbbreviation: '',
  collegeCode: null,
  description: ''
})

const rules = computed(() => ({
  majorName: [{ required: true, message: t('major.majorNameRequired'), trigger: 'blur' }],
  collegeCode: [{ required: true, message: t('major.collegeRequired'), trigger: 'change' }]
}))

function resetForm() {
  Object.assign(form, {
    majorCode: '',
    majorName: '',
    majorAbbreviation: '',
    collegeCode: userInfo.value?.collegeCode || null,
    description: ''
  })
}

async function loadCollegeOptions() {
  const res = await getCollegeList({ page: 1, size: 500 })
  collegeOptions.value = res.data?.records || []
  if (!searchForm.collegeCode && userInfo.value?.collegeCode) {
    searchForm.collegeCode = userInfo.value.collegeCode
  }
}

async function fetchList() {
  loading.value = true
  try {
    const res = await getMajorList({
      page: page.value,
      size: size.value,
      keyword: searchForm.keyword || undefined,
      collegeCode: searchForm.collegeCode || undefined,
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
  searchForm.collegeCode = userInfo.value?.collegeCode || null
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

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    majorName: form.majorName,
    majorAbbreviation: form.majorAbbreviation || undefined,
    collegeCode: form.collegeCode,
    description: form.description
  }

  if (isEdit.value) {
    await updateMajor(form.majorCode, payload)
    ElMessage.success(t('major.updateSuccess'))
  } else {
    await createMajor(payload)
    ElMessage.success(t('major.createSuccess'))
  }
  dialogVisible.value = false
  fetchList()
}

async function toggleStatus(row) {
  const next = Number(row.status) === 1 ? 0 : 1
  await updateMajorStatus(row.majorCode, next)
  ElMessage.success(t('major.statusUpdated'))
  fetchList()
}

onMounted(async () => {
  await loadCollegeOptions()
  await fetchList()
})
</script>

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}
</style>
