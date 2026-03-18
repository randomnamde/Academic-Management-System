<template>
  <section class="crud-filter-bar">
    <div class="crud-filter-bar__grid" :style="gridStyle">
      <div
        v-for="field in fields"
        :key="field.model"
        class="crud-filter-bar__field"
        :style="spanStyle(field)"
      >
        <AppField :label="field.label" :required="field.required">
          <CrudFieldRenderer :field="field" :model="model" :update-field="updateField">
            <template #default="slotProps">
              <slot :name="field.slot || field.model" v-bind="slotProps" />
            </template>
          </CrudFieldRenderer>
        </AppField>
      </div>

      <div class="crud-filter-bar__actions">
        <slot name="actions" />
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed } from 'vue'
import AppField from '@/components/ui/AppField.vue'
import CrudFieldRenderer from '@/components/crud/CrudFieldRenderer.vue'

const props = defineProps({
  fields: { type: Array, default: () => [] },
  model: { type: Object, required: true },
  columns: { type: Number, default: 12 },
  updateField: { type: Function, default: null }
})

const gridStyle = computed(() => ({ gridTemplateColumns: `repeat(${props.columns}, minmax(0, 1fr))` }))

function spanStyle(field) {
  const span = Number(field.span || 12)
  return { gridColumn: `span ${Math.min(Math.max(span, 1), props.columns)} / span ${Math.min(Math.max(span, 1), props.columns)}` }
}
</script>

<style scoped>
.crud-filter-bar {
  width: 100%;
}

.crud-filter-bar__grid {
  display: grid;
  gap: 12px;
}

.crud-filter-bar__field,
.crud-filter-bar__actions {
  min-width: 0;
}

.crud-filter-bar__actions {
  display: flex;
  flex-wrap: wrap;
  align-items: end;
  gap: 8px;
}

@media (max-width: 767px) {
  .crud-filter-bar__grid {
    grid-template-columns: 1fr !important;
  }

  .crud-filter-bar__field,
  .crud-filter-bar__actions {
    grid-column: 1 / -1 !important;
  }
}
</style>
