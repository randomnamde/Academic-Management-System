<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('rbac.center.pageTitle')" content-class="p-4 space-y-4">
      <div class="center-hero">
        <p class="center-hero-badge">{{ t('rbac.center.heroBadge') }}</p>
        <p class="center-hero-desc">{{ t('rbac.center.pageDesc') }}</p>
      </div>

      <div class="center-stats">
        <div
          v-for="stat in stats"
          :key="stat.label"
          class="center-stat"
        >
          <p class="center-stat-label">{{ stat.label }}</p>
          <div class="center-stat-row">
            <p class="center-stat-value">{{ stat.value }}</p>
            <span class="center-stat-note">{{ stat.note }}</span>
          </div>
        </div>
      </div>

      <div class="center-grid">
        <button
          v-for="card in cards"
          :key="card.path"
          class="center-card touch-target"
          :class="`center-card-${card.variant}`"
          type="button"
          @click="router.push(card.path)"
        >
          <div class="center-card-head">
            <div class="center-card-badge">
              <component :is="card.icon" class="h-5 w-5" />
            </div>
            <span class="center-card-arrow">
              <ArrowRight class="h-4 w-4" />
            </span>
          </div>

          <div class="center-card-body">
            <p class="center-card-kicker">{{ card.kicker }}</p>
            <h2 class="center-card-title">{{ card.title }}</h2>
            <p class="center-card-desc">{{ card.desc }}</p>
          </div>

          <div class="center-card-tags">
            <span v-for="tag in card.tags" :key="tag" class="center-card-tag">{{ tag }}</span>
          </div>

          <div class="center-card-footer">
            <span>{{ t('rbac.center.enterAction') }}</span>
            <ArrowRight class="h-4 w-4" />
          </div>
        </button>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onActivated, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { useI18n } from 'vue-i18n'
import { ArrowRight, FileSearch, KeyRound, LayoutTemplate, UsersRound } from 'lucide-vue-next'
import AppCard from '@/components/ui/AppCard.vue'
import { getUserList } from '@/api/user'

const router = useRouter()
const store = useStore()
const { t } = useI18n()

const userTotal = ref(null)
const currentRole = computed(() => store.state.userInfo?.primaryRole || store.state.userInfo?.role || '')
const isSchoolAdmin = computed(() => currentRole.value === 'SCHOOL_ADMIN')

const cards = computed(() => {
  const items = [
    {
      path: '/rbac/users',
      variant: 'users',
      icon: UsersRound,
      kicker: t('rbac.center.usersKicker'),
      title: t('route.rbacUsers'),
      desc: t('rbac.center.usersDesc'),
      tags: [t('rbac.center.usersTagAccounts'), t('rbac.center.usersTagImport')]
    }
  ]

  if (isSchoolAdmin.value) {
    items.push(
      {
        path: '/rbac/roles',
        variant: 'roles',
        icon: LayoutTemplate,
        kicker: t('rbac.center.rolesKicker'),
        title: t('route.rbacRoles'),
        desc: t('rbac.center.rolesDesc'),
        tags: [t('rbac.center.rolesTagTemplate'), t('rbac.center.rolesTagPolicy')]
      },
      {
        path: '/rbac/permissions',
        variant: 'permissions',
        icon: KeyRound,
        kicker: t('rbac.center.permissionsKicker'),
        title: t('route.rbacPermissions'),
        desc: t('rbac.center.permissionsDesc'),
        tags: [t('rbac.center.permissionsTagMatrix'), t('rbac.center.permissionsTagActions')]
      },
      {
        path: '/rbac/audit',
        variant: 'audit',
        icon: FileSearch,
        kicker: t('rbac.center.auditKicker'),
        title: t('route.rbacAudit'),
        desc: t('rbac.center.auditDesc'),
        tags: [t('rbac.center.auditTagTrace'), t('rbac.center.auditTagRisk')]
      }
    )
  }

  return items
})

const stats = computed(() => [
  {
    label: t('rbac.center.statUsersLabel'),
    value: userTotal.value ?? '--',
    note: t('rbac.center.statUsersNote')
  },
  {
    label: t('rbac.center.statEntriesLabel'),
    value: cards.value.length,
    note: t('rbac.center.statEntriesNote')
  }
])

async function loadStats() {
  try {
    const response = await getUserList({ page: 1, size: 1 })
    userTotal.value = Number(response.data?.total ?? 0)
  } catch (_error) {
    userTotal.value = null
  }
}

