<template>
  <div class="inline-flex rounded-xl bg-slate-100 p-1">
    <button
      v-for="item in items"
      :key="item.value"
      class="rounded-lg px-3 py-1.5 text-sm transition"
      :class="modelValue === item.value ? 'bg-white text-brand-700 shadow-sm' : 'text-slate-600 hover:text-slate-800'"
      @click="$emit('update:modelValue', item.value)"
    >
      {{ item.label }}
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: [String, Number],
    default: ''
  },
  items: {
    type: Array,
    default: () => []
  }
})

defineEmits(['update:modelValue'])
</script>
