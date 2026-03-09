<template>
  <div class="app-page space-y-3 profile-page">
    <AppCard surface="elevated" body-class="overflow-hidden" content-class="p-0">
      <section class="profile-hero">
        <div class="profile-hero-main">
          <div class="profile-hero-avatar-wrap">
            <div class="profile-avatar-shell">
              <el-avatar :size="96" :src="user.avatar || defaultAvatar" />
            </div>
            <span class="profile-status-pill" :class="user.status === 1 ? 'is-success' : 'is-danger'">
              {{ user.status === 1 ? t('profile.enabled') : t('profile.disabled') }}
            </span>
          </div>

          <div class="profile-hero-copy">
            <p class="profile-hero-kicker">{{ t('profile.pageTitle') }}</p>
            <div class="profile-hero-heading">
              <div>
                <h1 class="profile-hero-title">{{ displayName }}</h1>
                <p class="profile-hero-subtitle">{{ t('profile.summaryDesc') }}</p>
              </div>
              <span class="profile-role-pill profile-role-pill-primary">
                {{ primaryRoleLabel }}
              </span>
            </div>

            <div class="profile-role-list">
              <span
                v-for="role in roleList"
                :key="role"
                class="profile-role-pill"
                :class="{ 'profile-role-pill-primary': role === primaryRoleCode }"
              >
                {{ roleLabel(role) }}
              </span>
            </div>

            <div class="profile-scope-list">
              <span v-for="item in scopeItems" :key="item.label" class="profile-scope-pill">
                <strong>{{ item.label }}</strong>
                <span>{{ item.value }}</span>
              </span>
            </div>

            <div class="profile-hero-actions">
              <AppButton @click="scrollToSection('profile')">{{ t('profile.quickEditProfile') }}</AppButton>
              <AppButton variant="secondary" @click="scrollToSection('password')">{{ t('profile.quickChangePassword') }}</AppButton>
            </div>
          </div>
        </div>

        <div class="profile-hero-stats">
          <article v-for="item in heroStats" :key="item.label" class="profile-stat-card">
            <div class="profile-stat-icon">
              <component :is="item.icon" class="h-4 w-4" />
            </div>
            <div class="profile-stat-body">
              <p class="profile-stat-label">{{ item.label }}</p>
              <p class="profile-stat-value">{{ item.value }}</p>
            </div>
          </article>
        </div>
      </section>
    </AppCard>

    <div class="profile-content-grid">
      <div class="space-y-3">
        <div ref="profileSectionRef" class="profile-section-anchor" :class="{ 'is-highlighted': highlightedSection === 'profile' }">
          <AppCard :title="t('profile.profileSectionTitle')" surface="glass" content-class="p-4 md:p-5">
            <p class="profile-section-desc">{{ t('profile.profileSectionDesc') }}</p>

            <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="profile-form">
              <div class="profile-form-grid">
                <el-form-item :label="t('profile.realName')" prop="realName" class="profile-field">
                  <el-input v-model="form.realName" />
                </el-form-item>

                <el-form-item :label="t('profile.phone')" class="profile-field">
                  <el-input v-model="form.phone" />
                </el-form-item>

                <el-form-item :label="t('profile.email')" class="profile-field">
                  <el-input v-model="form.email" />
                </el-form-item>

                <el-form-item :label="t('profile.avatarUrl')" class="profile-field">
                  <el-input v-model="form.avatar" :placeholder="t('profile.avatarUrlPlaceholder')" />
                </el-form-item>

                <el-form-item :label="t('profile.avatar')" class="profile-field profile-field-span-2">
                  <div class="avatar-upload-panel">
                    <div class="avatar-preview-panel">
                      <el-avatar :size="72" :src="form.avatar || defaultAvatar" />
                      <div class="avatar-preview-copy">
                        <p class="avatar-preview-title">{{ t('profile.avatarPreviewTitle') }}</p>
                        <p class="avatar-preview-desc">{{ t('profile.avatarTip') }}</p>
                      </div>
                    </div>

                    <div class="avatar-upload-actions">
                      <el-upload
                        class="avatar-uploader"
                        :show-file-list="false"
                        :before-upload="beforeAvatarUpload"
                        :http-request="handleAvatarUpload"
                        :disabled="uploadingAvatar"
                        accept="image/*"
                      >
                        <AppButton variant="secondary" :loading="uploadingAvatar">{{ t('profile.uploadAvatar') }}</AppButton>
                      </el-upload>
                      <span class="avatar-upload-note">{{ t('profile.avatarUploadHint') }}</span>
                    </div>
                  </div>
                </el-form-item>

                <el-form-item class="profile-field profile-field-span-2">
                  <div class="profile-form-actions">
                    <AppButton :loading="saving" @click="submit">{{ t('profile.saveProfile') }}</AppButton>
                    <AppButton variant="secondary" @click="resetFromUser">{{ t('common.reset') }}</AppButton>
                  </div>
                </el-form-item>
              </div>
            </el-form>
          </AppCard>
        </div>

        <div ref="passwordSectionRef" class="profile-section-anchor" :class="{ 'is-highlighted': highlightedSection === 'password' }">
          <AppCard :title="t('profile.passwordSectionTitle')" surface="base" content-class="p-4 md:p-5">
            <p class="profile-section-desc">{{ t('profile.passwordSectionDesc') }}</p>

            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top" class="profile-form">
              <div class="profile-form-grid">
                <el-form-item :label="t('profile.oldPassword')" prop="oldPassword" class="profile-field profile-field-span-2">
                  <el-input v-model="pwdForm.oldPassword" type="password" show-password />
                </el-form-item>

                <el-form-item :label="t('profile.newPassword')" prop="newPassword" class="profile-field">
                  <el-input v-model="pwdForm.newPassword" type="password" show-password />
                </el-form-item>

                <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword" class="profile-field">
                  <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
                </el-form-item>

                <el-form-item class="profile-field profile-field-span-2">
                  <div class="profile-form-actions">
                    <AppButton :loading="updatingPassword" @click="submitPassword">{{ t('profile.updatePassword') }}</AppButton>
                    <AppButton variant="secondary" @click="resetPasswordForm">{{ t('common.reset') }}</AppButton>
                  </div>
                </el-form-item>
              </div>
            </el-form>
          </AppCard>
        </div>
      </div>

      <div class="space-y-3">
        <AppCard :title="t('profile.accountInfo')" surface="base" content-class="p-4">
          <div class="profile-overview-list">
            <div v-for="item in overviewItems" :key="item.label" class="profile-overview-row">
              <span class="profile-overview-label">{{ item.label }}</span>
              <span class="profile-overview-value">{{ item.value }}</span>
            </div>
          </div>
        </AppCard>

        <AppCard :title="t('profile.securityTipsTitle')" surface="base" content-class="p-4">
          <ul class="profile-security-list">
            <li>{{ t('profile.securityTip1') }}</li>
            <li>{{ t('profile.securityTip2') }}</li>
            <li>{{ t('profile.securityTip3') }}</li>
          </ul>
        </AppCard>
      </div>
    </div>
  </div>
