<template>
  <div class="app-page space-y-3 profile-page" :class="`theme-${roleThemeKey}`">
    <AppCard surface="glass" body-class="overflow-hidden" content-class="p-0">
      <section class="profile-command">
        <div class="profile-command-hero">
          <div class="profile-identity-panel">
            <div class="profile-avatar-shell">
              <el-avatar :size="96" :src="user.avatar || defaultAvatar" />
            </div>

            <div class="profile-identity-copy">
              <p class="profile-kicker">{{ t('profile.pageTitle') }}</p>
              <div class="profile-identity-head">
                <div>
                  <h1 class="profile-title">{{ displayName }}</h1>
                  <p class="profile-subtitle">{{ t('profile.summaryDesc') }}</p>
                </div>
                <span class="profile-status-pill" :class="user.status === 1 ? 'is-success' : 'is-danger'">
                  <BadgeCheck class="h-4 w-4" />
                  <span>{{ user.status === 1 ? t('profile.enabled') : t('profile.disabled') }}</span>
                </span>
              </div>

              <div class="profile-role-list">
                <span
                  v-for="role in roleList"
                  :key="role"
                  class="profile-role-pill"
                  :class="{ 'is-primary': role === primaryRoleCode }"
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
            </div>
          </div>

          <aside class="profile-role-focus">
            <div class="profile-role-focus-head">
              <div class="profile-role-badge">
                <component :is="roleWorkspaceMeta.icon" class="h-5 w-5" />
              </div>
              <span class="preference-pill">
                <LayoutPanelTop class="h-4 w-4" />
                <span>{{ primaryRoleLabel }}</span>
              </span>
            </div>

            <div class="profile-role-focus-body">
              <p class="profile-role-focus-kicker">{{ t('profile.scope') }}</p>
              <h2 class="profile-role-focus-title">{{ roleWorkspaceMeta.title }}</h2>
              <p class="profile-role-focus-desc">{{ roleWorkspaceMeta.desc }}</p>
            </div>

            <div class="profile-role-focus-tags">
              <span v-for="tag in roleWorkspaceMeta.tags" :key="tag" class="entry-card-tag">{{ tag }}</span>
            </div>

            <div class="profile-action-grid">
              <button type="button" class="profile-action-card touch-target" @click="scrollToSection('profile')">
                <div class="profile-action-icon">
                  <UserRound class="h-4 w-4" />
                </div>
                <div class="profile-action-copy">
                  <span class="profile-action-title">{{ t('profile.quickEditProfile') }}</span>
                  <span class="profile-action-desc">{{ t('profile.profileSectionTitle') }}</span>
                </div>
                <ArrowRight class="h-4 w-4 profile-action-arrow" />
              </button>

              <button type="button" class="profile-action-card touch-target" @click="scrollToSection('password')">
                <div class="profile-action-icon">
                  <KeyRound class="h-4 w-4" />
                </div>
                <div class="profile-action-copy">
                  <span class="profile-action-title">{{ t('profile.quickChangePassword') }}</span>
                  <span class="profile-action-desc">{{ t('profile.passwordSectionTitle') }}</span>
                </div>
                <ArrowRight class="h-4 w-4 profile-action-arrow" />
              </button>
            </div>
          </aside>
        </div>

        <div class="profile-metric-grid">
          <article v-for="item in heroStats" :key="item.label" class="metric-card">
            <div class="metric-card-icon">
              <component :is="item.icon" class="h-4 w-4" />
            </div>
            <div class="metric-card-copy">
              <p class="metric-card-label">{{ item.label }}</p>
              <p class="metric-card-value">{{ item.value }}</p>
            </div>
          </article>
        </div>
      </section>
    </AppCard>

    <div class="profile-layout">
      <AppCard class="profile-column-card" surface="glass" content-class="p-4 md:p-5">
        <div class="profile-column-body">
        <div ref="profileSectionRef" class="profile-section-anchor" :class="{ 'is-highlighted': highlightedSection === 'profile' }">
            <section class="profile-group-panel">
              <div class="group-section-head">
                <h3 class="group-section-title">{{ t('profile.profileSectionTitle') }}</h3>
                <span class="preference-pill">
                  <Sparkles class="h-4 w-4" />
                  <span>{{ t('profile.quickEditProfile') }}</span>
                </span>
              </div>

            <div class="panel-head">
              <p class="panel-desc">{{ t('profile.profileSectionDesc') }}</p>
            </div>

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
                  <div class="profile-inline-panel">
                    <div class="avatar-preview-panel">
                      <div class="avatar-preview-avatar">
                        <el-avatar :size="72" :src="form.avatar || defaultAvatar" />
                      </div>
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
            </section>
        </div>

        <div ref="passwordSectionRef" class="profile-section-anchor profile-password-anchor" :class="{ 'is-highlighted': highlightedSection === 'password' }">
            <section class="profile-group-panel profile-group-panel-muted profile-password-panel">
              <div class="group-section-head">
                <h3 class="group-section-title">{{ t('profile.passwordSectionTitle') }}</h3>
                <span class="preference-pill">
                  <ShieldCheck class="h-4 w-4" />
                  <span>{{ passwordStrengthMeta.label }}</span>
                </span>
              </div>

            <div class="panel-head">
              <p class="panel-desc">{{ t('profile.passwordSectionDesc') }}</p>
            </div>

            <div class="security-strength-card">
              <div class="security-strength-head">
                <span class="security-strength-title">{{ t('profile.passwordSectionTitle') }}</span>
                <span class="security-strength-badge" :class="`is-${passwordStrengthMeta.tone}`">
                  {{ passwordStrengthMeta.label }}
                </span>
              </div>
              <div class="security-strength-bar" aria-hidden="true">
                <span :class="`is-${passwordStrengthMeta.tone}`" :style="{ width: `${passwordStrengthMeta.percent}%` }" />
              </div>
              <div class="security-rule-list">
                <div
                  v-for="rule in passwordRuleChecks"
                  :key="rule.key"
                  class="security-rule-item"
                  :class="{ 'is-passed': rule.passed }"
                >
                  <component :is="rule.passed ? BadgeCheck : ShieldAlert" class="h-4 w-4 shrink-0" />
                  <span>{{ rule.label }}</span>
                </div>
              </div>
            </div>

            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-position="top" class="profile-form profile-password-form">
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

            <section class="profile-group-panel profile-group-panel-muted profile-security-tips-inline">
              <div class="group-section-head">
                <h3 class="group-section-title">{{ t('profile.securityTipsTitle') }}</h3>
              </div>
              <ul class="profile-security-list">
                <li>{{ t('profile.securityTip1') }}</li>
                <li>{{ t('profile.securityTip2') }}</li>
                <li>{{ t('profile.securityTip3') }}</li>
              </ul>
            </section>
            </section>
        </div>
        </div>
      </AppCard>

      <AppCard class="profile-column-card" surface="base" content-class="p-4 md:p-5">
        <div class="profile-column-body">
          <section class="profile-group-panel profile-group-panel-muted">
            <div class="group-section-head">
              <h3 class="group-section-title">{{ t('profile.accountInfo') }}</h3>
            </div>
          <div class="overview-grid">
            <article v-for="item in overviewItems" :key="item.label" class="overview-card">
              <p class="overview-card-label">{{ item.label }}</p>
              <p class="overview-card-value">{{ item.value }}</p>
            </article>
          </div>
          </section>

          <section class="profile-group-panel">
            <div class="group-section-head">
              <h3 class="group-section-title">{{ t('profile.infoAndSecurity') }}</h3>
            </div>
          <section class="identity-status-card">
            <div class="identity-status-head">
              <div class="identity-status-badge">
                <component :is="roleWorkspaceMeta.icon" class="h-5 w-5" />
              </div>
              <div>
                <p class="identity-status-title">{{ primaryRoleLabel }}</p>
                <p class="identity-status-desc">{{ roleWorkspaceMeta.desc }}</p>
              </div>
            </div>

            <div class="identity-contact-grid">
              <article v-for="item in contactCards" :key="item.label" class="identity-contact-card">
                <div class="identity-contact-icon">
                  <component :is="item.icon" class="h-4 w-4" />
                </div>
                <div class="identity-contact-copy">
                  <p class="identity-contact-label">{{ item.label }}</p>
                  <p class="identity-contact-value">{{ item.value }}</p>
                </div>
              </article>
            </div>
          </section>
          </section>

          <section class="profile-group-panel">
            <div class="group-section-head">
              <h3 class="group-section-title">{{ t('profile.roleLensTitle') }}</h3>
            </div>
          <section class="role-lens-card">
            <div class="role-lens-head">
              <div class="role-lens-badge">
                <component :is="roleExclusiveModule.icon" class="h-5 w-5" />
              </div>
              <div>
                <p class="role-lens-title">{{ roleExclusiveModule.title }}</p>
                <p class="role-lens-desc">{{ roleExclusiveModule.desc }}</p>
              </div>
            </div>

            <div class="role-lens-metrics">
              <article v-for="item in roleExclusiveModule.metrics" :key="item.label" class="role-lens-metric">
                <p class="role-lens-metric-label">{{ item.label }}</p>
                <p class="role-lens-metric-value">{{ item.value }}</p>
              </article>
            </div>

            <div class="role-lens-focus">
              <p class="role-lens-focus-kicker">{{ t('profile.roleLensFocusTitle') }}</p>
              <ul class="role-lens-checklist">
                <li v-for="item in roleExclusiveModule.highlights" :key="item">{{ item }}</li>
              </ul>
            </div>
          </section>
          </section>

        </div>
      </AppCard>
    </div>
  </div>
