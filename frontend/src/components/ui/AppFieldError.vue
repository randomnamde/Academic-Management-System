<template>
  <p class="app-field-error" role="alert">
    <slot />
  </p>
</template>

<style scoped>
.app-field-error {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--danger);
}
</style>
