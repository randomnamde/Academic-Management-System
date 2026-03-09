<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('system.title')" surface="glass" content-class="p-4">
      <div class="grid gap-3 md:grid-cols-2">
        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <div class="preference-card-head">
            <div>
              <p class="preference-card-title">{{ t('system.theme.title') }}</p>
              <p class="preference-card-desc">{{ t('system.theme.desc') }}</p>
            </div>
            <span class="preference-pill">
              <component :is="activeThemeOption.icon" class="h-4 w-4" />
              <span>{{ t('system.theme.current', { mode: t(`theme.mode.${themeMode}`) }) }}</span>
            </span>
          </div>
          <div class="option-grid option-grid-theme mt-3">
            <button
              v-for="option in themeOptions"
              :key="option.value"
              type="button"
              class="option-tile touch-target"
              :class="{ 'is-active': themeMode === option.value }"
              @click="setThemeMode(option.value)"
            >
              <component :is="option.icon" class="h-4 w-4" />
              <span>{{ option.label }}</span>
            </button>
          </div>
          <p class="preference-card-note mt-3">{{ t('system.theme.cycleHint') }}</p>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <div class="preference-card-head">
            <div>
              <p class="preference-card-title">{{ t('system.language.title') }}</p>
              <p class="preference-card-desc">{{ t('system.language.desc') }}</p>
            </div>
            <span class="preference-pill">
              <Languages class="h-4 w-4" />
              <span>{{ t('system.language.current', { lang: languageLabel }) }}</span>
            </span>
          </div>
          <div class="option-grid mt-3">
            <button
              v-for="option in languageOptions"
              :key="option.value"
              type="button"
              class="option-tile touch-target"
              :class="{ 'is-active': language === option.value }"
              @click="setLanguage(option.value)"
            >
              <Languages class="h-4 w-4" />
              <span>{{ option.label }}</span>
            </button>
          </div>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="preference-card-title">{{ t('system.tableDensity.title') }}</p>
          <p class="preference-card-desc">{{ t('system.tableDensity.desc') }}</p>
          <div class="mt-3 flex items-center gap-2">
            <AppButton :variant="tableDensity === 'compact' ? 'primary' : 'secondary'" @click="setDensity('compact')">{{ t('system.tableDensity.compact') }}</AppButton>
            <AppButton :variant="tableDensity === 'comfortable' ? 'primary' : 'secondary'" @click="setDensity('comfortable')">{{ t('system.tableDensity.comfortable') }}</AppButton>
          </div>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3 md:col-span-2">
          <div class="preference-card-head">
            <div>
              <p class="preference-card-title">{{ t('system.courseTimeSlots.title') }}</p>
              <p class="preference-card-desc">{{ t('system.courseTimeSlots.desc') }}</p>
            </div>
            <span class="preference-pill">
              <Clock3 class="h-4 w-4" />
              <span>{{ t('system.courseTimeSlots.preview') }}</span>
            </span>
          </div>

          <div class="course-slot-preview mt-3">
            <span v-for="slot in courseTimeSlotPreview" :key="slot" class="course-slot-tag">{{ slot }}</span>
          </div>

          <p class="preference-card-note mt-3">{{ t('system.courseTimeSlots.hint') }}</p>

          <div v-loading="courseTimeSlotsLoading" class="course-slot-editor mt-3">
            <div v-for="(slot, index) in courseTimeSlots" :key="`slot-${index}`" class="course-slot-row">
              <el-input
                v-model="courseTimeSlots[index]"
                :placeholder="t('system.courseTimeSlots.placeholder')"
                class="course-slot-input"
              />
              <button
                type="button"
                class="course-slot-remove touch-target"
                :disabled="courseTimeSlots.length === 1 && !courseTimeSlots[0]?.trim()"
                @click="removeCourseTimeSlot(index)"
              >
                <Trash2 class="h-4 w-4" />
              </button>
            </div>
          </div>

          <div class="course-slot-actions mt-3">
            <AppButton variant="secondary" :disabled="courseTimeSlots.length >= 12" @click="addCourseTimeSlot">
              <Plus class="h-4 w-4" />
              <span>{{ t('system.courseTimeSlots.add') }}</span>
            </AppButton>
            <AppButton variant="secondary" @click="resetCourseTimeSlots">{{ t('system.courseTimeSlots.reset') }}</AppButton>
            <AppButton :loading="courseTimeSlotsSaving" @click="submitCourseTimeSlots">{{ t('system.courseTimeSlots.save') }}</AppButton>
          </div>
        </div>

        <button
          type="button"
          class="entry-card entry-card-semester touch-target"
          @click="goSemesterManagement"
        >
          <div class="entry-card-head">
            <div class="entry-card-badge">
              <CalendarRange class="h-5 w-5" />
            </div>
            <span class="entry-card-arrow">
              <ArrowRight class="h-4 w-4" />
            </span>
          </div>

          <div class="entry-card-body">
            <p class="entry-card-kicker">{{ t('system.currentSemester.kicker') }}</p>
            <p class="entry-card-title">{{ t('system.currentSemester.title') }}</p>
            <p class="entry-card-desc">{{ t('system.currentSemester.desc') }}</p>
          </div>

          <div class="entry-card-tags">
            <span class="entry-card-tag">{{ currentSemester || t('system.currentSemester.emptyValue') }}</span>
            <span class="entry-card-tag">{{ currentSemesterStatusLabel }}</span>
          </div>

          <div class="entry-card-footer">
            <span>{{ t('system.currentSemester.enter') }}</span>
            <ArrowRight class="h-4 w-4" />
          </div>
        </button>

        <button
          type="button"
          class="entry-card entry-card-permission touch-target md:col-span-2"
          @click="goPermissionCenter"
        >
          <div class="entry-card-head">
            <div class="entry-card-badge">
              <ShieldCheck class="h-5 w-5" />
            </div>
            <span class="entry-card-arrow">
              <ArrowRight class="h-4 w-4" />
            </span>
          </div>

          <div class="entry-card-body">
            <p class="entry-card-kicker">{{ t('system.permissionCenter.kicker') }}</p>
            <p class="entry-card-title">{{ t('system.permissionCenter.title') }}</p>
            <p class="entry-card-desc">{{ t('system.permissionCenter.desc') }}</p>
          </div>

          <div class="entry-card-tags">
            <span class="entry-card-tag">{{ t('system.permissionCenter.tagEntry') }}</span>
            <span class="entry-card-tag">{{ t('system.permissionCenter.tagAudit') }}</span>
          </div>

          <div class="entry-card-footer">
            <span>{{ t('system.permissionCenter.enter') }}</span>
            <ArrowRight class="h-4 w-4" />
          </div>
        </button>
      </div>
    </AppCard>

    <AppCard :title="t('system.accountSecurity')" surface="base" content-class="p-4">
      <el-form ref="pwdFormRef" :model="passwordForm" :rules="pwdRules" label-width="96px" class="max-w-2xl">
        <el-form-item :label="t('system.password.old')" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('system.password.new')" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item :label="t('system.password.confirm')" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <AppButton :loading="pwdLoading" @click="submitPassword">{{ t('system.password.save') }}</AppButton>
        </el-form-item>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { ArrowRight, CalendarRange, Clock3, Languages, Monitor, Moon, Plus, ShieldCheck, SunMedium, Trash2 } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { updatePassword } from '@/api/user'