onMounted(loadStats)
onActivated(loadStats)
</script>

<style scoped>
.center-hero {
  display: grid;
  gap: 10px;
  padding: 18px 20px;
  border-radius: 20px;
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 18%, transparent), transparent 45%),
    linear-gradient(135deg, color-mix(in srgb, var(--surface-elevated) 88%, transparent), color-mix(in srgb, var(--surface-base) 92%, transparent));
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
}

.center-hero-badge {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.center-hero-desc {
  margin: 0;
  max-width: 720px;
  font-size: 14px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 80%, var(--text-secondary));
}

.center-stats {
  display: grid;
  gap: 12px;
}

.center-grid {
  display: grid;
  gap: 14px;
}

@media (min-width: 768px) {
  .center-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .center-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.center-stat {
  display: grid;
  gap: 10px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 76%, transparent));
}

.center-stat-label {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.center-stat-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
}

.center-stat-value {
  margin: 0;
  font-size: 30px;
  line-height: 1;
  font-weight: 700;
  color: var(--text-primary);
}

.center-stat-note {
  font-size: 12px;
  color: color-mix(in srgb, var(--text-primary) 66%, var(--text-secondary));
}

.center-card {
  position: relative;
  display: grid;
  gap: 18px;
  min-height: 252px;
  padding: 22px;
  overflow: hidden;
  border-radius: 24px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  text-align: left;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.center-card::before {
  content: '';
  position: absolute;
  inset: auto -40px -48px auto;
  width: 140px;
  height: 140px;
  border-radius: 999px;
  opacity: 0.45;
  filter: blur(8px);
}

.center-card:hover,
.center-card:focus-visible {
  transform: translateY(-3px);
  box-shadow: 0 22px 36px color-mix(in srgb, var(--accent-500) 12%, transparent);
}

.center-card-users {
  background:
    linear-gradient(135deg, rgba(15, 95, 148, 0.12), rgba(40, 137, 108, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-users::before {
  background: linear-gradient(135deg, rgba(40, 137, 108, 0.42), rgba(15, 95, 148, 0.3));
}

.center-card-users:hover,
.center-card-users:focus-visible {
  border-color: rgba(15, 95, 148, 0.24);
}

.center-card-roles {
  background:
    linear-gradient(135deg, rgba(176, 92, 23, 0.12), rgba(194, 122, 52, 0.14)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-roles::before {
  background: linear-gradient(135deg, rgba(194, 122, 52, 0.48), rgba(176, 92, 23, 0.3));
}

.center-card-roles:hover,
.center-card-roles:focus-visible {
  border-color: rgba(176, 92, 23, 0.24);
}

.center-card-permissions {
  background:
    linear-gradient(135deg, rgba(97, 60, 196, 0.12), rgba(64, 129, 223, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-permissions::before {
  background: linear-gradient(135deg, rgba(97, 60, 196, 0.4), rgba(64, 129, 223, 0.3));
}

.center-card-permissions:hover,
.center-card-permissions:focus-visible {
  border-color: rgba(97, 60, 196, 0.24);
}

.center-card-audit {
  background:
    linear-gradient(135deg, rgba(121, 48, 108, 0.12), rgba(191, 74, 109, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-audit::before {
  background: linear-gradient(135deg, rgba(121, 48, 108, 0.38), rgba(191, 74, 109, 0.28));
}

.center-card-audit:hover,
.center-card-audit:focus-visible {
  border-color: rgba(121, 48, 108, 0.24);
}

.center-card-head,
.center-card-body,
.center-card-tags,
.center-card-footer {
  position: relative;
  z-index: 1;
}

.center-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.center-card-badge,
.center-card-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 78%, transparent);
}

.center-card-badge {
  width: 44px;
  height: 44px;
}

.center-card-arrow {
  width: 34px;
  height: 34px;
  color: var(--text-secondary);
}

.center-card-body {
  display: grid;
  gap: 8px;
}

.center-card-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.center-card-title {
  margin: 0;
  font-size: 24px;
  line-height: 1.2;
  color: var(--text-primary);
}

.center-card-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
}

.center-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.center-card-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
  border: 1px solid color-mix(in srgb, var(--panel-border) 70%, transparent);
  font-size: 12px;
  color: var(--text-secondary);
}

.center-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}
</style>
