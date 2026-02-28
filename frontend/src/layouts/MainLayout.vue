<template>
  <el-container class="main-layout">
    <el-aside
      v-if="!isMobile"
      :width="isCollapse ? '72px' : '236px'"
      class="sidebar"
      :class="{ collapsed: isCollapse }"
    >
      <div class="brand-zone">
        <div class="brand-mark">
          <el-icon><School /></el-icon>
        </div>
        <div v-show="!isCollapse" class="brand-copy">
          <h1>Campus OS</h1>
          <p>学生管理系统</p>
        </div>
      </div>

      <div class="sidebar-nav-root">
        <el-menu
          :default-active="$route.path"
          :collapse="isCollapse"
          :collapse-transition="false"
          router
          class="main-menu"
          background-color="transparent"
          text-color="#d8cffd"
          active-text-color="#ffffff"
        >
          <el-menu-item-group
            v-for="group in visibleMenuGroups"
            :key="group.key"
            :title="isCollapse ? '' : group.title"
            class="menu-group"
          >
            <el-menu-item
              v-for="item in group.items"
              :key="item.path"
              :index="item.path"
              :title="isCollapse ? item.title : ''"
              :data-menu-path="item.path"
              tabindex="0"
              class="menu-item"
              @keydown="(event) => handleMenuItemKeydown(event, item.path)"
            >
              <div class="menu-item-content" :class="{ collapsed: isCollapse }">
                <el-icon class="menu-item-icon">
                  <component :is="item.icon" />
                </el-icon>
                <span v-if="!isCollapse" class="menu-item-title">{{ item.title }}</span>
              </div>
            </el-menu-item>
          </el-menu-item-group>
        </el-menu>
      </div>

      <div class="sidebar-bottom-tools">
        <button
          v-for="action in visibleBottomActions"
          :key="action.key"
          class="tool-button"
          type="button"
          :title="action.label"
          @click="handleBottomAction(action)"
        >
          <el-icon class="tool-icon">
            <component :is="action.icon" />
          </el-icon>
          <span v-show="!isCollapse">{{ action.label }}</span>
        </button>
      </div>
    </el-aside>

    <el-drawer
      v-model="mobileDrawerVisible"
      class="mobile-sidebar-drawer"
      direction="ltr"
      size="272px"
      :with-header="false"
      :append-to-body="true"
    >
      <div class="mobile-sidebar">
        <div class="brand-zone mobile">
          <div class="brand-mark">
            <el-icon><School /></el-icon>
          </div>
          <div class="brand-copy">
            <h1>Campus OS</h1>
            <p>学生管理系统</p>
          </div>
        </div>

        <div class="sidebar-nav-root mobile-nav">
          <el-menu
            :default-active="$route.path"
            :collapse="false"
            :collapse-transition="false"
            router
            class="main-menu mobile-menu"
            background-color="transparent"
            text-color="#d8cffd"
            active-text-color="#ffffff"
          >
            <el-menu-item-group
              v-for="group in visibleMenuGroups"
              :key="group.key"
              :title="group.title"
              class="menu-group"
            >
              <el-menu-item
                v-for="item in group.items"
                :key="`mobile-${item.path}`"
                :index="item.path"
                :data-menu-path="item.path"
                tabindex="0"
                class="menu-item"
                @click="mobileDrawerVisible = false"
                @keydown="(event) => handleMenuItemKeydown(event, item.path)"
              >
                <div class="menu-item-content">
                  <el-icon class="menu-item-icon">
                    <component :is="item.icon" />
                  </el-icon>
                  <span class="menu-item-title">{{ item.title }}</span>
                </div>
              </el-menu-item>
            </el-menu-item-group>
          </el-menu>
        </div>

        <div class="sidebar-bottom-tools mobile-tools">
          <button
            v-for="action in visibleBottomActions"
            :key="`mobile-${action.key}`"
            class="tool-button"
            type="button"
            @click="handleBottomAction(action, true)"
          >
            <el-icon class="tool-icon">
              <component :is="action.icon" />
            </el-icon>
            <span>{{ action.label }}</span>
          </button>
        </div>
      </div>
    </el-drawer>

    <el-container>
      <el-header class="header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="handleMenuToggle">
            <Fold v-if="!isMobile && !isCollapse" />
            <Expand v-else />
          </el-icon>
          <breadcrumb />
        </div>
        <div class="header-right">
          <el-popover
            placement="bottom"
            :width="340"
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
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'
import { canRoute } from '@/permission/ability'

