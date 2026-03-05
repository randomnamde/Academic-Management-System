<template>
  <div class="app-page space-y-3">
    <section class="grid gap-3 xl:grid-cols-[1.65fr,1fr]">
      <AppCard :title="title || t('components.analyticsShell.defaultTitle')" surface="glass" content-class="p-4 space-y-3">
        <slot name="filters" />
      </AppCard>
      <AppCard :title="t('components.analyticsShell.insightTitle')" surface="base" content-class="p-4">
        <slot name="insight" />
      </AppCard>
    </section>

    <section class="grid gap-3 md:grid-cols-2 xl:grid-cols-5">
      <slot name="kpi" />
    </section>

    <section class="grid gap-3 xl:grid-cols-2">
      <slot name="charts" />
    </section>

    <section>
      <slot />
    </section>
  </div>
</template>

<script setup>
import AppCard from '@/components/ui/AppCard.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>

