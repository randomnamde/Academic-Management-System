<template>
  <div class="login-page min-h-screen px-4 py-8 md:px-6 md:py-10">
    <div class="mx-auto grid w-full max-w-6xl gap-4 lg:grid-cols-[1.06fr,0.94fr]">
      <section class="brand-panel app-surface-glass rounded-lg border p-6 md:p-8">
        <p class="brand-kicker">教育运营中枢</p>
        <h1 class="brand-title mt-2">学生管理系统</h1>
        <p class="brand-desc mt-3">
          以数据为核心，统一处理学生、课程、成绩、考勤、请假与权限流程，提升教学协同效率与决策质量。
        </p>

        <div class="mt-6 grid gap-3 sm:grid-cols-3">
          <article class="value-card">
            <p class="value-title">全链路可视</p>
            <p class="value-desc">从课堂到成绩，关键节点可追踪可回溯。</p>
          </article>
          <article class="value-card">
            <p class="value-title">角色协同</p>
            <p class="value-desc">管理员、教师、学生按权限高效协作。</p>
          </article>
          <article class="value-card">
            <p class="value-title">风险预警</p>
            <p class="value-desc">低分与异常考勤自动聚合，减少遗漏。</p>
          </article>
        </div>

        <div class="metric-strip mt-6 grid gap-2 sm:grid-cols-3">
          <article class="metric-item">
            <p>节点状态</p>
            <strong>{{ runtimeMetrics.nodeStatus }}</strong>
          </article>
          <article class="metric-item">
            <p>并发网关</p>
            <strong>{{ runtimeMetrics.gatewayConcurrency }}</strong>
          </article>
          <article class="metric-item">
            <p>同步延迟</p>
            <strong>{{ runtimeMetrics.syncDelayMs }}</strong>
          </article>
        </div>
      </section>

      <section class="auth-panel app-surface-glass rounded-lg border p-5 md:p-6">
        <header class="mb-4 text-center">
          <div class="auth-mark mx-auto mb-2 inline-flex h-11 w-11 items-center justify-center rounded-md">
            <School class="h-5 w-5" />
          </div>
          <h2 class="text-[22px] font-semibold tracking-tight text-primary-900">身份验证</h2>
          <p class="mt-1 text-[13px] text-slatex-500">欢迎回来，请登录你的账户</p>
        </header>

        <AppTabs v-model="activeTab" :items="tabs" id-prefix="auth" aria-label="登录注册切换" />

        <form
          v-if="activeTab === 'login'"
          id="auth-panel-login"
          role="tabpanel"
          aria-labelledby="auth-tab-login"
          class="mt-4 space-y-3"
          @submit.prevent="handleLogin"
        >
          <div>
            <label for="login-username" class="mb-1 block text-[13px] font-medium text-slatex-700">用户名</label>
            <AppInput id="login-username" v-model="loginForm.username" name="username" autocomplete="username" placeholder="请输入用户名" />
          </div>
          <div>
            <label for="login-password" class="mb-1 block text-[13px] font-medium text-slatex-700">密码</label>
            <AppInput id="login-password" v-model="loginForm.password" name="password" type="password" autocomplete="current-password" placeholder="请输入密码" />
          </div>
          <AppButton tone="accent" block :loading="loading" native-type="submit">登录系统</AppButton>
        </form>

        <form
          v-else
          id="auth-panel-register"
          role="tabpanel"
          aria-labelledby="auth-tab-register"
          class="mt-4 space-y-3"
          @submit.prevent="handleRegister"
        >
          <div>
            <label for="register-username" class="mb-1 block text-[13px] font-medium text-slatex-700">用户名</label>
            <AppInput
              id="register-username"
              v-model="registerForm.username"
              name="registerUsername"
              autocomplete="username"
              placeholder="请输入用户名"
            />
          </div>
          <div>
            <label for="register-real-name" class="mb-1 block text-[13px] font-medium text-slatex-700">真实姓名</label>
            <AppInput id="register-real-name" v-model="registerForm.realName" name="realName" autocomplete="name" placeholder="请输入真实姓名" />
          </div>
          <div>
            <label for="register-password" class="mb-1 block text-[13px] font-medium text-slatex-700">密码</label>
            <AppInput
              id="register-password"
              v-model="registerForm.password"
              name="registerPassword"
              type="password"
              autocomplete="new-password"
              placeholder="请输入密码"
            />
          </div>
          <div>
            <label for="register-confirm-password" class="mb-1 block text-[13px] font-medium text-slatex-700">确认密码</label>
            <AppInput
              id="register-confirm-password"
              v-model="registerForm.confirmPassword"
              name="confirmPassword"
              type="password"
              autocomplete="new-password"
              placeholder="请再次输入密码"
            />
          </div>
          <div>
            <label for="register-role" class="mb-1 block text-[13px] font-medium text-slatex-700">角色</label>
            <AppSelect id="register-role" v-model="registerForm.role" name="role" :options="[{ label: '学生', value: 'STUDENT' }]" />
          </div>
          <AppButton tone="neutral" variant="primary" block :loading="loading" native-type="submit">完成注册</AppButton>
        </form>

        <footer class="mt-5 border-t border-neutralx-200 pt-3 text-center text-[11px] text-slatex-500">
          <p>测试账号</p>
          <p>管理员：demo_admin / 123456</p>
          <p>学生：student001 / 123456</p>
        </footer>
      </section>
    </div>
  </div>
