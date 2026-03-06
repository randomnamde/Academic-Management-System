<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('courseCenter.pageTitle')" content-class="p-4 space-y-4">
      <div class="center-hero">
        <p class="center-hero-badge">{{ t('courseCenter.heroBadge') }}</p>
        <p class="center-hero-desc">{{ t('courseCenter.pageDesc') }}</p>
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
            <span>{{ t('courseCenter.enterAction') }}</span>
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
import { useI18n } from 'vue-i18n'
import { ArrowRight, BookOpenText, CalendarRange } from 'lucide-vue-next'
import AppCard from '@/components/ui/AppCard.vue'
import { getCourseList } from '@/api/course'
import { getCourseArrangementList } from '@/api/courseArrangement'

const router = useRouter()
const { t } = useI18n()
const courseTotal = ref(null)
const arrangementTotal = ref(null)

const stats = computed(() => [
  {
    label: t('courseCenter.courseStatLabel'),
    value: courseTotal.value ?? '--',
    note: t('courseCenter.courseStatNote')
  },
  {
    label: t('courseCenter.arrangementStatLabel'),
    value: arrangementTotal.value ?? '--',
    note: t('courseCenter.arrangementStatNote')
  }
])

const cards = computed(() => [
  {
    path: '/course',
    variant: 'course',
    icon: BookOpenText,
    kicker: t('courseCenter.courseKicker'),
    title: t('courseCenter.courseTitle'),
    desc: t('courseCenter.courseDesc'),
    tags: [t('courseCenter.courseTagCatalog'), t('courseCenter.courseTagCredit')]
  },
  {
    path: '/course-arrangement',
    variant: 'arrangement',
    icon: CalendarRange,
    kicker: t('courseCenter.arrangementKicker'),
    title: t('courseCenter.arrangementTitle'),
    desc: t('courseCenter.arrangementDesc'),
    tags: [t('courseCenter.arrangementTagScope'), t('courseCenter.arrangementTagSchedule')]
  }
])

async function loadStats() {
  try {
    const [courseRes, arrangementRes] = await Promise.all([
      getCourseList({ page: 1, size: 1 }),
      getCourseArrangementList({ page: 1, size: 1 })
    ])
    courseTotal.value = Number(courseRes.data?.total ?? 0)
    arrangementTotal.value = Number(arrangementRes.data?.total ?? 0)
  } catch (_error) {
    courseTotal.value = null
    arrangementTotal.value = null
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
  max-width: 620px;
  font-size: 14px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 80%, var(--text-secondary));
}

.center-grid {
  display: grid;
  gap: 14px;
}

.center-stats {
  display: grid;
  gap: 12px;
}

@media (min-width: 768px) {
  .center-stats {
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

@media (min-width: 768px) {
  .center-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

.center-card {
  position: relative;
  display: grid;
  gap: 18px;
  min-height: 260px;
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

.center-card-course {
  background:
    linear-gradient(135deg, rgba(16, 85, 138, 0.12), rgba(240, 186, 64, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-course::before {
  background: linear-gradient(135deg, rgba(240, 186, 64, 0.48), rgba(16, 85, 138, 0.3));
}

.center-card-course:hover,
.center-card-course:focus-visible {
  border-color: rgba(16, 85, 138, 0.24);
}

.center-card-arrangement {
  background:
    linear-gradient(135deg, rgba(0, 134, 102, 0.12), rgba(24, 119, 242, 0.12)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 92%, transparent), color-mix(in srgb, var(--surface-elevated) 78%, transparent));
}

.center-card-arrangement::before {
  background: linear-gradient(135deg, rgba(0, 134, 102, 0.42), rgba(24, 119, 242, 0.3));
}

.center-card-arrangement:hover,
.center-card-arrangement:focus-visible {
  border-color: rgba(0, 134, 102, 0.24);
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
