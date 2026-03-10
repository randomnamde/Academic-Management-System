<template>
  <CrudPageShell :title="t('selection.pageTitle')">
    <template #header-actions v-if="isAdmin">
      <AppButton @click="handlePreload">{{ t('selection.preloadInventory') }}</AppButton>
    </template>

    <div class="mb-6 rounded-xl border border-primary-100 bg-primary-50/30 p-4">
      <div class="flex items-start gap-3">
        <div class="rounded-lg bg-primary-100 p-2 text-primary-600">
          <CalendarRange class="h-5 w-5" />
        </div>
        <div>
          <h3 class="font-medium text-primary-900">{{ t('selection.pageTitle') }}</h3>
          <p class="text-sm text-primary-700/80">{{ t('selection.pageDesc') }}</p>
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
          <div class="flex justify-end gap-2">
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

    <!-- 选课状态展示区域 (如果有的话) -->
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
  </CrudPageShell>
</template>

<script setup>
import { computed, onMounted, ref, reactive } from 'vue'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import { ElMessage, ElMessageBox } from 'element-plus'
import CrudPageShell from '@/components/shell/CrudPageShell.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppTable from '@/components/ui/AppTable.vue'
import AppBadge from '@/components/ui/AppBadge.vue'
import { CalendarRange } from 'lucide-vue-next'
import { getCourseArrangementList } from '@/api/courseArrangement'
import { selectCourse, getSelectionResult, preloadInventory } from '@/api/selection'

const { t } = useI18n()
const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)
const user = computed(() => store.state.userInfo)
const isAdmin = computed(() => store.getters.isAdmin)

const loading = ref(false)
const courseList = ref([])
const mySelections = ref([])
const processingIds = ref(new Set())

const columns = computed(() => [
  { key: 'arrangementCode', title: t('courseArrangement.arrangementCode'), width: 140 },
  { key: 'courseName', title: t('courseArrangement.course'), width: 200 },
  { key: 'teacherName', title: t('courseArrangement.teacher'), width: 120 },
  { key: 'schedule', title: t('courseArrangement.schedule'), width: 180 },
  { key: 'room', title: t('courseArrangement.room'), width: 120 },
  { key: 'enrolledCount', title: t('selection.enrolledCount'), width: 180 },
  { key: 'actions', title: t('common.actions'), width: 120, align: 'right' }
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

async function handleSelect(row) {
  try {
    await ElMessageBox.confirm(
      t('selection.selectConfirm'),
      t('common.tip'),
      { type: 'info' }
    )

    const res = await selectCourse({
      studentId: user.value.id,
      courseArrangementId: row.id
    })
    
    ElMessage.success(res.data)
    processingIds.value.add(row.id)
    
    // 开始轮询结果
    pollResult(row.id)
  } catch (e) {
    if (e !== 'cancel') {
      console.error(e)
    }
  }
}

async function pollResult(arrangementId) {
  const timer = setInterval(async () => {
    try {
      const res = await getSelectionResult({
        studentId: user.value.id,
        arrangementId: arrangementId
      })
      
      const status = res.data
      if (status !== 'QUEUING') {
        clearInterval(timer)
        processingIds.value.delete(arrangementId)
        
        if (status === 'SUCCESS') {
          ElMessage.success(t('selection.success'))
        } else {
          ElMessage.error(t('selection.failed'))
        }
        
        fetchList()
        // 这里可以更新已选列表
      }
    } catch (e) {
      clearInterval(timer)
      processingIds.value.delete(arrangementId)
    }
  }, 2000)
}

async function handlePreload() {
  await preloadInventory()
  ElMessage.success(t('selection.preloadSuccess'))
}

function statusBadgeType(status) {
  if (status === 'SUCCESS') return 'success'
  if (status === 'FAILED') return 'danger'
  if (status === 'DROPPED') return 'info'
  return 'warning'
}

onMounted(() => {
  fetchList()
})
</script>
