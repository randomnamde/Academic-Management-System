<template>
  <router-view v-slot="{ Component, route }">
    <Transition name="app-shell-motion" mode="out-in">
      <component :is="Component" :key="route.meta?.public ? route.fullPath : route.matched?.[0]?.path || route.path" />
    </Transition>
  </router-view>
</template>
