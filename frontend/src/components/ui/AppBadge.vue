<template>
  <span :class="classes"><slot /></span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'info' }
})

const classes = computed(() => {
  if (props.type === 'success') return 'app-tag-success'
  if (props.type === 'warning') return 'app-tag-warning'
  if (props.type === 'danger') return 'app-tag-danger'
  return 'app-tag-info'
})
</script>
