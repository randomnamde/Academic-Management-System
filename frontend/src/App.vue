<template>
  <router-view v-slot="{ Component, route }">
    <component :is="Component" :key="route.meta?.public ? route.fullPath : route.matched?.[0]?.path || route.path" />
  </router-view>
</template>
