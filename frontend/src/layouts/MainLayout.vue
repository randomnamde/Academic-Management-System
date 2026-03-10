<template>
  <div class="layout-shell h-screen overflow-hidden text-slatex-900">
    <a href="#main-content" class="skip-link">{{ t('app.skipToMain') }}</a>

    <div class="grid h-full" :style="gridStyle">
      <aside
        class="layout-sidebar hidden h-screen border-r md:flex md:flex-col"
        :style="{ width: isCollapsed ? '96px' : '252px' }"
      >
        <div class="sidebar-brand sidebar-divider-bottom" :class="isCollapsed ? 'is-collapsed' : 'is-expanded'">
          <div class="brand-mark inline-flex h-8 w-8 items-center justify-center rounded-md text-[var(--accent-700)]">
            <Shield class="h-4 w-4" />
          </div>
          <h1 v-if="!isCollapsed" class="sidebar-brand-text text-[13px] font-semibold tracking-tight text-primary-900">Academic Management System</h1>
          <span v-else class="sidebar-brand-short text-[12px] font-semibold text-primary-900">AMS</span>
        </div>

        <nav class="flex-1 overflow-y-auto px-2 py-3">
          <section v-for="group in visibleMenuGroups" :key="group.key" class="mb-5">
            <button
              v-if="!isCollapsed"
              class="menu-group-title menu-group-toggle touch-target"
              type="button"
              :aria-expanded="isGroupExpanded(group.key) ? 'true' : 'false'"
              @click="toggleGroup(group.key)"
            >
              <component :is="group.icon || LayoutGrid" class="h-3.5 w-3.5" />
              <span>{{ group.title }}</span>
              <ChevronDown class="menu-group-caret h-3.5 w-3.5" :class="isGroupExpanded(group.key) ? 'is-open' : ''" />
            </button>

            <div
              v-show="isGroupExpanded(group.key)"
              :class="['menu-children', isCollapsed ? 'is-collapsed' : '']"
            >
              <router-link
                v-for="item in group.items"
                :key="item.path"
                :to="item.path"
                class="menu-link touch-target"
                :class="[isActive(item.path) ? 'is-active' : '', isCollapsed ? 'is-collapsed-item' : '']"
                :title="isCollapsed ? item.title : ''"
              >
                <span class="menu-rail" aria-hidden="true"></span>
                <component :is="item.icon || EpMenu" class="h-4 w-4 shrink-0" />
                <span v-if="!isCollapsed" class="menu-label">{{ item.title }}</span>
              </router-link>
            </div>
          </section>
        </nav>

        <div class="sidebar-divider-top sidebar-account p-2">
          <button class="menu-link sidebar-account-action touch-target" @click="handleCommand('profile')">
            <User class="h-4 w-4" />
            <span v-if="!isCollapsed" class="menu-label">{{ t('layout.profile') }}</span>
          </button>
          <button class="menu-link sidebar-account-action touch-target is-danger mt-1" @click="handleCommand('logout')">
            <LogOut class="h-4 w-4" />
            <span v-if="!isCollapsed" class="menu-label">{{ t('layout.logout') }}</span>
          </button>
        </div>
      </aside>

      <section class="layout-main flex h-screen min-w-0 flex-col">
        <header class="layout-header z-20 flex h-16 shrink-0 items-center gap-3 border-b px-3 md:px-5">
          <div class="header-zone nav-zone flex min-w-0 flex-1 items-center gap-2">
            <button
              class="header-ctl-btn touch-target"
              :aria-label="mobileMenuVisible ? t('layout.collapseNav') : t('layout.expandNav')"
              @click="toggleSidebar"
            >
              <PanelLeft class="h-4 w-4" />
            </button>
            <Breadcrumb class="min-w-0" />
          </div>

          <div class="header-zone quick-zone hidden items-center gap-2 lg:flex">
            <button
              class="header-chip touch-target"
              :aria-label="t('layout.densitySwitch')"
              @click="toggleDensity"
            >
              <Rows4 class="h-3.5 w-3.5" />
              {{ tableDensity === 'compact' ? t('layout.densityCompact') : t('layout.densityStandard') }}
            </button>
          </div>

          <div class="header-zone user-zone flex items-center gap-2">
            <button
              class="header-ctl-btn touch-target"
              :aria-label="t('layout.languageToggle')"
              :title="`${t('layout.languageToggle')} (${languageLabel})`"
              @click="toggleLanguage"
            >
              <Languages class="h-4 w-4" />
            </button>

            <button
              class="header-ctl-btn touch-target"
              :aria-label="t('layout.themeToggle')"
              :title="`${t('layout.themeToggle')} (${currentThemeLabel})`"
              @click="cycleThemeMode"
            >
              <component :is="currentThemeIcon" class="h-4 w-4" />
            </button>

            <div class="relative">
              <button
                class="header-ctl-btn relative touch-target"
                :aria-label="t('layout.notifications')"
                :aria-expanded="noticePopoverVisible ? 'true' : 'false'"
                aria-haspopup="menu"
                @click="toggleNoticePopover"
              >
                <Bell class="h-4 w-4" />
                <span
                  v-if="notificationCount > 0"
                  class="notice-count absolute -right-1 -top-1 inline-flex h-4 min-w-4 items-center justify-center rounded-md px-1 text-[10px] text-white"
                >
                  {{ notificationCount > 99 ? '99+' : notificationCount }}
                </span>
              </button>

              <div v-if="noticePopoverVisible" class="notice-popover absolute right-0 z-30 mt-2 w-96">
                <div class="mb-1 flex items-center justify-between px-1">
                  <p class="text-[12px] font-semibold text-primary-900">{{ t('layout.latestAnnouncement') }}</p>
                  <button class="text-[11px] text-[var(--accent-600)] hover:underline touch-target" @click="openAnnouncementPage">{{ t('layout.viewAll') }}</button>
                </div>
                <div v-if="!latestAnnouncements.length" class="py-6 text-center text-[12px] text-slatex-500">{{ t('layout.noAnnouncement') }}</div>
                <div v-else class="max-h-72 space-y-1 overflow-y-auto">
                  <button
                    v-for="item in latestAnnouncements"
                    :key="item.id"
                    class="notice-card block w-full text-left"
                    @click="openAnnouncementDetail(item)"
                  >
                    <div class="line-clamp-1 text-[13px] font-medium text-slatex-900">{{ item.title }}</div>
                    <div class="mt-1 flex items-center gap-1.5 text-[11px] text-slatex-500">
                      <span class="app-tag-info">{{ getTypeLabel(item.type) }}</span>
                      <span class="app-tag-info">{{ getPriorityLabel(item.priority) }}</span>
                      <span>{{ item.createTime || '-' }}</span>
                    </div>
                  </button>
                </div>
              </div>
            </div>

            <div class="relative">
              <button
                class="header-chip touch-target"
                :aria-label="t('layout.openUserMenu')"
                :aria-expanded="userMenuVisible ? 'true' : 'false'"
                aria-haspopup="menu"
                @click="userMenuVisible = !userMenuVisible"
              >
                <img :src="userInfo.avatar || defaultAvatar" alt="avatar" class="h-6 w-6 rounded-md object-cover" />
                <span class="hidden max-w-24 truncate md:block">{{ userInfo.realName || userInfo.account || userInfo.username || t('common.user') }}</span>
                <ChevronDown class="h-3.5 w-3.5 text-slatex-500" />
              </button>

              <div v-if="userMenuVisible" class="user-popover absolute right-0 z-30 mt-2 w-44" role="menu">
                <div class="menu-section">
                  <button class="menu-item touch-target" role="menuitem" @click="handleCommand('profile')">{{ t('layout.profile') }}</button>
                  <button class="menu-item touch-target" role="menuitem" @click="handleCommand('password')">{{ t('layout.changePassword') }}</button>
                </div>
                <div class="menu-divider"></div>
                <div class="menu-section">
                  <button class="menu-item touch-target text-state-danger" role="menuitem" @click="handleCommand('logout')">{{ t('layout.logout') }}</button>
                </div>
              </div>
            </div>
          </div>
        </header>

        <main id="main-content" tabindex="-1" class="flex-1 overflow-y-auto p-3 md:p-4">
          <router-view v-slot="{ Component, route: activeRoute }">
            <Transition name="page-motion" mode="out-in">
              <KeepAlive v-if="activeRoute.meta?.keepAlive !== false">
                <component
                  :is="Component"
                  :key="String(activeRoute.name || activeRoute.path)"
                />
              </KeepAlive>
              <component
                :is="Component"
                v-else
                :key="String(activeRoute.name || activeRoute.path)"
              />
            </Transition>
          </router-view>
        </main>
      </section>
    </div>

    <Transition name="app-fade">
      <div v-if="mobileMenuVisible" class="fixed inset-0 z-40 md:hidden">
        <div class="absolute inset-0 bg-slatex-900/45" @click="mobileMenuVisible = false"></div>
        <div class="layout-sidebar absolute left-0 top-0 h-full w-72 border-r p-2">
          <div class="mobile-sidebar-brand sidebar-divider-bottom mb-2">
            <div class="mobile-sidebar-brand-main">
              <div class="brand-mark inline-flex h-7 w-7 items-center justify-center rounded-md text-[var(--accent-700)]">
                <Shield class="h-3.5 w-3.5" />
              </div>
              <h1 class="sidebar-brand-text text-[13px] font-semibold tracking-tight text-primary-900">Academic Management System</h1>
            </div>
            <button class="mobile-sidebar-close rounded-md px-2 py-1 text-[12px] text-slatex-600 hover:bg-neutralx-100 touch-target" @click="mobileMenuVisible = false">{{ t('layout.mobileClose') }}</button>
          </div>
          <section v-for="group in visibleMenuGroups" :key="`mobile-${group.key}`" class="mb-4">
            <button
              class="menu-group-title menu-group-toggle mb-1 touch-target"
              type="button"
              :aria-expanded="isGroupExpanded(group.key) ? 'true' : 'false'"
              @click="toggleGroup(group.key)"
            >
              <component :is="group.icon || LayoutGrid" class="h-3.5 w-3.5" />
              <span>{{ group.title }}</span>
              <ChevronDown class="menu-group-caret h-3.5 w-3.5" :class="isGroupExpanded(group.key) ? 'is-open' : ''" />
            </button>
            <div v-show="isGroupExpanded(group.key)" class="menu-children">
              <router-link
                v-for="item in group.items"
                :key="`mobile-${item.path}`"
                :to="item.path"
                class="menu-link touch-target"
                :class="isActive(item.path) ? 'is-active' : ''"
                @click="mobileMenuVisible = false"
              >
                <span class="menu-rail" aria-hidden="true"></span>
                <component :is="item.icon || EpMenu" class="h-4 w-4 shrink-0" />
                <span class="menu-label">{{ item.title }}</span>
              </router-link>
            </div>
          </section>
        </div>
      </div>
    </Transition>

    <AppModal v-model="noticeDetailVisible" :title="t('layout.noticeDialog.title')" width="720px">
      <div class="grid gap-2 text-[13px] text-slatex-700 md:grid-cols-2">
        <div class="rounded-md border border-neutralx-200 bg-neutralx-100 px-2 py-1.5 md:col-span-2"><strong>{{ t('layout.noticeDialog.fieldTitle') }}</strong>{{ noticeDetail.title || '-' }}</div>
        <div class="rounded-md border border-neutralx-200 bg-neutralx-100 px-2 py-1.5"><strong>{{ t('layout.noticeDialog.fieldType') }}</strong>{{ noticeDetail.type || '-' }}</div>
        <div class="rounded-md border border-neutralx-200 bg-neutralx-100 px-2 py-1.5"><strong>{{ t('layout.noticeDialog.fieldTargetRole') }}</strong>{{ noticeDetail.targetRole || '-' }}</div>
        <div class="rounded-md border border-neutralx-200 bg-neutralx-100 px-2 py-1.5 md:col-span-2"><strong>{{ t('layout.noticeDialog.fieldCreateTime') }}</strong>{{ noticeDetail.createTime || '-' }}</div>
      </div>
      <div class="mt-3 rounded-md border border-neutralx-200 p-3 text-[13px] leading-6 text-slatex-700">
        {{ noticeDetail.content || t('layout.noticeDialog.noContent') }}
      </div>
      <template #footer>
        <button class="app-btn-secondary" @click="noticeDetailVisible = false">{{ t('common.close') }}</button>
        <button class="app-btn-primary" @click="openAnnouncementPage">{{ t('layout.goAnnouncementList') }}</button>
      </template>
    </AppModal>

    <AppModal v-model="logoutDialogVisible" :title="t('layout.logoutDialog.title')" width="460px">
      <div class="logout-dialog">
        <div class="logout-dialog__surface">
          <div class="logout-dialog__badge">
            <ShieldCheck class="h-3.5 w-3.5" />
            <span>{{ t('layout.logoutDialog.badge') }}</span>
          </div>

          <div class="logout-dialog__hero">
            <div class="logout-dialog__icon" aria-hidden="true">
              <LogOut class="h-5 w-5" />
            </div>
            <div class="logout-dialog__copy">
              <p class="logout-dialog__headline">{{ t('layout.logoutDialog.headline') }}</p>
              <p class="logout-dialog__description">{{ t('layout.logoutDialog.description') }}</p>
            </div>
          </div>

          <div class="logout-dialog__account">
            <div class="logout-dialog__account-avatar-wrap">
              <img :src="userInfo.avatar || defaultAvatar" alt="avatar" class="logout-dialog__account-avatar" />
            </div>
            <div class="logout-dialog__account-copy">
              <p class="logout-dialog__account-label">{{ t('layout.logoutDialog.currentAccount') }}</p>
              <p class="logout-dialog__account-name">{{ logoutDisplayName }}</p>
              <p class="logout-dialog__account-meta">{{ logoutAccountMeta }}</p>
            </div>
          </div>

          <div class="logout-dialog__note">
            <Shield class="h-4 w-4 shrink-0" />
            <span>{{ t('layout.logoutDialog.note') }}</span>
          </div>
        </div>
      </div>
      <template #footer>
        <button class="app-btn-secondary" @click="logoutDialogVisible = false">{{ t('common.cancel') }}</button>
        <button class="app-btn-danger" @click="confirmLogout">{{ t('layout.logoutDialog.confirmAction') }}</button>
      </template>
    </AppModal>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useStore } from 'vuex'
