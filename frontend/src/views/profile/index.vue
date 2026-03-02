<template>
  <ProfilePageShell title="个人中心">
    <div class="grid gap-4 xl:grid-cols-[320px,1fr]">
      <AppCard title="账户信息" content-class="p-5">
        <div class="user-info">
          <el-avatar :size="88" :src="user.avatar || defaultAvatar" />
          <div class="user-name">{{ user.realName || user.username }}</div>
          <div class="user-role">{{ user.role || '-' }}</div>
        </div>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="用户名">{{ user.username || '-' }}</el-descriptions-item>
          <el-descriptions-item label="账号状态">
            <el-tag :type="user.status === 1 ? 'success' : 'danger'">{{ user.status === 1 ? '启用' : '禁用' }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ user.createTime || '-' }}</el-descriptions-item>
        </el-descriptions>
      </AppCard>

      <AppCard title="资料与安全" content-class="p-5">
        <el-tabs v-model="activeTab">
          <el-tab-pane label="个人资料" name="profile">
            <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
              <el-form-item label="真实姓名" prop="realName">
                <el-input v-model="form.realName" />
              </el-form-item>
              <el-form-item label="手机号">
                <el-input v-model="form.phone" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="form.email" />
              </el-form-item>
              <el-form-item label="头像">
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
                      <AppButton variant="secondary" :loading="uploadingAvatar">上传头像</AppButton>
                    </el-upload>
                    <div class="avatar-tip">支持 jpg/png/gif/webp，大小不超过 5MB</div>
                  </div>
                </div>
              </el-form-item>
              <el-form-item label="头像地址">
                <el-input v-model="form.avatar" placeholder="上传后会自动填充，也可手动输入 URL" />
              </el-form-item>
              <el-form-item>
                <AppButton :loading="saving" @click="submit">保存资料</AppButton>
                <AppButton variant="secondary" class="ml-2" @click="resetFromUser">重置</AppButton>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="修改密码" name="password">
            <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input v-model="pwdForm.oldPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="新密码" prop="newPassword">
                <el-input v-model="pwdForm.newPassword" type="password" show-password />
              </el-form-item>
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input v-model="pwdForm.confirmPassword" type="password" show-password />
              </el-form-item>
              <el-form-item>
                <AppButton :loading="updatingPassword" @click="submitPassword">更新密码</AppButton>
                <AppButton variant="secondary" class="ml-2" @click="resetPasswordForm">重置</AppButton>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
      </AppCard>
    </div>

    <template #aside>
      <ul class="space-y-2">
        <li>建议每 90 天更新一次密码，避免重复使用旧密码。</li>
        <li>头像建议使用清晰人像，便于班级与教学协作识别。</li>
        <li>手机号和邮箱建议保持可用，用于接收系统通知。</li>
      </ul>
    </template>
  </ProfilePageShell>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import ProfilePageShell from '@/components/shell/ProfilePageShell.vue'
import AppCard from '@/components/ui/AppCard.vue'
import AppButton from '@/components/ui/AppButton.vue'
import { getUserInfo, updatePassword, updateProfile, uploadAvatar } from '@/api/user'

const route = useRoute()
const store = useStore()
const formRef = ref()
const saving = ref(false)
const activeTab = ref('profile')
const pwdFormRef = ref()
const updatingPassword = ref(false)
const uploadingAvatar = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const user = reactive({
  id: null,
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

const rules = {
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }]
}

const pwdForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== pwdForm.newPassword) {
          callback(new Error('两次密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

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
    ElMessage.success('个人资料已更新')
  } finally {
    saving.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = !!file.type && file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过 5MB')
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
    ElMessage.success('头像上传成功')
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
    ElMessage.success('密码已更新')
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
  color: #17384a;
}

.user-role {
  margin-top: 4px;
  color: #68818d;
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
  color: #68818d;
}
</style>
