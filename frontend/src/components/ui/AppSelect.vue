<template>
  <select
    :id="id"
    :name="name"
    :value="modelValue"
    :disabled="disabled"
    :required="required"
    :aria-label="ariaLabel"
    :aria-describedby="ariaDescribedby"
    :aria-invalid="invalid ? 'true' : undefined"
    class="app-select"
    :class="{ 'is-invalid': invalid }"
    @change="$emit('update:modelValue', normalizeValue($event.target.value))"
  >
    <option v-if="placeholder" value="">{{ placeholder }}</option>
    <template v-for="item in normalizedOptions" :key="item.groupKey || item.value">
      <optgroup v-if="item.isGroup" :label="item.label">
        <option v-for="option in item.options" :key="option.value" :value="option.value" :disabled="option.disabled">
          {{ option.label }}
        </option>
      </optgroup>
      <option v-else :value="item.value" :disabled="item.disabled">
        {{ item.label }}
      </option>
    </template>
  </select>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  id: { type: String, default: '' },
  name: { type: String, default: '' },
  modelValue: { type: [String, Number, Boolean], default: '' },
  options: { type: Array, default: () => [] },
  placeholder: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  required: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
  ariaLabel: { type: String, default: '' },
  ariaDescribedby: { type: String, default: '' },
  valueType: { type: String, default: 'string' }
})

defineEmits(['update:modelValue'])

const normalizedOptions = computed(() =>
  props.options.map((item) => {
    if (item?.options) {
      return {
        isGroup: true,
        groupKey: item.label,
        label: item.label,
        options: item.options
      }
    }
    return {
      isGroup: false,
      label: item.label,
      value: item.value,
      disabled: Boolean(item.disabled)
    }
  })
)

function normalizeValue(value) {
  if (value === '') return value
  if (props.valueType === 'number') return Number(value)
  if (props.valueType === 'boolean') return value === 'true'
  return value
}
</script>

<style scoped>
.app-select {
  width: 100%;
  min-height: 40px;
  border-radius: var(--radius-control);
  border: 1px solid color-mix(in srgb, var(--panel-border) 90%, transparent);
  background: color-mix(in srgb, var(--surface-base) 86%, transparent);
  color: var(--text-primary);
  padding: 0 14px;
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.app-select:focus {
  border-color: var(--accent-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent-500) 22%, transparent);
}

.app-select:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.app-select.is-invalid {
  border-color: var(--danger);
}
</style>
