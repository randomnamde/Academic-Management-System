<template>
  <button
    :type="nativeType"
    :class="classes"
    :disabled="disabled || loading"
    :aria-busy="loading ? 'true' : undefined"
    @click="$emit('click', $event)"
  >
    <svg
      v-if="loading"
      class="mr-2 h-3.5 w-3.5 animate-spin"
      aria-hidden="true"
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
  tone: { type: String, default: 'default' },
  size: { type: String, default: 'md' },
  nativeType: { type: String, default: 'button' },
  loading: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  block: { type: Boolean, default: false }
})

defineEmits(['click'])

const toneClass = {
  default: 'border border-[var(--button-primary-bg)] bg-[var(--button-primary-bg)] text-[var(--button-primary-text)] hover:bg-[var(--button-primary-bg-hover)] hover:border-[var(--button-primary-bg-hover)] active:bg-[var(--button-primary-bg-hover)] active:border-[var(--button-primary-bg-hover)] shadow-soft',
  accent: 'border border-[var(--button-primary-bg)] bg-[var(--button-primary-bg)] text-[var(--button-primary-text)] hover:bg-[var(--button-primary-bg-hover)] hover:border-[var(--button-primary-bg-hover)] active:bg-[var(--button-primary-bg-hover)] active:border-[var(--button-primary-bg-hover)] shadow-soft',
  neutral: 'border border-[var(--button-secondary-border)] bg-[var(--button-secondary-bg)] text-[var(--button-secondary-text)] hover:bg-[var(--button-secondary-bg-hover)] hover:border-[var(--button-secondary-border-hover)] active:bg-[var(--button-secondary-bg-hover)] active:border-[var(--button-secondary-border-hover)]'
}

const variantClass = {
  primary: '',
  secondary: 'border border-[var(--button-secondary-border)] bg-[var(--button-secondary-bg)] text-[var(--button-secondary-text)] hover:bg-[var(--button-secondary-bg-hover)] hover:border-[var(--button-secondary-border-hover)] active:bg-[var(--button-secondary-bg-hover)] active:border-[var(--button-secondary-border-hover)]',
  ghost: 'border border-transparent bg-transparent text-[var(--text-primary)] hover:bg-[var(--button-secondary-bg-hover)] hover:text-[var(--text-primary)]',
  danger: 'border border-[var(--button-danger-bg)] bg-[var(--button-danger-bg)] text-[var(--button-danger-text)] hover:bg-[var(--button-danger-bg-hover)] hover:border-[var(--button-danger-bg-hover)] active:bg-[var(--button-danger-bg-hover)] active:border-[var(--button-danger-bg-hover)]',
  text: 'border border-transparent bg-transparent text-[var(--accent-500)] hover:text-[var(--accent-600)]'
}

const sizeClass = {
  sm: 'h-7 px-2.5 text-[12px]',
  md: 'h-8 px-3 text-[13px]',
  lg: 'h-9 px-3.5 text-[13px]'
}

const classes = computed(() => [
  'inline-flex items-center justify-center rounded-sm font-medium transition-all duration-180 ease-in-out disabled:cursor-not-allowed disabled:opacity-55 min-h-[44px] touch-manipulation md:min-h-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[var(--accent-500)]/45',
  props.variant === 'primary' ? (toneClass[props.tone] || toneClass.default) : (variantClass[props.variant] || variantClass.primary),
  sizeClass[props.size] || sizeClass.md,
  props.block ? 'w-full' : ''
])
</script>

