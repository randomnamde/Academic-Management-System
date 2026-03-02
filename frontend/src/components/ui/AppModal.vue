<template>
  <Transition name="app-fade">
    <div v-if="modelValue" class="fixed inset-0 z-40 overflow-y-auto" @keydown.esc="$emit('update:modelValue', false)">
      <div class="app-modal-backdrop" @click="closeOnBackdrop && $emit('update:modelValue', false)"></div>
      <div class="relative z-50 flex min-h-full items-center justify-center p-4">
        <div class="app-modal-panel" :style="panelStyle" role="dialog" aria-modal="true">
          <header class="mb-3 flex items-center justify-between border-b border-neutralx-200 pb-2">
            <h3 class="text-[15px] font-semibold tracking-tight text-primary-900">{{ title }}</h3>
            <button
              class="rounded-sm border border-transparent px-1.5 py-0.5 text-slatex-500 transition-all duration-180 hover:bg-neutralx-100"
              @click="$emit('update:modelValue', false)"
            >
              ×
            </button>
          </header>
          <div>
            <slot />
          </div>
          <footer v-if="$slots.footer" class="mt-4 flex justify-end gap-2 border-t border-neutralx-200 pt-3">
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
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '760px' },
  closeOnBackdrop: { type: Boolean, default: true }
})

defineEmits(['update:modelValue'])

const panelStyle = computed(() => ({ maxWidth: props.width }))
</script>
