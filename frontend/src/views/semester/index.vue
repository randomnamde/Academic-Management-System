<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('semester.pageTitle')" content-class="p-4 space-y-4">
      <div class="semester-hero">
        <div class="semester-hero-copy">
          <p class="semester-hero-badge">{{ t('semester.heroBadge') }}</p>
          <h2 class="semester-hero-title">{{ t('semester.pageTitle') }}</h2>
          <p class="semester-hero-desc">{{ t('semester.pageDesc') }}</p>
          <p class="semester-hero-insight">
            {{ currentActiveSemester ? t('semester.heroInsightActive', { code: currentActiveSemester.semesterCode }) : t('semester.heroInsightEmpty') }}
          </p>
        </div>

        <div class="semester-hero-pill">
          <span class="semester-hero-pill-label">{{ t('semester.activeLabel') }}</span>
          <strong>{{ currentActiveSemester?.semesterCode || t('semester.emptyActive') }}</strong>
        </div>
      </div>

      <div class="semester-stats">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="semester-stat"
        >
          <p class="semester-stat-label">{{ stat.label }}</p>
          <div class="semester-stat-row">
            <p class="semester-stat-value">{{ stat.value }}</p>
            <span class="semester-stat-note">{{ stat.note }}</span>
          </div>
        </div>
      </div>

      <section class="semester-panel">
        <div class="semester-panel-head">
          <div>
            <p class="semester-panel-title">{{ t('semester.filterTitle') }}</p>
            <p class="semester-panel-desc">{{ t('semester.filterDesc') }}</p>
          </div>
          <div class="semester-panel-actions">
            <AppButton variant="secondary" @click="page = 1; loadData()">{{ t('common.search') }}</AppButton>
            <AppButton variant="secondary" @click="resetFilters">{{ t('common.reset') }}</AppButton>
            <AppButton @click="openCreateDialog">{{ t('semester.createAction') }}</AppButton>
          </div>
        </div>

        <el-form class="semester-filter-form" label-position="top">
          <el-form-item :label="t('semester.semesterCode')">
            <el-input
              v-model="filters.semesterCode"
              clearable
              :placeholder="t('semester.semesterCodePlaceholder')"
              @keyup.enter="loadData"
            />
          </el-form-item>
          <el-form-item :label="t('semester.statusLabel')">
            <el-select v-model="filters.status" clearable :placeholder="t('semester.selectStatus')">
              <el-option
                v-for="option in statusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </el-form>
      </section>

      <section class="semester-panel">
        <div class="semester-panel-head">
          <div>
            <p class="semester-panel-title">{{ t('semester.listTitle') }}</p>
            <p class="semester-panel-desc">{{ t('semester.listDesc') }}</p>
          </div>
        </div>

        <el-table v-loading="loading" :data="records" class="semester-table">
          <el-table-column prop="semesterCode" :label="t('semester.semesterCode')" min-width="180" />
          <el-table-column :label="t('semester.dateRange')" min-width="240">
            <template #default="{ row }">{{ formatRange(row.startDate, row.endDate) }}</template>
          </el-table-column>
          <el-table-column :label="t('semester.statusLabel')" width="130">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ t(`semester.status.${row.status}`) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" :label="t('semester.remark')" min-width="220" show-overflow-tooltip />
          <el-table-column :label="t('semester.updatedAt')" width="180">
            <template #default="{ row }">{{ formatDateTime(row.updateTime || row.createTime) }}</template>
          </el-table-column>
          <el-table-column :label="t('common.actions')" min-width="300" fixed="right">
            <template #default="{ row }">
              <div class="semester-row-actions">
                <AppButton size="sm" variant="secondary" @click="openEditDialog(row)">{{ t('common.edit') }}</AppButton>
                <AppButton v-if="row.status !== 'ACTIVE'" size="sm" @click="changeStatus(row, 'ACTIVE')">{{ t('semester.actions.activate') }}</AppButton>
                <AppButton v-if="row.status !== 'ENDED'" size="sm" variant="secondary" @click="changeStatus(row, 'ENDED')">{{ t('semester.actions.end') }}</AppButton>
                <AppButton v-if="row.status !== 'ARCHIVED'" size="sm" variant="secondary" @click="changeStatus(row, 'ARCHIVED')">{{ t('semester.actions.archive') }}</AppButton>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          class="pagination"
          v-model:current-page="page"
          v-model:page-size="size"
          :total="total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @current-change="loadPage"
          @size-change="handleSizeChange"
        />
      </section>
    </AppCard>

    <el-dialog
      v-model="dialogVisible"
      :title="isEditing ? t('semester.editTitle') : t('semester.createTitle')"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item :label="t('semester.semesterCode')" prop="semesterCode">
          <el-input v-model="form.semesterCode" :placeholder="t('semester.semesterCodePlaceholder')" />
        </el-form-item>
        <el-form-item :label="t('semester.dateRange')" prop="dateRange">
          <el-date-picker
            v-model="form.dateRange"
            type="daterange"
            unlink-panels
            value-format="YYYY-MM-DD"
            :start-placeholder="t('semester.startDate')"
            :end-placeholder="t('semester.endDate')"
            class="w-full"
          />
        </el-form-item>
        <el-form-item :label="t('semester.remark')" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" :placeholder="t('semester.remarkPlaceholder')" />
        </el-form-item>
      </el-form>

      <template #footer>
        <div class="dialog-footer">
          <AppButton variant="secondary" @click="dialogVisible = false">{{ t('common.cancel') }}</AppButton>
          <AppButton :loading="saving" @click="submitForm">{{ t('common.save') }}</AppButton>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { createSemester, getSemesterList, updateSemester, updateSemesterStatus } from '@/api/semester'

