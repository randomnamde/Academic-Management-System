<template>
  <router-view v-slot="{ Component, route }">
    <transition name="app-fade" mode="out-in" appear>
      <component :is="Component" :key="route.fullPath" />
    </transition>
  </router-view>
</template>
