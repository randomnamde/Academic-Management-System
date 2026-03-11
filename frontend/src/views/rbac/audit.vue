<template>
  <CrudPageShell :title="t('rbac.audit.pageTitle')">
    <template #filters>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item :label="t('rbac.audit.filterUserId')">
          <el-input-number v-model="searchForm.userId" :min="1" style="width: 140px" />
        </el-form-item>
        <el-form-item :label="t('rbac.audit.filterStatus')">
          <el-select v-model="searchForm.status" clearable style="width: 120px">
            <el-option :label="t('rbac.common.success')" :value="1" />
            <el-option :label="t('rbac.common.failed')" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('rbac.audit.filterOperation')">
          <el-input v-model="searchForm.operation" clearable :placeholder="t('rbac.audit.filterOperationPlaceholder')" />
        </el-form-item>
        <el-form-item class="search-form__actions">
          <div class="app-filter-action-bar">
            <AppButton variant="secondary" @click="handleReset">{{ t('common.reset') }}</AppButton>
            <AppButton @click="handleSearch">{{ t('common.search') }}</AppButton>
          </div>
        </el-form-item>
      </el-form>
    </template>

    <template #table>
      <AppTable :columns="columns" :rows="tableData" :loading="loading" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="Number(row.status) === 1 ? 'success' : 'danger'">
            {{ Number(row.status) === 1 ? t('rbac.common.success') : t('rbac.common.failed') }}
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
import { useI18n } from 'vue-i18n'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppTable from '@/components/ui/AppTable.vue'
import { getSysLogList } from '@/api/sysLog'

const store = useStore()
const { t } = useI18n()
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

const columns = computed(() => [
  { key: 'userId', title: t('rbac.audit.colUserId'), width: 100 },
  { key: 'operation', title: t('rbac.audit.colOperation'), width: 220 },
  { key: 'method', title: t('rbac.audit.colMethod'), width: 220 },
  { key: 'ip', title: t('rbac.audit.colIp'), width: 140 },
  { key: 'duration', title: t('rbac.audit.colDuration'), width: 120, align: 'left' },
  { key: 'status', title: t('rbac.audit.colStatus'), width: 110, align: 'left' },
  { key: 'errorMsg', title: t('rbac.audit.colErrorMsg') },
  { key: 'createTime', title: t('rbac.audit.colCreateTime'), width: 170 }
])

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

