<template>
  <div class="app-action-group">
    <slot />
  </div>
</template>

<style scoped>
.app-action-group {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}
</style>