</template>

<script setup>
import { IdCard, Landmark, ShieldCheck, UserRound } from 'lucide-vue-next'
import dayjs from 'dayjs'
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import AppCard from '@/components/ui/AppCard.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { getUserInfo, updatePassword, updateProfile, uploadAvatar } from '@/api/user'

const route = useRoute()
const store = useStore()
const { t } = useI18n()

const formRef = ref()
const pwdFormRef = ref()
const profileSectionRef = ref()
const passwordSectionRef = ref()
const saving = ref(false)
const updatingPassword = ref(false)
const uploadingAvatar = ref(false)
const highlightedSection = ref('profile')
const pageReady = ref(false)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const user = reactive({
  id: null,
  account: '',
  username: '',
  realName: '',
  role: '',
  primaryRole: '',
  roles: [],
  avatar: '',
  phone: '',
  email: '',
  status: 1,
  createTime: '',
  collegeName: '',
  teacherNo: '',
  teacherDepartment: '',
  studentNo: '',
  className: ''
})

const form = reactive({
  realName: '',
  avatar: '',
  phone: '',
  email: ''
})

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const rules = computed(() => ({
  realName: [{ required: true, message: t('profile.realNameRequired'), trigger: 'blur' }]
}))

const pwdRules = computed(() => ({
  oldPassword: [{ required: true, message: t('profile.oldPasswordRequired'), trigger: 'blur' }],
  newPassword: [{ required: true, message: t('profile.newPasswordRequired'), trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: t('profile.confirmPasswordRequired'), trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error(t('profile.passwordNotMatch')))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}))

const displayName = computed(() => user.realName || user.account || user.username || t('common.user'))
const primaryRoleCode = computed(() => user.primaryRole || user.role || '')
const primaryRoleLabel = computed(() => roleLabel(primaryRoleCode.value))
const formattedCreateTime = computed(() => formatDateTime(user.createTime))
const roleList = computed(() => {
  const roles = Array.isArray(user.roles) ? user.roles.filter(Boolean) : []
  if (!roles.length && primaryRoleCode.value) return [primaryRoleCode.value]
  return roles
})
const isContactReady = computed(() => Boolean(user.phone && user.email))

const scopeItems = computed(() => {
  if (primaryRoleCode.value === 'SCHOOL_ADMIN') {
    return [{ label: t('profile.scope'), value: t('profile.scopeSchool') }]
  }

  if (primaryRoleCode.value === 'COLLEGE_ADMIN') {
    return [
      { label: t('profile.scope'), value: t('profile.scopeCollege') },
      user.collegeName ? { label: t('profile.collegeName'), value: user.collegeName } : null
    ].filter(Boolean)
  }

  if (primaryRoleCode.value === 'HOMEROOM_TEACHER' || primaryRoleCode.value === 'COURSE_TEACHER') {
    return [
      { label: t('profile.scope'), value: t('profile.scopeTeacher') },
      user.teacherNo ? { label: t('profile.teacherNo'), value: user.teacherNo } : null,
      user.collegeName ? { label: t('profile.collegeName'), value: user.collegeName } : null,
      user.teacherDepartment ? { label: t('profile.teacherDepartment'), value: user.teacherDepartment } : null
    ].filter(Boolean)
  }

  if (primaryRoleCode.value === 'STUDENT') {
    return [
      { label: t('profile.scope'), value: t('profile.scopeStudent') },
      user.studentNo ? { label: t('profile.studentNo'), value: user.studentNo } : null,
      user.className ? { label: t('profile.className'), value: user.className } : null,
      user.collegeName ? { label: t('profile.collegeName'), value: user.collegeName } : null
    ].filter(Boolean)
  }

  return []
})

const heroStats = computed(() => ([
  {
    label: t('profile.username'),
    value: user.account || user.username || '-',
    icon: UserRound
  },
  {
    label: t('profile.primaryRole'),
    value: primaryRoleLabel.value,
    icon: ShieldCheck
  },
  {
    label: t('profile.scope'),
    value: scopeItems.value[0]?.value || '-',
    icon: Landmark
  },
  {
    label: t('profile.createdAt'),
    value: formattedCreateTime.value,
    icon: IdCard
  }
]))

const overviewItems = computed(() => {
  const items = [
    { label: t('profile.username'), value: user.account || user.username || '-' },
    { label: t('profile.primaryRole'), value: primaryRoleLabel.value },
    roleList.value.length > 1 ? { label: t('profile.allRoles'), value: roleList.value.map((role) => roleLabel(role)).join(' / ') } : null,
    { label: t('profile.accountStatus'), value: user.status === 1 ? t('profile.enabled') : t('profile.disabled') },
    { label: t('profile.createdAt'), value: formattedCreateTime.value },
    user.collegeName ? { label: t('profile.collegeName'), value: user.collegeName } : null,
    user.teacherNo ? { label: t('profile.teacherNo'), value: user.teacherNo } : null,
    user.teacherDepartment ? { label: t('profile.teacherDepartment'), value: user.teacherDepartment } : null,
    user.studentNo ? { label: t('profile.studentNo'), value: user.studentNo } : null,
    user.className ? { label: t('profile.className'), value: user.className } : null,
    {
      label: t('profile.contactStatus'),
      value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete')
    }
  ]

  return items.filter(Boolean)
})

function roleLabel(role) {
  if (role === 'SCHOOL_ADMIN') return t('roles.schoolAdmin')
  if (role === 'COLLEGE_ADMIN') return t('roles.collegeAdmin')
  if (role === 'HOMEROOM_TEACHER') return t('roles.homeroomTeacher')
  if (role === 'COURSE_TEACHER') return t('roles.courseTeacher')
  if (role === 'STUDENT') return t('roles.student')
  return role || '-'
}

function formatDateTime(value) {
  if (!value) return '-'
  const date = dayjs(value)
  return date.isValid() ? date.format('YYYY-MM-DD HH:mm') : String(value)
}

function resetFromUser() {
  form.realName = user.realName || ''
  form.avatar = user.avatar || ''
  form.phone = user.phone || ''
  form.email = user.email || ''
}

function assignUser(data = {}) {
  Object.assign(user, {
    id: data.id ?? null,
    account: data.account || data.username || '',
    username: data.username || data.account || '',
    realName: data.realName || '',
    role: data.role || '',
    primaryRole: data.primaryRole || '',
    roles: Array.isArray(data.roles) ? data.roles : [],
    avatar: data.avatar || '',
    phone: data.phone || '',
    email: data.email || '',
    status: typeof data.status === 'number' ? data.status : 1,
    createTime: data.createTime || '',
    collegeName: data.collegeName || '',
    teacherNo: data.teacherNo || '',
    teacherDepartment: data.teacherDepartment || '',
    studentNo: data.studentNo || '',
    className: data.className || ''
  })
}

async function fetchUser() {
  const res = await getUserInfo()
  assignUser(res.data || {})
  resetFromUser()
  store.commit('SET_USER_INFO', res.data || {})
}

function resetPasswordForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
}

