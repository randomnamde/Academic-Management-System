<template>
  <div class="app-page space-y-3">
    <AppCard title="系统偏好" content-class="p-4">
      <div class="grid gap-3 md:grid-cols-2">
        <div class="rounded-sm border border-neutralx-200 bg-white p-3">
          <p class="text-[13px] font-semibold text-primary-900">表格密度</p>
          <p class="mt-1 text-[12px] text-slatex-500">在紧凑与标准模式间切换，影响所有列表与审计视图。</p>
          <div class="mt-3 flex items-center gap-2">
            <AppButton :variant="tableDensity === 'compact' ? 'primary' : 'secondary'" @click="setDensity('compact')">紧凑</AppButton>
            <AppButton :variant="tableDensity === 'comfortable' ? 'primary' : 'secondary'" @click="setDensity('comfortable')">标准</AppButton>
          </div>
        </div>

        <div class="rounded-sm border border-neutralx-200 bg-white p-3">
          <p class="text-[13px] font-semibold text-primary-900">权限管理入口</p>
          <p class="mt-1 text-[12px] text-slatex-500">角色、权限矩阵与审计日志已迁移到独立 RBAC 模块。</p>
          <div class="mt-3">
            <AppButton variant="secondary" @click="$router.push('/rbac/users')">进入 RBAC</AppButton>
          </div>
        </div>
      </div>
    </AppCard>

    <AppCard title="账号安全" content-class="p-4">
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
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
import { updatePassword } from '@/api/user'

const store = useStore()
const tableDensity = computed(() => store.getters.tableDensity)

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