import { useRoute, useRouter } from 'vue-router'
import {
  Bell,
  BookOpenCheck,
  ChevronDown,
  GraduationCap,
  LayoutGrid,
  Languages,
  LogOut,
  Monitor,
  Moon,
  PanelLeft,
  Rows4,
  Shield,
  ShieldCheck,
  SunMedium,
  User
} from 'lucide-vue-next'
import { ElMessage } from 'element-plus'
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
import { useTheme } from '@/composables/useTheme'
import { useLanguage } from '@/composables/useLanguage'
import { useI18n } from 'vue-i18n'

const store = useStore()
const route = useRoute()
const router = useRouter()
const { t } = useI18n()
const { mode: themeMode, cycleThemeMode } = useTheme(store)
const { language, toggleLanguage } = useLanguage(store)

const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const viewportWidth = ref(window.innerWidth)
const notificationCount = ref(0)
const latestAnnouncements = ref([])
const lastNotificationFetchAt = ref(0)
const noticePopoverVisible = ref(false)
const noticeDetailVisible = ref(false)
const logoutDialogVisible = ref(false)
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

const themeIconMap = {
  system: Monitor,
  light: SunMedium,
  dark: Moon
}

const currentThemeIcon = computed(() => themeIconMap[themeMode.value] || Monitor)
const currentThemeLabel = computed(() => t(`theme.mode.${themeMode.value}`))
const languageLabel = computed(() => (language.value === 'en-US' ? t('language.enUS') : t('language.zhCN')))

