<template>
  <section class="app-panel" :class="bodyClass">
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
defineProps({
  title: {
    type: String,
    default: ''
  },
  bodyClass: {
    type: String,
    default: ''
  },
  contentClass: {
    type: String,
    default: 'p-5'
  }
})
</script>