const store = useStore()
const route = useRoute()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const mobileDrawerVisible = ref(false)
const isMobile = ref(window.innerWidth < 992)
const notificationCount = ref(0)
const latestAnnouncements = ref([])
const noticeDetailVisible = ref(false)
const readAnnouncementIds = ref([])
const noticeDetail = ref({
  id: null,
  title: '',
  content: '',
  type: '',
  targetRole: '',
  createTime: ''
})

const userInfo = computed(() => store.state.userInfo || {})
const sidebarOpened = computed(() => store.state.sidebar?.opened !== false)
const isCollapse = computed(() => !sidebarOpened.value)
const readAnnouncementStorageKey = computed(() => `announcement:read:${userInfo.value?.id || 'guest'}`)
const role = computed(() => userInfo.value?.role || '')
const permissions = computed(() => userInfo.value?.permissions || [])

const menuGroupMeta = {
  ops: { title: '运营分析', order: 1 },
  teaching: { title: '教学管理', order: 2 },
  process: { title: '过程管理', order: 3 },
  system: { title: '系统管理', order: 4 }
}

const layoutChildren = computed(() => {
  const layoutRoute = router.options.routes.find((item) => item.name === 'Layout')
  return Array.isArray(layoutRoute?.children) ? layoutRoute.children : []
})

const menuGroups = computed(() => {
  const buckets = new Map()
  layoutChildren.value
    .filter((item) => item?.meta?.title && item.path !== 'profile')
    .forEach((item) => {
      const hiddenRoles = Array.isArray(item.meta?.hideInMenuForRoles) ? item.meta.hideInMenuForRoles : []
      if (hiddenRoles.includes(role.value)) {
        return
      }
      const key = item.meta?.menuGroup || 'process'
      if (!buckets.has(key)) {
        const groupMeta = menuGroupMeta[key] || { title: '其他', order: 99 }
        buckets.set(key, {
          key,
          title: groupMeta.title,
          order: groupMeta.order,
          items: []
        })
      }
      buckets.get(key).items.push({
        path: item.path.startsWith('/') ? item.path : `/${item.path}`,
        title: item.meta?.title || item.name,
        icon: item.meta?.icon || 'Menu',
        routeName: String(item.name)
      })
    })

  return Array.from(buckets.values()).sort((a, b) => a.order - b.order)
})

const bottomActions = [
  { key: 'profile', label: '个人中心', icon: 'User', type: 'route', path: '/profile', routeName: 'Profile' },
  { key: 'settings', label: '系统设置', icon: 'Setting', type: 'route', path: '/system', routeName: 'System' },
  { key: 'logout', label: '退出登录', icon: 'SwitchButton', type: 'command', command: 'logout' }
]

const hasRoutePermission = (routeName) => canRoute(role.value, routeName, permissions.value)

const visibleMenuGroups = computed(() =>
  menuGroups.value
    .map((group) => ({
      ...group,
      items: group.items.filter((item) => hasRoutePermission(item.routeName))
    }))
    .filter((group) => group.items.length > 0)
)

const visibleBottomActions = computed(() =>
  bottomActions.filter((item) => item.type !== 'route' || hasRoutePermission(item.routeName))
)

const handleResize = () => {
  const mobile = window.innerWidth < 992
  isMobile.value = mobile
  if (!mobile) {
    mobileDrawerVisible.value = false
  }
}

const handleMenuToggle = () => {
  if (isMobile.value) {
    mobileDrawerVisible.value = !mobileDrawerVisible.value
    return
  }
  store.commit('TOGGLE_SIDEBAR')
}

const parseSafe = (value, fallback) => {
  try {
    const parsed = JSON.parse(value)
    return parsed ?? fallback
  } catch (_e) {
    return fallback
  }
}

const loadReadAnnouncements = () => {
  const stored = parseSafe(localStorage.getItem(readAnnouncementStorageKey.value), [])
  readAnnouncementIds.value = Array.isArray(stored) ? stored : []
}

const saveReadAnnouncements = () => {
  localStorage.setItem(readAnnouncementStorageKey.value, JSON.stringify(readAnnouncementIds.value))
}

const isAnnouncementRead = (id) => readAnnouncementIds.value.includes(id)

const markAnnouncementAsRead = (id) => {
  if (!id || isAnnouncementRead(id)) return
  readAnnouncementIds.value = [id, ...readAnnouncementIds.value].slice(0, 500)
  saveReadAnnouncements()
}

