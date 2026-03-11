<template>
  <button
    :type="nativeType"
    :class="classes"
    :disabled="disabled || loading"
    :aria-busy="loading ? 'true' : undefined"
    @click="$emit('click', $event)"
  >
    <span class="app-button__sheen" aria-hidden="true"></span>
    <svg
      v-if="loading"
      class="app-button__spinner"
      aria-hidden="true"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" class="opacity-25" />
      <path d="M22 12a10 10 0 0 1-10 10" stroke="currentColor" stroke-width="3" class="opacity-75" />
    </svg>
    <span class="app-button__label"><slot /></span>
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'primary' },
  tone: { type: String, default: 'default' },
  size: { type: String, default: 'md' },
  nativeType: { type: String, default: 'button' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: false }
})

defineEmits(['click'])

const resolvedVariant = computed(() => {
  if (props.variant === 'primary' && props.tone === 'neutral') return 'secondary'
  if (props.variant === 'primary' && props.tone === 'accent') return 'primary'
  return props.variant
})

const classes = computed(() => [
  'app-button',
  `app-button--${resolvedVariant.value}`,
  `app-button--${props.size}`,
  props.block ? 'app-button--block' : '',
  props.loading ? 'is-loading' : ''
])
</script>

<style scoped>
.app-button {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  border-radius: var(--button-radius);
  border: 1px solid transparent;
  padding: 0 16px;
  overflow: hidden;
  isolation: isolate;
  font-family: var(--font-sans);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 1;
  white-space: nowrap;
  user-select: none;
  touch-action: manipulation;
  transition:
    transform 180ms ease,
    border-color 180ms ease,
    background 180ms ease,
    box-shadow 200ms ease,
    color 180ms ease,
    opacity 180ms ease;
}

.app-button::before {
  content: '';
  position: absolute;
  inset: 1px;
  border-radius: calc(var(--button-radius) - 1px);
  pointer-events: none;
  opacity: 0.9;
  background: linear-gradient(180deg, color-mix(in srgb, var(--color-white) 10%, transparent), transparent 48%);
}

.app-button:hover:not(:disabled) {
  transform: translateY(-1px);
}

.app-button:active:not(:disabled) {
  transform: translateY(0);
}

.app-button:focus-visible {
  outline: none;
  box-shadow:
    var(--button-shadow, none),
    0 0 0 4px var(--button-ring);
}

.app-button:disabled {
  cursor: not-allowed;
  opacity: 0.58;
  box-shadow: none;
}

.app-button--block {
  width: 100%;
}

.app-button__sheen {
  position: absolute;
  top: -34%;
  left: -24%;
  width: 52%;
  height: 180%;
  transform: rotate(18deg);
  pointer-events: none;
  opacity: 0.48;
  background: linear-gradient(
    115deg,
    transparent 0%,
    color-mix(in srgb, var(--color-white) 26%, transparent) 50%,
    transparent 100%
  );
  transition: transform 240ms ease, opacity 220ms ease;
}

.app-button:hover:not(:disabled) .app-button__sheen,
.app-button:focus-visible .app-button__sheen {
  transform: translateX(18%) rotate(18deg);
  opacity: 0.72;
}

.app-button__spinner,
.app-button__label {
  position: relative;
  z-index: 1;
}

.app-button__label {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  white-space: nowrap;
}

.app-button__spinner {
  width: 14px;
  height: 14px;
  animation: app-button-spin 0.9s linear infinite;
}

.app-button--sm {
  min-height: 32px;
  padding: 0 12px;
  border-radius: 12px;
  font-size: 12px;
}

.app-button--md {
  min-height: 36px;
  padding: 0 16px;
}

.app-button--lg {
  min-height: 42px;
  padding: 0 18px;
  font-size: 14px;
}

.app-button--primary {
  --button-shadow: var(--button-primary-shadow);
  color: var(--button-primary-text);
  border-color: var(--button-primary-border);
  background: var(--button-primary-surface);
  box-shadow: var(--button-primary-shadow);
}

.app-button--primary:hover:not(:disabled),
.app-button--primary:active:not(:disabled) {
  background: var(--button-primary-surface-hover);
}

.app-button--secondary {
  --button-shadow: var(--button-secondary-shadow);
  color: var(--button-secondary-text);
  border-color: var(--button-secondary-border);
  background: var(--button-secondary-surface);
  box-shadow: var(--button-secondary-shadow);
}

.app-button--secondary:hover:not(:disabled),
.app-button--secondary:active:not(:disabled) {
  border-color: var(--button-secondary-border-hover);
  background: var(--button-secondary-surface-hover);
}

.app-button--ghost {
  --button-shadow: none;
  color: var(--text-primary);
  border-color: color-mix(in srgb, var(--panel-border) 42%, transparent);
  background: var(--button-ghost-surface);
  box-shadow: inset 0 1px 0 color-mix(in srgb, var(--color-white) 10%, transparent);
}

.app-button--ghost:hover:not(:disabled),
.app-button--ghost:active:not(:disabled) {
  background: var(--button-ghost-surface-hover);
  border-color: color-mix(in srgb, var(--panel-border) 72%, transparent);
}

.app-button--danger {
  --button-shadow: var(--button-danger-shadow);
  color: var(--button-danger-text);
  border-color: var(--button-danger-border);
  background: var(--button-danger-surface);
  box-shadow: var(--button-danger-shadow);
}

.app-button--danger:hover:not(:disabled),
.app-button--danger:active:not(:disabled) {
  background: var(--button-danger-surface-hover);
}

.app-button--text {
  --button-shadow: none;
  min-height: auto;
  padding: 0;
  border-color: transparent;
  background: transparent;
  color: var(--accent-600);
  border-radius: 10px;
}

.app-button--text::before,
.app-button--text .app-button__sheen {
  display: none;
}

.app-button--text:hover:not(:disabled),
.app-button--text:active:not(:disabled) {
  background: var(--button-text-hover);
  color: var(--accent-700);
  box-shadow: none;
}

@media (max-width: 767px) {
  .app-button--sm,
  .app-button--md,
  .app-button--lg {
    min-height: 44px;
  }
}

@keyframes app-button-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
