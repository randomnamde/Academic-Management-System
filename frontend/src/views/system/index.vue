<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="16">
        <el-card>
          <template #header>
            <div class="header-row">
              <span>系统用户管理</span>
            </div>
          </template>

          <el-form :inline="true" :model="searchForm" class="search-form">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.username" clearable />
            </el-form-item>
            <el-form-item label="角色">
              <el-select v-model="searchForm.role" clearable style="width: 140px">
                <el-option label="管理员" value="ADMIN" />
                <el-option label="教师" value="TEACHER" />
                <el-option label="学生" value="STUDENT" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSearch">查询</el-button>
              <el-button @click="handleReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="tableData" v-loading="loading" stripe>
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="username" label="用户名" width="140" />
            <el-table-column prop="realName" label="姓名" width="130" />
            <el-table-column prop="role" label="角色" width="100" />
            <el-table-column prop="phone" label="电话" width="140" />
            <el-table-column prop="email" label="邮箱" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-switch
                  :model-value="row.status === 1"
                  @change="(val) => handleStatusChange(row, val)"
                />
              </template>
            </el-table-column>
          </el-table>

          <el-pagination
            class="pagination"
            v-model:current-page="page"
            v-model:page-size="size"
            :total="total"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchList"
            @current-change="fetchList"
          />
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card>
          <template #header>
            <span>修改当前账号密码</span>
          </template>
          <el-form ref="pwdFormRef" :model="passwordForm" :rules="pwdRules" label-width="80px">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-button type="primary" @click="submitPassword" :loading="pwdLoading">保存密码</el-button>
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, updatePassword, updateUserStatus } from '@/api/user'

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])

const searchForm = reactive({
  username: '',
  role: ''
})

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

async function fetchList() {
  loading.value = true
  try {
    const res = await getUserList({
      page: page.value,
      size: size.value,
      username: searchForm.username || undefined,
      role: searchForm.role || undefined
    })
    tableData.value = res.data?.records || []
    total.value = res.data?.total || 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchList()
}

function handleReset() {
  searchForm.username = ''
  searchForm.role = ''
  handleSearch()
}

async function handleStatusChange(row, enabled) {
  await updateUserStatus(row.id, enabled ? 1 : 0)
  ElMessage.success('状态已更新')
  fetchList()
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

onMounted(fetchList)
</script>

<style scoped>
.page-container {
  padding: 20px;
}
.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 16px;
}
.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