import { useTheme } from '@/composables/useTheme'
import { useLanguage } from '@/composables/useLanguage'
import { getCourseTimeSlots, getCurrentSemester, updateCourseTimeSlots } from '@/api/system'
import { getSemesterList } from '@/api/semester'

const store = useStore()
const router = useRouter()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const { mode: themeMode, setThemeMode } = useTheme(store)
const { language, setLanguage } = useLanguage(store)

const themeIconMap = {
  system: Monitor,
  light: SunMedium,
  dark: Moon
}

const currentThemeIcon = computed(() => themeIconMap[themeMode.value] || Monitor)
const languageLabel = computed(() => (language.value === 'en-US' ? t('language.enUS') : t('language.zhCN')))
const themeOptions = computed(() => ([
  { value: 'system', label: t('theme.mode.system'), icon: Monitor },
  { value: 'light', label: t('theme.mode.light'), icon: SunMedium },
  { value: 'dark', label: t('theme.mode.dark'), icon: Moon }
]))
const languageOptions = computed(() => ([
  { value: 'zh-CN', label: t('language.zhCN') },
  { value: 'en-US', label: t('language.enUS') }
]))
const activeThemeOption = computed(() => themeOptions.value.find((option) => option.value === themeMode.value) || {
  value: 'system',
  label: t('theme.mode.system'),
  icon: currentThemeIcon.value
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdFormRef = ref()
const pwdLoading = ref(false)
const currentSemester = ref('')
const currentSemesterStatus = ref('')
const courseTimeSlots = ref([''])
const courseTimeSlotsSnapshot = ref([])
const courseTimeSlotsLoading = ref(false)
const courseTimeSlotsSaving = ref(false)

const currentSemesterStatusLabel = computed(() => {
  if (!currentSemesterStatus.value) {
    return t('system.currentSemester.emptyStatus')
  }
  return t(`semester.status.${currentSemesterStatus.value}`)
})
const courseTimeSlotPreview = computed(() => courseTimeSlots.value.map((item) => item.trim()).filter(Boolean))

const pwdRules = computed(() => ({
  oldPassword: [{ required: true, message: t('system.password.oldRequired'), trigger: 'blur' }],
  newPassword: [{ required: true, message: t('system.password.newRequired'), trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: t('system.password.confirmRequired'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error(t('system.password.notMatch')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}))

function setDensity(density) {
  store.commit('SET_TABLE_DENSITY', density)
}

function goPermissionCenter() {
  router.push('/rbac')
}

function goSemesterManagement() {
  router.push('/semester')
}

async function fetchSemesterSummary() {
  const [currentRes, activeRes] = await Promise.all([
    getCurrentSemester(),
    getSemesterList({ page: 1, size: 1, status: 'ACTIVE' })
  ])
  currentSemester.value = currentRes.data?.currentSemester || ''
  currentSemesterStatus.value = activeRes.data?.records?.[0]?.status || ''
}

function setCourseTimeSlotsState(slots = []) {
  const nextSlots = Array.isArray(slots) && slots.length ? slots : ['']
  courseTimeSlots.value = nextSlots.map((item) => String(item ?? ''))
}

async function fetchCourseTimeSlots() {
  courseTimeSlotsLoading.value = true
  try {
    const res = await getCourseTimeSlots()
    const slots = Array.isArray(res.data?.timeSlots) ? res.data.timeSlots : []
    courseTimeSlotsSnapshot.value = [...slots]
    setCourseTimeSlotsState(slots)
  } finally {
    courseTimeSlotsLoading.value = false
  }
}

function addCourseTimeSlot() {
  if (courseTimeSlots.value.length >= 12) return
  courseTimeSlots.value.push('')
}

function removeCourseTimeSlot(index) {
  if (courseTimeSlots.value.length === 1) {
    courseTimeSlots.value = ['']
    return
  }
  courseTimeSlots.value.splice(index, 1)
}

function resetCourseTimeSlots() {
  setCourseTimeSlotsState(courseTimeSlotsSnapshot.value)
}

async function submitCourseTimeSlots() {
  courseTimeSlotsSaving.value = true
  try {
    const payload = courseTimeSlotPreview.value
    await updateCourseTimeSlots(payload)
    courseTimeSlotsSnapshot.value = [...payload]
    setCourseTimeSlotsState(payload)
    ElMessage.success(t('system.courseTimeSlots.updated'))
  } finally {
    courseTimeSlotsSaving.value = false
  }
}

async function submitPassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success(t('system.password.updated'))
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}

onMounted(async () => {
  await Promise.all([fetchSemesterSummary(), fetchCourseTimeSlots()])
})
</script>

<style scoped>
.preference-card {
  border-color: color-mix(in srgb, var(--panel-border) 84%, transparent);
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
}

.preference-card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.preference-card-title {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
}

.preference-card-desc {
  margin: 6px 0 0;
  font-size: 13px;
  line-height: 1.7;
  color: color-mix(in srgb, var(--text-primary) 74%, var(--text-secondary));
}

.preference-card-note {
  margin: 0;
  font-size: 12px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.preference-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 86%, transparent);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.option-grid {
  display: grid;
  gap: 10px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.option-grid-theme {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.option-tile {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 14px;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  transition: all 180ms ease;
}

.option-tile:hover,
.option-tile:focus-visible {
  color: var(--text-primary);
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
}

.option-tile.is-active {
  border-color: color-mix(in srgb, var(--accent-500) 34%, transparent);
  background: color-mix(in srgb, var(--accent-500) 16%, transparent);
  color: var(--accent-700);
  box-shadow: inset 0 0 0 1px color-mix(in srgb, var(--accent-500) 16%, transparent);
}

.course-slot-preview {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.course-slot-tag {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--accent-500) 22%, transparent);
  background: color-mix(in srgb, var(--accent-500) 12%, transparent);
  color: var(--accent-700);
  font-size: 12px;
  font-weight: 600;
}

.course-slot-editor {
  display: grid;
  gap: 10px;
}

.course-slot-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  align-items: center;
}

.course-slot-remove {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--danger) 28%, transparent);
  background: color-mix(in srgb, var(--danger) 10%, transparent);
  color: var(--danger);
  transition: all 180ms ease;
}

.course-slot-remove:hover,
.course-slot-remove:focus-visible {
  background: color-mix(in srgb, var(--danger) 16%, transparent);
}

.course-slot-remove:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.course-slot-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.entry-card {
  position: relative;
  display: grid;
  gap: 18px;
  padding: 22px;
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  text-align: left;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.entry-card::before {
  content: '';
  position: absolute;
  inset: auto -32px -46px auto;
  width: 150px;
  height: 150px;
  border-radius: 999px;
  filter: blur(10px);
  opacity: 0.7;
}

.entry-card:hover,
.entry-card:focus-visible {
  transform: translateY(-3px);
  box-shadow: 0 22px 36px color-mix(in srgb, var(--accent-500) 12%, transparent);
}

.entry-card-head,
.entry-card-body,
.entry-card-tags,
.entry-card-footer {
  position: relative;
  z-index: 1;
}

.entry-card-head,
.entry-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.entry-card-badge,
.entry-card-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
}

.entry-card-badge {
  width: 46px;
  height: 46px;
  color: color-mix(in srgb, var(--accent-700) 86%, var(--text-primary));
}

.entry-card-arrow {
  width: 34px;
  height: 34px;
  color: var(--text-secondary);
}

.entry-card-body {
  display: grid;
  gap: 8px;
}

.entry-card-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.entry-card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.entry-card-desc {
  margin: 0;
  max-width: 720px;
  font-size: 14px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 80%, var(--text-secondary));
}

.entry-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.entry-card-tag {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
  border: 1px solid color-mix(in srgb, var(--panel-border) 74%, transparent);
  font-size: 12px;
  color: color-mix(in srgb, var(--text-primary) 76%, var(--text-secondary));
}

.entry-card-footer {
  font-size: 13px;
  font-weight: 600;
  color: color-mix(in srgb, var(--accent-700) 88%, var(--text-primary));
}

.entry-card-semester {
  background:
    radial-gradient(circle at top left, rgba(176, 92, 23, 0.18), transparent 42%),
    linear-gradient(135deg, rgba(194, 122, 52, 0.12), rgba(143, 76, 20, 0.14)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 80%, transparent));
}

.entry-card-semester::before {
  background: linear-gradient(135deg, rgba(194, 122, 52, 0.35), rgba(143, 76, 20, 0.28));
}

.entry-card-semester:hover,
.entry-card-semester:focus-visible {
  border-color: rgba(176, 92, 23, 0.24);
}

.entry-card-permission {
  background:
    radial-gradient(circle at top left, rgba(14, 113, 145, 0.2), transparent 42%),
    linear-gradient(135deg, rgba(28, 113, 168, 0.12), rgba(34, 151, 112, 0.14)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 80%, transparent));
}

.entry-card-permission::before {
  background: linear-gradient(135deg, rgba(34, 151, 112, 0.35), rgba(28, 113, 168, 0.28));
}

.entry-card-permission:hover,
.entry-card-permission:focus-visible {
  border-color: rgba(28, 113, 168, 0.24);
}

@media (max-width: 767px) {
  .preference-card-head {
    flex-direction: column;
    align-items: stretch;
  }

  .preference-pill {
    align-self: flex-start;
  }
}

@media (max-width: 479px) {
  .option-grid,
  .option-grid-theme {
    grid-template-columns: 1fr;
  }
}
</style>
