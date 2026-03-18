<template>
  <component
    :is="resolvedComponent"
    v-bind="resolvedProps"
    :model-value="fieldValue"
    @update:model-value="handleUpdate"
  >
    <template v-if="field.type === 'slot' || field.component" #default>
      <slot :field="field" :model="model" />
    </template>
  </component>
</template>

<script setup>
import { computed } from 'vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppTextarea from '@/components/ui/AppTextarea.vue'
import AppNumberInput from '@/components/ui/AppNumberInput.vue'
import AppDatePicker from '@/components/ui/AppDatePicker.vue'
import AppRadioGroup from '@/components/ui/AppRadioGroup.vue'
import AppCheckbox from '@/components/ui/AppCheckbox.vue'

const props = defineProps({
  field: { type: Object, required: true },
  model: { type: Object, required: true },
  updateField: { type: Function, default: null }
})

const fieldValue = computed(() => props.model[props.field.model])

function handleUpdate(value) {
  if (typeof props.updateField === 'function') {
    props.updateField(props.field.model, value)
  }
}

const componentMap = {
  input: AppInput,
  select: AppSelect,
  textarea: AppTextarea,
  number: AppNumberInput,
  date: AppDatePicker,
  radio: AppRadioGroup,
  checkbox: AppCheckbox
}

const resolvedComponent = computed(() => {
  if (props.field.component) return props.field.component
  return componentMap[props.field.type] || AppInput
})

const resolvedProps = computed(() => {
  const field = props.field
  const options = typeof field.options === 'function' ? field.options(props.model) : field.options

  return {
    id: field.id,
    name: field.name || field.model,
    placeholder: typeof field.placeholder === 'function' ? field.placeholder(props.model) : field.placeholder,
    disabled: typeof field.disabled === 'function' ? field.disabled(props.model) : field.disabled,
    required: field.required,
    invalid: field.invalid,
    clearable: field.clearable,
    showPassword: field.showPassword,
    autocomplete: field.autocomplete,
    inputmode: field.inputmode,
    min: field.min,
    max: field.max,
    step: field.step,
    rows: field.rows,
    valueType: field.valueType,
    ariaLabel: field.ariaLabel,
    ariaDescribedby: field.ariaDescribedby,
    options: options || []
  }
})
</script>
