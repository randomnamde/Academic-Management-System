<template>
  <Transition name="app-fade">
    <div
      v-if="modelValue"
      ref="overlayRef"
      class="fixed inset-0 z-40 overflow-y-auto"
      tabindex="-1"
      @keydown="onKeydown"
    >
      <div class="app-modal-backdrop" @click="onBackdropClick"></div>
      <div class="relative z-50 flex min-h-full items-center justify-center p-4">
        <div
          ref="panelRef"
          class="app-modal-panel"
          :style="panelStyle"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="titleId"
          tabindex="-1"
        >
          <header class="mb-3 flex items-center justify-between border-b border-neutralx-200 pb-2">
            <h3 :id="titleId" class="text-[15px] font-semibold tracking-tight text-primary-900">{{ title }}</h3>
            <button
              class="rounded-md border border-transparent px-1.5 py-0.5 text-slatex-500 transition-all duration-180 hover:bg-neutralx-100 touch-target"
              :aria-label="t('components.appModal.close')"
              @click="close"
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
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '760px' },
  closeOnBackdrop: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue'])
const { t } = useI18n()

const panelRef = ref(null)
const previousActiveElement = ref(null)
const titleId = computed(() => `modal-title-${String(props.title || 'dialog').replace(/\s+/g, '-').toLowerCase()}`)
const panelStyle = computed(() => ({ maxWidth: props.width }))

const focusableSelector = [
  'a[href]',
  'button:not([disabled])',
  'textarea:not([disabled])',
  'input:not([disabled])',
  'select:not([disabled])',
  '[tabindex]:not([tabindex="-1"])'
].join(',')

function close() {
  emit('update:modelValue', false)
}

function onBackdropClick() {
  if (props.closeOnBackdrop) close()
}

function getFocusableElements() {
  if (!panelRef.value) return []
  return Array.from(panelRef.value.querySelectorAll(focusableSelector))
}

function focusFirstElement() {
  const elements = getFocusableElements()
  if (elements.length) {
    elements[0].focus()
    return
  }
  panelRef.value?.focus()
}

function trapFocus(event) {
  const elements = getFocusableElements()
  if (!elements.length) {
    event.preventDefault()
    return
  }

  const first = elements[0]
  const last = elements[elements.length - 1]
  const active = document.activeElement

  if (!elements.includes(active)) {
    event.preventDefault()
    first.focus()
  } else if (event.shiftKey && active === first) {
    event.preventDefault()
    last.focus()
  } else if (!event.shiftKey && active === last) {
    event.preventDefault()
    first.focus()
  }
}

function onKeydown(event) {
  if (event.key === 'Escape') {
    event.preventDefault()
    close()
    return
  }

  if (event.key === 'Tab') {
    trapFocus(event)
  }
}

watch(
  () => props.modelValue,
  (open) => {
    if (open) {
      previousActiveElement.value = document.activeElement
      nextTick(() => focusFirstElement())
      return
    }
    previousActiveElement.value?.focus?.()
  }
)

onBeforeUnmount(() => {
  previousActiveElement.value?.focus?.()
})
</script>

