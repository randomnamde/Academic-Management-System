<template>
  <CrudPageShell title="学院管理">
    <template #header-actions>
      <AppButton @click="openCreate">新增学院</AppButton>
    </template>

    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" clearable placeholder="学院编码/学院名称" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <AppButton @click="handleSearch">查询</AppButton>
          <AppButton variant="secondary" class="ml-2" @click="handleReset">重置</AppButton>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'info'">{{ Number(row.status) === 1 ? '启用' : '停用' }}</AppBadge>
        </template>
        <template #cell-actions="{ row }">
          <div class="flex justify-end gap-2">
            <button class="text-[12px] text-primary-700 hover:text-primary-800" @click="openEdit(row)">编辑</button>
            <button class="text-[12px] text-slatex-600 hover:text-slatex-900" @click="openBindAdmin(row)">绑定管理员</button>
            <button class="text-[12px] text-state-danger hover:opacity-80" @click="toggleStatus(row)">
              {{ Number(row.status) === 1 ? '停用' : '启用' }}
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

    <AppModal v-model="dialogVisible" :title="isEdit ? '编辑学院' : '新增学院'" width="640px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
        <el-form-item label="学院编码" prop="collegeCode">
          <el-input v-model="form.collegeCode" />
        </el-form-item>
        <el-form-item label="学院名称" prop="collegeName">
          <el-input v-model="form.collegeName" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="dialogVisible = false">取消</AppButton>
        <AppButton @click="submit">保存</AppButton>
      </template>
    </AppModal>

    <AppModal v-model="bindVisible" title="绑定学院管理员" width="520px">
      <el-form label-width="110px">
        <el-form-item label="管理员用户ID">
          <el-input-number v-model="bindAdminUserId" :min="1" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <AppButton variant="secondary" @click="bindVisible = false">取消</AppButton>
        <AppButton @click="submitBindAdmin">确定</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { bindCollegeAdmin, createCollege, getCollegeList, updateCollege, updateCollegeStatus } from '@/api/college'

const store = useStore()
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

const columns = [
  { key: 'collegeCode', title: '学院编码', width: 160 },
  { key: 'collegeName', title: '学院名称', width: 220 },
  { key: 'description', title: '描述' },
  { key: 'status', title: '状态', width: 100 },
  { key: 'actions', title: '操作', width: 250, align: 'right' }
]

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

const rules = {
  collegeCode: [{ required: true, message: '请输入学院编码', trigger: 'blur' }],
  collegeName: [{ required: true, message: '请输入学院名称', trigger: 'blur' }]
}

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
    ElMessage.success('更新成功')
  } else {
    await createCollege({
      collegeCode: form.collegeCode,
      collegeName: form.collegeName,
      description: form.description
    })
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchList()
}

async function submitBindAdmin() {
  if (!currentCollegeId.value || !bindAdminUserId.value) return
  await bindCollegeAdmin(currentCollegeId.value, bindAdminUserId.value)
  ElMessage.success('绑定成功')
  bindVisible.value = false
  fetchList()
}

async function toggleStatus(row) {
  const next = Number(row.status) === 1 ? 0 : 1
  await updateCollegeStatus(row.id, next)
  ElMessage.success('状态已更新')
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