const userInfo = computed(() => store.state.userInfo || {})
const logoutDisplayName = computed(() => userInfo.value?.realName || userInfo.value?.account || userInfo.value?.username || t('common.user'))
const logoutAccountMeta = computed(() => userInfo.value?.account || userInfo.value?.username || t('layout.logoutDialog.sessionMeta'))
const isAuthenticated = computed(() => Boolean(store.state.token))
const role = computed(() => userInfo.value?.primaryRole || userInfo.value?.role || '')
const permissions = computed(() => userInfo.value?.permissions || [])
const sidebarOpened = computed(() => store.state.sidebar?.opened !== false)
const tableDensity = computed(() => store.state.uiPreference?.tableDensity || 'compact')
const isCollapsed = computed(() => !sidebarOpened.value)
const readAnnouncementStorageKey = computed(() => `announcement:read:${userInfo.value?.id || 'guest'}`)
const collapsedGroupKeys = ref([])

const groupTitleKey = {
  overview: 'menuGroup.overview',
  teaching: 'menuGroup.teaching',
  assessment: 'menuGroup.assessment',
  access: 'menuGroup.access'
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
    .filter((item) => {
      const hasDisplayTitle = Boolean(item?.meta?.titleKey || item?.meta?.title)
      return hasDisplayTitle && item.path !== 'profile' && item?.meta?.hideInMenu !== true
    })
    .forEach((item) => {
      const hiddenRoles = Array.isArray(item.meta?.hideInMenuForRoles) ? item.meta.hideInMenuForRoles : []
      if (hiddenRoles.includes(role.value)) return

      const routeName = String(item.name || '')
      if (!canRoute(role.value, routeName, permissions.value)) return

      const key = item.meta?.menuGroup || 'teaching'
      if (!groups.has(key)) {
        groups.set(key, {
          key,
          title: t(groupTitleKey[key] || 'menuGroup.other'),
          icon: groupIcon[key] || LayoutGrid,
          items: []
        })
      }

      groups.get(key).items.push({
        title: item.meta?.titleKey ? t(item.meta.titleKey) : (item.meta?.title || routeName),
        path: item.path.startsWith('/') ? item.path : `/${item.path}`,
        icon: resolveRouteIcon(item.meta?.icon)
      })
    })

  const groupOrder = { overview: 1, teaching: 2, assessment: 3, access: 4 }
  return Array.from(groups.values()).sort((left, right) => (groupOrder[left.key] || 99) - (groupOrder[right.key] || 99))
})

