<template>
  <section class="app-stat-card">
    <p class="app-stat-card__label">{{ label }}</p>
    <div class="app-stat-card__value-row">
      <strong class="app-stat-card__value">{{ value }}</strong>
      <span v-if="note" class="app-stat-card__note">{{ note }}</span>
    </div>
  </section>
</template>

<script setup>
defineProps({
  label: { type: String, default: '' },
  value: { type: [String, Number], default: '' },
  note: { type: String, default: '' }
})
</script>

<style scoped>
.app-stat-card {
  display: grid;
  gap: 10px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.app-stat-card__label {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.app-stat-card__value-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.app-stat-card__value {
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: var(--text-primary);
}

.app-stat-card__note {
  font-size: 12px;
  color: color-mix(in srgb, var(--text-primary) 66%, var(--text-secondary));
}
</style>