</template>

<script setup>
import { ArrowRight, BadgeCheck, BookOpenCheck, Building2, GraduationCap, IdCard, KeyRound, Landmark, LayoutPanelTop, Mail, Phone, ShieldAlert, ShieldCheck, Sparkles, UserRound } from 'lucide-vue-next'
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
const roleThemeKey = computed(() => {
  if (primaryRoleCode.value === 'SCHOOL_ADMIN') return 'school-admin'
  if (primaryRoleCode.value === 'COLLEGE_ADMIN') return 'college-admin'
  if (primaryRoleCode.value === 'HOMEROOM_TEACHER' || primaryRoleCode.value === 'COURSE_TEACHER') return 'teacher'
  if (primaryRoleCode.value === 'STUDENT') return 'student'
  return 'default'
})
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

const roleWorkspaceMeta = computed(() => {
  if (primaryRoleCode.value === 'SCHOOL_ADMIN') {
    return {
      icon: ShieldCheck,
      title: t('profile.scopeSchool'),
      desc: t('profile.summaryDesc'),
      tags: [primaryRoleLabel.value, t('profile.scopeSchool')]
    }
  }

  if (primaryRoleCode.value === 'COLLEGE_ADMIN') {
    return {
      icon: Building2,
      title: user.collegeName || t('profile.scopeCollege'),
      desc: t('profile.scopeCollege'),
      tags: [primaryRoleLabel.value, user.collegeName || t('profile.collegeName')]
    }
  }

  if (primaryRoleCode.value === 'HOMEROOM_TEACHER' || primaryRoleCode.value === 'COURSE_TEACHER') {
    return {
      icon: BookOpenCheck,
      title: user.teacherDepartment || t('profile.scopeTeacher'),
      desc: t('profile.scopeTeacher'),
      tags: [primaryRoleLabel.value, user.teacherNo || t('profile.teacherNo')]
    }
  }

  if (primaryRoleCode.value === 'STUDENT') {
    return {
      icon: GraduationCap,
      title: user.className || t('profile.scopeStudent'),
      desc: t('profile.scopeStudent'),
      tags: [primaryRoleLabel.value, user.studentNo || t('profile.studentNo')]
    }
  }

  return {
    icon: UserRound,
    title: primaryRoleLabel.value,
    desc: t('profile.summaryDesc'),
    tags: [primaryRoleLabel.value]
  }
})

