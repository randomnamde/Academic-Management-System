<template>
  <div class="h-screen overflow-hidden bg-neutralx-50 text-slatex-900">
    <a href="#main-content" class="skip-link">跳到主要内容</a>
    <div class="grid h-full" :style="gridStyle">
      <aside
        class="glass-lite hidden h-screen border-r border-neutralx-200 bg-panel md:flex md:flex-col"
        :style="{ width: isCollapsed ? '72px' : '236px' }"
      >
        <div class="flex h-14 items-center border-b border-neutralx-200 px-4">
          <div class="inline-flex h-6 w-6 items-center justify-center rounded-sm border border-neutralx-200 bg-white/70 text-primary-800">
            <Shield class="h-3.5 w-3.5" />
          </div>
          <h1 v-if="!isCollapsed" class="ml-2 text-[13px] font-semibold tracking-tight text-primary-900">Academic Management System</h1>
          <span v-else class="text-[12px] font-semibold text-primary-900">AMS</span>
        </div>

        <nav class="flex-1 overflow-y-auto p-2">
          <section v-for="group in visibleMenuGroups" :key="group.key" class="mb-4">
            <p v-if="!isCollapsed" class="mb-1 flex items-center justify-start gap-1 px-2 text-[15px] font-semibold tracking-[0.01em] text-slatex-500">
              <component :is="group.icon || LayoutGrid" class="h-3 w-3" />
              <span>{{ group.title }}</span>
            </p>
            <router-link
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="mb-1 flex h-8 items-center justify-center gap-2 rounded-sm px-2 text-[14px] font-medium transition-all duration-180 touch-target"
              :class="isActive(item.path)
                ? 'bg-neutralx-100 text-primary-800'
                : 'text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900'"
              :title="isCollapsed ? item.title : ''"
            >
              <component :is="item.icon || EpMenu" class="h-3.5 w-3.5 shrink-0" />
              <span v-if="!isCollapsed" class="truncate text-center">{{ item.title }}</span>
            </router-link>
          </section>
        </nav>

        <div class="border-t border-neutralx-200 p-2">
          <button
            class="flex h-8 w-full items-center justify-center gap-2 rounded-sm px-2 text-[13px] text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900 touch-target"
            @click="handleCommand('profile')"
          >
            <User class="h-3.5 w-3.5" />
            <span v-if="!isCollapsed">个人中心</span>
          </button>
          <button
            class="mt-1 flex h-8 w-full items-center justify-center gap-2 rounded-sm px-2 text-[13px] text-state-danger hover:bg-red-50 touch-target"
            @click="handleCommand('logout')"
          >
            <LogOut class="h-3.5 w-3.5" />
            <span v-if="!isCollapsed">退出登录</span>
          </button>
        </div>
      </aside>

      <section class="flex h-screen min-w-0 flex-col">
        <header class="glass-lite z-20 flex h-14 shrink-0 items-center justify-between border-b border-neutralx-200 bg-panel px-3 md:px-5">
          <div class="flex min-w-0 items-center gap-2">
            <button
              class="inline-flex h-8 w-8 items-center justify-center rounded-sm border border-neutralx-200 bg-white text-slatex-600 hover:bg-neutralx-100 touch-target"
              :aria-label="mobileMenuVisible ? '关闭导航菜单' : '展开导航菜单'"
              @click="toggleSidebar"
            >
              <PanelLeft class="h-3.5 w-3.5" />
            </button>
            <Breadcrumb class="min-w-0" />
          </div>

          <div class="flex items-center gap-2">
            <button
              class="inline-flex h-8 items-center gap-1 rounded-sm border border-neutralx-200 bg-white px-2 text-[12px] text-slatex-600 hover:bg-neutralx-100 touch-target"
              aria-label="切换表格密度"
              @click="toggleDensity"
            >
              <Rows4 class="h-3.5 w-3.5" />
              {{ tableDensity === 'compact' ? '紧凑' : '标准' }}
            </button>

            <div class="relative">
              <button
                class="relative inline-flex h-8 w-8 items-center justify-center rounded-sm border border-neutralx-200 bg-white text-slatex-600 hover:bg-neutralx-100 touch-target"
                aria-label="通知公告"
                :aria-expanded="noticePopoverVisible ? 'true' : 'false'"
                aria-haspopup="menu"
                @click="toggleNoticePopover"
              >
                <Bell class="h-3.5 w-3.5" />
                <span
                  v-if="notificationCount > 0"
                  class="absolute -right-1 -top-1 inline-flex h-4 min-w-4 items-center justify-center rounded-sm bg-primary-800 px-1 text-[10px] text-white"
                >
                  {{ notificationCount > 99 ? '99+' : notificationCount }}
                </span>
              </button>

              <div
                v-if="noticePopoverVisible"
                class="glass-lite absolute right-0 z-30 mt-2 w-80 rounded-md border border-neutralx-200 bg-white p-2 shadow-pop"
              >
                <div class="mb-1 flex items-center justify-between px-1">
                  <p class="text-[12px] font-semibold text-primary-900">最新公告</p>
                  <button class="text-[11px] text-primary-700 hover:underline touch-target" @click="openAnnouncementPage">查看全部</button>
                </div>
                <div v-if="!latestAnnouncements.length" class="py-5 text-center text-[12px] text-slatex-500">暂无公告</div>
                <div v-else class="max-h-60 space-y-1 overflow-y-auto">
                  <button
                    v-for="item in latestAnnouncements"
                    :key="item.id"
                    class="block w-full rounded-sm px-2 py-1.5 text-left transition-all duration-180 hover:bg-neutralx-100"
                    @click="openAnnouncementDetail(item)"
                  >
                    <div class="truncate text-[13px] text-slatex-800">{{ item.title }}</div>
                    <div class="mt-0.5 text-[11px] text-slatex-500">{{ item.createTime || '-' }}</div>
                  </button>
                </div>
              </div>
            </div>

            <div class="relative">
              <button
                class="inline-flex h-8 items-center gap-2 rounded-sm border border-neutralx-200 bg-white px-2 text-[12px] text-slatex-700 hover:bg-neutralx-100 touch-target"
                aria-label="打开用户菜单"
                :aria-expanded="userMenuVisible ? 'true' : 'false'"
                aria-haspopup="menu"
                @click="userMenuVisible = !userMenuVisible"
              >
                <img :src="userInfo.avatar || defaultAvatar" alt="avatar" class="h-5 w-5 rounded-sm object-cover" />
                <span class="hidden max-w-24 truncate md:block">{{ userInfo.realName || userInfo.username || '用户' }}</span>
                <ChevronDown class="h-3.5 w-3.5 text-slatex-500" />
              </button>

              <div
                v-if="userMenuVisible"
                class="glass-lite absolute right-0 z-30 mt-2 w-40 rounded-md border border-neutralx-200 bg-white p-1.5 shadow-pop"
                role="menu"
              >
                <div class="menu-section">
                  <button class="menu-item touch-target" role="menuitem" @click="handleCommand('profile')">个人中心</button>
                  <button class="menu-item touch-target" role="menuitem" @click="handleCommand('password')">修改密码</button>
                </div>
                <div class="menu-divider"></div>
                <div class="menu-section">
                  <button class="menu-item touch-target text-state-danger" role="menuitem" @click="handleCommand('logout')">退出登录</button>
                </div>
              </div>
            </div>
          </div>
        </header>

        <main id="main-content" tabindex="-1" class="flex-1 overflow-y-auto p-3 md:p-4">
          <router-view v-slot="{ Component, route: activeRoute }">
            <KeepAlive>
              <component
                :is="Component"
                v-if="activeRoute.meta?.keepAlive !== false"
                :key="String(activeRoute.name || activeRoute.path)"
              />
            </KeepAlive>
            <component
              :is="Component"
              v-if="activeRoute.meta?.keepAlive === false"
              :key="String(activeRoute.name || activeRoute.path)"
            />
          </router-view>
        </main>
      </section>
    </div>

    <Transition name="app-fade">
      <div v-if="mobileMenuVisible" class="fixed inset-0 z-40 md:hidden">
        <div class="absolute inset-0 bg-slatex-900/35" @click="mobileMenuVisible = false"></div>
        <div class="glass-lite absolute left-0 top-0 h-full w-72 border-r border-neutralx-200 bg-panel p-2">
          <div class="mb-2 flex h-10 items-center justify-between border-b border-neutralx-200 px-2">
            <div class="flex items-center gap-1.5">
              <div class="inline-flex h-6 w-6 items-center justify-center rounded-sm border border-neutralx-200 bg-white/70 text-primary-800">
                <Shield class="h-3.5 w-3.5" />
              </div>
              <h1 class="text-[13px] font-semibold tracking-tight text-primary-900">Academic Management System</h1>
            </div>
            <button class="rounded-sm px-2 py-1 text-[12px] text-slatex-600 hover:bg-neutralx-100 touch-target" @click="mobileMenuVisible = false">关闭</button>
          </div>
          <section v-for="group in visibleMenuGroups" :key="`mobile-${group.key}`" class="mb-4">
            <p class="mb-1 flex items-center justify-start gap-1 px-2 text-[15px] font-semibold tracking-[0.01em] text-slatex-500">
              <component :is="group.icon || LayoutGrid" class="h-3 w-3" />
              <span>{{ group.title }}</span>
            </p>
            <router-link
              v-for="item in group.items"
              :key="`mobile-${item.path}`"
              :to="item.path"
              class="mb-1 flex h-8 items-center justify-center gap-2 rounded-sm px-2 text-[14px] font-medium transition-all duration-180 touch-target"
              :class="isActive(item.path)
                ? 'bg-neutralx-100 text-primary-800'
                : 'text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900'"
              @click="mobileMenuVisible = false"
            >
              <component :is="item.icon || EpMenu" class="h-3.5 w-3.5 shrink-0" />
              <span class="truncate">{{ item.title }}</span>
            </router-link>
          </section>
        </div>
      </div>
    </Transition>

    <AppModal v-model="noticeDetailVisible" title="公告详情" width="720px">
      <div class="grid gap-2 text-[13px] text-slatex-700 md:grid-cols-2">
        <div class="rounded-sm border border-neutralx-200 bg-neutralx-100 px-2 py-1.5 md:col-span-2"><strong>标题：</strong>{{ noticeDetail.title || '-' }}</div>
        <div class="rounded-sm border border-neutralx-200 bg-neutralx-100 px-2 py-1.5"><strong>类型：</strong>{{ noticeDetail.type || '-' }}</div>
        <div class="rounded-sm border border-neutralx-200 bg-neutralx-100 px-2 py-1.5"><strong>目标角色：</strong>{{ noticeDetail.targetRole || '-' }}</div>
        <div class="rounded-sm border border-neutralx-200 bg-neutralx-100 px-2 py-1.5 md:col-span-2"><strong>发布时间：</strong>{{ noticeDetail.createTime || '-' }}</div>
      </div>
      <div class="mt-3 rounded-sm border border-neutralx-200 p-3 text-[13px] leading-6 text-slatex-700">
        {{ noticeDetail.content || '暂无内容' }}
      </div>
      <template #footer>
        <button class="app-btn-secondary" @click="noticeDetailVisible = false">关闭</button>
        <button class="app-btn-primary" @click="openAnnouncementPage">前往公告列表</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import { Bell, BookOpenCheck, ChevronDown, GraduationCap, LayoutGrid, LogOut, PanelLeft, Rows4, Shield, ShieldCheck, User } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BellFilled,
  Calendar as EpCalendar,
  DataAnalysis,
  Document,
  DocumentChecked,
  HomeFilled,
  Menu as EpMenu,
  Operation,
  Postcard,
  Reading,
  School as EpSchool,
  Setting,
  Tickets,
  TrendCharts,
  User as EpUser,
  UserFilled
} from '@element-plus/icons-vue'
import Breadcrumb from '@/components/Breadcrumb.vue'
import AppModal from '@/components/ui/AppModal.vue'
import { getAnnouncementDetail, getAnnouncementList } from '@/api/announcement'
import { canRoute } from '@/permission/ability'

