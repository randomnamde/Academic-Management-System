<template>
  <div class="app-shell">
    <particle-background />
    <div class="app-content">
      <router-view v-slot="{ Component, route }">
        <transition :name="getRootTransition(route.path)" mode="out-in" appear>
          <component :is="Component" :key="route.fullPath" />
        </transition>
      </router-view>
    </div>
  </div>
</template>

<script setup>
import ParticleBackground from '@/components/ParticleBackground.vue'

const getRootTransition = (path) => (path === '/login' ? 'auth-fade' : 'app-pop')
</script>

<style scoped lang="scss">
.app-shell {
  min-height: 100%;
  position: relative;
}

.app-content {
  min-height: 100%;
  position: relative;
  z-index: 1;
}

.auth-fade-enter-active,
.auth-fade-leave-active,
.app-pop-enter-active,
.app-pop-leave-active {
  transition:
    opacity var(--sms-motion-panel, 240ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    transform var(--sms-motion-panel, 240ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    filter var(--sms-motion-panel, 240ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.auth-fade-enter-from,
.auth-fade-leave-to {
  opacity: 0;
  filter: blur(8px);
}

.app-pop-enter-from,
.app-pop-leave-to {
  opacity: 0;
  transform: scale(0.985) translateY(14px);
  filter: blur(4px);
}

@media (prefers-reduced-motion: reduce) {
  .auth-fade-enter-active,
  .auth-fade-leave-active,
  .app-pop-enter-active,
  .app-pop-leave-active {
    transition-duration: 0.01ms;
  }
}
</style>
