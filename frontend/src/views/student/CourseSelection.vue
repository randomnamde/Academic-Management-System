<template>
  <CrudPageShell :title="t('selection.pageTitle')">
    <template v-if="isAdmin" #header-actions>
      <AppButton @click="handlePreload">{{ t('selection.preloadInventory') }}</AppButton>
    </template>

    <div class="selection-page-hero mb-6 p-4">
      <div class="flex items-start gap-3">
        <div class="selection-page-hero__icon">
          <CalendarRange class="h-5 w-5" />
        </div>
        <div>
          <h3 class="selection-page-hero__title">{{ t('selection.pageTitle') }}</h3>
          <p class="selection-page-hero__desc">{{ t('selection.pageDesc') }}</p>
        </div>
      </div>
    </div>

    <template #table>
      <AppTable :columns="columns" :rows="courseList" :loading="loading" :density="tableDensity">
        <template #cell-enrolledCount="{ row }">
          <div class="flex items-center gap-2">
            <span>{{ row.enrolledCount }} / {{ row.capacity }}</span>
            <div class="h-1.5 w-16 overflow-hidden rounded-full bg-slatex-100">
              <div
                class="h-full bg-primary-500 transition-all duration-500"
                :style="{ width: `${Math.min((row.enrolledCount / row.capacity) * 100, 100)}%` }"
              ></div>
            </div>
          </div>
        </template>

        <template #cell-actions="{ row }">
          <div class="app-table-actions">
            <AppButton
              size="sm"
              :disabled="row.enrolledCount >= row.capacity || isProcessing(row.id)"
              :loading="isProcessing(row.id)"
              @click="handleSelect(row)"
            >
              {{ isProcessing(row.id) ? t('selection.queuing') : t('selection.select') }}
            </AppButton>
          </div>
        </template>
      </AppTable>
    </template>

    <div v-if="mySelections.length > 0" class="mt-8">
      <h3 class="mb-4 text-lg font-semibold text-slatex-900">{{ t('selection.mySelections') }}</h3>
      <AppTable :columns="mySelectionColumns" :rows="mySelections" :density="tableDensity">
        <template #cell-status="{ row }">
          <AppBadge :type="statusBadgeType(row.status)">
            {{ t(`selection.${row.status.toLowerCase()}`) }}
          </AppBadge>
        </template>
      </AppTable>
    </div>

    <AppModal v-model="confirmVisible" :title="t('common.tip')" width="560px">
      <div class="selection-confirm">
        <div class="selection-confirm__hero">
          <div class="selection-confirm__icon">
            <CalendarRange class="h-5 w-5" />
          </div>
          <div class="selection-confirm__copy">
            <p class="selection-confirm__eyebrow">{{ t('selection.pageTitle') }}</p>
            <p class="selection-confirm__headline">{{ t('selection.selectConfirm') }}</p>
            <p class="selection-confirm__desc">{{ t('selection.pageDesc') }}</p>
          </div>
        </div>

        <div v-if="pendingSelectionRow" class="selection-confirm__panel">
          <div class="selection-confirm__grid">
            <div class="selection-confirm__item">
              <span>{{ t('courseArrangement.course') }}</span>
              <strong>{{ pendingSelectionRow.courseName || '-' }}</strong>
            </div>
            <div class="selection-confirm__item">
              <span>{{ t('courseArrangement.teacher') }}</span>
              <strong>{{ pendingSelectionRow.teacherName || '-' }}</strong>
            </div>
            <div class="selection-confirm__item">
              <span>{{ t('courseArrangement.schedule') }}</span>
              <strong>{{ pendingSelectionRow.schedule || '-' }}</strong>
            </div>
            <div class="selection-confirm__item">
              <span>{{ t('selection.enrolledCount') }}</span>
              <strong>{{ pendingSelectionRow.enrolledCount }} / {{ pendingSelectionRow.capacity }}</strong>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <AppButton variant="secondary" @click="closeConfirmDialog">{{ t('common.cancel') }}</AppButton>
        <AppButton :loading="selectSubmitting" @click="confirmSelection">{{ t('selection.select') }}</AppButton>
      </template>
    </AppModal>
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import { ElMessage } from 'element-plus'
import { CalendarRange } from 'lucide-vue-next'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppModal from '@/components/ui/AppModal.vue'
import AppTable from '@/components/ui/AppTable.vue'
import { getCourseArrangementList } from '@/api/courseArrangement'
import { getSelectionResult, preloadInventory, selectCourse } from '@/api/selection'

const { t } = useI18n()
const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)
const user = computed(() => store.state.userInfo)
const isAdmin = computed(() => store.getters.isAdmin)

const loading = ref(false)
const courseList = ref([])
const mySelections = ref([])
const processingIds = ref(new Set())
const confirmVisible = ref(false)
const selectSubmitting = ref(false)
const pendingSelectionRow = ref(null)

const columns = computed(() => [
  { key: 'arrangementCode', title: t('courseArrangement.arrangementCode'), width: 140 },
  { key: 'courseName', title: t('courseArrangement.course'), width: 200 },
  { key: 'teacherName', title: t('courseArrangement.teacher'), width: 120 },
  { key: 'schedule', title: t('courseArrangement.schedule'), width: 180 },
  { key: 'room', title: t('courseArrangement.room'), width: 120 },
  { key: 'enrolledCount', title: t('selection.enrolledCount'), width: 180 },
  { key: 'actions', title: t('common.actions'), width: 120, align: 'left' }
])

const mySelectionColumns = computed(() => [
  { key: 'courseName', title: t('courseArrangement.course') },
  { key: 'teacherName', title: t('courseArrangement.teacher') },
  { key: 'status', title: t('selection.status'), width: 120 }
])

