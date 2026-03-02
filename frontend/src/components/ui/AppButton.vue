<template>
  <button
    :type="nativeType"
    :class="classes"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <svg
      v-if="loading"
      class="mr-2 h-3.5 w-3.5 animate-spin"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" class="opacity-25" />
      <path d="M22 12a10 10 0 0 1-10 10" stroke="currentColor" stroke-width="3" class="opacity-75" />
    </svg>
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: { type: String, default: 'primary' },
  size: { type: String, default: 'md' },
  nativeType: { type: String, default: 'button' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: false }
})

defineEmits(['click'])

const variantClass = {
  primary: 'border border-primary-800 bg-primary-800 text-white hover:bg-primary-700 hover:border-primary-700',
  secondary: 'border border-neutralx-200 bg-white text-slatex-700 hover:bg-neutralx-100',
  ghost: 'border border-transparent bg-transparent text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900',
  danger: 'border border-state-danger bg-state-danger text-white hover:opacity-90',
  text: 'border border-transparent bg-transparent text-primary-700 hover:text-primary-800'
}

const sizeClass = {
  sm: 'h-7 px-2.5 text-[12px]',
  md: 'h-8 px-3 text-[13px]',
  lg: 'h-9 px-3.5 text-[13px]'
}

const classes = computed(() => [
  'inline-flex items-center justify-center rounded-sm font-medium transition-all duration-180 ease-in-out disabled:cursor-not-allowed disabled:opacity-55',
  variantClass[props.variant] || variantClass.primary,
  sizeClass[props.size] || sizeClass.md,
  props.block ? 'w-full' : ''
])
</script>