const roleExclusiveModule = computed(() => {
  if (primaryRoleCode.value === 'SCHOOL_ADMIN') {
    return {
      icon: ShieldCheck,
      title: t('profile.roleLens.schoolAdmin.title'),
      desc: t('profile.roleLens.schoolAdmin.desc'),
      metrics: [
        { label: t('profile.roleLens.metrics.scope'), value: t('profile.scopeSchool') },
        { label: t('profile.roleLens.metrics.roles'), value: String(roleList.value.length || 1) },
        { label: t('profile.roleLens.metrics.readiness'), value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete') }
      ],
      highlights: [
        t('profile.roleLens.schoolAdmin.highlight1'),
        t('profile.roleLens.schoolAdmin.highlight2'),
        t('profile.roleLens.schoolAdmin.highlight3')
      ]
    }
  }

  if (primaryRoleCode.value === 'COLLEGE_ADMIN') {
    return {
      icon: Building2,
      title: t('profile.roleLens.collegeAdmin.title'),
      desc: t('profile.roleLens.collegeAdmin.desc'),
      metrics: [
        { label: t('profile.roleLens.metrics.scope'), value: user.collegeName || t('profile.scopeCollege') },
        { label: t('profile.roleLens.metrics.identity'), value: primaryRoleLabel.value },
        { label: t('profile.roleLens.metrics.readiness'), value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete') }
      ],
      highlights: [
        t('profile.roleLens.collegeAdmin.highlight1'),
        t('profile.roleLens.collegeAdmin.highlight2'),
        t('profile.roleLens.collegeAdmin.highlight3')
      ]
    }
  }

  if (primaryRoleCode.value === 'HOMEROOM_TEACHER' || primaryRoleCode.value === 'COURSE_TEACHER') {
    return {
      icon: BookOpenCheck,
      title: t('profile.roleLens.teacher.title'),
      desc: t('profile.roleLens.teacher.desc'),
      metrics: [
        { label: t('profile.roleLens.metrics.department'), value: user.teacherDepartment || user.collegeName || '-' },
        { label: t('profile.roleLens.metrics.teacherNo'), value: user.teacherNo || '-' },
        { label: t('profile.roleLens.metrics.readiness'), value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete') }
      ],
      highlights: [
        t('profile.roleLens.teacher.highlight1'),
        t('profile.roleLens.teacher.highlight2'),
        t('profile.roleLens.teacher.highlight3')
      ]
    }
  }

  if (primaryRoleCode.value === 'STUDENT') {
    return {
      icon: GraduationCap,
      title: t('profile.roleLens.student.title'),
      desc: t('profile.roleLens.student.desc'),
      metrics: [
        { label: t('profile.roleLens.metrics.className'), value: user.className || '-' },
        { label: t('profile.roleLens.metrics.studentNo'), value: user.studentNo || '-' },
        { label: t('profile.roleLens.metrics.readiness'), value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete') }
      ],
      highlights: [
        t('profile.roleLens.student.highlight1'),
        t('profile.roleLens.student.highlight2'),
        t('profile.roleLens.student.highlight3')
      ]
    }
  }

  return {
    icon: UserRound,
    title: t('profile.pageTitle'),
    desc: t('profile.summaryDesc'),
    metrics: [
      { label: t('profile.roleLens.metrics.identity'), value: primaryRoleLabel.value },
      { label: t('profile.roleLens.metrics.scope'), value: scopeItems.value[0]?.value || '-' },
      { label: t('profile.roleLens.metrics.readiness'), value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete') }
    ],
    highlights: [t('profile.summaryDesc')]
  }
})

const contactCards = computed(() => ([
  {
    label: t('profile.phone'),
    value: user.phone || '-',
    icon: Phone
  },
  {
    label: t('profile.email'),
    value: user.email || '-',
    icon: Mail
  },
  {
    label: t('profile.contactStatus'),
    value: isContactReady.value ? t('profile.contactReady') : t('profile.contactIncomplete'),
    icon: ShieldCheck
  }
]))

const passwordRuleChecks = computed(() => {
  const password = pwdForm.newPassword || ''
  return [
    { key: 'length', label: t('profile.passwordRules.length'), passed: password.length >= 8 },
    { key: 'case', label: t('profile.passwordRules.caseMix'), passed: /[a-z]/.test(password) && /[A-Z]/.test(password) },
    { key: 'number', label: t('profile.passwordRules.number'), passed: /\d/.test(password) },
    { key: 'special', label: t('profile.passwordRules.special'), passed: /[^A-Za-z0-9]/.test(password) }
  ]
})

const passwordStrengthMeta = computed(() => {
  const password = pwdForm.newPassword || ''
  if (!password) {
    return { label: t('profile.passwordStrength.idle'), percent: 8, tone: 'idle' }
  }

  let score = 0
  if (password.length >= 8) score += 1
  if (password.length >= 12) score += 1
  if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score += 1
  if (/\d/.test(password)) score += 1
  if (/[^A-Za-z0-9]/.test(password)) score += 1

  if (score <= 1) {
    return { label: t('profile.passwordStrength.weak'), percent: 28, tone: 'weak' }
  }
  if (score <= 3) {
    return { label: t('profile.passwordStrength.medium'), percent: 62, tone: 'medium' }
  }
  if (score === 4) {
    return { label: t('profile.passwordStrength.strong'), percent: 84, tone: 'strong' }
  }
  return { label: t('profile.passwordStrength.excellent'), percent: 100, tone: 'excellent' }
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
  --profile-panel-border: color-mix(in srgb, var(--panel-border) 82%, transparent);
  --profile-panel-soft: color-mix(in srgb, var(--surface-base) 82%, transparent);
  --profile-panel-elevated: color-mix(in srgb, var(--surface-elevated) 88%, transparent);
  --profile-accent-soft: color-mix(in srgb, var(--accent-500) 12%, transparent);
  --profile-command-glow: color-mix(in srgb, var(--accent-500) 18%, transparent);
  --profile-command-tail: color-mix(in srgb, var(--accent-500) 8%, var(--surface-elevated));
  --profile-role-focus-radial: rgba(28, 113, 168, 0.14);
  --profile-identity-radial: color-mix(in srgb, var(--accent-500) 16%, transparent);
  --profile-status-success: #17715c;
  --profile-focus-badge: color-mix(in srgb, var(--accent-700) 86%, var(--text-primary));
  --profile-shadow: 0 24px 50px color-mix(in srgb, var(--accent-500) 10%, transparent);
}

.profile-page.theme-school-admin {
  --profile-command-glow: rgba(32, 104, 170, 0.22);
  --profile-command-tail: rgba(18, 90, 150, 0.14);
  --profile-role-focus-radial: rgba(21, 96, 164, 0.2);
  --profile-identity-radial: rgba(48, 122, 194, 0.2);
  --profile-status-success: #0f6d8c;
  --profile-focus-badge: #0f5f8d;
}

.profile-page.theme-college-admin {
  --profile-command-glow: rgba(200, 117, 39, 0.22);
  --profile-command-tail: rgba(165, 98, 31, 0.15);
  --profile-role-focus-radial: rgba(176, 92, 23, 0.2);
  --profile-identity-radial: rgba(208, 133, 59, 0.2);
  --profile-status-success: #8a5a18;
  --profile-focus-badge: #9a5f14;
}

.profile-page.theme-teacher {
  --profile-command-glow: rgba(38, 141, 102, 0.22);
  --profile-command-tail: rgba(23, 125, 92, 0.14);
  --profile-role-focus-radial: rgba(24, 122, 89, 0.2);
  --profile-identity-radial: rgba(43, 151, 116, 0.2);
  --profile-status-success: #17715c;
  --profile-focus-badge: #18735f;
}

.profile-page.theme-student {
  --profile-command-glow: rgba(111, 83, 191, 0.2);
  --profile-command-tail: rgba(93, 68, 170, 0.14);
  --profile-role-focus-radial: rgba(96, 72, 176, 0.2);
  --profile-identity-radial: rgba(128, 95, 214, 0.18);
  --profile-status-success: #5b4caf;
  --profile-focus-badge: #6250b7;
}

.profile-command {
  display: grid;
  gap: 16px;
  padding: 18px;
  background:
    radial-gradient(circle at top right, var(--profile-command-glow), transparent 32%),
    linear-gradient(
      140deg,
      color-mix(in srgb, var(--surface-base) 95%, transparent) 0%,
      color-mix(in srgb, var(--surface-elevated) 92%, transparent) 58%,
      var(--profile-command-tail) 100%
    );
}

.profile-command-hero {
  display: grid;
  gap: 16px;
}

.profile-identity-panel,
.profile-role-focus,
.metric-card,
.overview-card,
.identity-status-card,
.role-lens-card,
.role-lens-metric,
.identity-contact-card,
.profile-inline-panel,
.security-strength-card,
.security-rule-item {
  border: 1px solid var(--profile-panel-border);
}

.profile-identity-panel {
  display: flex;
  gap: 18px;
  min-width: 0;
  align-items: center;
  border-radius: 24px;
  padding: 20px;
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 88%, transparent), color-mix(in srgb, var(--surface-elevated) 82%, transparent));
}

.profile-avatar-shell {
  display: flex;
  width: 116px;
  height: 116px;
  flex: 0 0 auto;
  align-items: center;
  justify-content: center;
  border-radius: 30px;
  border: 1px solid color-mix(in srgb, var(--accent-500) 24%, var(--panel-border));
  background:
    radial-gradient(circle at top left, var(--profile-identity-radial), transparent 58%),
    color-mix(in srgb, var(--surface-base) 90%, transparent);
  box-shadow: inset 0 1px 0 color-mix(in srgb, white 40%, transparent);
}

.profile-identity-copy {
  display: grid;
  gap: 12px;
  min-width: 0;
  flex: 1;
}

.profile-kicker,
.profile-role-focus-kicker {
  margin: 0;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.profile-identity-head,
.profile-role-focus-head,
.panel-head,
.security-strength-head,
.identity-status-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.profile-title {
  margin: 0;
  font-size: clamp(30px, 3.4vw, 40px);
  line-height: 1.02;
  color: var(--text-primary);
}

.profile-subtitle,
.profile-role-focus-desc,
.panel-desc,
.avatar-preview-desc,
.avatar-upload-note,
.identity-status-desc {
  margin: 0;
  font-size: 13px;
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
}

.profile-status-pill,
.profile-role-pill,
.profile-scope-pill,
.preference-pill {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  min-height: 34px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--profile-panel-border);
  background: var(--profile-panel-elevated);
  color: var(--text-secondary);
  font-size: 12px;
  font-weight: 600;
}

.profile-status-pill.is-success {
  color: var(--profile-status-success);
}

.profile-status-pill.is-danger {
  color: var(--danger);
}

.profile-role-list,
.profile-scope-list,
.profile-role-focus-tags,
.profile-form-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.profile-role-pill,
.profile-scope-pill {
  min-height: 36px;
  background: var(--profile-panel-soft);
}

.profile-role-pill.is-primary {
  border-color: color-mix(in srgb, var(--accent-500) 30%, transparent);
  background: var(--profile-accent-soft);
  color: var(--accent-700);
}

.profile-scope-pill strong {
  color: var(--text-primary);
}

.profile-role-focus {
  display: grid;
  gap: 16px;
  padding: 20px;
  border-radius: 24px;
  background:
    radial-gradient(circle at top left, var(--profile-role-focus-radial), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 96%, transparent), color-mix(in srgb, var(--surface-elevated) 86%, transparent));
}

.profile-role-badge,
.profile-action-icon,
.metric-card-icon,
.role-lens-badge,
.identity-status-badge,
.identity-contact-icon,
.avatar-preview-avatar {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
}

.profile-role-badge,
.role-lens-badge,
.identity-status-badge {
  width: 46px;
  height: 46px;
  border: 1px solid color-mix(in srgb, var(--accent-500) 22%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  color: var(--profile-focus-badge);
}

.profile-role-focus-body,
.profile-action-copy,
.metric-card-copy,
.role-lens-head,
.identity-contact-copy,
.avatar-preview-copy {
  display: grid;
  gap: 4px;
}

.profile-role-focus-title,
.role-lens-title,
.identity-status-title,
.avatar-preview-title {
  margin: 0;
  color: var(--text-primary);
}

.profile-role-focus-title {
  font-size: 24px;
  line-height: 1.15;
}

.entry-card-tag {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 72%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  font-size: 12px;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
}

.profile-action-grid,
.overview-grid,
.role-lens-metrics,
.identity-contact-grid,
.security-rule-list {
  display: grid;
  gap: 10px;
}

.profile-action-card {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  min-height: 68px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid var(--profile-panel-border);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  text-align: left;
  transition: transform 180ms ease, border-color 180ms ease, box-shadow 180ms ease;
}

.profile-action-card:hover,
.profile-action-card:focus-visible {
  transform: translateY(-2px);
  border-color: color-mix(in srgb, var(--accent-500) 30%, transparent);
  box-shadow: var(--profile-shadow);
}

.profile-action-icon,
.metric-card-icon,
.identity-contact-icon {
  width: 40px;
  height: 40px;
  background: var(--profile-accent-soft);
  color: var(--accent-700);
}

.profile-action-title,
.metric-card-value,
.overview-card-value,
.identity-contact-value,
.security-strength-title {
  color: var(--text-primary);
  font-weight: 700;
}

.profile-action-title {
  font-size: 14px;
}

.profile-action-desc,
.metric-card-label,
.overview-card-label,
.identity-contact-label {
  color: var(--text-secondary);
  font-size: 12px;
}

.profile-action-arrow {
  color: var(--text-secondary);
}

.profile-metric-grid {
  display: grid;
  gap: 12px;
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric-card {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  min-height: 92px;
  padding: 16px;
  border-radius: 20px;
  background: var(--profile-panel-soft);
}

.metric-card-value,
.overview-card-value,
.identity-contact-value {
  margin: 0;
  font-size: 15px;
  line-height: 1.6;
  word-break: break-word;
}

.metric-card-label,
.overview-card-label,
.identity-contact-label {
  margin: 0;
}

.profile-layout {
  display: grid;
  gap: 12px;
}

.profile-column-card {
  height: 100%;
}

.profile-column-body {
  display: grid;
  gap: 12px;
}

.profile-section-anchor {
  border-radius: 22px;
  transition: box-shadow 180ms ease, transform 180ms ease;
}

.profile-section-anchor.is-highlighted {
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--accent-500) 28%, transparent), var(--profile-shadow);
}

.panel-head {
  margin-bottom: 18px;
}

.group-section-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.group-section-title {
  margin: 0;
  font-size: 16px;
  font-weight: 700;
  color: var(--text-primary);
}

.profile-group-panel {
  padding: 18px;
  border-radius: 22px;
  border: 1px solid var(--profile-panel-border);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 94%, transparent), color-mix(in srgb, var(--surface-elevated) 86%, transparent));
}

.profile-group-panel-muted {
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 90%, transparent), color-mix(in srgb, var(--surface-elevated) 82%, transparent));
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

