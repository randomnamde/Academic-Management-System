<template>
  <input
    :id="id"
    :name="name"
    :value="modelValue"
    type="number"
    :min="min"
    :max="max"
    :step="step"
    :placeholder="placeholder"
    :disabled="disabled"
    :readonly="readonly"
    :required="required"
    :aria-label="ariaLabel"
    :aria-describedby="ariaDescribedby"
    :aria-invalid="invalid ? 'true' : undefined"
    class="app-input app-number-input"
    :class="{ 'is-invalid': invalid }"
    @input="$emit('update:modelValue', normalizeValue($event.target.value))"
  />
</template>

<script setup>
defineProps({
  id: { type: String, default: '' },
  name: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '' },
  min: { type: [Number, String], default: undefined },
  max: { type: [Number, String], default: undefined },
  step: { type: [Number, String], default: 1 },
  disabled: { type: Boolean, default: false },
  readonly: { type: Boolean, default: false },
  required: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
  ariaLabel: { type: String, default: '' },
  ariaDescribedby: { type: String, default: '' }
})

const emit = defineEmits(['update:modelValue'])

function normalizeValue(value) {
  if (value === '') return ''
  const parsed = Number(value)
  return Number.isNaN(parsed) ? '' : parsed
}
</script>
