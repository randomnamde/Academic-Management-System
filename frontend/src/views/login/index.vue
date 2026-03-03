<template>
  <div class="min-h-screen bg-neutralx-50 px-4 py-10 md:px-6">
    <div class="mx-auto grid w-full max-w-5xl gap-4 lg:grid-cols-[1.1fr,0.9fr]">
      <section class="app-panel p-6">
        <p class="text-[12px] uppercase tracking-[0.08em] text-slatex-500">教务管理</p>
        <h1 class="mt-2 text-[30px] font-semibold tracking-tight text-primary-900">学生管理系统</h1>
        <p class="mt-2 text-[13px] text-slatex-600">统一处理学生、课程、成绩、考勤与权限管理任务。</p>

        <div class="mt-5 grid gap-2 sm:grid-cols-3">
          <article class="rounded-sm border border-neutralx-200 bg-white p-3">
            <p class="text-[11px] text-slatex-500">节点状态</p>
            <strong class="mt-1 block text-[16px] text-primary-900">{{ runtimeMetrics.nodeStatus }}</strong>
          </article>
          <article class="rounded-sm border border-neutralx-200 bg-white p-3">
            <p class="text-[11px] text-slatex-500">并发网关</p>
            <strong class="mt-1 block text-[16px] text-primary-900">{{ runtimeMetrics.gatewayConcurrency }}</strong>
          </article>
          <article class="rounded-sm border border-neutralx-200 bg-white p-3">
            <p class="text-[11px] text-slatex-500">同步延迟</p>
            <strong class="mt-1 block text-[16px] text-primary-900">{{ runtimeMetrics.syncDelayMs }}</strong>
          </article>
        </div>
      </section>

      <section class="app-panel p-5 md:p-6">
        <header class="mb-4 text-center">
          <div class="mx-auto mb-2 inline-flex h-10 w-10 items-center justify-center rounded-sm border border-neutralx-200 bg-white text-primary-800">
            <School class="h-5 w-5" />
          </div>
          <h2 class="text-[22px] font-semibold tracking-tight text-primary-900">身份验证</h2>
          <p class="mt-1 text-[12px] text-slatex-500">学生管理平台</p>
        </header>

        <AppTabs v-model="activeTab" :items="tabs" />

        <form v-if="activeTab === 'login'" class="mt-4 space-y-3" @submit.prevent="handleLogin">
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">用户名</label>
            <AppInput v-model="loginForm.username" placeholder="请输入用户名" />
          </div>
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">密码</label>
            <AppInput v-model="loginForm.password" type="password" placeholder="请输入密码" />
          </div>
          <AppButton block :loading="loading" native-type="submit">登录</AppButton>
        </form>

        <form v-else class="mt-4 space-y-3" @submit.prevent="handleRegister">
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">用户名</label>
            <AppInput v-model="registerForm.username" placeholder="请输入用户名" />
          </div>
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">真实姓名</label>
            <AppInput v-model="registerForm.realName" placeholder="请输入真实姓名" />
          </div>
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">密码</label>
            <AppInput v-model="registerForm.password" type="password" placeholder="请输入密码" />
          </div>
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">确认密码</label>
            <AppInput v-model="registerForm.confirmPassword" type="password" placeholder="请再次输入密码" />
          </div>
          <div>
            <label class="mb-1 block text-[13px] font-medium text-slatex-700">角色</label>
            <AppSelect v-model="registerForm.role" :options="[{ label: '学生', value: 'STUDENT' }]" />
          </div>
          <AppButton variant="secondary" block :loading="loading" native-type="submit">注册</AppButton>
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

