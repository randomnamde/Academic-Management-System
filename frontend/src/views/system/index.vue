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

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <div class="preference-card-head">
            <div>
              <p class="preference-card-title">{{ t('system.currentSemester.title') }}</p>
              <p class="preference-card-desc">{{ t('system.currentSemester.desc') }}</p>
            </div>
            <span class="preference-pill" :class="{ 'is-muted': !canEditSemester }">
              <span class="truncate">{{ currentSemester || '--' }}</span>
            </span>
          </div>
          <div class="semester-action-row mt-3">
            <el-input
              v-model="currentSemester"
              class="semester-input"
              :disabled="!canEditSemester"
              :placeholder="t('system.currentSemester.placeholder')"
            />
            <AppButton
              v-if="canEditSemester"
              class="semester-save"
              :loading="semesterSaving"
              @click="saveSemester"
            >
              {{ t('system.currentSemester.save') }}
            </AppButton>
          </div>
          <p v-if="!canEditSemester" class="preference-card-note mt-3">{{ t('system.currentSemester.noPermission') }}</p>
        </div>

        <button
          type="button"
          class="permission-card touch-target md:col-span-2"
          @click="goPermissionCenter"
        >
          <div class="permission-card-head">
            <div class="permission-card-badge">
              <ShieldCheck class="h-5 w-5" />
            </div>
            <span class="permission-card-arrow">
              <ArrowRight class="h-4 w-4" />
            </span>
          </div>

          <div class="permission-card-body">
            <p class="permission-card-kicker">{{ t('system.permissionCenter.kicker') }}</p>
            <p class="permission-card-title">{{ t('system.permissionCenter.title') }}</p>
            <p class="permission-card-desc">{{ t('system.permissionCenter.desc') }}</p>
          </div>

          <div class="permission-card-tags">
            <span class="permission-card-tag">{{ t('system.permissionCenter.tagEntry') }}</span>
            <span class="permission-card-tag">{{ t('system.permissionCenter.tagAudit') }}</span>
          </div>

          <div class="permission-card-footer">
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
import { ArrowRight, Languages, Monitor, Moon, ShieldCheck, SunMedium } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { updatePassword } from '@/api/user'
import { useTheme } from '@/composables/useTheme'
import { useLanguage } from '@/composables/useLanguage'
import { getCurrentSemester, updateCurrentSemester } from '@/api/system'

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
const semesterSaving = ref(false)
const allRoles = computed(() => {
  const set = new Set(Array.isArray(store.state.userInfo?.roles) ? store.state.userInfo.roles : [])
  if (store.state.userInfo?.primaryRole) set.add(store.state.userInfo.primaryRole)
  if (store.state.userInfo?.role) set.add(store.state.userInfo.role)
  return set
})
const canEditSemester = computed(() => allRoles.value.has('SCHOOL_ADMIN'))

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

async function fetchSemester() {
  const res = await getCurrentSemester()
  currentSemester.value = res.data?.currentSemester || ''
}

async function saveSemester() {
  if (!currentSemester.value.trim()) {
    ElMessage.warning(t('system.currentSemester.emptyWarn'))
    return
  }
  semesterSaving.value = true
  try {
    await updateCurrentSemester(currentSemester.value.trim())
    ElMessage.success(t('system.currentSemester.saveSuccess'))
  } finally {
    semesterSaving.value = false
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

onMounted(fetchSemester)
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

.preference-pill.is-muted {
  opacity: 0.78;
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

.semester-action-row {
  display: grid;
  gap: 12px;
}

.semester-save {
  justify-self: start;
}

@media (min-width: 640px) {
  .semester-action-row {
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
  }

  .semester-save {
    justify-self: auto;
  }
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

  .semester-save {
    width: 100%;
  }
}

.permission-card {
  position: relative;
  display: grid;
  gap: 18px;
  padding: 22px;
  overflow: hidden;
  border-radius: 22px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  text-align: left;
  background:
    radial-gradient(circle at top left, rgba(14, 113, 145, 0.2), transparent 42%),
    linear-gradient(135deg, rgba(28, 113, 168, 0.12), rgba(34, 151, 112, 0.14)),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 80%, transparent));
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.permission-card::before {
  content: '';
  position: absolute;
  inset: auto -32px -46px auto;
  width: 150px;
  height: 150px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(34, 151, 112, 0.35), rgba(28, 113, 168, 0.28));
  filter: blur(10px);
  opacity: 0.7;
}

.permission-card:hover,
.permission-card:focus-visible {
  transform: translateY(-3px);
  border-color: rgba(28, 113, 168, 0.24);
  box-shadow: 0 22px 36px color-mix(in srgb, var(--accent-500) 12%, transparent);
}

.permission-card-head,
.permission-card-body,
.permission-card-tags,
.permission-card-footer {
  position: relative;
  z-index: 1;
}

.permission-card-head,
.permission-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.permission-card-badge,
.permission-card-arrow {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 14px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
}

.permission-card-badge {
  width: 46px;
  height: 46px;
  color: color-mix(in srgb, var(--accent-700) 86%, var(--text-primary));
}

.permission-card-arrow {
  width: 34px;
  height: 34px;
  color: var(--text-secondary);
}

.permission-card-body {
  display: grid;
  gap: 8px;
}

.permission-card-kicker {
  margin: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.permission-card-title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.permission-card-desc {
  margin: 0;
  max-width: 720px;
  font-size: 14px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 80%, var(--text-secondary));
}

.permission-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.permission-card-tag {
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

.permission-card-footer {
  font-size: 13px;
  font-weight: 600;
  color: color-mix(in srgb, var(--accent-700) 88%, var(--text-primary));
}
</style>
