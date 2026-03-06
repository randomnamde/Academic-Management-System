<template>
  <ProfilePageShell :title="t('profile.pageTitle')">
    <div class="grid gap-4 xl:grid-cols-[320px,1fr]">
      <AppCard :title="t('profile.accountInfo')" content-class="p-5">
        <div class="user-info">
          <el-avatar :size="88" :src="user.avatar || defaultAvatar" />
          <div class="user-name">{{ user.realName || user.account || user.username }}</div>
          <div class="user-role">{{ roleLabel }}</div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item :label="t('profile.username')">{{ user.account || user.username || '-' }}</el-descriptions-item>
          <el-descriptions-item :label="t('profile.accountStatus')">
            <el-tag :type="user.status === 1 ? 'success' : 'danger'">{{ user.status === 1 ? t('profile.enabled') : t('profile.disabled') }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item :label="t('profile.createdAt')">{{ user.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </AppCard>

      <AppCard :title="t('profile.infoAndSecurity')" content-class="p-5">
        <el-tabs v-model="activeTab">
          <el-tab-pane :label="t('profile.tabs.profile')" name="profile">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
              <el-form-item :label="t('profile.realName')" prop="realName">
                <el-input v-model="form.realName" />
              </el-form-item>
              <el-form-item :label="t('profile.phone')">
                <el-input v-model="form.phone" />
              </el-form-item>
              <el-form-item :label="t('profile.email')">
                <el-input v-model="form.email" />
              </el-form-item>
              <el-form-item :label="t('profile.avatar')">
                <div class="avatar-upload-row">
                  <el-avatar :size="64" :src="form.avatar || defaultAvatar" />
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
                    <div class="avatar-tip">{{ t('profile.avatarTip') }}</div>
                  </div>
                </div>
              </el-form-item>
              <el-form-item :label="t('profile.avatarUrl')">
                <el-input v-model="form.avatar" :placeholder="t('profile.avatarUrlPlaceholder')" />
              </el-form-item>
              <el-form-item>
                <AppButton :loading="saving" @click="submit">{{ t('profile.saveProfile') }}</AppButton>
                <AppButton variant="secondary" class="ml-2" @click="resetFromUser">{{ t('common.reset') }}</AppButton>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane :label="t('profile.tabs.password')" name="password">
            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
              <el-form-item :label="t('profile.oldPassword')" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item :label="t('profile.newPassword')" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item :label="t('profile.confirmPassword')" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <AppButton :loading="updatingPassword" @click="submitPassword">{{ t('profile.updatePassword') }}</AppButton>
                <AppButton variant="secondary" class="ml-2" @click="resetPasswordForm">{{ t('common.reset') }}</AppButton>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </AppCard>
    </div>

    <template #aside>
      <ul class="space-y-2">
        <li>{{ t('profile.securityTip1') }}</li>
        <li>{{ t('profile.securityTip2') }}</li>
        <li>{{ t('profile.securityTip3') }}</li>
      </ul>
    </template>
  </ProfilePageShell>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'
import ProfilePageShell from '@/components/shell/ProfilePageShell.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { getUserInfo, updatePassword, updateProfile, uploadAvatar } from '@/api/user'

const route = useRoute()
const store = useStore()
const { t } = useI18n()
const formRef = ref()
const saving = ref(false)
const activeTab = ref('profile')
const pwdFormRef = ref()
const updatingPassword = ref(false)
const uploadingAvatar = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const user = reactive({
  id: null,
  account: '',
  username: '',
  realName: '',
  role: '',
  avatar: '',
  phone: '',
  email: '',
  status: 1,
  createTime: ''
})

const form = reactive({
  realName: '',
  avatar: '',
  phone: '',
  email: ''
})

const rules = computed(() => ({
  realName: [{ required: true, message: t('profile.realNameRequired'), trigger: 'blur' }]
}))

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

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

const roleLabel = computed(() => {
  if (user.role === 'SCHOOL_ADMIN') return t('roles.schoolAdmin')
  if (user.role === 'COLLEGE_ADMIN') return t('roles.collegeAdmin')
  if (user.role === 'HOMEROOM_TEACHER') return t('roles.homeroomTeacher')
  if (user.role === 'COURSE_TEACHER') return t('roles.courseTeacher')
  if (user.role === 'STUDENT') return t('roles.student')
  return user.role || '-'
})

function resetFromUser() {
  form.realName = user.realName || ''
  form.avatar = user.avatar || ''
  form.phone = user.phone || ''
  form.email = user.email || ''
}

async function fetchUser() {
  const res = await getUserInfo()
  Object.assign(user, res.data || {})
  resetFromUser()
  store.commit('SET_USER_INFO', res.data || {})
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

function resetPasswordForm() {
  pwdForm.oldPassword = ''
  pwdForm.newPassword = ''
  pwdForm.confirmPassword = ''
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

onMounted(fetchUser)

watch(
  () => route.query.tab,
  (tab) => {
    activeTab.value = tab === 'password' ? 'password' : 'profile'
  },
  { immediate: true }
)
</script>

<style scoped lang="scss">
.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
}

.user-name {
  margin-top: 10px;
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.user-role {
  margin-top: 4px;
  color: var(--text-secondary);
}

.avatar-upload-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar-upload-actions {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.avatar-uploader :deep(.el-upload) {
  display: inline-flex;
}

.avatar-tip {
  font-size: 12px;
  color: var(--text-secondary);
}
</style>
