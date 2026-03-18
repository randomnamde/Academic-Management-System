<template>
  <Transition name="app-fade">
    <div
      v-if="modelValue"
      class="app-dialog-root"
      tabindex="-1"
      @keydown="onKeydown"
    >
      <div class="app-dialog-backdrop" @click="onBackdropClick"></div>
      <div class="app-dialog-viewport">
        <div
          ref="panelRef"
          class="app-dialog-panel"
          :style="panelStyle"
          role="dialog"
          aria-modal="true"
          :aria-labelledby="resolvedTitleId"
          tabindex="-1"
        >
          <div class="app-dialog-orb app-dialog-orb-a" aria-hidden="true"></div>
          <div class="app-dialog-orb app-dialog-orb-b" aria-hidden="true"></div>
          <div class="app-dialog-noise" aria-hidden="true"></div>

          <div class="app-dialog-inner">
            <header class="app-dialog-header">
              <div class="app-dialog-heading">
                <span v-if="$slots.icon" class="app-dialog-heading-icon" aria-hidden="true">
                  <slot name="icon" />
                </span>
                <div class="app-dialog-title-wrap">
                  <slot name="title">
                    <h3 :id="resolvedTitleId" class="app-dialog-title">{{ title }}</h3>
                  </slot>
                </div>
              </div>

              <button
                v-if="showClose"
                class="app-dialog-close touch-target"
                :aria-label="closeLabel"
                @click="close"
              >
                <span aria-hidden="true">×</span>
              </button>
            </header>

            <div class="app-dialog-body">
              <slot />
            </div>

            <footer v-if="$slots.footer" class="app-dialog-footer">
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
  titleId: { type: String, default: '' },
  width: { type: String, default: '760px' },
  closeOnBackdrop: { type: Boolean, default: true },
  closeOnEsc: { type: Boolean, default: true },
  showClose: { type: Boolean, default: true }
})

const emit = defineEmits(['update:modelValue', 'open', 'close'])
const { t } = useI18n()

const panelRef = ref(null)
const previousActiveElement = ref(null)

const resolvedTitleId = computed(() => props.titleId || `dialog-title-${String(props.title || 'dialog').replace(/\s+/g, '-').toLowerCase()}`)
const panelStyle = computed(() => ({ '--dialog-width': props.width }))
const closeLabel = computed(() => t('common.close'))

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
  emit('close')
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
  if (event.key === 'Escape' && props.closeOnEsc) {
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
      emit('open')
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

<style scoped>
.app-dialog-root {
  position: fixed;
  inset: 0;
  z-index: 40;
  overflow-y: auto;
}

.app-dialog-backdrop {
  position: fixed;
  inset: 0;
  background: var(--dialog-overlay);
  backdrop-filter: blur(10px) saturate(120%);
}

.app-dialog-viewport {
  position: relative;
  display: grid;
  min-height: 100%;
  place-items: center;
  padding: 20px;
}

.app-dialog-panel {
  position: relative;
  width: min(100%, var(--dialog-width, 760px));
  border-radius: calc(var(--radius-pop) + 4px);
  border: 1px solid var(--dialog-border);
  background: var(--dialog-surface);
  box-shadow: var(--dialog-shadow);
  overflow: hidden;
  outline: none;
}

.app-dialog-orb,
.app-dialog-noise {
  pointer-events: none;
  position: absolute;
}

.app-dialog-orb {
  border-radius: 999px;
  filter: blur(14px);
  opacity: 0.55;
}

.app-dialog-orb-a {
  top: -44px;
  right: -52px;
  width: 120px;
  height: 120px;
  background: var(--dialog-glow-a);
}

.app-dialog-orb-b {
  bottom: -34px;
  left: -36px;
  width: 108px;
  height: 108px;
  background: var(--dialog-glow-b);
}

.app-dialog-noise {
  inset: 0;
  opacity: 0.18;
  background-image:
    linear-gradient(color-mix(in srgb, var(--color-white) 0.06%, transparent) 1px, transparent 1px),
    linear-gradient(90deg, color-mix(in srgb, var(--color-white) 0.06%, transparent) 1px, transparent 1px);
  background-size: 20px 20px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.82), rgba(0, 0, 0, 0.18));
}

.app-dialog-inner {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
}

.app-dialog-header,
.app-dialog-footer {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.app-dialog-header {
  padding: 18px 20px 14px;
  border-bottom: 1px solid color-mix(in srgb, var(--panel-border) 80%, transparent);
  background: var(--dialog-header-bg);
}

.app-dialog-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.app-dialog-heading-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
}

.app-dialog-title-wrap {
  min-width: 0;
}

.app-dialog-title {
  margin: 0;
  color: var(--text-primary);
  font-family: var(--font-display);
  font-size: 19px;
  font-weight: 600;
  letter-spacing: 0.01em;
}

.app-dialog-close {
  display: inline-grid;
  place-items: center;
  width: 40px;
  height: 40px;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 74%, transparent);
  background: color-mix(in srgb, var(--surface-base) 48%, transparent);
  color: var(--text-secondary);
  box-shadow: 0 10px 22px color-mix(in srgb, var(--accent-500) 8%, transparent);
}

.app-dialog-close span {
  display: none;
}

.app-dialog-close::before {
  content: '×';
  font-size: 18px;
  line-height: 1;
}

.app-dialog-body {
  padding: 18px 20px 20px;
}

.app-dialog-footer {
  padding: 14px 20px 18px;
  border-top: 1px solid color-mix(in srgb, var(--panel-border) 80%, transparent);
  background: var(--dialog-footer-bg);
}

@media (max-width: 767px) {
  .app-dialog-viewport {
    padding: 12px;
  }

  .app-dialog-header,
  .app-dialog-body,
  .app-dialog-footer {
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>
