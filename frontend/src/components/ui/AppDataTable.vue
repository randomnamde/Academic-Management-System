<template>
  <div
    class="app-data-table-wrap"
    :class="{ 'is-fixed-header': fixedHeader, 'is-virtual-scroll': virtualScroll }"
    :style="wrapStyle"
    ref="tableWrapRef"
  >
    <div v-if="loading && !rows.length" class="app-data-table-state">
      <slot name="loading">
        <div class="app-data-table-placeholder">{{ t('common.loading') }}</div>
      </slot>
    </div>

    <div v-else-if="!rows.length" class="app-data-table-state">
      <slot name="empty">
        <div class="app-data-table-placeholder">{{ emptyText || t('common.noData') }}</div>
      </slot>
    </div>

    <template v-else>
      <div v-if="virtualScroll" class="virtual-scroll-spacer" :style="{ height: totalHeight + 'px' }">
        <table class="app-data-table" :class="densityClass">
          <thead>
            <tr>
              <th
                v-for="column in columns"
                :key="column.key"
                :style="headerStyle(column)"
                :class="alignClass(column.align)"
                :width="column.width"
              >
                <slot :name="`header-${column.key}`" :column="column">
                  {{ column.title }}
                </slot>
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="(row, index) in visibleRows"
              :key="row[rowKey] ?? index"
              :style="{ transform: `translateY(${getRowOffset(index)}px)` }"
            >
              <td
                v-for="column in columns"
                :key="column.key"
                :style="cellStyle(column)"
                :class="alignClass(column.align)"
              >
                <slot :name="`cell-${column.key}`" :row="row" :index="virtualStartIndex + index">
                  {{ row[column.key] }}
                </slot>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <table v-else class="app-data-table" :class="densityClass">
        <thead>
          <tr>
            <th
              v-for="column in columns"
              :key="column.key"
              :style="headerStyle(column)"
              :class="alignClass(column.align)"
              :width="column.width"
            >
              <slot :name="`header-${column.key}`" :column="column">
                {{ column.title }}
              </slot>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="(row, index) in rows"
            :key="row[rowKey] ?? index"
          >
            <td
              v-for="column in columns"
              :key="column.key"
              :style="cellStyle(column)"
              :class="alignClass(column.align)"
            >
              <slot :name="`cell-${column.key}`" :row="row" :index="index">
                {{ row[column.key] }}
              </slot>
            </td>
          </tr>
        </tbody>
      </table>
    </template>
  </div>

  <slot name="pagination">
    <AppPagination
      v-if="showPagination"
      :page="page"
      :size="pageSize"
      :total="total"
      :page-sizes="pageSizes"
      @update:page="updatePage"
      @update:size="updatePageSize"
    />
  </slot>
</template>

<script setup>
import { computed, ref, onMounted, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AppPagination from '@/components/ui/AppPagination.vue'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  rowKey: { type: String, default: 'id' },
  density: { type: String, default: 'compact' },
  loading: { type: Boolean, default: false },
  fixedHeader: { type: Boolean, default: false },
  maxHeight: { type: [String, Number], default: 500 },
  virtualScroll: { type: Boolean, default: false },
  estimatedRowHeight: { type: Number, default: 40 },
  emptyText: { type: String, default: '' },
  pagination: { type: Boolean, default: false },
  page: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageSizes: { type: Array, default: () => [10, 20, 50] }
})

const emit = defineEmits(['update:page', 'update:pageSize', 'page-change', 'size-change'])
const { t } = useI18n()

const tableWrapRef = ref(null)
const scrollTop = ref(0)

const densityClass = computed(() => (props.density === 'comfortable' ? 'density-comfortable' : 'density-compact'))
const showPagination = computed(() => props.pagination && props.total > 0)

const wrapStyle = computed(() => {
  if (props.fixedHeader || props.virtualScroll) {
    return {
      maxHeight: typeof props.maxHeight === 'number' ? `${props.maxHeight}px` : props.maxHeight,
      overflowY: 'auto'
    }
  }
  return {}
})

const rowHeight = computed(() => props.estimatedRowHeight)
const totalHeight = computed(() => props.rows.length * rowHeight.value)

