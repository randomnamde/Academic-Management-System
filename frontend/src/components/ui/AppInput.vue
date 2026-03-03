<template>
  <input
    :id="id"
    :name="name"
    :value="modelValue"
    :type="type"
    :placeholder="placeholder"
    :autocomplete="autocomplete"
    :inputmode="inputmode"
    :required="required"
    :readonly="readonly"
    :disabled="disabled"
    :aria-label="ariaLabel"
    :aria-describedby="ariaDescribedby"
    :class="classes"
    @input="$emit('update:modelValue', $event.target.value)"
    @keyup.enter="$emit('enter')"
  />
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  id: { type: String, default: '' },
  name: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  autocomplete: { type: String, default: '' },
  inputmode: { type: String, default: '' },
  ariaLabel: { type: String, default: '' },
  ariaDescribedby: { type: String, default: '' },
  required: { type: Boolean, default: false },
  readonly: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false }
})

defineEmits(['update:modelValue', 'enter'])

const classes = computed(() => [
  'app-input',
  props.disabled ? 'cursor-not-allowed bg-neutralx-100 text-slatex-500' : ''
])
</script>