const { t } = useI18n()

const loading = ref(false)
const saving = ref(false)
const dialogVisible = ref(false)
const isEditing = ref(false)
const editingId = ref(null)
const records = ref([])
const total = ref(0)
const page = ref(1)
const size = ref(10)
const currentActiveSemester = ref(null)
const statsRaw = reactive({
  total: 0,
  active: 0,
  planned: 0
})

const filters = reactive({
  semesterCode: '',
  status: ''
})

const formRef = ref()
const form = reactive({
  semesterCode: '',
  dateRange: [],
  remark: ''
})

const statusOptions = computed(() => ([
  { value: 'PLANNED', label: t('semester.status.PLANNED') },
  { value: 'ACTIVE', label: t('semester.status.ACTIVE') },
  { value: 'ENDED', label: t('semester.status.ENDED') },
  { value: 'ARCHIVED', label: t('semester.status.ARCHIVED') }
]))

const stats = computed(() => ([
  {
    label: t('semester.stats.totalLabel'),
    value: statsRaw.total,
    note: t('semester.stats.totalNote')
  },
  {
    label: t('semester.stats.activeLabel'),
    value: statsRaw.active,
    note: t('semester.stats.activeNote')
  },
  {
    label: t('semester.stats.plannedLabel'),
    value: statsRaw.planned,
    note: t('semester.stats.plannedNote')
  }
]))

const rules = computed(() => ({
  semesterCode: [{ required: true, message: t('semester.validation.semesterCode'), trigger: 'blur' }],
  dateRange: [{ required: true, message: t('semester.validation.dateRange'), trigger: 'change' }]
}))

function buildQuery(extra = {}) {
  return {
    page: page.value,
    size: size.value,
    semesterCode: filters.semesterCode?.trim() || undefined,
    status: filters.status || undefined,
    ...extra
  }
}

function resetForm() {
  editingId.value = null
  isEditing.value = false
  form.semesterCode = ''
  form.dateRange = []
  form.remark = ''
}

function openCreateDialog() {
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  editingId.value = row.id
  isEditing.value = true
  form.semesterCode = row.semesterCode || ''
  form.dateRange = [row.startDate, row.endDate]
  form.remark = row.remark || ''
  dialogVisible.value = true
}

function resetFilters() {
  filters.semesterCode = ''
  filters.status = ''
  page.value = 1
  loadData()
}