.profile-inline-panel {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  border-radius: 20px;
  padding: 16px;
  background: color-mix(in srgb, var(--surface-base) 78%, transparent);
}

.avatar-preview-panel {
  display: flex;
  min-width: 0;
  align-items: center;
  gap: 14px;
}

.avatar-preview-avatar {
  width: 88px;
  height: 88px;
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
  border: 1px solid var(--profile-panel-border);
}

.avatar-preview-title {
  font-size: 14px;
  font-weight: 700;
}

.avatar-upload-actions {
  display: grid;
  gap: 8px;
  min-width: 220px;
}

.avatar-uploader :deep(.el-upload) {
  display: inline-flex;
}

.profile-password-form {
  margin-top: 18px;
}

.profile-security-tips-inline {
  margin-top: 24px;
}

.security-strength-card {
  display: grid;
  gap: 12px;
  padding: 16px;
  border-radius: 20px;
  background: color-mix(in srgb, var(--surface-base) 78%, transparent);
}

.security-strength-badge {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  padding: 0 12px;
  border-radius: 999px;
  border: 1px solid var(--profile-panel-border);
  background: color-mix(in srgb, var(--surface-elevated) 88%, transparent);
  font-size: 12px;
  font-weight: 700;
}

.security-strength-badge.is-idle {
  color: var(--text-secondary);
}

