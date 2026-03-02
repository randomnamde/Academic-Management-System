<template>
  <div class="app-page space-y-4">
    <section class="grid gap-4 xl:grid-cols-[2fr,1fr]">
      <AppCard :title="title" content-class="p-5 space-y-4">
        <slot name="filters" />
      </AppCard>
      <AppCard title="分析说明" content-class="p-5">
        <slot name="insight" />
      </AppCard>
    </section>

    <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <slot name="kpi" />
    </section>

    <section class="grid gap-4 xl:grid-cols-2">
      <slot name="charts" />
    </section>

    <section>
      <slot />
    </section>
  </div>
</template>

<script setup>
import AppCard from '@/components/ui/AppCard.vue'

defineProps({
  title: {
    type: String,
    default: '分析中心'
  }
})
</script>
