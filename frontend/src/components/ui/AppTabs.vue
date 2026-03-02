<template>
  <div class="inline-flex rounded-sm border border-neutralx-200 bg-white p-0.5">
    <button
      v-for="item in items"
      :key="item.value"
      class="rounded-sm px-2.5 py-1 text-[12px] font-medium transition-all duration-180"
      :class="modelValue === item.value ? 'bg-neutralx-100 text-primary-800' : 'text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900'"
      @click="$emit('update:modelValue', item.value)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  items: { type: Array, default: () => [] }
})

defineEmits(['update:modelValue'])
</script>
