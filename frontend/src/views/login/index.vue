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

let transparentStyleTimer = null
let runtimeMetricsTimer = null

const runtimeMetrics = reactive({
  nodeStatus: '--',
  gatewayConcurrency: '--',
  syncDelayMs: '--'
})

const forceTransparentInputs = () => {
  const root = document.querySelector('.login-container')
  if (!root) return

  root.querySelectorAll('.el-input__wrapper, .el-select__wrapper').forEach((node) => {
    node.style.setProperty('background', 'transparent', 'important')
    node.style.setProperty('background-color', 'transparent', 'important')
    node.style.setProperty('box-shadow', 'none', 'important')
    node.style.setProperty('outline', 'none', 'important')
  })

  root.querySelectorAll('input').forEach((node) => {
    node.style.setProperty('background', 'transparent', 'important')
    node.style.setProperty('background-color', 'transparent', 'important')
    node.style.setProperty('background-image', 'none', 'important')
    node.style.setProperty('box-shadow', 'none', 'important')
    node.style.setProperty('-webkit-box-shadow', '0 0 0 1000px rgba(0, 0, 0, 0) inset', 'important')
    node.style.setProperty('-webkit-background-clip', 'text', 'important')
    node.style.setProperty('background-clip', 'text', 'important')
    node.style.setProperty('-webkit-text-fill-color', '#efe8ff', 'important')
    node.style.setProperty('caret-color', '#efe8ff', 'important')
    node.style.setProperty('border', 'none', 'important')
    node.style.setProperty('outline', 'none', 'important')
    node.style.setProperty('appearance', 'none', 'important')
    node.style.setProperty('-webkit-appearance', 'none', 'important')
  })
}

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
  forceTransparentInputs()
  transparentStyleTimer = window.setInterval(forceTransparentInputs, 350)
})

onBeforeUnmount(() => {
  if (runtimeMetricsTimer) {
    window.clearInterval(runtimeMetricsTimer)
    runtimeMetricsTimer = null
  }
  if (transparentStyleTimer) {
    window.clearInterval(transparentStyleTimer)
    transparentStyleTimer = null
  }
})
</script>

<style scoped lang="scss">
.login-container {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    inset: 5% 6%;
    background:
      radial-gradient(circle at 8% 14%, rgba(154, 130, 255, 0.24), transparent 36%),
      radial-gradient(circle at 88% 84%, rgba(198, 167, 255, 0.22), transparent 42%);
    filter: blur(8px);
    pointer-events: none;
    animation: breatheGlow 10s ease-in-out infinite alternate;
  }
}

.grid-overlay {
  position: absolute;
  inset: -30% -10% -20%;
  background-image:
    linear-gradient(rgba(176, 156, 255, 0.12) 1px, transparent 1px),
    linear-gradient(90deg, rgba(176, 156, 255, 0.12) 1px, transparent 1px);
  background-size: 38px 38px;
  transform: perspective(900px) rotateX(72deg);
  transform-origin: top center;
  opacity: 0.35;
  pointer-events: none;
}

.scanline {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background: linear-gradient(to bottom, transparent 0%, rgba(193, 164, 255, 0.14) 52%, transparent 100%);
  mix-blend-mode: screen;
  animation: scanMove 8s linear infinite;
  opacity: 0.35;
}

.login-shell {
  width: min(1080px, 100%);
  display: grid;
  grid-template-columns: 1.1fr 0.9fr;
  gap: 24px;
  z-index: 2;
  align-items: stretch;
}

.intro-panel {
  position: relative;
  border-radius: 24px;
  padding: 34px 32px;
  border: 1px solid rgba(174, 156, 255, 0.28);
  background: linear-gradient(170deg, rgba(31, 24, 58, 0.8), rgba(27, 20, 50, 0.62));
  backdrop-filter: blur(8px);
  box-shadow: inset 0 0 0 1px rgba(220, 209, 255, 0.08), 0 24px 42px rgba(26, 17, 50, 0.4);
}

.intro-panel::before,
.intro-panel::after {
  content: '';
  position: absolute;
  width: 54px;
  height: 54px;
  border: 2px solid rgba(191, 171, 255, 0.75);
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
  color: rgba(221, 211, 255, 0.9);
  border: 1px solid rgba(191, 171, 255, 0.4);
  border-radius: 999px;
  padding: 8px 14px;
  margin-bottom: 16px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #b594ff;
  box-shadow: 0 0 14px rgba(181, 148, 255, 0.8);
}

.intro-panel h1 {
  margin: 0;
  color: #f7f2ff;
  font-size: clamp(28px, 4vw, 42px);
  line-height: 1.15;
  letter-spacing: 0.04em;
  text-shadow: 0 0 22px rgba(178, 150, 255, 0.26);
  font-family: 'Bahnschrift', 'Segoe UI Variable', 'Avenir Next', sans-serif;
}

.intro-panel > p {
  margin: 12px 0 24px;
  color: rgba(214, 202, 245, 0.78);
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
  border: 1px solid rgba(188, 171, 255, 0.24);
  background: rgba(57, 43, 93, 0.46);
}

.data-item span {
  color: rgba(216, 203, 247, 0.76);
  font-size: 12px;
}

.data-item strong {
  display: block;
  margin-top: 6px;
  font-size: 20px;
  color: #ffffff;
}

