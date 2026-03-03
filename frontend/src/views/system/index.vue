<template>
  <div class="app-page space-y-3">
    <AppCard title="系统偏好" surface="glass" content-class="p-4">
      <div class="grid gap-3 md:grid-cols-2">
        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="text-[15px] font-semibold text-primary-900">主题模式</p>
          <p class="mt-1 text-[13px] text-slatex-500">支持跟随系统、浅色和深色三态切换，刷新后保持当前选择。</p>
          <div class="mt-3 grid gap-2 sm:grid-cols-3">
            <button
              v-for="item in themeOptions"
              :key="item.value"
              class="theme-option touch-target"
              :class="themeMode === item.value ? 'is-active' : ''"
              @click="setThemeMode(item.value)"
            >
              <component :is="item.icon" class="h-4 w-4" />
              <span>{{ item.label }}</span>
            </button>
          </div>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3">
          <p class="text-[15px] font-semibold text-primary-900">表格密度</p>
          <p class="mt-1 text-[13px] text-slatex-500">在紧凑与标准模式间切换，影响所有列表与审计视图。</p>
          <div class="mt-3 flex items-center gap-2">
            <AppButton :variant="tableDensity === 'compact' ? 'primary' : 'secondary'" @click="setDensity('compact')">紧凑</AppButton>
            <AppButton :variant="tableDensity === 'comfortable' ? 'primary' : 'secondary'" @click="setDensity('comfortable')">标准</AppButton>
          </div>
        </div>

        <div class="preference-card rounded-md border border-neutralx-200 p-3 md:col-span-2">
          <p class="text-[15px] font-semibold text-primary-900">权限管理入口</p>
          <p class="mt-1 text-[13px] text-slatex-500">角色、权限矩阵与审计日志已迁移到独立权限中心模块。</p>
          <div class="mt-3">
            <AppButton variant="secondary" @click="$router.push('/rbac/users')">进入权限中心</AppButton>
          </div>
        </div>
      </div>
    </AppCard>

    <AppCard title="账号安全" surface="base" content-class="p-4">
      <el-form ref="pwdFormRef" :model="passwordForm" :rules="pwdRules" label-width="96px" class="max-w-2xl">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
        <el-form-item>
          <AppButton :loading="pwdLoading" @click="submitPassword">保存密码</AppButton>
        </el-form-item>
      </el-form>
    </AppCard>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useStore } from 'vuex'
import { ElMessage } from 'element-plus'
import { Monitor, Moon, SunMedium } from 'lucide-vue-next'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { updatePassword } from '@/api/user'
import { useTheme } from '@/composables/useTheme'

const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)
const { mode: themeMode, setThemeMode } = useTheme(store)

const themeOptions = [
  { value: 'system', label: '系统', icon: Monitor },
  { value: 'light', label: '浅色', icon: SunMedium },
  { value: 'dark', label: '深色', icon: Moon }
]

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const pwdFormRef = ref()
const pwdLoading = ref(false)

const pwdRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function setDensity(density) {
  store.commit('SET_TABLE_DENSITY', density)
}

async function submitPassword() {
  const valid = await pwdFormRef.value.validate().catch(() => false)
  if (!valid) return

  pwdLoading.value = true
  try {
    await updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    ElMessage.success('密码已更新')
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
  } finally {
    pwdLoading.value = false
  }
}
</script>

<style scoped>
.preference-card {
  border-color: color-mix(in srgb, var(--panel-border) 84%, transparent);
  background: color-mix(in srgb, var(--surface-base) 82%, transparent);
}

.theme-option {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 40px;
  border-radius: 10px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  color: var(--text-secondary);
  font-size: 13px;
  font-weight: 500;
  transition: all 180ms ease;
}

.theme-option:hover {
  color: var(--text-primary);
  background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
}

.theme-option.is-active {
  border-color: color-mix(in srgb, var(--accent-500) 34%, transparent);
  background: color-mix(in srgb, var(--accent-500) 16%, transparent);
  color: var(--accent-700);
}
</style>

