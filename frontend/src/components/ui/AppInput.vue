<template>
  <input
    :value="modelValue"
    :type="type"
    :placeholder="placeholder"
    :disabled="disabled"
    :class="classes"
    @input="$emit('update:modelValue', $event.target.value)"
    @keyup.enter="$emit('enter')"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  type: {
    type: String,
    default: 'text'
  },
  placeholder: {
    type: String,
    default: ''
  },
  disabled: {
    type: Boolean,
    default: false
  }
})

defineEmits(['update:modelValue', 'enter'])

const classes = computed(() => [
  'app-input',
  props.disabled ? 'cursor-not-allowed bg-slate-100 text-slate-500' : ''
])
</script>
