<template>
  <AppDialog
    v-model="open"
    :title="title"
    :width="width"
    :close-on-backdrop="closeOnBackdrop"
    :show-close="showClose"
    @close="$emit('close')"
    @open="$emit('open')"
  >
    <el-form ref="formRef" :model="model" :rules="rules" label-position="top" class="crud-form-dialog__form">
      <div class="crud-form-dialog__grid" :style="gridStyle">
        <div
          v-for="field in fields"
          :key="field.model"
          class="crud-form-dialog__field"
          :style="spanStyle(field)"
        >
          <el-form-item :prop="field.model" :label="field.label">
            <CrudFieldRenderer :field="field" :model="model" :update-field="updateField">
              <template #default="slotProps">
                <slot :name="field.slot || field.model" v-bind="slotProps" />
              </template>
            </CrudFieldRenderer>
          </el-form-item>
        </div>
      </div>
    </el-form>

    <template #footer>
      <AppActionGroup>
        <AppButton variant="secondary" @click="cancel">{{ cancelText }}</AppButton>
        <AppButton :loading="loading" @click="submit">{{ submitText }}</AppButton>
      </AppActionGroup>
    </template>
  </AppDialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import AppDialog from '@/components/ui/AppDialog.vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppActionGroup from '@/components/ui/AppActionGroup.vue'
import CrudFieldRenderer from '@/components/crud/CrudFieldRenderer.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  title: { type: String, default: '' },
  width: { type: String, default: '760px' },
  fields: { type: Array, default: () => [] },
  model: { type: Object, required: true },
  rules: { type: Object, default: () => ({}) },
  columns: { type: Number, default: 2 },
  loading: { type: Boolean, default: false },
  submitText: { type: String, default: 'Confirm' },
  cancelText: { type: String, default: 'Cancel' },
  closeOnBackdrop: { type: Boolean, default: true },
  showClose: { type: Boolean, default: true },
  updateField: { type: Function, default: null }
})

const emit = defineEmits(['update:modelValue', 'submit', 'cancel', 'open', 'close'])
const formRef = ref(null)

const open = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const gridStyle = computed(() => ({ gridTemplateColumns: `repeat(${props.columns}, minmax(0, 1fr))` }))

function spanStyle(field) {
  const span = Number(field.span || props.columns)
  const resolved = Math.min(Math.max(span, 1), props.columns)
  return { gridColumn: `span ${resolved} / span ${resolved}` }
}

async function submit() {
  const valid = await formRef.value?.validate?.().catch(() => false)
  if (!valid) return
  emit('submit')
}

function cancel() {
  emit('update:modelValue', false)
  emit('cancel')
}

defineExpose({
  validate: () => formRef.value?.validate?.(),
  resetFields: () => formRef.value?.resetFields?.()
})
</script>

<style scoped>
.crud-form-dialog__form {
  width: 100%;
}

.crud-form-dialog__grid {
  display: grid;
  gap: 16px;
}

.crud-form-dialog__field {
  min-width: 0;
}

@media (max-width: 767px) {
  .crud-form-dialog__grid {
    grid-template-columns: 1fr !important;
  }

  .crud-form-dialog__field {
    grid-column: 1 / -1 !important;
  }
}
</style>