const store = useStore()
const route = useRoute()
const router = useRouter()
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const viewportWidth = ref(window.innerWidth)
const notificationCount = ref(0)
const latestAnnouncements = ref([])
const lastNotificationFetchAt = ref(0)
const noticePopoverVisible = ref(false)
const noticeDetailVisible = ref(false)
const userMenuVisible = ref(false)
const mobileMenuVisible = ref(false)
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
const role = computed(() => userInfo.value?.role || '')
const permissions = computed(() => userInfo.value?.permissions || [])
const sidebarOpened = computed(() => store.state.sidebar?.opened !== false)
const tableDensity = computed(() => store.state.uiPreference?.tableDensity || 'compact')
const isCollapsed = computed(() => !sidebarOpened.value)
const readAnnouncementStorageKey = computed(() => `announcement:read:${userInfo.value?.id || 'guest'}`)

const groupLabel = {
  overview: '总览',
  teaching: '教学管理',
  assessment: '考核分析',
  access: '权限控制'
}

const groupIcon = {
  overview: LayoutGrid,
  teaching: GraduationCap,
  assessment: BookOpenCheck,
  access: ShieldCheck
}

const routeIconMap = {
  HomeFilled,
  DataAnalysis,
  UserFilled,
  User: EpUser,
  School: EpSchool,
  Reading,
  Tickets,
  TrendCharts,
  Calendar: EpCalendar,
  DocumentChecked,
  BellFilled,
  Postcard,
  Operation,
  Document,
  Setting,
  Menu: EpMenu
}