function formatRange(startDate, endDate) {
  if (!startDate && !endDate) return '--'
  return `${startDate || '--'} ~ ${endDate || '--'}`
}

function formatDateTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}`
}

function statusTagType(status) {
  if (status === 'ACTIVE') return 'success'
  if (status === 'PLANNED') return 'warning'
  if (status === 'ARCHIVED') return 'info'
  return ''
}

async function loadPage() {
  loading.value = true
  try {
    const res = await getSemesterList(buildQuery())
    records.value = res.data?.records || []
    total.value = Number(res.data?.total || 0)
  } finally {
    loading.value = false
  }
}

async function loadStats() {
  const [totalRes, activeRes, plannedRes] = await Promise.all([
    getSemesterList({ page: 1, size: 1 }),
    getSemesterList({ page: 1, size: 1, status: 'ACTIVE' }),
    getSemesterList({ page: 1, size: 1, status: 'PLANNED' })
  ])
  statsRaw.total = Number(totalRes.data?.total || 0)
  statsRaw.active = Number(activeRes.data?.total || 0)
  statsRaw.planned = Number(plannedRes.data?.total || 0)
  currentActiveSemester.value = activeRes.data?.records?.[0] || null
}

async function loadData() {
  await Promise.all([loadPage(), loadStats()])
}

async function handleSizeChange() {
  page.value = 1
  await loadPage()
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const payload = {
    semesterCode: form.semesterCode.trim(),
    startDate: form.dateRange?.[0],
    endDate: form.dateRange?.[1],
    remark: form.remark?.trim() || ''
  }

  saving.value = true
  try {
    if (isEditing.value && editingId.value) {
      await updateSemester(editingId.value, payload)
      ElMessage.success(t('semester.messages.updateSuccess'))
    } else {
      await createSemester(payload)
      ElMessage.success(t('semester.messages.createSuccess'))
    }
    dialogVisible.value = false
    page.value = 1
    await loadData()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row, status) {
  try {
    await ElMessageBox.confirm(
      t('semester.messages.statusConfirm', { code: row.semesterCode, status: t(`semester.status.${status}`) }),
      t('common.tip'),
      {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        type: 'warning'
      }
    )
    await updateSemesterStatus(row.id, status)
    ElMessage.success(t('semester.messages.statusSuccess'))
    await loadData()
  } catch (_error) {
    // Ignore cancel actions.
  }
}

onMounted(loadData)
</script>

<style scoped lang="scss">
.semester-hero {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 20px 22px;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    radial-gradient(circle at top left, rgba(176, 92, 23, 0.16), transparent 42%),
    linear-gradient(135deg, color-mix(in srgb, var(--surface-elevated) 88%, transparent), color-mix(in srgb, var(--surface-base) 92%, transparent));
}

.semester-hero-copy {
  display: grid;
  gap: 10px;
  max-width: 760px;
}

.semester-hero-badge {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.semester-hero-title {
  margin: 0;
  font-size: 30px;
  line-height: 1.15;
  color: var(--text-primary);
}

.semester-hero-desc,
.semester-hero-insight {
  margin: 0;
  font-size: 14px;
  line-height: 1.85;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
}

.semester-hero-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 38px;
  padding: 0 14px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 74%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  color: var(--text-primary);
  font-size: 13px;
}

.semester-hero-pill-label {
  color: var(--text-secondary);
}

.semester-stats {
  display: grid;
  gap: 12px;
}

.semester-stat {
  display: grid;
  gap: 10px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.semester-stat-label {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.semester-stat-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.semester-stat-value {
  margin: 0;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: var(--text-primary);
}

.semester-stat-note {
  font-size: 12px;
  color: color-mix(in srgb, var(--text-primary) 66%, var(--text-secondary));
}

.semester-panel {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.semester-panel-head {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.semester-panel-title {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.semester-panel-desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.75;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
}

.semester-panel-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.semester-filter-form {
  display: grid;
  gap: 10px 14px;
}

.semester-table {
  width: 100%;
}

.semester-row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.pagination {
  justify-content: flex-end;
}

@media (min-width: 768px) {
  .semester-stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .semester-filter-form {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