function getSectionRef(section) {
  return section === 'password' ? passwordSectionRef.value : profileSectionRef.value
}

function scrollToSection(section, smooth = true) {
  highlightedSection.value = section
  nextTick(() => {
    const target = getSectionRef(section)
    target?.scrollIntoView({
      behavior: smooth ? 'smooth' : 'auto',
      block: 'start'
    })
  })
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateProfile({ ...form })
    await fetchUser()
    ElMessage.success(t('profile.profileUpdated'))
  } finally {
    saving.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = !!file.type && file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error(t('profile.onlyImageAllowed'))
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error(t('profile.avatarTooLarge'))
    return false
  }
  return true
}

async function handleAvatarUpload(option) {
  uploadingAvatar.value = true
  try {
    const res = await uploadAvatar(option.file)
    form.avatar = res.data || ''
    await fetchUser()
    ElMessage.success(t('profile.avatarUploaded'))
    option.onSuccess?.(res)
  } catch (error) {
    option.onError?.(error)
  } finally {
    uploadingAvatar.value = false
  }
}

async function submitPassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  updatingPassword.value = true
  try {
    await updatePassword({
      oldPassword: pwdForm.oldPassword,
      newPassword: pwdForm.newPassword
    })
    ElMessage.success(t('profile.passwordUpdated'))
    resetPasswordForm()
  } finally {
    updatingPassword.value = false
  }
}

