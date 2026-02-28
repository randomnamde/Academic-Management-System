<template>
  <el-container class="main-layout">
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar" :class="{ collapsed: isCollapse }">
      <div class="logo">
        <el-icon :size="30"><School /></el-icon>
        <span v-show="!isCollapse">学生管理</span>
      </div>
      <el-menu
        :default-active="$route.path"
        :collapse="isCollapse"
        :collapse-transition="false"
        router
        class="main-menu"
        background-color="transparent"
        text-color="#d8cffd"
        active-text-color="#c7b4ff"
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.path"
          :index="item.path"
          :title="isCollapse ? item.title : ''"
          v-show="hasPermission(item)"
        >
          <div class="menu-item-content" :class="{ collapsed: isCollapse }">
            <el-icon class="menu-item-icon">
              <component :is="item.icon" />
            </el-icon>
            <span v-if="!isCollapse" class="menu-item-title">{{ item.title }}</span>
          </div>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-popover
            placement="bottom"
            :width="320"
            trigger="click"
            @show="fetchNotificationSummary"
          >
            <template #reference>
              <el-badge :value="notificationCount" :max="99" :hidden="notificationCount === 0" class="message-badge">
                <el-icon :size="20"><Bell /></el-icon>
              </el-badge>
            </template>

            <div class="notice-popover">
              <div class="notice-title-row">
                <span>最新公告</span>
              </div>

              <el-empty v-if="!latestAnnouncements.length" description="暂无公告" :image-size="70" />
              <div v-else class="notice-list">
                <div
                  v-for="item in latestAnnouncements"
                  :key="item.id"
                  class="notice-item"
                  @click="openAnnouncementDetail(item)"
                >
                  <div class="notice-item-title">{{ item.title }}</div>
                  <div class="notice-item-time">{{ item.createTime || '-' }}</div>
                </div>
              </div>

              <div class="notice-footer">
                <el-button link type="primary" @click="openAnnouncementPage">查看全部公告</el-button>
              </div>
            </div>
          </el-popover>
          <el-dropdown @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="30" :src="userInfo.avatar || defaultAvatar" />
              <span>{{ userInfo.realName || userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view v-slot="{ Component, route }">
          <transition name="panel-switch" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </el-main>
    </el-container>

    <el-dialog v-model="noticeDetailVisible" title="公告详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="标题" :span="2">{{ noticeDetail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ noticeDetail.type || '-' }}</el-descriptions-item>
        <el-descriptions-item label="目标角色">{{ noticeDetail.targetRole || '-' }}</el-descriptions-item>
        <el-descriptions-item label="发布时间">{{ noticeDetail.createTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <el-divider />
      <div class="notice-detail-content">{{ noticeDetail.content || '暂无内容' }}</div>
      <template #footer>
        <el-button @click="noticeDetailVisible = false">关闭</el-button>
        <el-button type="primary" @click="openAnnouncementPage">前往公告列表</el-button>
      </template>
    </el-dialog>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'

const store = useStore()
const route = useRoute()
const router = useRouter()
const isCollapse = ref(false)
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'
const notificationCount = ref(0)
const latestAnnouncements = ref([])
const noticeDetailVisible = ref(false)
const noticeDetail = ref({
  id: null,
  title: '',
  content: '',
  type: '',
  targetRole: '',
  createTime: ''
})

const userInfo = computed(() => store.state.userInfo)

const menuItems = [
  { path: '/dashboard', title: '首页', icon: 'HomeFilled', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/student', title: '学生管理', icon: 'UserFilled', roles: ['ADMIN', 'TEACHER'] },
  { path: '/teacher', title: '教师管理', icon: 'User', roles: ['ADMIN'] },
  { path: '/class', title: '班级管理', icon: 'School', roles: ['ADMIN', 'TEACHER'] },
  { path: '/course', title: '课程管理', icon: 'Reading', roles: ['ADMIN', 'TEACHER'] },
  { path: '/course-arrangement', title: '排课管理', icon: 'Tickets', roles: ['ADMIN', 'TEACHER'] },
  { path: '/score', title: '成绩管理', icon: 'TrendCharts', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/attendance', title: '考勤管理', icon: 'Calendar', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/leave-request', title: '请假审批', icon: 'DocumentChecked', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/announcement', title: '通知公告', icon: 'BellFilled', roles: ['ADMIN', 'TEACHER', 'STUDENT'] },
  { path: '/system', title: '系统设置', icon: 'Setting', roles: ['ADMIN'] }
]

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const hasPermission = (item) => {
  return item.roles.includes(userInfo.value.role)
}

const fetchNotificationSummary = async () => {
  try {
    const res = await getAnnouncementList({
      page: 1,
      size: 5,
      status: 1
    })
    const records = res.data?.records || []
    latestAnnouncements.value = records
    notificationCount.value = res.data?.total || records.length || 0
  } catch (_e) {
    latestAnnouncements.value = []
    notificationCount.value = 0
  }
}

const openAnnouncementPage = () => {
  noticeDetailVisible.value = false
  router.push('/announcement')
}

const openAnnouncementDetail = async (item) => {
  try {
    const res = await getAnnouncementDetail(item.id)
    noticeDetail.value = res.data || {}
    noticeDetailVisible.value = true
  } catch (_e) {
    ElMessage.error('获取公告详情失败')
  }
}

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      ElMessageBox.confirm('确认退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        store.dispatch('logout')
        router.push('/login')
        ElMessage.success('已退出登录')
      })
      break
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      router.push('/profile?tab=password')
      break
  }
}

watch(
  () => route.fullPath,
  () => {
    fetchNotificationSummary()
  }
)

onMounted(() => {
  fetchNotificationSummary()
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.sidebar {
  background:
    linear-gradient(180deg, rgba(33, 24, 63, 0.94) 0%, rgba(24, 17, 49, 0.9) 100%);
  border-right: 1px solid rgba(177, 158, 255, 0.28);
  box-shadow: 10px 0 30px rgba(28, 18, 58, 0.38);
  backdrop-filter: blur(14px);
  transition: width 0.3s;
  z-index: 2;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #f8f4ff;
    font-size: 18px;
    font-weight: 700;
    border-bottom: 1px solid rgba(176, 154, 255, 0.2);
    letter-spacing: 0.5px;

    .el-icon {
      margin-right: 10px;
      color: #b79aff;
      filter: drop-shadow(0 0 10px rgba(183, 154, 255, 0.36));
    }
  }

  &.collapsed {
    .logo {
      .el-icon {
        margin-right: 0;
      }
    }
  }

  .main-menu {
    border-right: none;
    padding-top: 8px;
  }

  :deep(.el-menu-item) {
    margin: 6px 10px;
    border-radius: 12px;
    height: 44px;
    line-height: 44px;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 0 !important;
    transition: all 0.25s ease;
  }

  :deep(.el-menu--collapse .el-menu-item) {
    width: 44px;
    margin: 6px auto;
    padding: 0 !important;
    justify-content: center;
  }

  :deep(.el-menu--collapse .el-menu-item:hover) {
    transform: none;
  }

  .menu-item-content {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
  }

  .menu-item-content.collapsed {
    gap: 0;
  }

  .menu-item-icon {
    width: 18px;
    min-width: 18px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
  }

  .menu-item-title {
    line-height: 1;
    white-space: nowrap;
  }

  :deep(.el-menu-item:hover) {
    background-color: rgba(167, 145, 255, 0.2);
  }

  :deep(.el-menu-item.is-active) {
    background: linear-gradient(90deg, rgba(135, 110, 244, 0.88), rgba(182, 136, 255, 0.84));
    color: #fdfcff;
    font-weight: 700;
    box-shadow: 0 8px 20px rgba(127, 100, 228, 0.4);
  }

  :deep(.el-menu-item.is-active .el-icon) {
    color: #fdfcff;
  }
}

.header {
  background: linear-gradient(90deg, rgba(37, 27, 69, 0.78), rgba(54, 39, 98, 0.66));
  border: 1px solid rgba(172, 154, 255, 0.24);
  box-shadow: 0 12px 30px rgba(28, 18, 58, 0.34);
  backdrop-filter: blur(12px);
  color: #f5f1ff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  margin: 12px 12px 0;
  border-radius: 14px;

  .header-left {
    display: flex;
    align-items: center;

    :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
      color: #fdfbff;
      font-weight: 600;
    }

    :deep(.el-breadcrumb__inner) {
      color: rgba(225, 214, 255, 0.88);
    }

    :deep(.el-breadcrumb__separator) {
      color: rgba(204, 188, 247, 0.76);
    }

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      margin-right: 15px;
      color: #d6c7ff;
      transition: transform 0.25s ease, color 0.25s ease;

      &:hover {
        color: #bfa6ff;
        transform: scale(1.08);
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .message-badge {
      cursor: pointer;
      color: #e0d5ff;
      transition: color 0.25s ease;

      &:hover {
        color: #c2a9ff;
      }
    }

    :deep(.el-badge__content) {
      background-color: #7e67f6;
      border-color: #7e67f6;
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      gap: 8px;
      background: rgba(158, 132, 255, 0.16);
      border: 1px solid rgba(181, 158, 255, 0.3);
      border-radius: 999px;
      padding: 6px 12px;
      transition: all 0.25s ease;

      span {
        font-size: 14px;
      }

      &:hover {
        background: rgba(167, 144, 255, 0.24);
        box-shadow: 0 8px 20px rgba(116, 90, 211, 0.32);
      }
    }
  }
}

.notice-popover {
  .notice-title-row {
    font-weight: 600;
    margin-bottom: 10px;
    color: #4b3b85;
  }

  .notice-list {
    max-height: 260px;
    overflow-y: auto;
  }

  .notice-item {
    padding: 8px 6px;
    border-radius: 8px;
    cursor: pointer;
    transition: background-color 0.2s ease;
  }

  .notice-item:hover {
    background: rgba(157, 131, 255, 0.12);
  }

  .notice-item-title {
    font-size: 14px;
    color: #2f2558;
    line-height: 1.4;
  }

  .notice-item-time {
    margin-top: 4px;
    font-size: 12px;
    color: #8f86b1;
  }

  .notice-footer {
    display: flex;
    justify-content: flex-end;
    margin-top: 6px;
  }
}

.notice-detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #3a315d;
  min-height: 80px;
}

.main-content {
  position: relative;
  background:
    linear-gradient(165deg, rgba(250, 247, 255, 0.95) 0%, rgba(241, 234, 255, 0.9) 100%);
  border: 1px solid rgba(183, 166, 255, 0.34);
  border-radius: 18px;
  box-shadow: 0 16px 34px rgba(58, 43, 110, 0.2);
  padding: 20px;
  overflow-y: auto;
  margin: 12px;
}

.main-content::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 18px;
  pointer-events: none;
  background: radial-gradient(circle at 4% 6%, rgba(176, 150, 255, 0.3), transparent 32%);
}

.panel-switch-enter-active,
.panel-switch-leave-active {
  transition: all 0.35s cubic-bezier(0.22, 1, 0.36, 1);
}

.panel-switch-enter-from,
.panel-switch-leave-to {
  opacity: 0;
  transform: translateY(10px);
  filter: blur(2px);
}

@media (max-width: 900px) {
  .header {
    margin: 8px 8px 0;
    padding: 0 12px;
  }

  .main-content {
    margin: 8px;
    border-radius: 14px;
  }
}
</style>
