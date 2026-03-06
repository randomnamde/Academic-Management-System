<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('rbac.users.hubTitle')" content-class="p-4 space-y-3">
      <p class="text-[13px] leading-6 text-slatex-600">{{ t('rbac.users.hubDesc') }}</p>
      <div class="grid gap-3 md:grid-cols-2">
        <button
          v-if="isSchoolAdmin"
          class="hub-card touch-target"
          type="button"
          @click="router.push('/rbac/users/assignments')"
        >
          <div>
            <p class="hub-card-kicker">{{ t('rbac.users.assignmentCardKicker') }}</p>
            <h2 class="hub-card-title">{{ t('rbac.users.assignmentEntryTitle') }}</h2>
          </div>
          <p class="hub-card-desc">{{ t('rbac.users.assignmentEntryDesc') }}</p>
        </button>

        <button
          class="hub-card touch-target"
          type="button"
          @click="router.push('/rbac/users/list')"
        >
          <div>
            <p class="hub-card-kicker">{{ t('rbac.users.listCardKicker') }}</p>
            <h2 class="hub-card-title">{{ t('rbac.users.listEntryTitle') }}</h2>
          </div>
          <p class="hub-card-desc">{{ t('rbac.users.listEntryDesc') }}</p>
        </button>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import AppCard from '@/components/ui/AppCard.vue'

const router = useRouter()
const store = useStore()
const { t } = useI18n()

const currentRole = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const isSchoolAdmin = computed(() => currentRole.value === 'SCHOOL_ADMIN')
</script>

<style scoped>
.hub-card {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 88%, transparent), color-mix(in srgb, var(--surface-elevated) 72%, transparent));
  text-align: left;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.hub-card:hover,
.hub-card:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--accent-500) 26%, transparent);
  box-shadow: 0 18px 30px color-mix(in srgb, var(--accent-500) 12%, transparent);
}

.hub-card-kicker {
  margin: 0 0 6px;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.hub-card-title {
  margin: 0;
  font-size: 20px;
  line-height: 1.2;
  color: var(--text-primary);
}

.hub-card-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.7;
  color: var(--text-secondary);
}
</style>
