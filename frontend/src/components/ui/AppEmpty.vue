<template>
  <div class="app-empty">
    <div class="app-empty__icon" aria-hidden="true">
      <slot name="icon">
        <span>∅</span>
      </slot>
    </div>
    <div class="app-empty__body">
      <p class="app-empty__title">{{ title }}</p>
      <p v-if="description" class="app-empty__description">{{ description }}</p>
    </div>
    <div v-if="$slots.action" class="app-empty__action">
      <slot name="action" />
    </div>
  </div>
</template>

<script setup>
defineProps({
  title: { type: String, default: '' },
  description: { type: String, default: '' }
})
</script>

<style scoped>
.app-empty {
  display: grid;
  justify-items: center;
  gap: 14px;
  padding: 28px 20px;
  border-radius: 18px;
  border: 1px dashed color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  text-align: center;
}

.app-empty__icon {
  display: grid;
  place-items: center;
  width: 44px;
  height: 44px;
  border-radius: 16px;
  color: var(--text-secondary);
  background: color-mix(in srgb, var(--surface-elevated) 82%, transparent);
}

.app-empty__icon span {
  display: none;
}

.app-empty__icon::before {
  content: 'empty';
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.app-empty__body {
  display: grid;
  gap: 6px;
}

.app-empty__title,
.app-empty__description {
  margin: 0;
}

.app-empty__title {
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.app-empty__description {
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
}
</style>