const isGroupExpanded = (groupKey) => isCollapsed.value || !collapsedGroupKeys.value.includes(groupKey)

const toggleGroup = (groupKey) => {
  if (!groupKey) return
  if (collapsedGroupKeys.value.includes(groupKey)) {
    collapsedGroupKeys.value = collapsedGroupKeys.value.filter((key) => key !== groupKey)
    return
  }
  collapsedGroupKeys.value = [...collapsedGroupKeys.value, groupKey]
}

const ensureActiveGroupExpanded = () => {
  const activeGroup = visibleMenuGroups.value.find((group) => group.items.some((item) => isActive(item.path)))
  if (!activeGroup) return
  collapsedGroupKeys.value = collapsedGroupKeys.value.filter((key) => key !== activeGroup.key)
}

const gridStyle = computed(() => ({
  gridTemplateColumns: viewportWidth.value < 768 ? '1fr' : `${isCollapsed.value ? 96 : 252}px minmax(0, 1fr)`
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

const isActive = (path) => (route.meta?.activeMenu || route.path) === path

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

const getTypeLabel = (type) => {
  const map = {
    SYSTEM: t('layout.noticeType.system'),
    NOTICE: t('layout.noticeType.notice'),
    ALERT: t('layout.noticeType.alert')
  }
  return map[type] || type || t('layout.notifications')
}

const getPriorityLabel = (priority) => {
  const value = Number(priority)
  if (Number.isNaN(value)) return t('layout.priority.normal')
  if (value >= 8) return t('layout.priority.high')
  if (value >= 5) return t('layout.priority.medium')
  return t('layout.priority.normal')
}

const fetchNotificationSummary = async ({ force = false } = {}) => {
  if (!isAuthenticated.value) {
    latestAnnouncements.value = []
    notificationCount.value = 0
    return
  }

  const now = Date.now()
  if (!force && now - lastNotificationFetchAt.value < notificationCacheTtl && latestAnnouncements.value.length) {
    syncNotificationCount()
    return
  }

  try {
    const res = await getAnnouncementList({ page: 1, size: 8, status: 1 }, { silent: true })
    latestAnnouncements.value = res.data?.records || []
    lastNotificationFetchAt.value = now
    syncNotificationCount()
  } catch (_e) {
    latestAnnouncements.value = []
    notificationCount.value = 0
  }
}

const toggleNoticePopover = async () => {
  if (!isAuthenticated.value) return
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
    ElMessage.error(t('layout.announcementDetailFailed'))
  }
}

const executeLogout = () => {
  logoutDialogVisible.value = true
}

const confirmLogout = () => {
  logoutDialogVisible.value = false
  store.dispatch('logout')
  router.push('/login')
  ElMessage.success(t('layout.logoutSuccess'))
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
    logoutDialogVisible.value = false
    userMenuVisible.value = false
    mobileMenuVisible.value = false
    fetchNotificationSummary()
    ensureActiveGroupExpanded()
  }
)

watch(
  visibleMenuGroups,
  (groups) => {
    const validGroupKeys = new Set(groups.map((group) => group.key))
    collapsedGroupKeys.value = collapsedGroupKeys.value.filter((key) => validGroupKeys.has(key))
    ensureActiveGroupExpanded()
  },
  { immediate: true }
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
.layout-shell {
  background:
    radial-gradient(circle at 8% -6%, color-mix(in srgb, var(--accent-500) 16%, transparent), transparent 45%),
    radial-gradient(circle at 96% 2%, color-mix(in srgb, var(--accent-500) 10%, transparent), transparent 52%),
    var(--bg-base);
}

.layout-sidebar,
.layout-header {
  border-color: color-mix(in srgb, var(--panel-border) 84%, transparent);
  background: var(--panel-glass);
  backdrop-filter: blur(12px) saturate(130%);
}

:root[data-theme='dark'] .layout-sidebar,
:root[data-theme='dark'] .layout-header {
  backdrop-filter: blur(10px) saturate(120%);
}

.notice-popover,
.user-popover {
  border-color: var(--popup-border);
  background: var(--surface-popup);
  backdrop-filter: blur(var(--popup-blur)) saturate(var(--popup-saturate));
}

.layout-sidebar,
.layout-header {
  box-shadow: var(--shadow-soft);
}

.brand-mark {
  border: 1px solid color-mix(in srgb, var(--accent-500) 32%, var(--panel-border));
  background: color-mix(in srgb, var(--surface-base) 84%, transparent);
  flex-shrink: 0;
  min-width: 32px;
  min-height: 32px;
}

.sidebar-brand {
  display: flex;
  align-items: center;
  min-height: 64px;
}

.sidebar-brand.is-expanded {
  justify-content: center;
  gap: 8px;
  padding-inline: 10px;
}

.sidebar-brand.is-collapsed {
  justify-content: center;
  gap: 6px;
  padding-inline: 6px;
}

.sidebar-brand-text {
  text-align: center;
  white-space: nowrap;
}

.sidebar-brand-short {
  display: inline-block;
  line-height: 1;
  letter-spacing: 0.01em;
  white-space: nowrap;
}

.sidebar-divider-bottom {
  border-bottom: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
}

.sidebar-divider-top {
  border-top: 1px solid color-mix(in srgb, var(--panel-border) 82%, transparent);
}

.menu-group-title {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 8px;
  margin-bottom: 8px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.01em;
  line-height: 1.25;
  color: color-mix(in srgb, var(--text-primary) 88%, var(--text-secondary));
}

.menu-group-toggle {
  width: 100%;
  border: 0;
  background: transparent;
  text-align: left;
  cursor: pointer;
}

.menu-group-caret {
  margin-left: auto;
  color: color-mix(in srgb, var(--text-secondary) 88%, transparent);
  transform: rotate(-90deg);
  transition: transform 180ms ease, color 180ms ease;
}

.menu-group-caret.is-open {
  transform: rotate(0deg);
  color: color-mix(in srgb, var(--text-primary) 88%, var(--text-secondary));
}

.menu-children {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-left: 10px;
  padding-left: 10px;
  border-left: 1px solid color-mix(in srgb, var(--text-secondary) 24%, transparent);
}

.menu-children.is-collapsed {
  margin-left: 0;
  padding-left: 0;
  border-left: none;
}

:root[data-theme='dark'] .menu-children {
  border-left-color: color-mix(in srgb, var(--text-secondary) 20%, transparent);
}

.menu-link {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  min-height: 36px;
  border: 1px solid transparent;
  border-radius: 10px;
  padding: 0 10px;
  color: var(--text-secondary);
  transition: all 180ms ease;
}

.menu-link.is-collapsed-item {
  justify-content: center;
  padding-inline: 0;
}

.menu-link.is-collapsed-item .menu-rail {
  display: none;
}

.menu-link:hover {
  background: color-mix(in srgb, var(--surface-elevated) 82%, transparent);
  color: var(--text-primary);
}

.menu-link.is-active {
  border-color: color-mix(in srgb, var(--accent-500) 30%, transparent);
  background: color-mix(in srgb, var(--accent-500) 16%, var(--surface-elevated));
  color: color-mix(in srgb, var(--text-primary) 85%, var(--accent-700));
}

.menu-link.is-danger {
  color: var(--danger);
}

.menu-link.is-danger:hover {
  background: color-mix(in srgb, var(--danger) 10%, transparent);
}

.menu-rail {
  position: absolute;
  left: 3px;
  top: 7px;
  bottom: 7px;
  width: 3px;
  border-radius: 999px;
  background: transparent;
  transition: background-color 180ms ease;
}

.menu-link.is-active .menu-rail {
  background: linear-gradient(180deg, var(--accent-500), var(--accent-700));
}

.menu-label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 13px;
  font-weight: 500;
}

.sidebar-account {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.sidebar-account-action {
  justify-content: center;
  text-align: center;
  gap: 8px;
}

.mobile-sidebar-brand {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
  padding: 0 44px 0 10px;
}

.mobile-sidebar-brand-main {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 0;
}

.mobile-sidebar-close {
  position: absolute;
  right: 8px;
  top: 50%;
  transform: translateY(-50%);
}

.layout-header {
  border-color: color-mix(in srgb, var(--panel-border) 84%, transparent);
}

.header-zone {
  min-height: 40px;
}

.header-ctl-btn,
.header-chip {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  min-height: 34px;
  font-size: 13px;
  font-weight: 500;
  border-radius: 10px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 88%, transparent);
  background: color-mix(in srgb, var(--surface-base) 88%, transparent);
  color: var(--text-primary);
  padding: 0 10px;
  transition: all 180ms ease;
}

.header-ctl-btn {
  width: 34px;
  padding: 0;
}

.header-ctl-btn:hover,
.header-chip:hover {
  background: color-mix(in srgb, var(--surface-elevated) 92%, transparent);
  color: var(--text-primary);
}

.notice-count {
  background: linear-gradient(140deg, var(--accent-600), var(--accent-700));
}

.notice-popover,
.user-popover {
  border: 1px solid var(--popup-border);
  border-radius: 14px;
  padding: 8px;
  box-shadow: var(--popup-shadow);
}

.notice-card {
  border: 1px solid color-mix(in srgb, var(--panel-border) 62%, transparent);
  border-radius: 10px;
  padding: 8px;
  background: color-mix(in srgb, var(--surface-base) 68%, transparent);
  transition: all 180ms ease;
}

.notice-card:hover {
  border-color: color-mix(in srgb, var(--accent-500) 28%, transparent);
  background: color-mix(in srgb, var(--surface-elevated) 86%, transparent);
}

:root[data-theme='light'] .notice-card {
  background: color-mix(in srgb, var(--surface-base) 52%, transparent);
}

:root[data-theme='light'] .notice-card:hover {
  background: color-mix(in srgb, var(--surface-elevated) 72%, transparent);
}

.menu-item {
  width: 100%;
  border-radius: 8px;
  padding: 7px 8px;
  text-align: left;
  font-size: 13px;
  color: var(--text-secondary);
  transition: all 180ms ease;
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-divider {
  margin: 5px 0;
  border-top: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
}

.menu-item:hover {
  background: color-mix(in srgb, var(--surface-elevated) 82%, transparent);
  color: var(--text-primary);
}

.logout-dialog {
  position: relative;
}

.logout-dialog__surface {
  position: relative;
  display: grid;
  gap: 16px;
  overflow: hidden;
  border: 1px solid color-mix(in srgb, var(--popup-border) 86%, transparent);
  border-radius: 18px;
  padding: 14px;
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 18%, transparent), transparent 38%),
    radial-gradient(circle at 82% 18%, color-mix(in srgb, var(--danger) 16%, transparent), transparent 34%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-elevated) 92%, transparent), color-mix(in srgb, var(--surface-base) 94%, transparent));
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, white 22%, transparent),
    0 18px 40px color-mix(in srgb, black 10%, transparent);
}