.security-strength-badge.is-weak {
  color: var(--danger);
}

.security-strength-badge.is-medium {
  color: #a16912;
}

.security-strength-badge.is-strong,
.security-strength-badge.is-excellent {
  color: #17715c;
}

.security-strength-bar {
  height: 8px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--surface-elevated) 92%, transparent);
}

.security-strength-bar > span {
  display: block;
  height: 100%;
  border-radius: inherit;
  transition: width 180ms ease, background-color 180ms ease;
}

.security-strength-bar > span.is-idle {
  background: color-mix(in srgb, var(--panel-border) 72%, transparent);
}

.security-strength-bar > span.is-weak {
  background: linear-gradient(90deg, rgba(214, 74, 74, 0.9), rgba(190, 44, 44, 0.86));
}

.security-strength-bar > span.is-medium {
  background: linear-gradient(90deg, rgba(219, 157, 44, 0.94), rgba(194, 122, 52, 0.9));
}

.security-strength-bar > span.is-strong,
.security-strength-bar > span.is-excellent {
  background: linear-gradient(90deg, rgba(32, 157, 115, 0.92), rgba(27, 120, 136, 0.92));
}

.security-rule-item {
  display: flex;
  align-items: center;
  gap: 10px;
  min-height: 42px;
  padding: 0 14px;
  border-radius: 14px;
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
}

