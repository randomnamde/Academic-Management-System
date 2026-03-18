<template>
  <span :class="classes"><slot /></span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  type: { type: String, default: 'info' },
  emphasis: { type: String, default: 'soft' }
})

const classes = computed(() => {
  return ['app-badge', `app-badge--${props.emphasis}`, `app-badge--${props.type || 'info'}`]
})
</script>

<style scoped>
.app-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  border: 1px solid transparent;
  font-size: 12px;
  font-weight: 600;
  line-height: 1;
  white-space: nowrap;
}

.app-badge--soft.app-badge--success {
  color: var(--success);
  background: color-mix(in srgb, var(--success) 12%, transparent);
  border-color: color-mix(in srgb, var(--success) 28%, transparent);
}

.app-badge--soft.app-badge--warning {
  color: var(--warn);
  background: color-mix(in srgb, var(--warn) 12%, transparent);
  border-color: color-mix(in srgb, var(--warn) 28%, transparent);
}

.app-badge--soft.app-badge--danger {
  color: var(--danger);
  background: color-mix(in srgb, var(--danger) 12%, transparent);
  border-color: color-mix(in srgb, var(--danger) 28%, transparent);
}

.app-badge--soft.app-badge--info {
  color: var(--accent-600);
  background: color-mix(in srgb, var(--accent-500) 12%, transparent);
  border-color: color-mix(in srgb, var(--accent-500) 28%, transparent);
}

.app-badge--solid.app-badge--success,
.app-badge--solid.app-badge--warning,
.app-badge--solid.app-badge--danger,
.app-badge--solid.app-badge--info {
  color: var(--text-on-accent);
}

.app-badge--solid.app-badge--success {
  background: var(--success);
  border-color: var(--success);
}

.app-badge--solid.app-badge--warning {
  background: var(--warn);
  border-color: var(--warn);
}

.app-badge--solid.app-badge--danger {
  background: var(--danger);
  border-color: var(--danger);
}

.app-badge--solid.app-badge--info {
  background: var(--accent-600);
  border-color: var(--accent-600);
}
</style>

