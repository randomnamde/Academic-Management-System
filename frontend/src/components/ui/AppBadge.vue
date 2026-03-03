<template>
  <span :class="classes"><slot /></span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'info' },
  emphasis: { type: String, default: 'soft' }
})

const solidClassMap = {
  success: 'bg-state-success text-white border-state-success',
  warning: 'bg-state-warn text-white border-state-warn',
  danger: 'bg-state-danger text-white border-state-danger',
  info: 'bg-[var(--accent-600)] text-white border-[var(--accent-600)]'
}

const softClassMap = {
  success: 'app-tag-success',
  warning: 'app-tag-warning',
  danger: 'app-tag-danger',
  info: 'app-tag-info'
}

const classes = computed(() => {
  if (props.emphasis === 'solid') {
    return `app-tag ${solidClassMap[props.type] || solidClassMap.info}`
  }
  return softClassMap[props.type] || softClassMap.info
})
</script>