.logout-dialog__surface::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    linear-gradient(135deg, color-mix(in srgb, white 14%, transparent), transparent 28%),
    linear-gradient(180deg, transparent, color-mix(in srgb, black 5%, transparent));
  pointer-events: none;
}

.logout-dialog__badge {
  position: relative;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  width: fit-content;
  border: 1px solid color-mix(in srgb, var(--accent-500) 20%, transparent);
  border-radius: 999px;
  padding: 5px 10px;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: color-mix(in srgb, var(--text-primary) 88%, var(--accent-700));
  background: color-mix(in srgb, var(--surface-base) 70%, transparent);
  backdrop-filter: blur(10px);
}

.logout-dialog__hero {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.logout-dialog__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 16px;
  color: color-mix(in srgb, white 92%, var(--danger));
  background:
    radial-gradient(circle at 30% 25%, color-mix(in srgb, white 48%, transparent), transparent 55%),
    linear-gradient(135deg, color-mix(in srgb, var(--danger) 86%, white), color-mix(in srgb, var(--danger) 68%, black 6%));
  box-shadow:
    inset 0 1px 0 color-mix(in srgb, white 42%, transparent),
    0 18px 30px color-mix(in srgb, var(--danger) 18%, transparent);
  flex-shrink: 0;
}

.logout-dialog__copy {
  min-width: 0;
}

