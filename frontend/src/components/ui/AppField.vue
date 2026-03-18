<template>
  <div class="app-field" :data-invalid="error ? '' : undefined" :data-disabled="disabled ? '' : undefined">
    <label v-if="label" :for="forId" class="app-field__label">
      <span>{{ label }}</span>
      <span v-if="required" class="app-field__required" aria-hidden="true">*</span>
    </label>

    <div class="app-field__control">
      <slot />
    </div>

    <p v-if="hint" class="app-field__hint">
      {{ hint }}
    </p>
    <p v-if="error" class="app-field__error" role="alert">
      {{ error }}
    </p>
  </div>
</template>

<script setup>
defineProps({
  label: { type: String, default: '' },
  hint: { type: String, default: '' },
  error: { type: String, default: '' },
  required: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  forId: { type: String, default: '' }
})
</script>

<style scoped>
.app-field {
  display: grid;
  gap: 8px;
}

.app-field__label {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 700;
  line-height: 1.35;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.app-field__required {
  color: var(--danger);
}

.app-field__control {
  min-width: 0;
}

.app-field__hint,
.app-field__error {
  margin: 0;
  font-size: 12px;
  line-height: 1.6;
}

.app-field__hint {
  color: color-mix(in srgb, var(--text-secondary) 88%, transparent);
}

.app-field__error {
  color: var(--danger);
}
</style>
