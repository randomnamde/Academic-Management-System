<template>
  <div class="app-table-wrap">
    <table class="app-table">
      <thead>
        <tr>
          <th v-for="column in columns" :key="column.key" :style="column.width ? { width: column.width } : null">
            {{ column.title }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-if="!rows.length">
          <td :colspan="columns.length" class="px-4 py-8 text-center text-slate-500">暂无数据</td>
        </tr>
        <tr v-for="(row, index) in rows" :key="row[rowKey] ?? index">
          <td v-for="column in columns" :key="column.key">
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
defineProps({
  columns: {
    type: Array,
    default: () => []
  },
  rows: {
    type: Array,
    default: () => []
  },
  rowKey: {
    type: String,
    default: 'id'
  }
})
</script>