.logout-dialog__headline {
  margin: 1px 0 6px;
  font-size: 15px;
  font-weight: 700;
  color: var(--text-primary);
}

.logout-dialog__description {
  margin: 0;
  font-size: 13px;
  line-height: 1.65;
  color: var(--text-secondary);
}

.logout-dialog__account {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid color-mix(in srgb, var(--panel-border) 76%, transparent);
  border-radius: 16px;
  padding: 11px 12px;
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--surface-base) 84%, transparent), color-mix(in srgb, var(--surface-elevated) 80%, transparent));
  backdrop-filter: blur(10px);
}

.logout-dialog__account-avatar-wrap {
  position: relative;
  flex-shrink: 0;
}

.logout-dialog__account-avatar-wrap::after {
  content: '';
  position: absolute;
  inset: -4px;
  border-radius: 16px;
  border: 1px solid color-mix(in srgb, var(--accent-500) 18%, transparent);
}

.logout-dialog__account-avatar {
  display: block;
  width: 42px;
  height: 42px;
  border-radius: 12px;
  object-fit: cover;
  box-shadow: 0 12px 24px color-mix(in srgb, var(--text-primary) 10%, transparent);
}

.logout-dialog__account-copy {
  min-width: 0;
}

.logout-dialog__account-label,
.logout-dialog__account-meta,
.logout-dialog__account-name {
  margin: 0;
}

