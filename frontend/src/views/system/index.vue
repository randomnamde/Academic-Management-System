<template>
  <div class="app-page space-y-3">
    <AppCard :title="t('system.title')" surface="glass" content-class="p-4">
      <div class="grid gap-3 md:grid-cols-2">
        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="text-[15px] font-semibold text-primary-900">{{ t('system.theme.title') }}</p>
          <p class="mt-1 text-[13px] text-slatex-500">{{ t('system.theme.desc') }}</p>
          <div class="mt-3 flex items-center gap-2">
            <button class="theme-option touch-target is-active" @click="cycleThemeMode">
              <component :is="currentThemeIcon" class="h-4 w-4" />
              <span>{{ t('system.theme.current', { mode: t(`theme.mode.${themeMode}`) }) }}</span>
            </button>
          </div>
          <p class="mt-2 text-[12px] text-slatex-500">{{ t('system.theme.cycleHint') }}</p>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="text-[15px] font-semibold text-primary-900">{{ t('system.language.title') }}</p>
          <p class="mt-1 text-[13px] text-slatex-500">{{ t('system.language.desc') }}</p>
          <div class="mt-3 flex items-center gap-2">
            <button class="theme-option touch-target is-active" @click="toggleLanguage">
              <Languages class="h-4 w-4" />
              <span>{{ t('system.language.current', { lang: languageLabel }) }}</span>
            </button>
          </div>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="text-[15px] font-semibold text-primary-900">{{ t('system.tableDensity.title') }}</p>
          <p class="mt-1 text-[13px] text-slatex-500">{{ t('system.tableDensity.desc') }}</p>
          <div class="mt-3 flex items-center gap-2">
            <AppButton :variant="tableDensity === 'compact' ? 'primary' : 'secondary'" @click="setDensity('compact')">{{ t('system.tableDensity.compact') }}</AppButton>
            <AppButton :variant="tableDensity === 'comfortable' ? 'primary' : 'secondary'" @click="setDensity('comfortable')">{{ t('system.tableDensity.comfortable') }}</AppButton>
          </div>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="text-[15px] font-semibold text-primary-900">{{ t('system.currentSemester.title') }}</p>
          <p class="mt-1 text-[13px] text-slatex-500">{{ t('system.currentSemester.desc') }}</p>
          <div class="mt-3 flex items-center gap-2">
            <el-input v-model="currentSemester" :disabled="!canEditSemester" :placeholder="t('system.currentSemester.placeholder')" />
            <AppButton v-if="canEditSemester" :loading="semesterSaving" @click="saveSemester">{{ t('system.currentSemester.save') }}</AppButton>
          </div>
          <p v-if="!canEditSemester" class="mt-2 text-[12px] text-slatex-500">{{ t('system.currentSemester.noPermission') }}</p>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3 md:col-span-2">
          <p class="text-[15px] font-semibold text-primary-900">{{ t('system.permissionCenter.title') }}</p>
          <p class="mt-1 text-[13px] text-slatex-500">{{ t('system.permissionCenter.desc') }}</p>
          <div class="mt-3">
            <AppButton variant="secondary" @click="$router.push('/rbac/users')">{{ t('system.permissionCenter.enter') }}</AppButton>
          </div>
        </div>
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
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Languages, Monitor, Moon, SunMedium } from 'lucide-vue-next'
import { useI18n } from 'vue-i18n'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { updatePassword } from '@/api/user'
import { useTheme } from '@/composables/useTheme'
import { useLanguage } from '@/composables/useLanguage'
import { getCurrentSemester, updateCurrentSemester } from '@/api/system'

const store = useStore()
const { t } = useI18n()
const tableDensity = computed(() => store.getters.tableDensity)
const { mode: themeMode, cycleThemeMode } = useTheme(store)
const { language, toggleLanguage } = useLanguage(store)

const themeIconMap = {
  system: Monitor,
  light: SunMedium,
  dark: Moon
}

const currentThemeIcon = computed(() => themeIconMap[themeMode.value] || Monitor)
const languageLabel = computed(() => (language.value === 'en-US' ? t('language.enUS') : t('language.zhCN')))

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

.theme-option {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  border-radius: 10px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  transition: all 180ms ease;
}

.theme-option:hover {
  color: var(--text-primary);
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
}

.theme-option.is-active {
  border-color: color-mix(in srgb, var(--accent-500) 34%, transparent);
  background: color-mix(in srgb, var(--accent-500) 16%, transparent);
  color: var(--accent-700);
}
</style>

