<template>
  <p class="app-field-hint">
    <slot />
  </p>
</template>

<style scoped>
.app-field-hint {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: color-mix(in srgb, var(--text-secondary) 88%, transparent);
}
</style>