async function fetchList() {
  loading.value = true
  try {
    const res = await getCourseArrangementList({ page: 1, size: 100 })
    courseList.value = res.data?.records || []
  } finally {
    loading.value = false
  }
}

function isProcessing(id) {
  return processingIds.value.has(id)
}

function handleSelect(row) {
  pendingSelectionRow.value = row
  confirmVisible.value = true
}

function closeConfirmDialog() {
  confirmVisible.value = false
  pendingSelectionRow.value = null
}

async function confirmSelection() {
  if (!pendingSelectionRow.value) return

  selectSubmitting.value = true
  try {
    const row = pendingSelectionRow.value
    const res = await selectCourse({
      studentId: user.value.id,
      courseArrangementId: row.id
    })

    showSelectionMessage('success', res.data)
    processingIds.value.add(row.id)
    closeConfirmDialog()
    pollResult(row.id)
  } catch (error) {
    console.error(error)
  } finally {
    selectSubmitting.value = false
  }
}

async function pollResult(arrangementId) {
  const timer = setInterval(async () => {
    try {
      const res = await getSelectionResult({
        studentId: user.value.id,
        arrangementId
      })

      const status = res.data
      if (status !== 'QUEUING') {
        clearInterval(timer)
        processingIds.value.delete(arrangementId)

        if (status === 'SUCCESS') {
          showSelectionMessage('success', t('selection.success'))
        } else {
          showSelectionMessage('error', t('selection.failed'))
        }

        fetchList()
      }
    } catch (_error) {
      clearInterval(timer)
      processingIds.value.delete(arrangementId)
    }
  }, 2000)
}

async function handlePreload() {
  await preloadInventory()
  showSelectionMessage('success', t('selection.preloadSuccess'))
}

function statusBadgeType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'DROPPED') return 'info'
  return 'warning'
}

function showSelectionMessage(type, message) {
  ElMessage({
    type,
    message,
    showClose: true,
    duration: 2400,
    customClass: `selection-flow-message selection-flow-message--${type}`
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.selection-page-hero {
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--accent-500) 18%, var(--panel-border));
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 10%, transparent), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 90%, transparent), color-mix(in srgb, var(--surface-elevated) 82%, transparent));
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, var(--color-white) 14%, transparent),
    0 14px 30px color-mix(in srgb, var(--color-black) 8%, transparent);
}

.selection-page-hero__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  padding: 10px;
  color: var(--accent-600);
  background: color-mix(in srgb, var(--accent-500) 12%, transparent);
  box-shadow: inset 0 1px 0 color-mix(in srgb, var(--color-white) 12%, transparent);
}

.selection-page-hero__title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
}

.selection-page-hero__desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: color-mix(in srgb, var(--text-secondary) 92%, transparent);
}

.selection-confirm {
  display: grid;
  gap: 16px;
}

.selection-confirm__hero {
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.selection-confirm__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 48px;
  height: 48px;
  border-radius: 16px;
  flex-shrink: 0;
  color: var(--text-on-accent);
  background:
    radial-gradient(circle at 30% 25%, color-mix(in srgb, var(--color-white) 42%, transparent), transparent 52%),
    linear-gradient(135deg, var(--accent-500), var(--accent-700));
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, var(--color-white) 22%, transparent),
    0 16px 28px color-mix(in srgb, var(--accent-500) 20%, transparent);
}

.selection-confirm__copy {
  min-width: 0;
}

.selection-confirm__eyebrow,
.selection-confirm__headline,
.selection-confirm__desc {
  margin: 0;
}

.selection-confirm__eyebrow {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.selection-confirm__headline {
  margin-top: 5px;
  font-family: var(--font-display);
  font-size: 20px;
  line-height: 1.35;
  color: var(--text-primary);
}

.selection-confirm__desc {
  margin-top: 7px;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.selection-confirm__panel {
  border: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  border-radius: 18px;
  padding: 14px;
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 90%, transparent), color-mix(in srgb, var(--surface-elevated) 82%, transparent));
}

.selection-confirm__grid {
  display: grid;
  gap: 10px;
}

.selection-confirm__item {
  display: grid;
  gap: 4px;
  border-radius: 14px;
  padding: 10px 12px;
  background: color-mix(in srgb, var(--surface-elevated) 76%, transparent);
}

.selection-confirm__item span {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.selection-confirm__item strong {
  font-size: 14px;
  line-height: 1.55;
  color: var(--text-primary);
}

:global(.selection-flow-message) {
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 84%, transparent));
  box-shadow:
    0 18px 40px color-mix(in srgb, var(--color-black) 12%, transparent),
    inset 0 1px 0 color-mix(in srgb, var(--color-white) 16%, transparent);
  backdrop-filter: blur(14px) saturate(130%);
  padding: 12px 14px;
}

:global(.selection-flow-message .el-message__content) {
  font-size: 13px;
  line-height: 1.65;
  color: var(--text-primary);
}

:global(.selection-flow-message .el-message__icon) {
  font-size: 18px;
}

:global(.selection-flow-message .el-message__closeBtn) {
  color: var(--text-secondary);
}

:global(.selection-flow-message--success) {
  border-color: color-mix(in srgb, var(--accent-500) 24%, var(--panel-border));
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 12%, transparent), transparent 40%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 84%, transparent));
}

:global(.selection-flow-message--success .el-message__icon) {
  color: var(--accent-600);
}

:global(.selection-flow-message--error) {
  border-color: color-mix(in srgb, var(--danger) 24%, var(--panel-border));
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--danger) 10%, transparent), transparent 40%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 84%, transparent));
}

:global(.selection-flow-message--error .el-message__icon) {
  color: var(--danger);
}

@media (min-width: 640px) {
  .selection-confirm__grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
