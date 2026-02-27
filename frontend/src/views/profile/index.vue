<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :xs="24" :lg="8">
        <el-card>
          <template #header>
            <span>账号信息</span>
          </template>
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
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="16">
        <el-card>
          <template #header>
            <span>个人中心</span>
          </template>
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
                <el-form-item label="头像地址">
                  <el-input v-model="form.avatar" placeholder="https://..." />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" :loading="saving" @click="submit">保存资料</el-button>
                  <el-button @click="resetFromUser">重置</el-button>
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
                  <el-button type="primary" :loading="updatingPassword" @click="submitPassword">更新密码</el-button>
                  <el-button @click="resetPasswordForm">重置</el-button>
                </el-form-item>
              </el-form>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { getUserInfo, updatePassword, updateProfile } from '@/api/user'

const route = useRoute()
const store = useStore()
const formRef = ref()
const saving = ref(false)
const activeTab = ref('profile')
const pwdFormRef = ref()
const updatingPassword = ref(false)
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

<style scoped>
.page-container {
  padding: 20px;
}

.user-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 16px;
}

.user-name {
  margin-top: 10px;
  font-size: 18px;
  font-weight: 600;
}

.user-role {
  margin-top: 4px;
  color: #909399;
}
</style>
