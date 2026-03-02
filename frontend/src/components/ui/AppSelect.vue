<template>
  <select
    :value="modelValue"
    :disabled="disabled"
    class="app-select"
    @change="$emit('update:modelValue', normalizeValue($event.target.value))"
  >
    <option v-if="placeholder" value="">{{ placeholder }}</option>
    <option v-for="item in options" :key="item.value" :value="item.value">{{ item.label }}</option>
  </select>
</template>

<script setup>
const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  options: {
    type: Array,
    default: () => []
  },
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  },
  valueType: {
    type: String,
    default: 'string'
  }
})

defineEmits(['update:modelValue'])

function normalizeValue(value) {
  if (value === '') return value
  if (props.valueType === 'number') return Number(value)
  return value
}
</script>
