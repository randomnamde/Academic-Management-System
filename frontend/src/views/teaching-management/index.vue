<template>
  <div class="app-page space-y-3">
    <AppCard title="教学组织管理" content-class="p-4 space-y-4">
      <div class="center-hero">
        <p class="center-hero-badge">Teaching Entry</p>
        <p class="center-hero-desc">
          统一从这里进入班级管理、学院管理和专业管理，减少侧栏层级，便于快速切换教学组织相关模块。
        </p>
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

          <div class="center-card-footer">
            <span>进入管理</span>
            <ArrowRight class="h-4 w-4" />
          </div>
        </button>
      </div>
    </AppCard>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ArrowRight, Building2, GraduationCap, School } from 'lucide-vue-next'
import AppCard from '@/components/ui/AppCard.vue'
import { canRoute } from '@/permission/ability'

const router = useRouter()
const store = useStore()
const userInfo = computed(() => store.state.userInfo || {})
const userRole = computed(() => userInfo.value?.primaryRole || userInfo.value?.role || '')
const userPermissions = computed(() => userInfo.value?.permissions || [])

const cards = computed(() =>
  [
    {
      path: '/college',
      routeName: 'College',
      variant: 'college',
      icon: Building2,
      title: '学院管理',
      kicker: 'Colleges',
      desc: '维护学院编码、学院名称以及学院管理员绑定关系。'
    },
    {
      path: '/major',
      routeName: 'Major',
      variant: 'major',
      icon: GraduationCap,
      title: '专业管理',
      kicker: 'Majors',
      desc: '维护专业编码、专业名称和所属学院关系。'
    },
    {
      path: '/class',
      routeName: 'Class',
      variant: 'class',
      icon: School,
      kicker: 'Classes',
      title: '班级管理',
      desc: '维护班级信息、班主任、年级和人数等核心数据。'
    }
  ].filter((item) => canRoute(userRole.value, item.routeName, userPermissions.value))
)
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
  max-width: 700px;
  font-size: 14px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 80%, var(--text-secondary));
}

.center-grid {
  display: grid;
  gap: 14px;
}

@media (min-width: 768px) {
  .center-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

.center-card {
  position: relative;
  display: grid;
  gap: 18px;
  min-height: 220px;
  padding: 20px;
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  text-align: left;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.center-card:hover,
.center-card:focus-visible {
  transform: translateY(-3px);
  box-shadow: 0 18px 30px color-mix(in srgb, var(--accent-500) 12%, transparent);
}

.center-card-class {
  background:
    linear-gradient(135deg, rgba(16, 85, 138, 0.12), rgba(70, 151, 230, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-college {
  background:
    linear-gradient(135deg, rgba(0, 134, 102, 0.12), rgba(54, 179, 126, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-major {
  background:
    linear-gradient(135deg, rgba(175, 119, 0, 0.12), rgba(255, 176, 32, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-head,
.center-card-body,
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
  width: 42px;
  height: 42px;
}

.center-card-arrow {
  width: 32px;
  height: 32px;
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
  font-size: 22px;
  line-height: 1.2;
  color: var(--text-primary);
}

.center-card-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.75;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
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
