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

    <el-row :gutter="16" class="log-row">
      <el-col :span="24">
        <el-card>
          <template #header>
            <div class="header-row">
              <span>操作审计日志</span>
            </div>
          </template>

          <el-form :inline="true" :model="logSearchForm" class="search-form">
            <el-form-item label="用户ID">
              <el-input-number v-model="logSearchForm.userId" :min="1" style="width: 130px" />
            </el-form-item>
            <el-form-item label="状态">
              <el-select v-model="logSearchForm.status" clearable style="width: 140px">
                <el-option label="成功" :value="1" />
                <el-option label="失败" :value="0" />
              </el-select>
            </el-form-item>
            <el-form-item label="操作">
              <el-input v-model="logSearchForm.operation" clearable placeholder="Controller#method" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogSearch">查询</el-button>
              <el-button @click="handleLogReset">重置</el-button>
            </el-form-item>
          </el-form>

          <el-table :data="logTableData" v-loading="logLoading" stripe>
            <el-table-column type="index" label="#" width="60" />
            <el-table-column prop="userId" label="用户ID" width="100" />
            <el-table-column prop="operation" label="操作" min-width="220" />
            <el-table-column prop="method" label="请求" min-width="220" />
            <el-table-column prop="ip" label="IP" width="150" />
            <el-table-column prop="duration" label="耗时(ms)" width="110" />
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'danger'">
                  {{ row.status === 1 ? '成功' : '失败' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="errorMsg" label="错误信息" min-width="180" show-overflow-tooltip />
            <el-table-column prop="createTime" label="时间" width="180" />
          </el-table>

          <el-pagination
            class="pagination"
            v-model:current-page="logPage"
            v-model:page-size="logSize"
            :total="logTotal"
            :page-sizes="[20, 50, 100]"
            layout="total, sizes, prev, pager, next"
            @size-change="fetchLogList"
            @current-change="fetchLogList"
          />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserList, updatePassword, updateUserStatus } from '@/api/user'
import { getSysLogList } from '@/api/sysLog'

const loading = ref(false)
const page = ref(1)
const size = ref(10)
const total = ref(0)
const tableData = ref([])
const logLoading = ref(false)
const logPage = ref(1)
const logSize = ref(20)
const logTotal = ref(0)
const logTableData = ref([])

const searchForm = reactive({
  username: '',
  role: ''
})

const logSearchForm = reactive({
  userId: null,
  status: null,
  operation: ''
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

async function fetchLogList() {
  logLoading.value = true
  try {
    const res = await getSysLogList({
      page: logPage.value,
      size: logSize.value,
      userId: logSearchForm.userId || undefined,
      status: logSearchForm.status,
      operation: logSearchForm.operation || undefined
    })
    logTableData.value = res.data?.records || []
    logTotal.value = Number(res.data?.total || 0)
  } finally {
    logLoading.value = false
  }
}

function handleLogSearch() {
  logPage.value = 1
  fetchLogList()
}

function handleLogReset() {
  logSearchForm.userId = null
  logSearchForm.status = null
  logSearchForm.operation = ''
  handleLogSearch()
}

onMounted(() => {
  fetchList()
  fetchLogList()
})
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
.log-row {
  margin-top: 16px;
}
</style>
