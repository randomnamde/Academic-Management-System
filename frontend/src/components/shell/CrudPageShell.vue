<template>
  <div class="app-page space-y-3">
    <section class="app-panel">
      <header class="app-panel-header">
        <div>
          <h1 class="text-[18px] font-semibold tracking-tight text-primary-900">{{ title }}</h1>
        </div>
        <div class="flex items-center gap-2">
          <slot name="header-actions" />
        </div>
      </header>

      <div class="space-y-3 p-4">
        <section v-if="$slots.filters" class="app-toolbar">
          <slot name="filters" />
        </section>

        <section>
          <slot name="table" />
        </section>

        <section v-if="$slots.pagination">
          <slot name="pagination" />
        </section>
      </div>
    </section>

    <slot />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: ''
  }
})
</script>
