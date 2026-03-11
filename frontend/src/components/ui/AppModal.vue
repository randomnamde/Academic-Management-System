<template>
  <Transition name="app-fade">
    <div
      v-if="modelValue"
      class="app-modal-root fixed inset-0 z-40 overflow-y-auto"
      tabindex="-1"
      @keydown="onKeydown"
    >
      <div class="app-modal-backdrop" @click="onBackdropClick"></div>
      <div class="app-modal-viewport">
        <div
          ref="panelRef"
          class="app-modal-panel"
          :style="panelStyle"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="titleId"
          tabindex="-1"
        >
          <div class="app-modal-orb app-modal-orb-a" aria-hidden="true"></div>
          <div class="app-modal-orb app-modal-orb-b" aria-hidden="true"></div>
          <div class="app-modal-noise" aria-hidden="true"></div>
          <div class="app-modal-inner">
            <header class="app-modal-header">
              <div class="app-modal-heading">
                <span class="app-modal-heading-mark" aria-hidden="true"></span>
                <div class="app-modal-title-wrap">
                  <h3 :id="titleId" class="app-modal-title">{{ title }}</h3>
                </div>
              </div>
              <button
                class="app-modal-close touch-target"
                :aria-label="t('components.appModal.close')"
                @click="close"
              >
                <span aria-hidden="true">×</span>
              </button>
            </header>
            <div class="app-modal-body">
              <slot />
            </div>
            <footer v-if="$slots.footer" class="app-modal-footer">
              <slot name="footer" />
            </footer>
          </div>
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

