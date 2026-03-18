<template>
  <section class="app-toolbar">
    <div class="app-toolbar__content">
      <slot />
    </div>
    <div v-if="$slots.actions" class="app-toolbar__actions">
      <slot name="actions" />
    </div>
  </section>
</template>

<style scoped>
.app-toolbar {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 16px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 80%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
}

.app-toolbar__content,
.app-toolbar__actions {
  min-width: 0;
}

.app-toolbar__actions {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