const resolveRouteIcon = (icon) => {
  if (typeof icon === 'string') return routeIconMap[icon] || EpMenu
  return icon || EpMenu
}

const layoutChildren = computed(() => {
  const layoutRoute = router.options.routes.find((item) => item.name === 'Layout')
  return Array.isArray(layoutRoute?.children) ? layoutRoute.children : []
})

const visibleMenuGroups = computed(() => {
  const groups = new Map()
  layoutChildren.value
    .filter((item) => item?.meta?.title && item.path !== 'profile')
    .forEach((item) => {
      const hiddenRoles = Array.isArray(item.meta?.hideInMenuForRoles) ? item.meta.hideInMenuForRoles : []
      if (hiddenRoles.includes(role.value)) return

      const routeName = String(item.name || '')
      if (!canRoute(role.value, routeName, permissions.value)) return

      const key = item.meta?.menuGroup || 'teaching'
      if (!groups.has(key)) {
        groups.set(key, {
          key,
          title: groupLabel[key] || '其他',
          icon: groupIcon[key] || LayoutGrid,
          items: []
        })
      }

      groups.get(key).items.push({
        title: item.meta?.title || routeName,
        path: item.path.startsWith('/') ? item.path : `/${item.path}`,
        icon: resolveRouteIcon(item.meta?.icon)
      })
    })

  return Array.from(groups.values())
})

