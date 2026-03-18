<template>
  <div class="app-pagination">
    <p class="app-pagination__summary">{{ t('common.totalRecords', { total }) }}</p>
    <div class="app-pagination__controls">
      <button class="app-pagination__button" :disabled="page <= 1" @click="change(page - 1)">
        {{ t('common.prevPage') }}
      </button>
      <span class="app-pagination__page">{{ page }} / {{ totalPages }}</span>
      <button class="app-pagination__button" :disabled="page >= totalPages" @click="change(page + 1)">
        {{ t('common.nextPage') }}
      </button>

      <label v-if="showPageSize" class="app-pagination__size">
        <span>{{ t('common.pageSize') }}</span>
        <select :value="size" @change="changeSize($event.target.value)">
          <option v-for="option in pageSizes" :key="option" :value="option">{{ option }}</option>
        </select>
      </label>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pageSizes: { type: Array, default: () => [] },
  showPageSize: { type: Boolean, default: false }
})

const emit = defineEmits(['update:page', 'update:size'])
const { t } = useI18n()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

function change(next) {
  if (next < 1 || next > totalPages.value) return
  emit('update:page', next)
}

function changeSize(next) {
  emit('update:size', Number(next))
}
</script>

<style scoped>
.app-pagination {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
}

.app-pagination__summary {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.app-pagination__controls {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
}

.app-pagination__button {
  min-height: 28px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: var(--button-secondary-surface);
  color: var(--button-secondary-text);
}

.app-pagination__button:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.app-pagination__page {
  font-size: 12px;
  color: var(--text-secondary);
}

.app-pagination__size {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--text-secondary);
}

.app-pagination__size select {
  min-height: 28px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: var(--surface-base);
  color: var(--text-primary);
  padding: 0 10px;
}
</style>