.security-rule-item.is-passed {
  border-color: color-mix(in srgb, var(--accent-500) 28%, transparent);
  background: color-mix(in srgb, var(--accent-500) 12%, transparent);
  color: color-mix(in srgb, var(--accent-700) 84%, var(--text-primary));
}

.overview-card,
.role-lens-metric,
.identity-contact-card {
  padding: 14px;
  border-radius: 16px;
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
}

.overview-card-value {
  margin-top: 6px;
}

.role-lens-card {
  display: grid;
  gap: 16px;
  padding: 18px;
  border-radius: 22px;
  background:
    radial-gradient(circle at top right, var(--profile-identity-radial), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 97%, transparent), color-mix(in srgb, var(--surface-elevated) 89%, transparent));
}

.role-lens-head {
  grid-template-columns: auto minmax(0, 1fr);
  align-items: start;
}

.role-lens-title {
  font-size: 18px;
  font-weight: 700;
}

.role-lens-desc,
.role-lens-focus-kicker,
.role-lens-metric-label {
  margin: 0;
  color: var(--text-secondary);
  font-size: 12px;
}

.role-lens-metric-value {
  margin: 6px 0 0;
  color: var(--text-primary);
  font-size: 15px;
  font-weight: 700;
  line-height: 1.6;
  word-break: break-word;
}

