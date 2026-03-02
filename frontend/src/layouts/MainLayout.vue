<template>
  <div class="flex min-h-screen">
    <aside
      v-if="!isMobile"
      class="hidden h-screen shrink-0 border-r border-slate-200/70 bg-ink-900 text-slate-100 md:flex md:flex-col"
      :style="{ width: isCollapse ? '84px' : `${sidebarExpandedWidth}px` }"
    >
      <div class="flex h-20 items-center gap-3 border-b border-slate-700/60 px-4">
        <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-brand-500/90 text-white shadow-lg">
          <School class="h-5 w-5" />
        </div>
        <div v-if="!isCollapse" class="min-w-0">
          <h1 class="truncate text-base font-semibold">Campus OS</h1>
          <p class="truncate text-xs text-slate-300">学生管理系统</p>
        </div>
      </div>

      <nav class="flex-1 overflow-y-auto px-3 py-4">
        <div v-for="group in visibleMenuGroups" :key="group.key" class="mb-5">
          <p v-if="!isCollapse" class="mb-2 px-2 text-xs font-semibold uppercase tracking-widest text-slate-400">
            {{ group.title }}
          </p>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            class="mb-1 flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition"
            :class="isActive(item.path)
              ? 'bg-brand-600 text-white shadow-md'
              : 'text-slate-200 hover:bg-slate-800 hover:text-white'"
          >
            <component :is="item.icon" class="h-4 w-4 shrink-0" />
            <span v-if="!isCollapse" class="truncate">{{ item.title }}</span>
          </router-link>
        </div>
      </nav>

      <div class="space-y-2 border-t border-slate-700/60 p-3">
        <button
          v-for="action in visibleBottomActions"
          :key="action.key"
          class="flex w-full items-center gap-3 rounded-xl px-3 py-2 text-sm text-slate-200 transition hover:bg-slate-800 hover:text-white"
          @click="handleBottomAction(action)"
        >
          <component :is="action.icon" class="h-4 w-4" />
          <span v-if="!isCollapse">{{ action.label }}</span>
        </button>
      </div>
    </aside>

    <TransitionRoot as="template" :show="mobileDrawerVisible">
      <Dialog as="div" class="relative z-40 md:hidden" @close="mobileDrawerVisible = false">
        <TransitionChild
          as="template"
          enter="transition-opacity duration-200"
          enter-from="opacity-0"
          enter-to="opacity-100"
          leave="transition-opacity duration-200"
          leave-from="opacity-100"
          leave-to="opacity-0"
        >
          <div class="fixed inset-0 bg-slate-950/45" />
        </TransitionChild>

        <div class="fixed inset-0 flex">
          <TransitionChild
            as="template"
            enter="transition duration-200 ease-out"
            enter-from="-translate-x-full"
            enter-to="translate-x-0"
            leave="transition duration-200 ease-in"
            leave-from="translate-x-0"
            leave-to="-translate-x-full"
          >
            <DialogPanel class="w-72 bg-ink-900 text-slate-100">
              <div class="flex h-20 items-center justify-between border-b border-slate-700/60 px-4">
                <div>
                  <h1 class="text-base font-semibold">Campus OS</h1>
                  <p class="text-xs text-slate-300">学生管理系统</p>
                </div>
                <button class="rounded-lg p-2 hover:bg-slate-800" @click="mobileDrawerVisible = false">
                  <X class="h-4 w-4" />
                </button>
              </div>

              <nav class="h-[calc(100%-80px)] overflow-y-auto p-3">
                <div v-for="group in visibleMenuGroups" :key="`mobile-${group.key}`" class="mb-5">
                  <p class="mb-2 px-2 text-xs font-semibold uppercase tracking-widest text-slate-400">{{ group.title }}</p>
                  <router-link
                    v-for="item in group.items"
                    :key="`mobile-${item.path}`"
                    :to="item.path"
                    class="mb-1 flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition"
                    :class="isActive(item.path)
                      ? 'bg-brand-600 text-white shadow-md'
                      : 'text-slate-200 hover:bg-slate-800 hover:text-white'"
                    @click="mobileDrawerVisible = false"
                  >
                    <component :is="item.icon" class="h-4 w-4" />
                    <span>{{ item.title }}</span>
                  </router-link>
                </div>
              </nav>
            </DialogPanel>
          </TransitionChild>
        </div>
      </Dialog>
    </TransitionRoot>

    <div class="min-w-0 flex-1">
      <header class="mx-3 mt-3 flex h-14 items-center justify-between rounded-2xl border border-slate-200 bg-white/85 px-4 shadow-soft backdrop-blur">
        <div class="flex min-w-0 items-center gap-3">
          <button class="rounded-lg p-2 text-slate-600 hover:bg-slate-100" @click="handleMenuToggle">
            <PanelLeft v-if="!isMobile" class="h-4 w-4" />
            <MenuIcon v-else class="h-4 w-4" />
          </button>
          <breadcrumb class="truncate" />
        </div>

        <div class="flex items-center gap-2">
          <div class="relative">
            <button
              class="relative rounded-lg p-2 text-slate-600 hover:bg-slate-100"
              @click="toggleNoticePopover"
            >
              <Bell class="h-4 w-4" />
              <span
                v-if="notificationCount > 0"
                class="absolute -right-1 -top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-brand-600 px-1 text-[10px] text-white"
              >
                {{ notificationCount > 99 ? '99+' : notificationCount }}
              </span>
            </button>
            <div
              v-if="noticePopoverVisible"
              class="absolute right-0 z-30 mt-2 w-80 rounded-2xl border border-slate-200 bg-white p-3 shadow-panel"
            >
              <div class="mb-2 flex items-center justify-between">
                <p class="text-sm font-semibold text-ink-900">最新公告</p>
                <button class="text-xs text-brand-700 hover:underline" @click="openAnnouncementPage">查看全部</button>
              </div>
              <div v-if="!latestAnnouncements.length" class="py-6 text-center text-sm text-slate-500">暂无公告</div>
              <div v-else class="max-h-60 space-y-1 overflow-y-auto">
                <button
                  v-for="item in latestAnnouncements"
                  :key="item.id"
                  class="block w-full rounded-lg px-2 py-2 text-left hover:bg-slate-100"
                  @click="openAnnouncementDetail(item)"
                >
                  <div class="truncate text-sm text-ink-800">{{ item.title }}</div>
                  <div class="mt-1 text-xs text-slate-500">{{ item.createTime || '-' }}</div>
                </button>
              </div>
            </div>
          </div>

          <Menu as="div" class="relative">
            <MenuButton class="flex items-center gap-2 rounded-full border border-slate-200 bg-white px-2 py-1 hover:bg-slate-50">
              <img :src="userInfo.avatar || defaultAvatar" alt="avatar" class="h-7 w-7 rounded-full object-cover" />
              <span class="hidden max-w-28 truncate text-sm text-ink-700 md:block">{{ userInfo.realName || userInfo.username }}</span>
              <ChevronDown class="h-4 w-4 text-slate-500" />
            </MenuButton>
            <Transition
              enter-active-class="transition duration-100 ease-out"
              enter-from-class="transform scale-95 opacity-0"
              enter-to-class="transform scale-100 opacity-100"
              leave-active-class="transition duration-75 ease-in"
              leave-from-class="transform scale-100 opacity-100"
              leave-to-class="transform scale-95 opacity-0"
            >
              <MenuItems class="absolute right-0 z-30 mt-2 w-44 rounded-xl border border-slate-200 bg-white p-1 shadow-panel focus:outline-none">
                <MenuItem v-slot="{ active }">
                  <button class="w-full rounded-lg px-3 py-2 text-left text-sm" :class="active ? 'bg-slate-100' : ''" @click="handleCommand('profile')">个人中心</button>
                </MenuItem>
                <MenuItem v-slot="{ active }">
                  <button class="w-full rounded-lg px-3 py-2 text-left text-sm" :class="active ? 'bg-slate-100' : ''" @click="handleCommand('password')">修改密码</button>
                </MenuItem>
                <MenuItem v-slot="{ active }">
                  <button class="w-full rounded-lg px-3 py-2 text-left text-sm text-rose-600" :class="active ? 'bg-rose-50' : ''" @click="handleCommand('logout')">退出登录</button>
                </MenuItem>
              </MenuItems>
            </Transition>
          </Menu>
        </div>
      </header>

      <main class="p-3">
        <div class="min-h-[calc(100vh-5.25rem)] rounded-2xl border border-slate-200 bg-white/70 shadow-soft">
          <router-view v-slot="{ Component, route: activeRoute }">
            <Transition name="app-fade" mode="out-in">
              <component :is="Component" :key="activeRoute.fullPath" />
            </Transition>
          </router-view>
        </div>
      </main>
    </div>

    <AppModal v-model="noticeDetailVisible" title="公告详情" width="720px">
      <div class="grid gap-3 text-sm text-slate-700 md:grid-cols-2">
        <div class="rounded-xl bg-slate-100 p-3 md:col-span-2"><strong>标题：</strong>{{ noticeDetail.title || '-' }}</div>
        <div class="rounded-xl bg-slate-100 p-3"><strong>类型：</strong>{{ noticeDetail.type || '-' }}</div>
        <div class="rounded-xl bg-slate-100 p-3"><strong>目标角色：</strong>{{ noticeDetail.targetRole || '-' }}</div>
        <div class="rounded-xl bg-slate-100 p-3 md:col-span-2"><strong>发布时间：</strong>{{ noticeDetail.createTime || '-' }}</div>
      </div>
      <div class="mt-4 rounded-xl border border-slate-200 p-4 text-sm leading-7 text-slate-700">
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
import { Dialog, DialogPanel, Menu, MenuButton, MenuItem, MenuItems, TransitionChild, TransitionRoot } from '@headlessui/vue'
import { Bell, ChevronDown, Menu as MenuIcon, PanelLeft, School, X } from 'lucide-vue-next'
import { ElMessage, ElMessageBox } from 'element-plus'
import Breadcrumb from '@/components/Breadcrumb.vue'
import AppModal from '@/components/ui/AppModal.vue'
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
const noticePopoverVisible = ref(false)
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
  if (width >= 1680) return 252
  if (width >= 1366) return 232
  return 214
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
      if (hiddenRoles.includes(role.value)) return

      const key = item.meta?.menuGroup || 'process'
      if (!buckets.has(key)) {
        const groupMeta = menuGroupMeta[key] || { title: '其他', order: 99 }
        buckets.set(key, { key, title: groupMeta.title, order: groupMeta.order, items: [] })
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
    .map((group) => ({ ...group, items: group.items.filter((item) => hasRoutePermission(item.routeName)) }))
    .filter((group) => group.items.length > 0)
)

const visibleBottomActions = computed(() =>
  bottomActions.filter((item) => item.type !== 'route' || hasRoutePermission(item.routeName))
)

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
  isMobile.value = window.innerWidth < 992
  if (!isMobile.value) {
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
  if (noticePopoverVisible.value) {
    await fetchNotificationSummary()
  }
}

const openAnnouncementPage = () => {
  noticeDetailVisible.value = false
  noticePopoverVisible.value = false
  mobileDrawerVisible.value = false
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

const handleBottomAction = (action) => {
  if (isMobile.value) {
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

watch(
  () => route.fullPath,
  () => {
    noticePopoverVisible.value = false
    if (isMobile.value) {
      mobileDrawerVisible.value = false
    }
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

onMounted(() => {
  loadReadAnnouncements()
  fetchNotificationSummary()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})
</script>
