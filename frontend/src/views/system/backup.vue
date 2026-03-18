<template>
  <div class="backup-page">
    <el-tabs v-model="activeTab">
      <el-tab-pane :label="t('backup.backupManagement')" name="backup">
        <div class="toolbar">
          <AppButton @click="openCreateDialog">
            <el-icon><Plus /></el-icon>
            {{ t('backup.createBackup') }}
          </AppButton>
        </div>

        <AppTable :columns="backupColumns" :rows="backupList" :loading="loading" :pagination="true"
          v-model:page="page" v-model:page-size="size" :total="total" @page-change="loadBackups">
          <template #cell-status="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
          <template #cell-fileSize="{ row }">
            {{ formatFileSize(row.fileSize) }}
          </template>
          <template #cell-actions="{ row }">
            <button class="app-table-action" @click="handleRestore(row)" :disabled="row.status !== 'SUCCESS'">
              {{ t('backup.restore') }}
            </button>
          </template>
        </AppTable>
      </el-tab-pane>

      <el-tab-pane :label="t('backup.strategyManagement')" name="strategy">
        <div class="toolbar">
          <AppButton @click="openStrategyDialog">
            <el-icon><Plus /></el-icon>
            {{ t('backup.addStrategy') }}
          </AppButton>
        </div>

        <AppTable :columns="strategyColumns" :rows="strategyList" :loading="loading">
          <template #cell-isEnabled="{ row }">
            <el-switch v-model="row.isEnabled" @change="toggleStrategy(row)" />
          </template>
          <template #cell-actions="{ row }">
            <button class="app-table-action" @click="openStrategyDialog(row)">{{ t('common.edit') }}</button>
            <button class="app-table-action app-table-action--danger" @click="handleDeleteStrategy(row)">{{ t('common.delete') }}</button>
          </template>
        </AppTable>
      </el-tab-pane>
    </el-tabs>

    <!-- Create Backup Dialog -->
    <el-dialog v-model="dialogVisible" :title="t('backup.createBackup')" width="500px">
      <el-form :model="backupForm" label-width="100px">
        <el-form-item :label="t('backup.backupName')">
          <el-input v-model="backupForm.backupName" :placeholder="t('backup.backupNamePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('backup.backupType')">
          <el-select v-model="backupForm.backupType">
            <el-option label="全量备份" value="FULL" />
            <el-option label="增量备份" value="PARTIAL" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleCreateBackup">{{ t('common.confirm') }}</AppButton>
      </template>
    </el-dialog>

    <!-- Strategy Dialog -->
    <el-dialog v-model="strategyDialogVisible" :title="isEdit ? t('common.edit') : t('backup.addStrategy')" width="500px">
      <el-form :model="strategyForm" label-width="100px">
        <el-form-item :label="t('backup.strategyName')">
          <el-input v-model="strategyForm.strategyName" />
        </el-form-item>
        <el-form-item :label="t('backup.backupType')">
          <el-select v-model="strategyForm.backupType">
            <el-option label="全量备份" value="FULL" />
            <el-option label="增量备份" value="PARTIAL" />
          </el-select>
        </el-form-item>
        <el-form-item :label="t('backup.scheduleCron')">
          <el-input v-model="strategyForm.scheduleCron" placeholder="0 2 * * *" />
        </el-form-item>
        <el-form-item :label="t('backup.retentionDays')">
          <el-input-number v-model="strategyForm.retentionDays" :min="1" :max="365" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="strategyDialogVisible = false">{{ t('common.cancel') }}</el-button>
        <AppButton @click="handleSaveStrategy">{{ t('common.save') }}</AppButton>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getBackupList, createBackup, restoreBackup, getStrategyList, createStrategy, updateStrategy, deleteStrategy } from '@/api/backup'

const { t } = useI18n()

const activeTab = ref('backup')
const loading = ref(false)
const backupList = ref([])
const strategyList = ref([])
const page = ref(1)
const size = ref(10)
const total = ref(0)
const dialogVisible = ref(false)
const strategyDialogVisible = ref(false)
const isEdit = ref(false)

const backupForm = reactive({
  backupName: '',
  backupType: 'FULL'
})

const strategyForm = reactive({
  id: null,
  strategyName: '',
  backupType: 'FULL',
  scheduleCron: '0 2 * * *',
  retentionDays: 7
})

