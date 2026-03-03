<template>
  <section class="app-panel" :class="[surfaceClass, bodyClass]">
    <header v-if="$slots.header || title" class="app-panel-header">
      <div class="app-panel-title">{{ title }}</div>
      <div class="flex items-center gap-2"><slot name="header" /></div>
    </header>
    <div :class="contentClass">
      <slot />
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  title: { type: String, default: '' },
  bodyClass: { type: String, default: '' },
  contentClass: { type: String, default: 'p-4' },
  surface: { type: String, default: 'glass' }
})

const surfaceClass = computed(() => {
  if (props.surface === 'base') return 'app-surface-base'
  if (props.surface === 'elevated') return 'app-surface-elevated'
  return 'app-surface-glass'
})
</script>

