<template>
  <div class="login-container">
    <div class="grid-overlay"></div>
    <div class="scanline"></div>
    <div class="login-shell">
      <section class="intro-panel">
        <div class="intro-badge">
          <span class="dot"></span>
          EDU CORE ONLINE
        </div>
        <h1>智能校园管控中枢</h1>
        <p>Unified Student Intelligence Platform</p>
        <div class="intro-data">
          <div class="data-item">
            <span>节点状态</span>
            <strong>{{ runtimeMetrics.nodeStatus }}</strong>
          </div>
          <div class="data-item">
            <span>并发网关</span>
            <strong>{{ runtimeMetrics.gatewayConcurrency }}</strong>
          </div>
          <div class="data-item">
            <span>同步延迟</span>
            <strong>{{ runtimeMetrics.syncDelayMs }}</strong>
          </div>
        </div>
      </section>

      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="login-header">
            <el-icon :size="40" class="logo-icon"><School /></el-icon>
            <h2>学生管理系统</h2>
            <p>Student Management System</p>
          </div>
        </template>

        <el-tabs v-model="activeTab" stretch>
          <el-tab-pane label="登录" name="login">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-position="top">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="loginForm.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="loginForm.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                  show-password
                  @keyup.enter="handleLogin"
                />
              </el-form-item>
              <el-form-item>
                <el-button
                  type="primary"
                  size="large"
                  style="width: 100%"
                  :loading="loading"
                  @click="handleLogin"
                >
                  登录
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>

          <el-tab-pane label="注册" name="register">
            <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-position="top">
              <el-form-item label="用户名" prop="username">
                <el-input
                  v-model="registerForm.username"
                  placeholder="请输入用户名"
                  :prefix-icon="User"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input
                  v-model="registerForm.password"
                  type="password"
                  placeholder="请输入密码"
                  :prefix-icon="Lock"
                  size="large"
                  show-password
                />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input
                  v-model="registerForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入密码"
                  :prefix-icon="Lock"
                  size="large"
                  show-password
                />
              </el-form-item>
              <el-form-item label="真实姓名" prop="realName">
                <el-input
                  v-model="registerForm.realName"
                  placeholder="请输入真实姓名"
                  :prefix-icon="UserFilled"
                  size="large"
                />
              </el-form-item>
              <el-form-item label="角色" prop="role">
                <el-select v-model="registerForm.role" placeholder="请选择角色" size="large" style="width: 100%">
                  <el-option label="学生" value="STUDENT" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button
                  type="success"
                  size="large"
                  style="width: 100%"
                  :loading="loading"
                  @click="handleRegister"
                >
                  注册
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>

        <div class="login-footer">
          <p>测试账号：</p>
          <p>管理员：demo_admin / 123456</p>
          <p>学生：student001 / 123456</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, UserFilled } from '@element-plus/icons-vue'
import { getRuntimeMetrics, register } from '@/api/user'

const store = useStore()
const router = useRouter()
const activeTab = ref('login')
const loading = ref(false)
const loginFormRef = ref(null)
const registerFormRef = ref(null)

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: 'STUDENT'
})

let runtimeMetricsTimer = null

const runtimeMetrics = reactive({
  nodeStatus: '--',
  gatewayConcurrency: '--',
  syncDelayMs: '--'
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await store.dispatch('login', loginForm)
        ElMessage.success('登录成功')
        router.push('/dashboard')
      } catch (error) {
        ElMessage.error(error.message || '登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await register({
          username: registerForm.username,
          password: registerForm.password,
          realName: registerForm.realName,
          role: registerForm.role
        })
        ElMessage.success('注册成功，请登录')
        activeTab.value = 'login'
      } catch (error) {
        ElMessage.error(error.message || '注册失败')
      } finally {
        loading.value = false
      }
    }
  })
}

const fetchRuntimeMetrics = async () => {
  try {
    const res = await getRuntimeMetrics()
    const data = res.data || {}
    runtimeMetrics.nodeStatus = data.nodeStatus || '--'
    runtimeMetrics.gatewayConcurrency = data.gatewayConcurrency ?? '--'
    runtimeMetrics.syncDelayMs = data.syncDelayMs != null ? `${data.syncDelayMs}ms` : '--'
  } catch (_e) {
    runtimeMetrics.nodeStatus = 'Degraded'
    runtimeMetrics.gatewayConcurrency = '--'
    runtimeMetrics.syncDelayMs = '--'
  }
}

onMounted(() => {
  fetchRuntimeMetrics()
  runtimeMetricsTimer = window.setInterval(fetchRuntimeMetrics, 10000)
})

onBeforeUnmount(() => {
  if (runtimeMetricsTimer) {
    window.clearInterval(runtimeMetricsTimer)
    runtimeMetricsTimer = null
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 24px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 8% 5%;
    background: radial-gradient(circle at 50% 50%, rgba(181, 226, 222, 0.26), transparent 70%);
    pointer-events: none;
  }
}

.grid-overlay {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(18, 82, 97, 0.07) 1px, transparent 1px),
    linear-gradient(90deg, rgba(18, 82, 97, 0.07) 1px, transparent 1px);
  background-size: 56px 56px;
  mask-image: radial-gradient(circle at 50% 44%, rgba(0, 0, 0, 0.66), transparent 82%);
  opacity: 0.48;
  pointer-events: none;
}

.scanline {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0), rgba(192, 223, 220, 0.2), rgba(255, 255, 255, 0));
  opacity: 0.32;
  transform: translateY(-100%);
  animation: scanMove 9s linear infinite;
}

