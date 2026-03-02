<template>
  <Transition name="app-fade">
    <div v-if="modelValue" class="fixed inset-0 z-40 overflow-y-auto">
      <div class="app-modal-backdrop" @click="closeOnBackdrop && $emit('update:modelValue', false)"></div>
      <div class="relative z-50 flex min-h-full items-center justify-center p-4">
        <div class="app-modal-panel" :style="panelStyle">
          <header class="mb-4 flex items-center justify-between">
            <h3 class="text-lg font-semibold text-ink-900">{{ title }}</h3>
            <button class="rounded-lg p-2 text-slate-500 hover:bg-slate-100" @click="$emit('update:modelValue', false)">✕</button>
          </header>
          <div>
            <slot />
          </div>
          <footer v-if="$slots.footer" class="mt-5 flex justify-end gap-2">
            <slot name="footer" />
          </footer>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  width: {
    type: String,
    default: '760px'
  },
  closeOnBackdrop: {
    type: Boolean,
    default: true
  }
})

defineEmits(['update:modelValue'])

const panelStyle = computed(() => ({ maxWidth: props.width }))
</script>
