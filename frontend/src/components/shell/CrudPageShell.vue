<template>
  <div class="app-page space-y-4">
    <AppCard :title="title" content-class="p-0">
      <template #header>
        <slot name="header-actions" />
      </template>
      <div class="space-y-4 p-5">
        <section v-if="$slots.filters" class="app-toolbar">
          <slot name="filters" />
        </section>
        <section>
          <slot name="table" />
        </section>
        <section>
          <slot name="pagination" />
        </section>
      </div>
    </AppCard>

    <slot />
  </div>
</template>

<script setup>
import AppCard from '@/components/ui/AppCard.vue'

defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>
