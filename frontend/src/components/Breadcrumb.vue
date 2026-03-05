<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item
      v-for="item in breadcrumbItems"
      :key="item.path"
      :to="item.path === route.path ? undefined : item.path"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const { t } = useI18n()

const breadcrumbItems = computed(() => {
  return route.matched
    .filter(item => item.path && item.meta && (item.meta.titleKey || item.meta.title))
    .map(item => ({
      path: item.path,
      title: item.meta.titleKey ? t(item.meta.titleKey) : item.meta.title
    }))
})
</script>