.role-lens-focus {
  display: grid;
  gap: 10px;
  padding: 14px;
  border-radius: 18px;
  border: 1px solid var(--profile-panel-border);
  background: color-mix(in srgb, var(--surface-base) 76%, transparent);
}

.role-lens-checklist {
  display: grid;
  gap: 10px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.role-lens-checklist li {
  position: relative;
  padding-left: 18px;
  color: color-mix(in srgb, var(--text-primary) 78%, var(--text-secondary));
  line-height: 1.75;
}

.role-lens-checklist li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 9px;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--profile-focus-badge);
}

.identity-status-card {
  display: grid;
  gap: 16px;
  padding: 18px;
  border-radius: 22px;
  background:
    radial-gradient(circle at top left, var(--profile-role-focus-radial), transparent 42%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 96%, transparent), color-mix(in srgb, var(--surface-elevated) 88%, transparent));
}

.identity-status-title {
  font-size: 18px;
  font-weight: 700;
}

.identity-contact-card {
  display: flex;
  gap: 12px;
  align-items: flex-start;
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
  line-height: 1.8;
  color: color-mix(in srgb, var(--text-primary) 76%, var(--text-secondary));
}

.profile-security-list li::before {
  content: '';
  position: absolute;
  left: 0;
  top: 10px;
  width: 6px;
  height: 6px;
  border-radius: 999px;
  background: var(--accent-500);
}

@media (min-width: 1280px) {
  .profile-command-hero {
    grid-template-columns: minmax(0, 1.5fr) minmax(340px, 0.9fr);
  }

  .profile-layout {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: start;
  }

  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .profile-security-tips-inline {
    margin-top: 32px;
  }
}

@media (max-width: 1023px) {
  .profile-metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 767px) {
  .profile-command,
  .profile-identity-panel,
  .profile-role-focus,
  .profile-group-panel {
    padding: 16px;
  }

  .profile-identity-head,
  .profile-role-focus-head,
  .group-section-head,
  .panel-head,
  .identity-status-head {
    flex-direction: column;
  }

  .profile-form-grid,
  .profile-metric-grid {
    grid-template-columns: 1fr;
  }

  .profile-field-span-2 {
    grid-column: auto;
  }

  .avatar-upload-actions {
    min-width: 0;
    width: 100%;
  }
}

@media (max-width: 479px) {
  .profile-avatar-shell {
    width: 92px;
    height: 92px;
    border-radius: 24px;
  }

  .profile-identity-panel {
    flex-direction: column;
    align-items: flex-start;
  }

  .profile-form-actions :deep(button),
  .avatar-upload-actions :deep(button) {
    width: 100%;
  }
}
</style>
