<template>
  <div class="app-page grid gap-3 xl:grid-cols-[1.32fr,1fr]">
    <AppCard :title="title || t('components.profileShell.defaultTitle')" surface="glass" content-class="p-4 md:p-5">
      <slot />
    </AppCard>
    <AppCard :title="t('components.profileShell.securityAdviceTitle')" surface="base" content-class="p-4 text-[13px] text-slatex-600 leading-6">
      <slot name="aside">
        {{ t('components.profileShell.securityAdviceContent') }}
      </slot>
    </AppCard>
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