const gridStyle = computed(() => ({
  gridTemplateColumns: viewportWidth.value < 768 ? '1fr' : `${isCollapsed.value ? 72 : 236}px minmax(0, 1fr)`
}))

const parseSafe = (value, fallback) => {
  try {
    const parsed = JSON.parse(value)
    return parsed ?? fallback
  } catch (_e) {
    return fallback
  }
}

const handleResize = () => {
  viewportWidth.value = window.innerWidth
  if (viewportWidth.value >= 768) {
    mobileMenuVisible.value = false
  }
}

const toggleSidebar = () => {
  if (viewportWidth.value < 768) {
    mobileMenuVisible.value = !mobileMenuVisible.value
    return
  }
  store.commit('TOGGLE_SIDEBAR')
}

const toggleDensity = () => {
  const next = tableDensity.value === 'compact' ? 'comfortable' : 'compact'
  store.commit('SET_TABLE_DENSITY', next)
}

const isActive = (path) => route.path === path

const loadReadAnnouncements = () => {
  const stored = parseSafe(localStorage.getItem(readAnnouncementStorageKey.value), [])
  readAnnouncementIds.value = Array.isArray(stored) ? stored : []
}

const saveReadAnnouncements = () => {
  localStorage.setItem(readAnnouncementStorageKey.value, JSON.stringify(readAnnouncementIds.value))
}

