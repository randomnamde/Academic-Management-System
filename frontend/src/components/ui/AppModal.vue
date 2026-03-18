<template>
  <AppDialog v-bind="{ ...$props, ...$attrs }" v-slots="$slots" />
</template>

<script setup>
import AppDialog from '@/components/ui/AppDialog.vue'

defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '760px' },
  closeOnBackdrop: { type: Boolean, default: true }
})

</script>
