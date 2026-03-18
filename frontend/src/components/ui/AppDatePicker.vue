<template>
  <input
    :id="id"
    :name="name"
    :value="modelValue"
    type="date"
    :min="min"
    :max="max"
    :disabled="disabled"
    :required="required"
    :aria-label="ariaLabel"
    :aria-describedby="ariaDescribedby"
    :aria-invalid="invalid ? 'true' : undefined"
    class="app-input"
    :class="{ 'is-invalid': invalid }"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<script setup>
defineProps({
  id: { type: String, default: '' },
  name: { type: String, default: '' },
  modelValue: { type: String, default: '' },
  min: { type: String, default: '' },
  max: { type: String, default: '' },
  disabled: { type: Boolean, default: false },
  required: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
  ariaLabel: { type: String, default: '' },
  ariaDescribedby: { type: String, default: '' }
})

defineEmits(['update:modelValue'])
</script>
