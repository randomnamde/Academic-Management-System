<template>
  <div class="flex items-center justify-between gap-3 border-t border-neutralx-200 pt-3">
    <p class="text-[12px] text-slatex-500">{{ t('common.totalRecords', { total }) }}</p>
    <div class="flex items-center gap-2">
      <button class="app-btn-secondary h-7 px-2" :disabled="page <= 1" @click="change(page - 1)">{{ t('common.prevPage') }}</button>
      <span class="text-[12px] text-slatex-600">{{ page }} / {{ totalPages }}</span>
      <button class="app-btn-secondary h-7 px-2" :disabled="page >= totalPages" @click="change(page + 1)">{{ t('common.nextPage') }}</button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  page: { type: Number, default: 1 },
  size: { type: Number, default: 10 },
  total: { type: Number, default: 0 }
})

const emit = defineEmits(['update:page'])
const { t } = useI18n()

const totalPages = computed(() => Math.max(1, Math.ceil(props.total / props.size)))

function change(next) {
  if (next < 1 || next > totalPages.value) return
  emit('update:page', next)
}
</script>