watch(
  () => route.query.tab,
  (tab) => {
    highlightedSection.value = tab === 'password' ? 'password' : 'profile'
    if (!pageReady.value || tab !== 'password') return
    scrollToSection('password')
  }
)

onMounted(async () => {
  await fetchUser()
  pageReady.value = true
  if (route.query.tab === 'password') {
    scrollToSection('password', false)
  }
})
</script>

<style scoped lang="scss">
.profile-page {
  --profile-hero-bg: linear-gradient(
    135deg,
    color-mix(in srgb, var(--accent-500) 14%, var(--surface-base)) 0%,
    color-mix(in srgb, var(--surface-elevated) 92%, var(--surface-base)) 52%,
    color-mix(in srgb, var(--accent-700) 12%, var(--surface-elevated)) 100%
  );
  --profile-hero-border: color-mix(in srgb, var(--accent-500) 24%, var(--panel-border));
  --profile-soft-panel: color-mix(in srgb, var(--surface-base) 82%, transparent);
  --profile-chip-bg: color-mix(in srgb, var(--surface-base) 88%, var(--accent-500) 12%);
  --profile-chip-border: color-mix(in srgb, var(--accent-500) 20%, var(--panel-border));
  --profile-highlight: color-mix(in srgb, var(--accent-500) 12%, transparent);
  --profile-highlight-strong: color-mix(in srgb, var(--accent-500) 24%, transparent);
}