</template>
<script setup>
import { reactive, ref, onMounted, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { School } from 'lucide-vue-next'
import AppButton from '@/components/ui/AppButton.vue'
import AppInput from '@/components/ui/AppInput.vue'
import AppSelect from '@/components/ui/AppSelect.vue'
import AppTabs from '@/components/ui/AppTabs.vue'
import { getRuntimeMetrics, register } from '@/api/user'

const store = useStore()
const router = useRouter()

const tabs = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' }
]

const activeTab = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  realName: '',
  role: 'STUDENT'
})

const runtimeMetrics = reactive({
  nodeStatus: '--',
  gatewayConcurrency: '--',
  syncDelayMs: '--'
})

let runtimeMetricsTimer = null

function formatNodeStatus(value) {
  const key = String(value || '').trim().toUpperCase()
  const map = {
    ONLINE: '在线',
    OFFLINE: '离线',
    DEGRADED: '降级',
    HEALTHY: '正常',
    UNHEALTHY: '异常'
  }
  return map[key] || (value ? String(value) : '--')
}

function validateLogin() {
  if (!loginForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return false
  }
  if (!loginForm.password.trim()) {
    ElMessage.error('请输入密码')
    return false
  }
  return true
}

function validateRegister() {
  if (!registerForm.username.trim()) {
    ElMessage.error('请输入用户名')
    return false
  }
  if (registerForm.username.trim().length < 3 || registerForm.username.trim().length > 20) {
    ElMessage.error('用户名长度需在 3-20 之间')
    return false
  }
  if (!registerForm.realName.trim()) {
    ElMessage.error('请输入真实姓名')
    return false
  }
  if (!registerForm.password.trim()) {
    ElMessage.error('请输入密码')
    return false
  }
  if (registerForm.password.length < 6 || registerForm.password.length > 20) {
    ElMessage.error('密码长度需在 6-20 之间')
    return false
  }
  if (registerForm.confirmPassword !== registerForm.password) {
    ElMessage.error('两次输入的密码不一致')
    return false
  }
  return true
}

async function handleLogin() {
  if (!validateLogin()) return

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

async function handleRegister() {
  if (!validateRegister()) return

  loading.value = true
  try {
    await register({
      username: registerForm.username.trim(),
      password: registerForm.password,
      realName: registerForm.realName.trim(),
      role: registerForm.role
    })
    ElMessage.success('注册成功，请登录')
    activeTab.value = 'login'
    registerForm.password = ''
    registerForm.confirmPassword = ''
  } catch (error) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

async function fetchRuntimeMetrics() {
  try {
    const res = await getRuntimeMetrics()
    const data = res.data || {}
    runtimeMetrics.nodeStatus = formatNodeStatus(data.nodeStatus)
    runtimeMetrics.gatewayConcurrency = data.gatewayConcurrency ?? '--'
    runtimeMetrics.syncDelayMs = data.syncDelayMs != null ? `${data.syncDelayMs}毫秒` : '--'
  } catch (_e) {
    runtimeMetrics.nodeStatus = '异常'
    runtimeMetrics.gatewayConcurrency = '--'
    runtimeMetrics.syncDelayMs = '--'
  }
}

onMounted(() => {
  fetchRuntimeMetrics()
  runtimeMetricsTimer = window.setInterval(fetchRuntimeMetrics, 10000)
})

onBeforeUnmount(() => {
  if (!runtimeMetricsTimer) return
  window.clearInterval(runtimeMetricsTimer)
  runtimeMetricsTimer = null
})
</script>

<style scoped>
.login-page {
  background:
    radial-gradient(circle at 0% 0%, color-mix(in srgb, var(--accent-500) 15%, transparent), transparent 36%),
    radial-gradient(circle at 100% -4%, color-mix(in srgb, var(--accent-600) 16%, transparent), transparent 40%),
    linear-gradient(140deg, color-mix(in srgb, var(--bg-elevated) 92%, transparent), var(--bg-base));
}

.brand-panel,
.auth-panel {
  border-color: color-mix(in srgb, var(--panel-border) 82%, transparent);
  box-shadow: var(--shadow-panel);
}

.brand-panel {
  position: relative;
  overflow: hidden;
}

.brand-panel::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    linear-gradient(to right, var(--surface-grid-line) 1px, transparent 1px),
    linear-gradient(to bottom, var(--surface-grid-line) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: radial-gradient(circle at 25% 15%, black 32%, transparent 78%);
}

.brand-kicker {
  font-size: 12px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--accent-700);
  font-weight: 600;
}

.brand-title {
  font-size: 30px;
  line-height: 1.12;
  color: var(--text-primary);
}

.brand-desc {
  max-width: 52ch;
  font-size: 15px;
  line-height: 1.7;
  color: var(--text-secondary);
}

.value-card {
  border-radius: 12px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 80%, transparent);
  background: color-mix(in srgb, var(--surface-base) 74%, transparent);
  padding: 12px;
  min-height: 100%;
}

.value-title {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
}

.value-desc {
  margin: 6px 0 0;
  font-size: 12px;
  line-height: 1.55;
  color: var(--text-secondary);
}

.metric-strip {
  position: relative;
  z-index: 1;
}

.metric-item {
  border: 1px solid color-mix(in srgb, var(--panel-border) 78%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  border-radius: 12px;
  padding: 10px;
}

.metric-item p {
  margin: 0;
  font-size: 11px;
  color: var(--text-secondary);
}

.metric-item strong {
  margin-top: 6px;
  display: block;
  font-size: 18px;
  color: var(--text-primary);
  font-variant-numeric: tabular-nums;
}

.auth-mark {
  border: 1px solid color-mix(in srgb, var(--accent-500) 32%, transparent);
  background: color-mix(in srgb, var(--accent-500) 14%, transparent);
  color: var(--accent-700);
}
</style>

