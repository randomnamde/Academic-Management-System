<template>
  <CrudPageShell title="Role & Permission / 用户">
    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" clearable placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.role" clearable style="width: 140px">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="学生" value="STUDENT" />
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
        <template #cell-role="{ row }">
          <AppBadge :type="roleBadgeType(row.role)">{{ roleLabel(row.role) }}</AppBadge>
        </template>
        <template #cell-status="{ row }">
          <div class="flex items-center gap-2">
            <el-switch :model-value="row.status === 1" @change="(val) => handleStatusChange(row, val)" />
            <span class="text-[12px] text-slatex-500">{{ row.status === 1 ? '启用' : '停用' }}</span>
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
import { getUserList, updateUserStatus } from '@/api/user'

const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  username: '',
  role: ''
})

const columns = [
  { key: 'username', title: '用户名', width: 180 },
  { key: 'realName', title: '姓名', width: 160 },
  { key: 'role', title: '角色', width: 140 },
  { key: 'phone', title: '电话', width: 160 },
  { key: 'email', title: '邮箱' },
  { key: 'status', title: '状态', width: 140 }
]

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: size.value,
      username: searchForm.username || undefined,
      role: searchForm.role || undefined
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
  searchForm.username = ''
  searchForm.role = ''
  handleSearch()
}

function roleLabel(role) {
  if (role === 'ADMIN') return '管理员'
  if (role === 'TEACHER') return '教师'
  if (role === 'STUDENT') return '学生'
  return role
}

function roleBadgeType(role) {
  if (role === 'ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  if (role === 'STUDENT') return 'success'
  return 'info'
}

async function handleStatusChange(row, enabled) {
  await updateUserStatus(row.id, enabled ? 1 : 0)
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