:global(:root[data-theme='dark']) .profile-page {
  --profile-hero-bg: linear-gradient(
    135deg,
    color-mix(in srgb, var(--accent-500) 18%, var(--surface-base)) 0%,
    color-mix(in srgb, var(--surface-elevated) 84%, var(--surface-base)) 48%,
    color-mix(in srgb, var(--accent-700) 18%, var(--surface-elevated)) 100%
  );
  --profile-hero-border: color-mix(in srgb, var(--accent-500) 30%, var(--panel-border));
  --profile-soft-panel: color-mix(in srgb, var(--surface-elevated) 78%, transparent);
  --profile-chip-bg: color-mix(in srgb, var(--surface-elevated) 84%, var(--accent-500) 16%);
  --profile-chip-border: color-mix(in srgb, var(--accent-500) 24%, var(--panel-border));
  --profile-highlight: color-mix(in srgb, var(--accent-500) 16%, transparent);
  --profile-highlight-strong: color-mix(in srgb, var(--accent-500) 28%, transparent);
}

.profile-hero {
  display: grid;
  gap: 18px;
  padding: 18px;
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent-500) 20%, transparent), transparent 36%),
    var(--profile-hero-bg);
}

.profile-hero-main {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 18px;
}

.profile-hero-avatar-wrap {
  display: flex;
  min-width: 116px;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.profile-avatar-shell {
  display: flex;
  height: 116px;
  width: 116px;
  align-items: center;
  justify-content: center;
  border-radius: 32px;
  border: 1px solid var(--profile-hero-border);
  background: color-mix(in srgb, var(--surface-base) 72%, transparent);
  box-shadow: inset 0 1px 0 color-mix(in srgb, var(--color-white) 32%, transparent);
}

.profile-status-pill,
.profile-role-pill,
.profile-scope-pill,
.profile-stat-card,
.avatar-upload-panel,
.profile-overview-row {
  border: 1px solid var(--profile-chip-border);
}

.profile-status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 999px;
  padding: 5px 12px;
  font-size: 12px;
  font-weight: 600;
  color: var(--text-primary);
  background: var(--profile-chip-bg);
}

.profile-status-pill.is-success {
  color: var(--success);
}

.profile-status-pill.is-danger {
  color: var(--danger);
}

.profile-hero-copy {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
  gap: 12px;
}

.profile-hero-kicker {
  margin: 0;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--accent-700);
}

.profile-hero-heading {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.profile-hero-title {
  margin: 0;
  font-size: clamp(28px, 3.2vw, 36px);
  line-height: 1.04;
}

.profile-hero-subtitle {
  margin: 8px 0 0;
  max-width: 48rem;
  color: var(--text-secondary);
}

.profile-role-list,
.profile-scope-list,
.profile-hero-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.profile-role-pill,
.profile-scope-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  border-radius: 999px;
  background: var(--profile-chip-bg);
  padding: 8px 12px;
  font-size: 12px;
  color: var(--text-secondary);
}

.profile-role-pill-primary {
  color: var(--accent-700);
  background: color-mix(in srgb, var(--accent-500) 16%, var(--surface-base));
}

.profile-scope-pill strong {
  font-weight: 600;
  color: var(--text-primary);
}