.login-shell {
  width: min(1120px, 100%);
  display: grid;
  grid-template-columns: 1.05fr 0.95fr;
  gap: 26px;
  z-index: 2;
  align-items: stretch;
}

.intro-panel {
  position: relative;
  border-radius: 24px;
  padding: 34px 32px;
  border: 1px solid rgba(18, 74, 94, 0.22);
  background: linear-gradient(158deg, rgba(245, 252, 251, 0.94), rgba(234, 245, 244, 0.88));
  box-shadow: 0 20px 36px rgba(21, 61, 72, 0.14);
}

.intro-panel::before,
.intro-panel::after {
  content: '';
  position: absolute;
  width: 62px;
  height: 62px;
  border: 2px solid rgba(21, 106, 112, 0.36);
}

.intro-panel::before {
  top: 14px;
  left: 14px;
  border-right: none;
  border-bottom: none;
}

.intro-panel::after {
  right: 14px;
  bottom: 14px;
  border-left: none;
  border-top: none;
}

.intro-badge {
  width: fit-content;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.16em;
  color: rgba(28, 77, 87, 0.9);
  border: 1px solid rgba(19, 95, 103, 0.24);
  border-radius: 999px;
  padding: 8px 14px;
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.76);
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #137b76;
  box-shadow: 0 0 0 4px rgba(19, 123, 118, 0.2);
}

.intro-panel h1 {
  margin: 0;
  color: #123042;
  font-size: clamp(30px, 4vw, 44px);
  line-height: 1.15;
  letter-spacing: 0.02em;
  font-family: var(--sms-font-display, 'IBM Plex Serif', serif);
}

.intro-panel > p {
  margin: 12px 0 24px;
  color: rgba(46, 89, 101, 0.74);
  letter-spacing: 0.08em;
  font-size: 13px;
  text-transform: uppercase;
}

.intro-data {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.data-item {
  padding: 16px 14px;
  border-radius: 14px;
  border: 1px solid rgba(22, 98, 106, 0.22);
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 8px 14px rgba(22, 65, 80, 0.08);
}

.data-item span {
  color: #5a6e7a;
  font-size: 12px;
}

.data-item strong {
  display: block;
  margin-top: 6px;
  font-size: 20px;
  color: #173548;
}

.login-card {
  width: min(460px, 100%);
  border-radius: 22px;
  border: 1px solid rgba(18, 75, 92, 0.2);
  box-shadow: 0 22px 40px rgba(18, 53, 66, 0.16);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.94) 0%, rgba(240, 249, 248, 0.9) 100%);
  position: relative;
  z-index: 1;
  justify-self: end;

  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(20, 87, 95, 0.16);
  }

  :deep(.el-card__body) {
    color: #173648;
  }

  :deep(.el-form-item__label) {
    color: #305361;
    font-weight: 600;
  }

  :deep(.el-tabs__item) {
    color: rgba(70, 100, 112, 0.88);
  }

  :deep(.el-tabs__item.is-active) {
    color: #137b76;
    font-weight: 700;
  }

  :deep(.el-tabs__active-bar) {
    background-color: #137b76;
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    background: rgba(255, 255, 255, 0.84);
    border: 1px solid rgba(34, 97, 110, 0.22);
    box-shadow: none;
    transition: border-color 0.25s ease, box-shadow 0.25s ease, background-color 0.25s ease;
  }

  :deep(.el-input__wrapper:hover),
  :deep(.el-select__wrapper:hover) {
    border-color: rgba(20, 107, 113, 0.32);
    background: #ffffff;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    border-color: rgba(19, 123, 118, 0.55);
    box-shadow: 0 0 0 3px rgba(19, 123, 118, 0.14);
    background: #ffffff;
  }

  :deep(.el-input__inner),
  :deep(.el-select__placeholder),
  :deep(.el-input__icon),
  :deep(.el-select__icon) {
    color: #204354;
  }

  :deep(.el-input__inner::placeholder) {
    color: #7c919a;
  }

  :deep(.el-button--primary) {
    border: none;
    background: linear-gradient(90deg, #116f6a 0%, #1b8e88 100%);
  }

  :deep(.el-button--success) {
    border: none;
    background: linear-gradient(90deg, #2f7e52 0%, #3e9967 100%);
  }

  .login-header {
    text-align: center;

    .logo-icon {
      color: #137b76;
      margin-bottom: 10px;
      filter: drop-shadow(0 8px 16px rgba(19, 123, 118, 0.2));
    }

    h2 {
      margin: 0;
      font-size: 24px;
      color: #133547;
      letter-spacing: 0.5px;
      font-family: var(--sms-font-display, 'IBM Plex Serif', serif);
    }

    p {
      margin: 5px 0 0;
      font-size: 14px;
      color: rgba(58, 96, 109, 0.82);
    }
  }

  .login-footer {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid rgba(20, 87, 95, 0.16);
    text-align: center;
    font-size: 12px;
    color: rgba(60, 95, 104, 0.72);

    p {
      margin: 5px 0;
    }
  }
}

@keyframes scanMove {
  0% {
    transform: translateY(-100%);
  }
  100% {
    transform: translateY(100%);
  }
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
    max-width: 520px;
  }

  .intro-panel {
    display: none;
  }

  .login-card {
    justify-self: center;
    width: 100%;
  }
}

@media (max-width: 640px) {
  .login-container {
    padding: 14px;
  }

  .login-card {
    border-radius: 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .scanline {
    animation: none;
  }
}
</style>
