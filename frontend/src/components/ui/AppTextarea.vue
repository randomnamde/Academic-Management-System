<template>
  <textarea
    :id="id"
    :name="name"
    :value="modelValue"
    :placeholder="placeholder"
    :autocomplete="autocomplete"
    :rows="rows"
    :disabled="disabled"
    :readonly="readonly"
    :required="required"
    :aria-label="ariaLabel"
    :aria-describedby="ariaDescribedby"
    :aria-invalid="invalid ? 'true' : undefined"
    class="app-textarea"
    :class="{ 'is-invalid': invalid }"
    @input="$emit('update:modelValue', $event.target.value)"
    @keyup.enter="$emit('enter')"
  />
</template>

<script setup>
defineProps({
  id: { type: String, default: '' },
  name: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  placeholder: { type: String, default: '' },
  autocomplete: { type: String, default: '' },
  rows: { type: Number, default: 3 },
  disabled: { type: Boolean, default: false },
  readonly: { type: Boolean, default: false },
  required: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
  ariaLabel: { type: String, default: '' },
  ariaDescribedby: { type: String, default: '' }
})

defineEmits(['update:modelValue', 'enter'])
</script>

<style scoped>
.app-textarea {
  width: 100%;
  min-height: 96px;
  resize: vertical;
  border-radius: var(--radius-control);
  border: 1px solid color-mix(in srgb, var(--panel-border) 90%, transparent);
  background: color-mix(in srgb, var(--surface-base) 86%, transparent);
  color: var(--text-primary);
  padding: 11px 14px;
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.app-textarea:focus {
  border-color: var(--accent-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent-500) 22%, transparent);
}

.app-textarea:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.app-textarea.is-invalid {
  border-color: var(--danger);
}
</style>