.login-card {
  width: min(460px, 100%);
  border-radius: 20px;
  border: 1px solid rgba(176, 160, 255, 0.28);
  box-shadow: 0 26px 52px rgba(31, 20, 58, 0.44);
  backdrop-filter: blur(16px);
  background: linear-gradient(160deg, rgba(35, 26, 66, 0.84) 0%, rgba(41, 30, 74, 0.78) 100%);
  position: relative;
  z-index: 1;
  justify-self: end;
  --el-fill-color-blank: transparent;
  --el-fill-color-light: transparent;
  --el-bg-color: transparent;
  --el-input-bg-color: transparent;
  --el-input-focus-border-color: transparent;

  :deep(.el-card__header) {
    border-bottom: 1px solid rgba(181, 163, 255, 0.24);
  }

  :deep(.el-card__body) {
    color: #efe9ff;
  }

  :deep(.el-form-item__label) {
    color: #e7dcff;
  }

  :deep(.el-tabs__item) {
    color: rgba(216, 203, 247, 0.78);
  }

  :deep(.el-tabs__item.is-active) {
    color: #c4adff;
    font-weight: 700;
  }

  :deep(.el-tabs__active-bar) {
    background-color: #b79aff;
    box-shadow: 0 0 16px rgba(183, 154, 255, 0.45);
  }

  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input),
  :deep(.el-select) {
    --el-fill-color-blank: transparent !important;
    --el-fill-color-light: transparent !important;
    --el-bg-color: transparent !important;
    --el-input-bg-color: transparent !important;
    --el-input-focus-border-color: transparent !important;
    background: transparent !important;
    background-color: transparent !important;
  }

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    background: transparent !important;
    background-color: transparent !important;
    border: 1px solid rgba(206, 188, 255, 0.36);
    backdrop-filter: blur(12px) saturate(130%);
    box-shadow: none !important;
    outline: none !important;
    transition: border-color 0.25s ease, box-shadow 0.25s ease, background-color 0.25s ease;
  }

  :deep(.el-input__wrapper:hover),
  :deep(.el-select__wrapper:hover) {
    border-color: rgba(206, 188, 255, 0.36);
    background: transparent !important;
    background-color: transparent !important;
    box-shadow: none !important;
    outline: none !important;
  }

  :deep(.el-input__wrapper.is-focus),
  :deep(.el-select__wrapper.is-focused) {
    border-color: transparent !important;
    background: transparent !important;
    background-color: transparent !important;
    box-shadow: none !important;
    outline: none !important;
  }

  :deep(.el-input__inner),
  :deep(.el-select__placeholder),
  :deep(.el-input__icon),
  :deep(.el-select__icon) {
    background: transparent !important;
    background-color: transparent !important;
    box-shadow: none !important;
    outline: none !important;
    color: #efe8ff !important;
    -webkit-text-fill-color: #efe8ff;
    text-shadow: 0 0 10px rgba(195, 171, 255, 0.25);
  }

  :deep(.el-input__inner::placeholder) {
    color: rgba(234, 225, 255, 0.65);
  }

  :deep(input),
  :deep(input:hover),
  :deep(input:focus),
  :deep(input:active) {
    background: transparent !important;
    background-color: transparent !important;
    border: none !important;
    box-shadow: none !important;
    outline: none !important;
  }

  :deep(.el-input__inner::selection) {
    background: transparent;
    color: #efe8ff;
  }

  :deep(.el-input__inner::-moz-selection) {
    background: transparent;
    color: #efe8ff;
  }

  :deep(.el-input__inner:focus),
  :deep(.el-input__inner:active) {
    background: transparent !important;
    background-color: transparent !important;
    box-shadow: none !important;
    outline: none !important;
  }

  :deep(input:-webkit-autofill),
  :deep(input:-webkit-autofill:hover),
  :deep(input:-webkit-autofill:focus),
  :deep(input:-webkit-autofill:active),
  :deep(.el-input__inner:-webkit-autofill),
  :deep(.el-input__inner:-webkit-autofill:hover),
  :deep(.el-input__inner:-webkit-autofill:focus),
  :deep(.el-input__inner:-webkit-autofill:active) {
    -webkit-text-fill-color: #efe8ff !important;
    caret-color: #efe8ff;
    border-radius: 10px;
    -webkit-box-shadow: 0 0 0 1000px transparent inset !important;
    box-shadow: 0 0 0 1000px transparent inset !important;
    -webkit-background-clip: padding-box !important;
    background-clip: padding-box !important;
    transition: background-color 9999s ease-in-out 0s;
  }

  :deep(.el-input__inner:-moz-autofill) {
    color: #efe8ff !important;
    caret-color: #efe8ff;
    box-shadow: 0 0 0 1000px transparent inset !important;
  }

  :deep(.el-button--primary) {
    border: none;
    background: linear-gradient(90deg, #7f68f8 0%, #a283ff 100%);
  }

  :deep(.el-button--success) {
    border: none;
    background: linear-gradient(90deg, #8d70fb 0%, #b294ff 100%);
  }

  .login-header {
    text-align: center;

    .logo-icon {
      color: #c2abff;
      margin-bottom: 10px;
      filter: drop-shadow(0 0 12px rgba(194, 171, 255, 0.4));
    }

    h2 {
      margin: 0;
      font-size: 24px;
      color: #fbf8ff;
      letter-spacing: 1px;
    }

    p {
      margin: 5px 0 0;
      font-size: 14px;
      color: rgba(223, 211, 251, 0.78);
    }
  }

  .login-footer {
    margin-top: 20px;
    padding-top: 20px;
    border-top: 1px solid rgba(173, 156, 255, 0.2);
    text-align: center;
    font-size: 12px;
    color: rgba(223, 211, 251, 0.72);

    p {
      margin: 5px 0;
    }
  }
}

@keyframes breatheGlow {
  0%,
  100% {
    opacity: 0.65;
    transform: scale(1);
  }
  50% {
    opacity: 0.95;
    transform: scale(1.04);
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
</style>
