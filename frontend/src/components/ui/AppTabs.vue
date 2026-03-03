<template>
  <div class="app-tabs-root inline-flex rounded-md p-0.5" role="tablist" :aria-label="ariaLabel">
    <button
      v-for="(item, index) in items"
      :id="tabId(item.value)"
      :key="item.value"
      :aria-controls="panelId(item.value)"
      :aria-selected="modelValue === item.value ? 'true' : 'false'"
      :tabindex="modelValue === item.value ? 0 : -1"
      role="tab"
      class="rounded-md px-2.5 py-1 text-[12px] font-medium transition-all duration-180 touch-target"
      :class="modelValue === item.value ? 'bg-neutralx-100 text-primary-800' : 'text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900'"
      @click="activate(item.value)"
      @keydown="onKeydown($event, index)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: [String, Number], default: '' },
  items: { type: Array, default: () => [] },
  idPrefix: { type: String, default: 'tabs' },
  ariaLabel: { type: String, default: '选项卡' }
})

const emit = defineEmits(['update:modelValue'])

const tabId = (value) => `${props.idPrefix}-tab-${value}`
const panelId = (value) => `${props.idPrefix}-panel-${value}`

function activate(value) {
  emit('update:modelValue', value)
}

function onKeydown(event, index) {
  if (!props.items.length) return

  let nextIndex = index
  if (event.key === 'ArrowRight') nextIndex = (index + 1) % props.items.length
  if (event.key === 'ArrowLeft') nextIndex = (index - 1 + props.items.length) % props.items.length
  if (event.key === 'Home') nextIndex = 0
  if (event.key === 'End') nextIndex = props.items.length - 1

  if (nextIndex === index) return
  event.preventDefault()
  const nextValue = props.items[nextIndex].value
  activate(nextValue)
  requestAnimationFrame(() => {
    document.getElementById(tabId(nextValue))?.focus()
  })
}
</script>

<style scoped>
.app-tabs-root {
  border: 1px solid color-mix(in srgb, var(--panel-border) 84%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
}
</style>

