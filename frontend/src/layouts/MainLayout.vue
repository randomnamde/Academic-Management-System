<template>
  <div class="min-h-screen bg-neutralx-50 text-slatex-900">
    <div class="grid min-h-screen" :style="gridStyle">
      <aside
        class="hidden border-r border-neutralx-200 bg-panel md:flex md:flex-col"
        :style="{ width: isCollapsed ? '72px' : '236px' }"
      >
        <div class="flex h-14 items-center border-b border-neutralx-200 px-4">
          <h1 v-if="!isCollapsed" class="text-[13px] font-semibold tracking-tight text-primary-900">Academic OS</h1>
          <span v-else class="text-[12px] font-semibold text-primary-900">AO</span>
        </div>

        <nav class="flex-1 overflow-y-auto p-2">
          <section v-for="group in visibleMenuGroups" :key="group.key" class="mb-4">
            <p v-if="!isCollapsed" class="mb-1 px-2 text-[11px] font-medium uppercase tracking-[0.08em] text-slatex-500">
              {{ group.title }}
            </p>
            <router-link
              v-for="item in group.items"
              :key="item.path"
              :to="item.path"
              class="mb-1 flex h-8 items-center gap-2 rounded-sm px-2 text-[13px] font-medium transition-all duration-180"
              :class="isActive(item.path)
                ? 'bg-neutralx-100 text-primary-800'
                : 'text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900'"
              :title="isCollapsed ? item.title : ''"
            >
              <component :is="item.icon || 'Menu'" class="h-3.5 w-3.5 shrink-0" />
              <span v-if="!isCollapsed" class="truncate">{{ item.title }}</span>
            </router-link>
          </section>
        </nav>

        <div class="border-t border-neutralx-200 p-2">
          <button
            class="flex h-8 w-full items-center gap-2 rounded-sm px-2 text-[13px] text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900"
            @click="handleCommand('profile')"
          >
            <User class="h-3.5 w-3.5" />
            <span v-if="!isCollapsed">个人中心</span>
          </button>
          <button
            class="mt-1 flex h-8 w-full items-center gap-2 rounded-sm px-2 text-[13px] text-state-danger hover:bg-red-50"
            @click="handleCommand('logout')"
          >
            <LogOut class="h-3.5 w-3.5" />
            <span v-if="!isCollapsed">退出登录</span>
          </button>
        </div>
      </aside>

      <section class="min-w-0">
        <header class="sticky top-0 z-20 flex h-14 items-center justify-between border-b border-neutralx-200 bg-panel px-3 md:px-5">
          <div class="flex min-w-0 items-center gap-2">
            <button
              class="inline-flex h-8 w-8 items-center justify-center rounded-sm border border-neutralx-200 bg-white text-slatex-600 hover:bg-neutralx-100"
              @click="toggleSidebar"
            >
              <PanelLeft class="h-3.5 w-3.5" />
            </button>
            <Breadcrumb class="min-w-0" />
          </div>

          <div class="flex items-center gap-2">
            <button
              class="inline-flex h-8 items-center gap-1 rounded-sm border border-neutralx-200 bg-white px-2 text-[12px] text-slatex-600 hover:bg-neutralx-100"
              @click="toggleDensity"
            >
              <Rows4 class="h-3.5 w-3.5" />
              {{ tableDensity === 'compact' ? '紧凑' : '标准' }}
            </button>

            <div class="relative">
              <button
                class="relative inline-flex h-8 w-8 items-center justify-center rounded-sm border border-neutralx-200 bg-white text-slatex-600 hover:bg-neutralx-100"
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
                class="absolute right-0 z-30 mt-2 w-80 rounded-md border border-neutralx-200 bg-white p-2 shadow-pop"
              >
                <div class="mb-1 flex items-center justify-between px-1">
                  <p class="text-[12px] font-semibold text-primary-900">最新公告</p>
                  <button class="text-[11px] text-primary-700 hover:underline" @click="openAnnouncementPage">查看全部</button>
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
                class="inline-flex h-8 items-center gap-2 rounded-sm border border-neutralx-200 bg-white px-2 text-[12px] text-slatex-700 hover:bg-neutralx-100"
                @click="userMenuVisible = !userMenuVisible"
              >
                <img :src="userInfo.avatar || defaultAvatar" alt="avatar" class="h-5 w-5 rounded-sm object-cover" />
                <span class="hidden max-w-24 truncate md:block">{{ userInfo.realName || userInfo.username || '用户' }}</span>
                <ChevronDown class="h-3.5 w-3.5 text-slatex-500" />
              </button>

              <div
                v-if="userMenuVisible"
                class="absolute right-0 z-30 mt-2 w-36 rounded-md border border-neutralx-200 bg-white p-1 shadow-pop"
              >
                <button class="menu-item" @click="handleCommand('profile')">个人中心</button>
                <button class="menu-item" @click="handleCommand('password')">修改密码</button>
                <button class="menu-item text-state-danger" @click="handleCommand('logout')">退出登录</button>
              </div>
            </div>
          </div>
        </header>

        <main class="p-3 md:p-4">
          <router-view v-slot="{ Component, route: activeRoute }">
            <Transition name="app-fade" mode="out-in">
              <component :is="Component" :key="activeRoute.fullPath" />
            </Transition>
          </router-view>
        </main>
      </section>
    </div>

    <Transition name="app-fade">
      <div v-if="mobileMenuVisible" class="fixed inset-0 z-40 md:hidden">
        <div class="absolute inset-0 bg-slatex-900/35" @click="mobileMenuVisible = false"></div>
        <div class="absolute left-0 top-0 h-full w-72 border-r border-neutralx-200 bg-panel p-2">
          <div class="mb-2 flex h-10 items-center justify-between border-b border-neutralx-200 px-2">
            <h1 class="text-[13px] font-semibold tracking-tight text-primary-900">Academic OS</h1>
            <button class="rounded-sm px-2 py-1 text-[12px] text-slatex-600 hover:bg-neutralx-100" @click="mobileMenuVisible = false">关闭</button>
          </div>
          <section v-for="group in visibleMenuGroups" :key="`mobile-${group.key}`" class="mb-4">
            <p class="mb-1 px-2 text-[11px] font-medium uppercase tracking-[0.08em] text-slatex-500">
              {{ group.title }}
            </p>
            <router-link
              v-for="item in group.items"
              :key="`mobile-${item.path}`"
              :to="item.path"
              class="mb-1 flex h-8 items-center gap-2 rounded-sm px-2 text-[13px] font-medium transition-all duration-180"
              :class="isActive(item.path)
                ? 'bg-neutralx-100 text-primary-800'
                : 'text-slatex-600 hover:bg-neutralx-100 hover:text-slatex-900'"
              @click="mobileMenuVisible = false"
            >
              <component :is="item.icon || 'Menu'" class="h-3.5 w-3.5 shrink-0" />
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
import { Bell, ChevronDown, LogOut, PanelLeft, Rows4, User } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
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
  overview: 'Overview',
  teaching: 'Teaching',
  assessment: 'Assessment',
  access: 'Access Control'
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
          title: groupLabel[key] || 'Others',
          items: []
        })
      }

      groups.get(key).items.push({
        title: item.meta?.title || routeName,
        path: item.path.startsWith('/') ? item.path : `/${item.path}`,
        icon: item.meta?.icon || 'Menu'
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
    const res = await getAnnouncementList({ page: 1, size: 8, status: 1 })
    latestAnnouncements.value = res.data?.records || []
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
    await fetchNotificationSummary()
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
    syncNotificationCount()
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
  fetchNotificationSummary()
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
  font-size: 12px;
  color: #475569;
  transition: all 180ms ease;
}

.menu-item:hover {
  background: #f1f5f9;
  color: #1e2938;
}
</style>
