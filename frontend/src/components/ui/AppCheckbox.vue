<template>
  <label class="app-checkbox" :class="{ 'is-disabled': disabled }">
    <input
      :checked="modelValue"
      type="checkbox"
      :disabled="disabled"
      :aria-invalid="invalid ? 'true' : undefined"
      @change="$emit('update:modelValue', $event.target.checked)"
    />
    <span class="app-checkbox__box" aria-hidden="true"></span>
    <span v-if="$slots.default" class="app-checkbox__label">
      <slot />
    </span>
  </label>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.app-checkbox {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: var(--text-primary);
}

.app-checkbox input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.app-checkbox__box {
  display: inline-grid;
  place-items: center;
  width: 18px;
  height: 18px;
  border-radius: 6px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 86%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  transition: background 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.app-checkbox input:checked + .app-checkbox__box {
  border-color: var(--accent-500);
  background: var(--accent-500);
}

.app-checkbox input:checked + .app-checkbox__box::after {
  content: '';
  width: 9px;
  height: 5px;
  border-left: 2px solid var(--text-on-accent);
  border-bottom: 2px solid var(--text-on-accent);
  transform: translateY(-1px) rotate(-45deg);
}

.app-checkbox__label {
  font-size: 13px;
}

.app-checkbox.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
}
</style>
