<template>
  <div class="syslog-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ t('sysLog.pageTitle') }}</span>
          <div class="header-filters">
            <el-input
              v-model="filters.userId"
              :placeholder="t('sysLog.userId')"
              clearable
              style="width: 150px"
              @change="loadLogs"
            />
            <el-select
              v-model="filters.status"
              :placeholder="t('sysLog.status')"
              clearable
              style="width: 120px"
              @change="loadLogs"
            >
              <el-option :label="t('sysLog.statusSuccess')" :value="1" />
              <el-option :label="t('sysLog.statusFailed')" :value="0" />
            </el-select>
            <el-input
              v-model="filters.operation"
              :placeholder="t('sysLog.operationPlaceholder')"
              clearable
              style="width: 200px"
              @change="loadLogs"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
          </div>
        </div>
      </template>

      <AppTable :columns="columns" :rows="logList" :loading="loading" :pagination="true"
        v-model:page="page" v-model:page-size="size" :total="total" @page-change="loadLogs">
        <template #cell-status="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? t('sysLog.statusSuccess') : t('sysLog.statusFailed') }}
          </el-tag>
        </template>
        <template #cell-duration="{ row }">
          <span>{{ row.duration }} ms</span>
        </template>
        <template #cell-errorMsg="{ row }">
          <el-text v-if="row.errorMsg" type="danger" size="small">{{ row.errorMsg }}</el-text>
          <span v-else>-</span>
        </template>
      </AppTable>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Search } from '@element-plus/icons-vue'
import { getSysLogList } from '@/api/sysLog'

const { t } = useI18n()

const loading = ref(false)
const logList = ref([])
const page = ref(1)
const size = ref(20)
const total = ref(0)

const filters = reactive({
  userId: '',
  status: '',
  operation: ''
})

const columns = [
  { key: 'userId', label: () => t('sysLog.userId'), width: 120 },
  { key: 'operation', label: () => t('sysLog.operation'), minWidth: 200 },
  { key: 'method', label: () => t('sysLog.method'), width: 80 },
  { key: 'ip', label: () => t('sysLog.ip'), width: 140 },
  { key: 'duration', label: () => t('sysLog.duration'), width: 100 },
  { key: 'status', label: () => t('sysLog.status'), width: 100 },
  { key: 'errorMsg', label: () => t('sysLog.errorMsg'), minWidth: 150 },
  { key: 'createTime', label: () => t('sysLog.createTime'), width: 180 }
]

const loadLogs = async () => {
  loading.value = true
  try {
    const params = {
      page: page.value,
      size: size.value,
      userId: filters.userId || undefined,
      status: filters.status || undefined,
      operation: filters.operation || undefined
    }
    const res = await getSysLogList(params)
    logList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load logs:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.syslog-page {
  padding: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.header-filters {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
</style>