const backupColumns = [
  { key: 'backupName', label: () => t('backup.backupName'), minWidth: 150 },
  { key: 'backupType', label: () => t('backup.backupType'), width: 100 },
  { key: 'status', label: () => t('backup.status'), width: 100 },
  { key: 'tableCount', label: () => t('backup.tableCount'), width: 100 },
  { key: 'fileSize', label: () => t('backup.fileSize'), width: 100 },
  { key: 'startTime', label: () => t('backup.startTime'), width: 160 },
  { key: 'endTime', label: () => t('backup.endTime'), width: 160 },
  { key: 'actions', label: () => t('common.actions'), width: 120 }
]

const strategyColumns = [
  { key: 'strategyName', label: () => t('backup.strategyName'), minWidth: 150 },
  { key: 'backupType', label: () => t('backup.backupType'), width: 100 },
  { key: 'scheduleCron', label: () => t('backup.scheduleCron'), width: 150 },
  { key: 'retentionDays', label: () => t('backup.retentionDays'), width: 100 },
  { key: 'isEnabled', label: () => t('backup.enabled'), width: 80 },
  { key: 'actions', label: () => t('common.actions'), width: 150 }
]

const loadBackups = async () => {
  loading.value = true
  try {
    const res = await getBackupList({ page: page.value, size: size.value })
    backupList.value = res.data?.records || []
    total.value = res.data?.total || 0
  } catch (error) {
    console.error('Failed to load backups:', error)
  } finally {
    loading.value = false
  }
}

const loadStrategies = async () => {
  try {
    const res = await getStrategyList()
    strategyList.value = res.data || []
  } catch (error) {
    console.error('Failed to load strategies:', error)
  }
}

const openCreateDialog = () => {
  backupForm.backupName = `Backup_${new Date().toISOString().slice(0, 10)}`
  backupForm.backupType = 'FULL'
  dialogVisible.value = true
}

const handleCreateBackup = async () => {
  try {
    await createBackup(backupForm)
    ElMessage.success(t('backup.backupStarted'))
    dialogVisible.value = false
    loadBackups()
  } catch (error) {
    ElMessage.error(t('backup.backupFailed'))
  }
}

const handleRestore = async (row) => {
  try {
    await ElMessageBox.confirm(t('backup.restoreConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await restoreBackup(row.id)
    ElMessage.success(t('backup.restoreStarted'))
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(t('backup.restoreFailed'))
    }
  }
}

const openStrategyDialog = (row = null) => {
  if (row) {
    isEdit.value = true
    Object.assign(strategyForm, row)
  } else {
    isEdit.value = false
    Object.assign(strategyForm, {
      id: null,
      strategyName: '',
      backupType: 'FULL',
      scheduleCron: '0 2 * * *',
      retentionDays: 7
    })
  }
  strategyDialogVisible.value = true
}

const handleSaveStrategy = async () => {
  try {
    if (isEdit.value) {
      await updateStrategy(strategyForm.id, strategyForm)
    } else {
      await createStrategy(strategyForm)
    }
    ElMessage.success(t('common.success'))
    strategyDialogVisible.value = false
    loadStrategies()
  } catch (error) {
    ElMessage.error(t('common.operationFailed'))
  }
}

const toggleStrategy = async (row) => {
  try {
    await updateStrategy(row.id, { isEnabled: row.isEnabled })
  } catch (error) {
    row.isEnabled = !row.isEnabled
  }
}

const handleDeleteStrategy = async (row) => {
  try {
    await ElMessageBox.confirm(t('backup.deleteConfirm'), t('common.tip'), {
      type: 'warning'
    })
    await deleteStrategy(row.id)
    ElMessage.success(t('common.success'))
    loadStrategies()
  } catch (error) {
    // cancel
  }
}

const getStatusType = (status) => {
  const map = { SUCCESS: 'success', FAILED: 'danger', RUNNING: 'warning', PENDING: 'info' }
  return map[status] || 'info'
}

const getStatusLabel = (status) => {
  const map = { SUCCESS: '成功', FAILED: '失败', RUNNING: '执行中', PENDING: '待执行' }
  return map[status] || status
}

const formatFileSize = (bytes) => {
  if (!bytes) return '-'
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  if (bytes < 1024 * 1024 * 1024) return (bytes / 1024 / 1024).toFixed(1) + ' MB'
  return (bytes / 1024 / 1024 / 1024).toFixed(1) + ' GB'
}

onMounted(() => {
  loadBackups()
  loadStrategies()
})
</script>

<style scoped>
.backup-page {
  padding: 16px;
}

.toolbar {
  margin-bottom: 16px;
}

.app-table-action:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
