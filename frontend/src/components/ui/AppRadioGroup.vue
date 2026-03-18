<template>
  <div class="app-radio-group" role="radiogroup" :aria-label="ariaLabel">
    <label v-for="item in options" :key="String(item.value)" class="app-radio">
      <input
        :name="name"
        :checked="modelValue === item.value"
        type="radio"
        :disabled="disabled || item.disabled"
        @change="$emit('update:modelValue', item.value)"
      />
      <span class="app-radio__dot" aria-hidden="true"></span>
      <span class="app-radio__label">{{ item.label }}</span>
    </label>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number, Boolean], default: '' },
  options: { type: Array, default: () => [] },
  name: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  ariaLabel: { type: String, default: '' }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
.app-radio-group {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
}

.app-radio {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: var(--text-primary);
}

.app-radio input {
  position: absolute;
  opacity: 0;
  pointer-events: none;
}

.app-radio__dot {
  width: 16px;
  height: 16px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 86%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  transition: border-color 180ms ease, box-shadow 180ms ease;
}

.app-radio input:checked + .app-radio__dot {
  border-color: var(--accent-500);
  box-shadow: inset 0 0 0 4px var(--accent-500);
}

.app-radio__label {
  font-size: 13px;
}
</style>
