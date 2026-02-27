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

const route = useRoute()

const breadcrumbItems = computed(() => {
  return route.matched
    .filter(item => item.path && item.meta && item.meta.title)
    .map(item => ({
      path: item.path,
      title: item.meta.title
    }))
})
</script>