const syncNotificationCount = () => {
  notificationCount.value = latestAnnouncements.value.filter((item) => !isAnnouncementRead(item.id)).length
}

const fetchNotificationSummary = async () => {
  try {
    const res = await getAnnouncementList({
      page: 1,
      size: 8,
      status: 1
    })
    const records = res.data?.records || []
    latestAnnouncements.value = records
    syncNotificationCount()
  } catch (_e) {
    latestAnnouncements.value = []
    notificationCount.value = 0
  }
}

const openAnnouncementPage = () => {
  noticeDetailVisible.value = false
  mobileDrawerVisible.value = false
  router.push('/announcement')
}

const openAnnouncementDetail = async (item) => {
  try {
    const res = await getAnnouncementDetail(item.id)
    noticeDetail.value = res.data || {}
    noticeDetailVisible.value = true
    markAnnouncementAsRead(item.id)
    syncNotificationCount()
  } catch (_e) {
    ElMessage.error('获取公告详情失败')
  }
}

const executeLogout = () => {
  ElMessageBox.confirm('确认退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    store.dispatch('logout')
    router.push('/login')
    ElMessage.success('已退出登录')
  })
}

const handleCommand = (command) => {
  switch (command) {
    case 'logout':
      executeLogout()
      break
    case 'profile':
      router.push('/profile')
      break
    case 'password':
      router.push('/profile?tab=password')
      break
  }
}

const handleBottomAction = (action, isMobileAction = false) => {
  if (isMobileAction) {
    mobileDrawerVisible.value = false
  }
  if (action.type === 'route' && action.path) {
    router.push(action.path)
    return
  }
  if (action.command === 'logout') {
    executeLogout()
  }
}

const handleMenuItemKeydown = (event, currentPath) => {
  const root = event.currentTarget?.closest('.sidebar-nav-root')
  const items = root ? Array.from(root.querySelectorAll('[data-menu-path]')) : []
  const currentIndex = items.findIndex((element) => element.dataset.menuPath === currentPath)

  if (event.key === 'ArrowDown' && currentIndex !== -1) {
    event.preventDefault()
    const target = items[(currentIndex + 1) % items.length]
    target?.focus()
    return
  }

  if (event.key === 'ArrowUp' && currentIndex !== -1) {
    event.preventDefault()
    const target = items[(currentIndex - 1 + items.length) % items.length]
    target?.focus()
    return
  }

  if (event.key === 'Enter') {
    event.preventDefault()
    router.push(currentPath)
    if (isMobile.value) {
      mobileDrawerVisible.value = false
    }
    return
  }

  if (event.key === 'Escape' && isMobile.value) {
    mobileDrawerVisible.value = false
  }
}

watch(
  () => route.fullPath,
  () => {
    fetchNotificationSummary()
    if (isMobile.value) {
      mobileDrawerVisible.value = false
    }
  }
)

watch(
  () => userInfo.value?.id,
  () => {
    loadReadAnnouncements()
    syncNotificationCount()
  }
)

