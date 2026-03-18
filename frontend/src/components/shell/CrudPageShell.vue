<template>
  <div class="app-page flex flex-col gap-3">
    <section class="crud-shell app-surface-glass">
      <header class="crud-shell-header app-panel-header">
        <div>
          <p class="crud-shell-kicker">Data Workspace</p>
          <h1 class="crud-shell-title">{{ title }}</h1>
        </div>
        <div class="crud-shell-actions">
          <AppButton
            v-if="showParentAction"
            variant="secondary"
            size="sm"
            @click="navigateToParent"
          >
            <ArrowLeft class="h-3.5 w-3.5" />
            <span>{{ parentActionLabel }}</span>
          </AppButton>
          <slot name="header-actions" />
        </div>
      </header>

      <div class="crud-shell-body flex flex-col gap-3 p-4">
        <section v-if="$slots.filters" class="crud-shell-toolbar">
          <slot name="filters" />
        </section>

        <section v-if="$slots['advanced-filters']" class="crud-shell-advanced">
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

    <slot name="dialogs" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import AppButton from '@/components/ui/AppButton.vue'

const props = defineProps({
  title: {
    type: String,
    default: ''
  },
  autoParentAction: {
    type: Boolean,
    default: true
  }
})

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const normalizePath = (path = '') => {
  if (!path) return ''
  return path.startsWith('/') ? path : `/${path}`
}

const routeRecordMap = computed(() => {
  const map = new Map()
  const visit = (records = []) => {
    records.forEach((record) => {
      const fullPath = normalizePath(record.path)
      if (fullPath) {
        map.set(fullPath, record)
      }
      if (Array.isArray(record.children) && record.children.length) {
        visit(record.children)
      }
    })
  }
  visit(router.options.routes || [])
  return map
})

const parentPath = computed(() => normalizePath(route.meta?.breadcrumbParent || ''))
const showParentAction = computed(() => props.autoParentAction && Boolean(parentPath.value))
const parentActionLabel = computed(() => {
  const record = routeRecordMap.value.get(parentPath.value)
  if (!record?.meta) return ''
  return record.meta.titleKey ? t(record.meta.titleKey) : (record.meta.title || '')
})

const navigateToParent = () => {
  if (!parentPath.value) return
  router.push(parentPath.value)
}
</script>

<style scoped>
.crud-shell {
  border-radius: var(--radius-panel);
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
}

.crud-shell-header {
  align-items: flex-end;
}

.crud-shell-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
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

.crud-shell-empty {
  padding-top: 4px;
}
</style>

