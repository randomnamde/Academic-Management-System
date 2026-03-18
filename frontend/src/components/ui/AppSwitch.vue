<template>
  <label class="app-switch" :class="{ 'is-disabled': disabled }">
    <input
      :checked="modelValue"
      type="checkbox"
      :disabled="disabled"
      @change="handleChange"
    />
    <span class="app-switch__track" aria-hidden="true">
      <span class="app-switch__thumb"></span>
    </span>
    <span v-if="$slots.default" class="app-switch__label">
      <slot />
    </span>
  </label>
</template>

<script setup>
defineProps({
  modelValue: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'change'])

function handleChange(event) {
  const value = event.target.checked
  emit('update:modelValue', value)
  emit('change', value)
}
</script>

<style scoped>
.app-switch {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  color: var(--text-primary);
}

.app-switch input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.app-switch__track {
  position: relative;
  width: 40px;
  height: 22px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 84%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 72%, transparent);
  transition: background 180ms ease, border-color 180ms ease;
}

.app-switch__thumb {
  position: absolute;
  top: 50%;
  left: 3px;
  width: 16px;
  height: 16px;
  border-radius: 999px;
  background: var(--surface-base);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.16);
  transform: translateY(-50%);
  transition: transform 180ms ease;
}

.app-switch input:checked + .app-switch__track {
  border-color: var(--accent-500);
  background: var(--accent-500);
}

.app-switch input:checked + .app-switch__track .app-switch__thumb {
  transform: translate(18px, -50%);
}

.app-switch.is-disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.app-switch__label {
  font-size: 13px;
}
</style>