onMounted(() => {
  loadReadAnnouncements()
  fetchNotificationSummary()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped lang="scss">
.main-layout {
  height: 100vh;
  position: relative;
  overflow: hidden;
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100%;
  border-right: 1px solid var(--sms-border-strong, rgba(177, 158, 255, 0.3));
  background:
    linear-gradient(180deg, rgba(28, 20, 54, 0.95) 0%, rgba(20, 14, 42, 0.9) 100%);
  box-shadow: 16px 0 36px rgba(14, 8, 31, 0.46);
  backdrop-filter: blur(16px);
  transition: width var(--sms-motion-standard, 180ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
  z-index: 3;
}

.brand-zone {
  height: 70px;
  padding: 12px 14px;
  border-bottom: 1px solid rgba(182, 162, 255, 0.2);
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-zone.mobile {
  padding: 14px 12px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 14px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, rgba(139, 112, 255, 0.95), rgba(186, 147, 255, 0.9));
  box-shadow: 0 10px 24px rgba(132, 98, 248, 0.36), inset 0 1px 0 rgba(255, 255, 255, 0.35);
  font-size: 20px;
}

.brand-copy {
  min-width: 0;
}

.brand-copy h1 {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  color: #f9f6ff;
  letter-spacing: 0.02em;
}

.brand-copy p {
  margin: 2px 0 0;
  font-size: 12px;
  color: rgba(221, 208, 255, 0.78);
}

.sidebar-nav-root {
  flex: 1;
  min-height: 0;
  overflow: auto;
  padding: 10px 8px;
}

.main-menu {
  border-right: none;
}

:deep(.menu-group .el-menu-item-group__title) {
  padding: 8px 12px 6px !important;
  font-size: 11px;
  font-weight: 600;
  color: rgba(194, 177, 247, 0.75);
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

:deep(.el-menu-item) {
  position: relative;
  margin: 4px 4px;
  border-radius: 12px;
  height: 44px;
  line-height: 44px;
  padding: 0 10px !important;
  transition:
    background-color var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    transform var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    box-shadow var(--sms-motion-standard, 180ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

:deep(.el-menu-item:hover) {
  background: rgba(150, 123, 245, 0.2);
  transform: translateY(-1px);
}

:deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(90deg, rgba(125, 99, 236, 0.92), rgba(168, 124, 251, 0.9));
  box-shadow: 0 10px 24px rgba(107, 79, 207, 0.34);
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 6px;
  top: 8px;
  width: 3px;
  height: 28px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(229, 219, 255, 0.4));
  box-shadow: 0 0 12px rgba(255, 255, 255, 0.6);
}

:deep(.el-menu-item:focus-visible) {
  outline: 2px solid rgba(219, 205, 255, 0.9);
  outline-offset: 1px;
}

.menu-item-content {
  width: 100%;
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-item-content.collapsed {
  justify-content: center;
  gap: 0;
}

.menu-item-icon {
  width: 18px;
  min-width: 18px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: transform var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

:deep(.el-menu-item.is-active .menu-item-icon) {
  transform: translateY(-1px) scale(1.04);
}

.menu-item-title {
  white-space: nowrap;
}

.sidebar-bottom-tools {
  padding: 10px 8px 12px;
  border-top: 1px solid rgba(176, 156, 255, 0.18);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tool-button {
  width: 100%;
  height: 40px;
  border: 1px solid rgba(176, 156, 255, 0.2);
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.05);
  color: rgba(233, 223, 255, 0.9);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  cursor: pointer;
  transition:
    transform var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    background-color var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.tool-button:hover {
  transform: translateY(-1px);
  background: rgba(175, 147, 252, 0.22);
}

.tool-icon {
  font-size: 16px;
}

.mobile-sidebar {
  height: 100%;
  display: flex;
  flex-direction: column;
}

:deep(.mobile-sidebar-drawer .el-drawer) {
  background:
    linear-gradient(180deg, rgba(28, 20, 54, 0.98) 0%, rgba(20, 14, 42, 0.96) 100%);
  border-right: 1px solid rgba(177, 158, 255, 0.3);
}

:deep(.mobile-sidebar-drawer .el-drawer__body) {
  padding: 0;
}

.mobile-nav {
  padding-left: 4px;
  padding-right: 4px;
}

.mobile-tools {
  padding-bottom: 4px;
}

.header {
  background: linear-gradient(90deg, rgba(37, 27, 69, 0.82), rgba(54, 39, 98, 0.68));
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
}

.header-left {
  display: flex;
  align-items: center;
}

.header-left :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
  color: #fdfbff;
  font-weight: 600;
}

.header-left :deep(.el-breadcrumb__inner) {
  color: rgba(225, 214, 255, 0.88);
}

.header-left :deep(.el-breadcrumb__separator) {
  color: rgba(204, 188, 247, 0.76);
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
  color: #d6c7ff;
  transition:
    transform var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    color var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.collapse-btn:hover {
  color: #bfa6ff;
  transform: scale(1.08);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-badge {
  cursor: pointer;
  color: #e0d5ff;
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
  transition: all var(--sms-motion-standard, 180ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.user-info:hover {
  background: rgba(167, 144, 255, 0.24);
  box-shadow: 0 8px 20px rgba(116, 90, 211, 0.32);
}

.notice-popover .notice-title-row {
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
  transition:
    opacity var(--sms-motion-panel, 240ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    transform var(--sms-motion-panel, 240ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.panel-switch-enter-from,
.panel-switch-leave-to {
  opacity: 0;
  transform: translateY(8px);
}

@media (max-width: 992px) {
  .header {
    margin: 8px 8px 0;
    padding: 0 12px;
  }

  .main-content {
    margin: 8px;
    border-radius: 14px;
  }

  .header-right {
    gap: 12px;
  }

  .user-info span {
    display: none;
  }
}

@media (prefers-reduced-motion: reduce) {
  .sidebar,
  .collapse-btn,
  .tool-button,
  .user-info,
  :deep(.el-menu-item) {
    transition: none !important;
  }
}
</style>
