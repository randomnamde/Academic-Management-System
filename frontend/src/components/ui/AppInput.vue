<template>
  <div class="app-input-wrap" :data-disabled="disabled ? '' : undefined">
    <input
      v-if="!isPassword"
      :id="id"
      :name="name"
      :value="modelValue"
      :type="type"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      :inputmode="inputmode"
      :required="required"
      :readonly="readonly"
      :disabled="disabled"
      :aria-label="ariaLabel"
      :aria-describedby="ariaDescribedby"
      :aria-invalid="invalid ? 'true' : undefined"
      class="app-input"
      :class="{ 'is-invalid': invalid, 'has-suffix': clearable || isPassword }"
      @input="handleInput"
      @keyup.enter="$emit('enter')"
    />

    <input
      v-else
      :id="id"
      :name="name"
      :value="modelValue"
       :type="passwordVisible ? 'text' : 'password'"
      :placeholder="placeholder"
      :autocomplete="autocomplete"
      :required="required"
      :readonly="readonly"
      :disabled="disabled"
      :aria-label="ariaLabel"
      :aria-describedby="ariaDescribedby"
      :aria-invalid="invalid ? 'true' : undefined"
      class="app-input"
      :class="{ 'is-invalid': invalid, 'has-suffix': true }"
      @input="handleInput"
      @keyup.enter="$emit('enter')"
    />

    <div v-if="clearable || isPassword" class="app-input__suffix">
      <button
        v-if="clearable && modelValue !== '' && modelValue !== null && modelValue !== undefined"
        type="button"
        class="app-input__icon-button"
        :aria-label="clearLabel"
        @click="clearValue"
      >
        ×
      </button>
      <button
        v-if="isPassword"
        type="button"
        class="app-input__icon-button"
        :aria-label="passwordVisible ? hidePasswordLabel : showPasswordLabel"
        @click="passwordVisible = !passwordVisible"
      >
        {{ passwordVisible ? 'visible' : 'hidden' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const props = defineProps({
  id: { type: String, default: '' },
  name: { type: String, default: '' },
  modelValue: { type: [String, Number], default: '' },
  type: { type: String, default: 'text' },
  placeholder: { type: String, default: '' },
  autocomplete: { type: String, default: '' },
  inputmode: { type: String, default: '' },
  ariaLabel: { type: String, default: '' },
  ariaDescribedby: { type: String, default: '' },
  required: { type: Boolean, default: false },
  readonly: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  invalid: { type: Boolean, default: false },
  clearable: { type: Boolean, default: false },
  showPassword: { type: Boolean, default: false }
})

const emit = defineEmits(['update:modelValue', 'enter'])
const { t } = useI18n()

const localShowPassword = ref(props.showPassword)

const isPassword = computed(() => props.type === 'password')

const clearLabel = computed(() => t('common.clear'))
const showPasswordLabel = computed(() => t('common.show') || 'Show')
const hidePasswordLabel = computed(() => t('common.hide') || 'Hide')

function handleInput(event) {
  emit('update:modelValue', event.target.value)
}

function clearValue() {
  emit('update:modelValue', '')
}

const passwordVisible = computed({
  get: () => localShowPassword.value,
  set: (value) => {
    localShowPassword.value = value
  }
})
</script>

<style scoped>
.app-input-wrap {
  position: relative;
  min-width: 0;
}

.app-input {
  width: 100%;
  min-height: 40px;
  border-radius: var(--radius-control);
  border: 1px solid color-mix(in srgb, var(--panel-border) 90%, transparent);
  background: color-mix(in srgb, var(--surface-base) 86%, transparent);
  color: var(--text-primary);
  padding: 0 14px;
  outline: none;
  transition: border-color 180ms ease, box-shadow 180ms ease, background 180ms ease;
}

.app-input.has-suffix {
  padding-right: 74px;
}

.app-input:focus {
  border-color: var(--accent-500);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent-500) 22%, transparent);
}

.app-input:disabled {
  cursor: not-allowed;
  opacity: 0.65;
}

.app-input.is-invalid {
  border-color: var(--danger);
}

.app-input__suffix {
  position: absolute;
  top: 0;
  right: 0;
  display: flex;
  align-items: stretch;
  gap: 6px;
  height: 100%;
  padding: 4px;
}

.app-input__icon-button {
  display: inline-grid;
  place-items: center;
  min-width: 28px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: var(--text-secondary);
  font-size: 0;
  line-height: 0;
}

.app-input__icon-button::before {
  font-size: 12px;
  line-height: 1;
}

.app-input__icon-button:first-of-type::before {
  content: 'x';
}

.app-input__icon-button:last-of-type::before {
  content: 'pw';
}
</style>