.logout-dialog__account-label {
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: var(--text-secondary);
}

.logout-dialog__account-name {
  margin-top: 4px;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-primary);
}

.logout-dialog__account-meta {
  margin-top: 3px;
  font-size: 12px;
  color: color-mix(in srgb, var(--text-secondary) 88%, transparent);
}

.logout-dialog__note {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  border: 1px solid color-mix(in srgb, var(--danger) 14%, var(--panel-border));
  border-radius: 14px;
  padding: 10px 12px;
  font-size: 12px;
  line-height: 1.6;
  color: color-mix(in srgb, var(--text-primary) 82%, var(--danger));
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--danger) 7%, transparent), transparent),
    color-mix(in srgb, var(--surface-elevated) 92%, transparent);
}

:root[data-theme='dark'] .logout-dialog__surface {
  background:
    radial-gradient(circle at top left, color-mix(in srgb, var(--accent-500) 14%, transparent), transparent 38%),
    radial-gradient(circle at 82% 18%, color-mix(in srgb, var(--danger) 12%, transparent), transparent 34%),
    linear-gradient(180deg, color-mix(in srgb, var(--surface-elevated) 92%, transparent), color-mix(in srgb, var(--surface-base) 96%, transparent));
}

@media (max-width: 1279px) {
  .layout-header {
    gap: 8px;
  }
}
</style>