const visibleCount = computed(() => {
  const visibleHeight = Number(props.maxHeight) || 500
  return Math.ceil(visibleHeight / rowHeight.value) + 5
})

const virtualStartIndex = computed(() => {
  return Math.max(0, Math.floor(scrollTop.value / rowHeight.value) - 2)
})

const visibleRows = computed(() => {
  const start = virtualStartIndex.value
  const count = Math.min(visibleCount.value, props.rows.length - start)
  return props.rows.slice(start, start + count)
})

const getRowOffset = (index) => (virtualStartIndex.value + index) * rowHeight.value

let rafId = null
const handleScroll = (e) => {
  if (rafId) return
  rafId = requestAnimationFrame(() => {
    scrollTop.value = e.target.scrollTop
    rafId = null
  })
}

onMounted(() => {
  if (props.virtualScroll && tableWrapRef.value) {
    tableWrapRef.value.addEventListener('scroll', handleScroll, { passive: true })
  }
})

onUnmounted(() => {
  if (tableWrapRef.value) {
    tableWrapRef.value.removeEventListener('scroll', handleScroll)
  }
  if (rafId) {
    cancelAnimationFrame(rafId)
  }
})

function updatePage(nextPage) {
  emit('update:page', nextPage)
  emit('page-change', nextPage)
}

function updatePageSize(nextSize) {
  emit('update:pageSize', nextSize)
  emit('size-change', nextSize)
}

function alignClass(align = 'left') {
  if (align === 'right') return 'text-right'
  if (align === 'center') return 'text-center'
  return 'text-left'
}

function withUnit(value) {
  return typeof value === 'number' ? `${value}px` : value
}

function normalizeAlign(align = 'left') {
  if (align === 'center' || align === 'right') return align
  return 'left'
}

function headerStyle(column = {}) {
  const style = {
    textAlign: normalizeAlign(column.align)
  }
  if (column.width) style.width = withUnit(column.width)
  return style
}

function cellStyle(column = {}) {
  return {
    textAlign: normalizeAlign(column.align)
  }
}
</script>

<style scoped>
.app-data-table-wrap {
  min-width: 0;
}

.app-data-table-state {
  padding: 24px 0;
}

.app-data-table-placeholder {
  display: grid;
  place-items: center;
  min-height: 140px;
  border-radius: 16px;
  border: 1px dashed color-mix(in srgb, var(--panel-border) 72%, transparent);
  color: var(--text-secondary);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
}

.density-compact td,
.density-compact th {
  height: 34px;
}

.density-comfortable td,
.density-comfortable th {
  height: 40px;
}

.app-data-table-wrap.is-fixed-header {
  overflow-y: auto;
}

.app-data-table-wrap.is-fixed-header thead {
  position: sticky;
  top: 0;
  z-index: 1;
}

.app-data-table-wrap.is-fixed-header thead th {
  background-color: var(--el-table-header-bg-color, #f5f7fa);
  box-shadow: 0 1px 0 0 var(--el-border-color-lighter, #ebeef5);
}

.app-data-table-wrap.is-virtual-scroll {
  overflow-y: auto;
  contain: strict;
}

.app-data-table {
  width: 100%;
  border-collapse: collapse;
  border-radius: var(--radius-control);
  border: 1px solid color-mix(in srgb, var(--panel-border) 88%, transparent);
  background: var(--el-table-bg-color);
  overflow: hidden;
}

.app-data-table th,
.app-data-table td {
  padding: 0 12px;
  border-bottom: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  color: var(--text-primary);
  vertical-align: middle;
}

.app-data-table th {
  color: var(--text-secondary);
  font-weight: 600;
  background: var(--el-table-header-bg-color);
}

.app-data-table tbody tr:hover > td {
  background: var(--el-table-row-hover-bg-color) !important;
}

.virtual-scroll-spacer {
  position: relative;
  width: 100%;
}

.virtual-scroll-spacer table {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
}

.virtual-scroll-spacer tbody tr {
  position: absolute;
  left: 0;
  right: 0;
  transition: transform 0.1s ease-out;
}

:deep(.el-pagination) {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
