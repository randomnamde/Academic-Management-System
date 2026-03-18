<template>
  <AppDataTable v-bind="{ ...$props, ...$attrs }" v-slots="$slots" />
</template>

<script setup>
import AppDataTable from '@/components/ui/AppDataTable.vue'

defineProps({
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

</script>
