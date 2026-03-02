<template>
  <el-container class="main-layout">
    <el-aside
      v-if="!isMobile"
      :width="isCollapse ? '72px' : `${sidebarExpandedWidth}px`"
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
          text-color="#d3e8ea"
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
            text-color="#d3e8ea"
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

    <el-container class="workspace-shell">
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
const viewportWidth = ref(window.innerWidth)
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
const sidebarExpandedWidth = computed(() => {
  const width = viewportWidth.value
  if (width >= 1680) return 248
  if (width >= 1440) return 236
  if (width >= 1280) return 220
  if (width >= 1120) return 206
  return 192
})
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
  viewportWidth.value = window.innerWidth
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
  height: 100dvh;
  min-height: 100vh;
  min-height: 100dvh;
  position: relative;
  overflow: hidden;
}

.sidebar {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  height: 100dvh;
  border-right: 1px solid rgba(191, 214, 217, 0.2);
  background: linear-gradient(180deg, rgba(25, 53, 73, 0.95) 0%, rgba(20, 42, 61, 0.94) 100%);
  box-shadow: 12px 0 28px rgba(15, 30, 45, 0.28);
  backdrop-filter: blur(16px);
  transition: width var(--sms-motion-standard, 180ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
  z-index: 3;
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: 0;
  pointer-events: none;
  background:
    linear-gradient(rgba(201, 228, 232, 0.06) 1px, transparent 1px),
    linear-gradient(90deg, rgba(201, 228, 232, 0.06) 1px, transparent 1px);
  background-size: 28px 28px;
  mask-image: linear-gradient(180deg, rgba(0, 0, 0, 0.45), transparent 72%);
}

.workspace-shell {
  min-width: 0;
  height: 100%;
  min-height: 0;
}

.brand-zone {
  height: 76px;
  padding: 14px 14px;
  border-bottom: 1px solid rgba(172, 206, 214, 0.22);
  display: flex;
  align-items: center;
  gap: 10px;
}

.brand-zone.mobile {
  padding: 16px 12px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  border-radius: 12px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background: linear-gradient(135deg, #1c9a94, #116f6a);
  box-shadow: 0 8px 18px rgba(12, 76, 74, 0.36);
  font-size: 20px;
}

.brand-copy {
  min-width: 0;
}

.brand-copy h1 {
  margin: 0;
  font-size: 15px;
  font-weight: 700;
  color: #eff8f9;
  letter-spacing: 0.01em;
}

.brand-copy p {
  margin: 2px 0 0;
  font-size: 12px;
  color: rgba(196, 221, 224, 0.82);
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
  font-weight: 700;
  color: rgba(169, 201, 207, 0.72);
  text-transform: uppercase;
  letter-spacing: 0.12em;
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
  background: rgba(94, 159, 167, 0.2);
}

:deep(.el-menu-item.is-active) {
  color: #fff;
  background: linear-gradient(90deg, rgba(25, 138, 132, 0.92), rgba(18, 109, 103, 0.92));
  box-shadow: 0 8px 22px rgba(15, 84, 82, 0.28);
}

:deep(.el-menu-item.is-active::before) {
  content: '';
  position: absolute;
  left: 6px;
  top: 8px;
  width: 3px;
  height: 28px;
  border-radius: 999px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.95), rgba(220, 244, 242, 0.35));
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.35);
}

:deep(.el-menu-item:focus-visible) {
  outline: 2px solid rgba(188, 232, 228, 0.95);
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
  border-top: 1px solid rgba(172, 207, 213, 0.2);
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.tool-button {
  width: 100%;
  height: 40px;
  border: 1px solid rgba(162, 204, 210, 0.24);
  border-radius: 12px;
  background: rgba(240, 250, 250, 0.08);
  color: rgba(215, 236, 239, 0.96);
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
  background: rgba(90, 155, 164, 0.28);
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
  background: linear-gradient(180deg, rgba(25, 53, 73, 0.99) 0%, rgba(20, 42, 61, 0.96) 100%);
  border-right: 1px solid rgba(191, 214, 217, 0.24);
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
  background: linear-gradient(90deg, rgba(255, 255, 255, 0.86), rgba(246, 252, 252, 0.84));
  border: 1px solid rgba(18, 69, 86, 0.18);
  box-shadow: 0 10px 22px rgba(26, 56, 68, 0.14);
  backdrop-filter: blur(12px);
  color: #1b3240;
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
  color: #173340;
  font-weight: 600;
}

.header-left :deep(.el-breadcrumb__inner) {
  color: rgba(50, 84, 102, 0.88);
}

.header-left :deep(.el-breadcrumb__separator) {
  color: rgba(73, 116, 136, 0.6);
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  margin-right: 15px;
  color: #2e5a6e;
  transition:
    transform var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1)),
    color var(--sms-motion-micro, 120ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.collapse-btn:hover {
  color: #137b76;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.message-badge {
  cursor: pointer;
  color: #2f5768;
}

:deep(.el-badge__content) {
  background-color: #147874;
  border-color: #147874;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 8px;
  background: rgba(224, 242, 241, 0.74);
  border: 1px solid rgba(16, 94, 103, 0.24);
  border-radius: 999px;
  padding: 6px 12px;
  transition: all var(--sms-motion-standard, 180ms) var(--sms-ease-standard, cubic-bezier(0.22, 1, 0.36, 1));
}

.user-info:hover {
  background: rgba(204, 235, 233, 0.88);
  box-shadow: 0 6px 16px rgba(16, 79, 78, 0.22);
}

.notice-popover .notice-title-row {
  font-weight: 600;
  margin-bottom: 10px;
  color: #274354;
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
  background: rgba(19, 123, 118, 0.08);
}

.notice-item-title {
  font-size: 14px;
  color: #2a495a;
  line-height: 1.4;
}

.notice-item-time {
  margin-top: 4px;
  font-size: 12px;
  color: #7b8d97;
}

.notice-footer {
  display: flex;
  justify-content: flex-end;
  margin-top: 6px;
}

.notice-detail-content {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #304a58;
  min-height: 80px;
}

.main-content {
  flex: 1;
  min-height: 0;
  position: relative;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.92), rgba(245, 251, 251, 0.88));
  border: 1px solid rgba(23, 79, 95, 0.2);
  border-radius: 18px;
  box-shadow: 0 16px 34px rgba(32, 67, 82, 0.15);
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
  background: radial-gradient(circle at 6% 8%, rgba(145, 210, 204, 0.26), transparent 34%);
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