.profile-hero-stats {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-stat-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 88px;
  border-radius: 18px;
  background: var(--profile-soft-panel);
  padding: 14px;
  box-shadow: var(--shadow-soft);
}

.profile-stat-icon {
  display: flex;
  height: 34px;
  width: 34px;
  flex-shrink: 0;
  align-items: center;
  justify-content: center;
  border-radius: 12px;
  background: color-mix(in srgb, var(--accent-500) 16%, transparent);
  color: var(--accent-700);
}

.profile-stat-body {
  min-width: 0;
}

.profile-stat-label {
  margin: 0;
  font-size: 12px;
  color: var(--text-secondary);
}

.profile-stat-value {
  margin: 8px 0 0;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
  word-break: break-word;
}

.profile-content-grid {
  display: grid;
  gap: 12px;
}

.profile-section-anchor {
  border-radius: 18px;
  transition: box-shadow var(--motion-standard), transform var(--motion-standard);
}

.profile-section-anchor.is-highlighted {
  box-shadow: 0 0 0 1px var(--profile-highlight-strong), 0 18px 40px var(--profile-highlight);
}

.profile-section-desc {
  margin: 0 0 16px;
  color: var(--text-secondary);
}

.profile-form-grid {
  display: grid;
  gap: 14px 16px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.profile-form-grid :deep(.el-form-item) {
  margin-bottom: 0;
}

.profile-field-span-2 {
  grid-column: 1 / -1;
}

.profile-form :deep(.el-form-item__label) {
  padding-bottom: 8px;
  color: var(--text-primary);
  font-weight: 600;
}

.avatar-upload-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-radius: 18px;
  background: color-mix(in srgb, var(--surface-base) 74%, transparent);
  padding: 16px;
}

.avatar-preview-panel {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.avatar-preview-copy {
  min-width: 0;
}

.avatar-preview-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.avatar-preview-desc,
.avatar-upload-note {
  margin: 4px 0 0;
  font-size: 12px;
  line-height: 1.6;
  color: var(--text-secondary);
}

.avatar-upload-actions {
  display: flex;
  min-width: 220px;
  flex: 1;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
}

.avatar-uploader :deep(.el-upload) {
  display: inline-flex;
}

.profile-form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.profile-overview-list {
  display: grid;
  gap: 10px;
}

.profile-overview-row {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--surface-base) 74%, transparent);
  padding: 12px 14px;
}

.profile-overview-label {
  color: var(--text-secondary);
}

.profile-overview-value {
  max-width: 60%;
  text-align: right;
  color: var(--text-primary);
  word-break: break-word;
}

.profile-security-list {
  display: grid;
  gap: 12px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.profile-security-list li {
  position: relative;
  padding-left: 18px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.profile-security-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  height: 6px;
  width: 6px;
  border-radius: 999px;
  background: var(--accent-500);
}

@media (min-width: 1280px) {
  .profile-hero {
    grid-template-columns: minmax(0, 1.45fr) minmax(320px, 0.95fr);
    align-items: stretch;
    padding: 22px;
  }

  .profile-content-grid {
    grid-template-columns: minmax(0, 2fr) minmax(280px, 1fr);
    align-items: start;
  }
}

@media (max-width: 1023px) {
  .profile-hero-stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .profile-hero {
    padding: 16px;
  }

  .profile-hero-main,
  .profile-hero-heading {
    align-items: flex-start;
  }

  .profile-hero-stats,
  .profile-form-grid {
    grid-template-columns: minmax(0, 1fr);
  }

  .profile-field-span-2 {
    grid-column: auto;
  }

  .profile-overview-row {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-overview-value {
    max-width: 100%;
    text-align: left;
  }
}

@media (max-width: 479px) {
  .profile-hero-avatar-wrap {
    width: 100%;
  }

  .profile-avatar-shell {
    height: 96px;
    width: 96px;
    border-radius: 28px;
  }

  .avatar-upload-actions {
    min-width: 0;
    width: 100%;
  }

  .profile-form-actions :deep(button) {
    width: 100%;
  }

  .profile-hero-actions :deep(button) {
    width: 100%;
  }
}
</style>
