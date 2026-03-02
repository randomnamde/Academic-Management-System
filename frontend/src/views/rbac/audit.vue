<template>
  <CrudPageShell title="Role & Permission / 审计日志">
    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="用户ID">
          <el-input-number v-model="searchForm.userId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作">
          <el-input v-model="searchForm.operation" clearable placeholder="Controller#method" />
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
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'danger'">
            {{ Number(row.status) === 1 ? '成功' : '失败' }}
          </AppBadge>
        </template>
      </AppTable>
    </template>

    <template #pagination>
      <el-pagination
        class="pagination"
        v-model:current-page="page"
        v-model:page-size="size"
        :total="total"
        :page-sizes="[20, 50, 100]"
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
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import { getSysLogList } from '@/api/sysLog'

const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)

const loading = ref(false)
const page = ref(1)
const size = ref(20)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  userId: null,
  status: null,
  operation: ''
})

const columns = [
  { key: 'userId', title: '用户ID', width: 100 },
  { key: 'operation', title: '操作', width: 220 },
  { key: 'method', title: '请求', width: 220 },
  { key: 'ip', title: 'IP', width: 140 },
  { key: 'duration', title: '耗时(ms)', width: 120, align: 'right' },
  { key: 'status', title: '状态', width: 110, align: 'center' },
  { key: 'errorMsg', title: '错误信息' },
  { key: 'createTime', title: '时间', width: 170 }
]

async function fetchList() {
  loading.value = true
  try {
    const res = await getSysLogList({
      page: page.value,
      size: size.value,
      userId: searchForm.userId || undefined,
      status: searchForm.status,
      operation: searchForm.operation || undefined
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
  searchForm.userId = null
  searchForm.status = null
  searchForm.operation = ''
  handleSearch()
}

onMounted(fetchList)
</script>

<style scoped>
.pagination {
  margin-top: 8px;
  justify-content: flex-end;
}
</style>
