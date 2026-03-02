<template>
  <div class="app-page grid gap-3 xl:grid-cols-[1.3fr,1fr]">
    <AppCard :title="title" content-class="p-4">
      <slot />
    </AppCard>
    <AppCard title="账户安全建议" content-class="p-4 text-[13px] text-slatex-600 leading-6">
      <slot name="aside">
        建议定期轮换密码，开启多因子认证，并避免在公共设备保存登录状态。
      </slot>
    </AppCard>
  </div>
</template>

<script setup>
import AppCard from '@/components/ui/AppCard.vue'

defineProps({
  title: {
    type: String,
    default: '个人中心'
  }
})
</script>
