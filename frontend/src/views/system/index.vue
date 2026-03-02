<template>
  <div class="system-page app-page">
    <section class="hero-panel">
      <div class="hero-content">
        <p class="hero-kicker">System Console</p>
        <h2 class="hero-title">系统设置与安全中心</h2>
        <p class="hero-description">
          统一管理系统账号状态、密码安全和操作审计，保障日常教务管理稳定运行。
        </p>
        <div class="hero-chip-list">
          <div class="hero-chip">
            <span>系统用户总数</span>
            <strong>{{ total }}</strong>
          </div>
          <div class="hero-chip">
            <span>当前页启用用户</span>
            <strong>{{ enabledUserCount }}</strong>
          </div>
          <div class="hero-chip">
            <span>审计日志总数</span>
            <strong>{{ logTotal }}</strong>
          </div>
        </div>
      </div>

      <div class="hero-metric-grid">
        <el-card class="metric-card admin-card">
          <div class="metric-icon">
            <el-icon><UserFilled /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">管理员</span>
            <strong class="metric-value">{{ adminCount }}</strong>
          </div>
        </el-card>

        <el-card class="metric-card teacher-card">
          <div class="metric-icon">
            <el-icon><Avatar /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">教师</span>
            <strong class="metric-value">{{ teacherCount }}</strong>
          </div>
        </el-card>

        <el-card class="metric-card student-card">
          <div class="metric-icon">
            <el-icon><School /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">学生</span>
            <strong class="metric-value">{{ studentCount }}</strong>
          </div>
        </el-card>

        <el-card class="metric-card risk-card">
          <div class="metric-icon">
            <el-icon><WarningFilled /></el-icon>
          </div>
          <div class="metric-content">
            <span class="metric-label">当前页失败日志</span>
            <strong class="metric-value">{{ failedLogCount }}</strong>
          </div>
        </el-card>
      </div>
    </section>

    <section class="bento-grid">
      <AppCard class="panel-card panel-user" title="系统用户管理" content-class="panel-body">
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="用户名">
            <el-input v-model="searchForm.username" clearable placeholder="请输入用户名" />
          </el-form-item>
          <el-form-item label="角色">
            <el-select v-model="searchForm.role" clearable style="width: 140px">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="教师" value="TEACHER" />
              <el-option label="学生" value="STUDENT" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <AppButton @click="handleSearch">查询</AppButton>
            <AppButton variant="secondary" class="ml-2" @click="handleReset">重置</AppButton>
          </el-form-item>
        </el-form>

        <el-table :data="tableData" v-loading="loading" stripe>
          <el-table-column type="index" label="序号" width="60" />
          <el-table-column prop="username" label="用户名" width="140" />
          <el-table-column prop="realName" label="姓名" width="130" />
          <el-table-column label="角色" width="110">
            <template #default="{ row }">
              <el-tag :type="getRoleTagType(row.role)" effect="light">{{ getRoleLabel(row.role) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="phone" label="电话" width="140" />
          <el-table-column prop="email" label="邮箱" min-width="220" />
          <el-table-column label="状态" width="110">
            <template #default="{ row }">
              <div class="status-cell">
                <el-switch
                  :model-value="row.status === 1"
                  @change="(val) => handleStatusChange(row, val)"
                />
                <span class="status-text">{{ row.status === 1 ? '启用' : '停用' }}</span>
              </div>
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
      </AppCard>

      <AppCard class="panel-card panel-security" title="账号安全" content-class="panel-body">
        <div class="security-tip">
          <el-icon><Lock /></el-icon>
          <span>建议定期更新密码，并避免使用与其他平台重复的弱口令。</span>
        </div>

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
          <AppButton :loading="pwdLoading" @click="submitPassword">保存密码</AppButton>
        </el-form>
      </AppCard>
    </section>

    <section class="bento-grid log-grid">
      <AppCard class="panel-card panel-log" title="操作审计日志" content-class="panel-body">
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
            <AppButton @click="handleLogSearch">查询</AppButton>
            <AppButton variant="secondary" class="ml-2" @click="handleLogReset">重置</AppButton>
          </el-form-item>
        </el-form>

        <el-table :data="logTableData" v-loading="logLoading" stripe>
          <el-table-column type="index" label="序号" width="60" />
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
      </AppCard>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Avatar, Lock, School, UserFilled, WarningFilled } from '@element-plus/icons-vue'
import AppButton from '@/components/ui/AppButton.vue'
import AppCard from '@/components/ui/AppCard.vue'
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

const enabledUserCount = computed(() => tableData.value.filter(item => item.status === 1).length)
const adminCount = computed(() => tableData.value.filter(item => item.role === 'ADMIN').length)
const teacherCount = computed(() => tableData.value.filter(item => item.role === 'TEACHER').length)
const studentCount = computed(() => tableData.value.filter(item => item.role === 'STUDENT').length)
const failedLogCount = computed(() => logTableData.value.filter(item => item.status === 0).length)

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

function getRoleLabel(role) {
  const roleMap = {
    ADMIN: '管理员',
    TEACHER: '教师',
    STUDENT: '学生'
  }
  return roleMap[role] || role
}

function getRoleTagType(role) {
  if (role === 'ADMIN') return 'danger'
  if (role === 'TEACHER') return 'warning'
  if (role === 'STUDENT') return 'success'
  return 'info'
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

<style scoped lang="scss">
.system-page {
  width: 100%;
  min-height: calc(100vh - 140px);
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 2px 0 14px;
  box-sizing: border-box;
}

.hero-panel {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(340px, 1fr) minmax(420px, 1.1fr);
  gap: 16px;
  padding: 24px;
  border-radius: 20px;
  border: 1px solid rgba(20, 84, 101, 0.2);
  background:
    radial-gradient(circle at 8% 12%, rgba(255, 255, 255, 0.46), transparent 38%),
    radial-gradient(circle at 92% 4%, rgba(172, 221, 217, 0.24), transparent 36%),
    linear-gradient(140deg, rgba(255, 255, 255, 0.9), rgba(239, 248, 247, 0.86));
  box-shadow: 0 18px 34px rgba(22, 66, 79, 0.12);
}

.hero-kicker {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
  color: #4a7d8c;
}

.hero-title {
  margin: 10px 0 12px;
  color: #173749;
  font-size: 32px;
  line-height: 1.2;
  font-weight: 700;
}

.hero-description {
  margin: 0;
  max-width: 580px;
  color: #446173;
  line-height: 1.7;
  font-size: 14px;
}

.hero-chip-list {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.hero-chip {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(24, 97, 110, 0.2);
  background: rgba(255, 255, 255, 0.74);
}

.hero-chip span {
  color: #4c6878;
  font-size: 13px;
}

.hero-chip strong {
  color: #173a4c;
  font-size: 24px;
  font-weight: 700;
}

.hero-metric-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.metric-card {
  border-radius: 16px;
  border: 1px solid rgba(24, 97, 110, 0.2);
  background: linear-gradient(155deg, rgba(255, 255, 255, 0.94), rgba(238, 248, 247, 0.84));
  transition: transform var(--sms-motion-standard), box-shadow var(--sms-motion-standard);
}

.metric-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 16px 26px rgba(20, 74, 87, 0.16);
}

:deep(.metric-card .el-card__body) {
  padding: 14px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.metric-icon {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #fff;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.22);
}

.admin-card .metric-icon {
  background: linear-gradient(135deg, #1f958e, #116c68);
}

.teacher-card .metric-icon {
  background: linear-gradient(135deg, #2f8e9e, #2a6f93);
}

.student-card .metric-icon {
  background: linear-gradient(135deg, #3da293, #257f71);
}

.risk-card .metric-icon {
  background: linear-gradient(135deg, #d9806c, #b15f4e);
}

.metric-content {
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.metric-label {
  color: #4e6574;
  font-size: 13px;
}

.metric-value {
  margin-top: 4px;
  color: #163548;
  font-size: 28px;
  line-height: 1;
}

.bento-grid {
  display: grid;
  grid-template-columns: repeat(12, minmax(0, 1fr));
  gap: 16px;
}

.panel-card {
  border-radius: 18px;
  border: 1px solid rgba(21, 88, 102, 0.17);
  background: linear-gradient(150deg, rgba(255, 255, 255, 0.92), rgba(240, 249, 248, 0.84));
  box-shadow: 0 10px 24px rgba(20, 68, 80, 0.1);
}

.panel-user {
  grid-column: span 8;
}

.panel-security {
  grid-column: span 4;
}

.panel-log {
  grid-column: span 12;
}

:deep(.panel-card .app-panel-header) {
  border-bottom: 1px solid rgba(19, 87, 98, 0.15);
  padding: 14px 18px;
}

:deep(.panel-card .panel-body) {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 250px;
  padding: 16px 18px;
  box-sizing: border-box;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.search-form {
  margin-bottom: 16px;
}

.pagination {
  margin-top: 16px;
  justify-content: flex-end;
}

.status-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-text {
  color: #5e7887;
  font-size: 12px;
}

.security-tip {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin-bottom: 16px;
  padding: 12px 14px;
  border-radius: 12px;
  border: 1px solid rgba(20, 93, 105, 0.16);
  background: rgba(238, 249, 248, 0.78);
  color: #365969;
  line-height: 1.6;
}

.security-tip .el-icon {
  margin-top: 2px;
  color: #166f6b;
}

.log-grid :deep(.panel-body) {
  min-height: 320px;
}

@media (max-width: 1400px) {
  .hero-panel {
    grid-template-columns: 1fr;
  }

  .panel-user,
  .panel-security,
  .panel-log {
    grid-column: span 12;
  }
}

@media (max-width: 992px) {
  .system-page {
    min-height: auto;
  }

  .hero-panel {
    padding: 16px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-chip-list {
    grid-template-columns: 1fr;
  }

  .hero-metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .bento-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-user,
  .panel-security,
  .panel-log {
    grid-column: span 2;
  }
}

@media (max-width: 768px) {
  .hero-metric-grid {
    grid-template-columns: 1fr;
  }

  .bento-grid {
    grid-template-columns: 1fr;
  }

  .panel-user,
  .panel-security,
  .panel-log {
    grid-column: span 1;
  }
}
</style>

