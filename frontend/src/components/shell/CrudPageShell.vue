<template>
  <div class="app-page space-y-3">
    <section class="crud-shell app-surface-glass">
      <header class="crud-shell-header app-panel-header">
        <div>
          <p class="crud-shell-kicker">Data Workspace</p>
          <h1 class="crud-shell-title">{{ title }}</h1>
        </div>
        <div class="flex items-center gap-2">
          <slot name="header-actions" />
        </div>
      </header>

      <div class="space-y-3 p-4">
        <section v-if="$slots.filters" class="crud-shell-toolbar app-toolbar">
          <slot name="filters" />
        </section>

        <section v-if="$slots['advanced-filters']" class="crud-shell-advanced app-toolbar">
          <slot name="advanced-filters" />
        </section>

        <section class="crud-shell-data">
          <slot name="table" />
        </section>

        <section v-if="$slots.pagination" class="crud-shell-pagination">
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

<style scoped>
.crud-shell {
  border-radius: var(--radius-panel);
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
}

.crud-shell-header {
  align-items: flex-end;
}

.crud-shell-kicker {
  margin: 0 0 4px;
  font-size: 11px;
  letter-spacing: 0.09em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.crud-shell-title {
  margin: 0;
  font-size: 22px;
  line-height: 1.2;
  color: var(--text-primary);
}

.crud-shell-toolbar,
.crud-shell-advanced {
  border-radius: 12px;
}
</style>

