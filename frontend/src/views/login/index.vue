<template>
  <div class="min-h-screen bg-[radial-gradient(circle_at_10%_10%,rgba(84,169,160,0.14),transparent_28%),radial-gradient(circle_at_92%_12%,rgba(36,115,109,0.16),transparent_30%),linear-gradient(135deg,#f6fbfc,#eaf2f5)] px-4 py-10 md:px-6">
    <div class="mx-auto grid w-full max-w-6xl gap-6 lg:grid-cols-[1.15fr,0.85fr]">
      <section class="app-panel relative overflow-hidden p-8">
        <div class="absolute right-0 top-0 h-56 w-56 rounded-full bg-brand-200/40 blur-3xl"></div>
        <p class="mb-4 inline-flex items-center rounded-full border border-brand-200 bg-brand-50 px-3 py-1 text-xs font-semibold tracking-widest text-brand-700">
          EDU CORE ONLINE
        </p>
        <h1 class="font-display text-4xl font-bold leading-tight text-ink-900 md:text-5xl">智慧校园管控中枢</h1>
        <p class="mt-3 text-sm uppercase tracking-[0.2em] text-ink-500">Unified Student Intelligence Platform</p>

        <div class="mt-8 grid gap-3 sm:grid-cols-3">
          <article class="rounded-2xl border border-slate-200 bg-white/80 p-4">
            <p class="text-xs text-slate-500">节点状态</p>
            <strong class="mt-1 block text-xl text-ink-900">{{ runtimeMetrics.nodeStatus }}</strong>
          </article>
          <article class="rounded-2xl border border-slate-200 bg-white/80 p-4">
            <p class="text-xs text-slate-500">并发网关</p>
            <strong class="mt-1 block text-xl text-ink-900">{{ runtimeMetrics.gatewayConcurrency }}</strong>
          </article>
          <article class="rounded-2xl border border-slate-200 bg-white/80 p-4">
            <p class="text-xs text-slate-500">同步延迟</p>
            <strong class="mt-1 block text-xl text-ink-900">{{ runtimeMetrics.syncDelayMs }}</strong>
          </article>
        </div>
      </section>

      <section class="app-panel p-6 md:p-8">
        <header class="mb-6 text-center">
          <div class="mx-auto mb-3 flex h-12 w-12 items-center justify-center rounded-2xl bg-brand-600 text-white shadow-lg">
            <School class="h-6 w-6" />
          </div>
          <h2 class="text-2xl font-semibold text-ink-900">学生管理系统</h2>
          <p class="mt-1 text-sm text-slate-500">Student Management System</p>
        </header>

        <AppTabs v-model="activeTab" :items="tabs" />

        <form v-if="activeTab === 'login'" class="mt-5 space-y-4" @submit.prevent="handleLogin">
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">用户名</label>
            <AppInput v-model="loginForm.username" placeholder="请输入用户名" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">密码</label>
            <AppInput v-model="loginForm.password" type="password" placeholder="请输入密码" />
          </div>
          <AppButton block :loading="loading" native-type="submit">登录</AppButton>
        </form>

        <form v-else class="mt-5 space-y-4" @submit.prevent="handleRegister">
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">用户名</label>
            <AppInput v-model="registerForm.username" placeholder="请输入用户名" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">真实姓名</label>
            <AppInput v-model="registerForm.realName" placeholder="请输入真实姓名" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">密码</label>
            <AppInput v-model="registerForm.password" type="password" placeholder="请输入密码" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">确认密码</label>
            <AppInput v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" />
          </div>
          <div>
            <label class="mb-1 block text-sm font-medium text-ink-700">角色</label>
            <AppSelect v-model="registerForm.role" :options="[{ label: '学生', value: 'STUDENT' }]" />
          </div>
          <AppButton variant="secondary" block :loading="loading" native-type="submit">注册</AppButton>
        </form>

        <footer class="mt-6 border-t border-slate-200 pt-4 text-center text-xs text-slate-500">
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
  if (!runtimeMetricsTimer) return
  window.clearInterval(runtimeMetricsTimer)
  runtimeMetricsTimer = null
})
</script>
