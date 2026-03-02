<template>
  <div class="app-table-wrap">
    <table class="app-table" :class="densityClass">
      <thead>
        <tr>
          <th
            v-for="column in columns"
            :key="column.key"
            :style="column.width ? { width: withUnit(column.width) } : null"
            :class="alignClass(column.align)"
          >
            {{ column.title }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="loading">
          <td :colspan="columns.length" class="px-3 py-6 text-center text-[12px] text-slatex-500">加载中...</td>
        </tr>
        <tr v-else-if="!rows.length">
          <td :colspan="columns.length" class="px-3 py-6 text-center text-[12px] text-slatex-500">暂无数据</td>
        </tr>
        <tr v-else v-for="(row, index) in rows" :key="row[rowKey] ?? index">
          <td
            v-for="column in columns"
            :key="column.key"
            :class="alignClass(column.align)"
          >
            <slot :name="`cell-${column.key}`" :row="row" :index="index">
              {{ row[column.key] }}
            </slot>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  columns: { type: Array, default: () => [] },
  rows: { type: Array, default: () => [] },
  rowKey: { type: String, default: 'id' },
  density: { type: String, default: 'compact' },
  loading: { type: Boolean, default: false }
})

const densityClass = computed(() => (props.density === 'comfortable' ? 'density-comfortable' : 'density-compact'))

function alignClass(align = 'left') {
  if (align === 'right') return 'text-right'
  if (align === 'center') return 'text-center'
  return 'text-left'
}

function withUnit(value) {
  return typeof value === 'number' ? `${value}px` : value
}
</script>

<style scoped>
.density-compact td,
.density-compact th {
  height: 34px;
}

.density-comfortable td,
.density-comfortable th {
  height: 40px;
}
</style>
