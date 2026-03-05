<template>
  <el-config-provider :locale="elementLocale">
    <router-view v-slot="{ Component, route }">
      <Transition name="app-shell-motion" mode="out-in">
        <component :is="Component" :key="route.meta?.public ? route.fullPath : route.matched?.[0]?.path || route.path" />
      </Transition>
    </router-view>
  </el-config-provider>
</template>

<script setup>
import { computed } from 'vue'
import { useStore } from 'vuex'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import { normalizeLocale } from '@/i18n/localeManager'

const store = useStore()

const elementLocale = computed(() => {
  const locale = normalizeLocale(store.getters.language)
  return locale === 'en-US' ? en : zhCn
})
</script>