const isAnnouncementRead = (id) => readAnnouncementIds.value.includes(id)
const notificationCacheTtl = 30 * 1000

const markAnnouncementAsRead = (id) => {
  if (!id || isAnnouncementRead(id)) return
  readAnnouncementIds.value = [id, ...readAnnouncementIds.value].slice(0, 500)
  saveReadAnnouncements()
}

const syncNotificationCount = () => {
  notificationCount.value = latestAnnouncements.value.filter((item) => !isAnnouncementRead(item.id)).length
}

const fetchNotificationSummary = async ({ force = false } = {}) => {
  const now = Date.now()
  if (!force && now - lastNotificationFetchAt.value < notificationCacheTtl && latestAnnouncements.value.length) {
    syncNotificationCount()
    return
  }

  try {
    const res = await getAnnouncementList({ page: 1, size: 8, status: 1 })
    latestAnnouncements.value = res.data?.records || []
    lastNotificationFetchAt.value = now
    syncNotificationCount()
  } catch (_e) {
    latestAnnouncements.value = []
    notificationCount.value = 0
  }
}

const toggleNoticePopover = async () => {
  noticePopoverVisible.value = !noticePopoverVisible.value
  userMenuVisible.value = false
  if (noticePopoverVisible.value) {
    await fetchNotificationSummary({ force: true })
  }
}

const openAnnouncementPage = () => {
  noticeDetailVisible.value = false
  noticePopoverVisible.value = false
  router.push('/announcement')
}

const openAnnouncementDetail = async (item) => {
  try {
    const res = await getAnnouncementDetail(item.id)
    noticeDetail.value = res.data || {}
    noticeDetailVisible.value = true
    noticePopoverVisible.value = false
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
  userMenuVisible.value = false
  if (command === 'logout') {
    executeLogout()
    return
  }
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'password') {
    router.push('/profile?tab=password')
  }
}

watch(
  () => route.fullPath,
  () => {
    noticePopoverVisible.value = false
    userMenuVisible.value = false
    mobileMenuVisible.value = false
    fetchNotificationSummary()
  }
)

watch(
  () => userInfo.value?.id,
  () => {
    loadReadAnnouncements()
    fetchNotificationSummary({ force: true })
  }
)

watch(
  tableDensity,
  (next) => {
    document.body.setAttribute('data-density', next)
  },
  { immediate: true }
)

onMounted(() => {
  loadReadAnnouncements()
  fetchNotificationSummary({ force: true })
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>

<style scoped>
.menu-item {
  width: 100%;
  border-radius: 6px;
  padding: 6px 8px;
  text-align: left;
  font-size: 14px;
  color: #475569;
  transition: all 180ms ease;
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-divider {
  margin: 4px 0;
  border-top: 1px solid #e2e8f0;
}

.menu-item:hover {
  background: #f1f5f9;
  color: #1e2938;
}

.glass-lite {
  background: rgba(252, 253, 254, 0.66);
  border-color: rgba(226, 232, 240, 0.74);
  backdrop-filter: blur(14px) saturate(138%);
}
</style>
