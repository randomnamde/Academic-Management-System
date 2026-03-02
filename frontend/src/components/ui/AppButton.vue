<template>
  <button
    :type="nativeType"
    :class="classes"
    :disabled="disabled || loading"
    @click="$emit('click', $event)"
  >
    <svg
      v-if="loading"
      class="mr-2 h-4 w-4 animate-spin"
      viewBox="0 0 24 24"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
    >
      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" class="opacity-25" />
      <path d="M22 12a10 10 0 0 1-10 10" stroke="currentColor" stroke-width="4" class="opacity-75" />
    </svg>
    <slot />
  </button>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  variant: {
    type: String,
    default: 'primary'
  },
  size: {
    type: String,
    default: 'md'
  },
  nativeType: {
    type: String,
    default: 'button'
  },
  loading: {
    type: Boolean,
    default: false
  },
  disabled: {
    type: Boolean,
    default: false
  },
  block: {
    type: Boolean,
    default: false
  }
})

defineEmits(['click'])

const variantClass = {
  primary: 'bg-brand-600 text-white hover:bg-brand-700',
  secondary: 'border border-slate-300 bg-white text-ink-700 hover:bg-slate-50',
  danger: 'bg-rose-600 text-white hover:bg-rose-700',
  ghost: 'bg-transparent text-ink-700 hover:bg-slate-100'
}

const sizeClass = {
  sm: 'h-8 px-3 text-xs',
  md: 'h-10 px-4 text-sm',
  lg: 'h-11 px-5 text-sm'
}

const classes = computed(() => [
  'inline-flex items-center justify-center rounded-xl font-medium transition active:scale-[0.98] disabled:opacity-60 disabled:cursor-not-allowed',
  variantClass[props.variant] || variantClass.primary,
  sizeClass[props.size] || sizeClass.md,
  props.block ? 'w-full' : ''
])
</script>
