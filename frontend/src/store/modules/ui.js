import { getStoredThemeMode, syncThemeMode } from '@/composables/useTheme'
import { getStoredLanguage, normalizeLocale, setStoredLanguage } from '@/i18n/localeManager'
import { setI18nLocale } from '@/i18n'

function getCurrentUserInfo() {
  try {
    return JSON.parse(localStorage.getItem('userInfo') || '{}')
  } catch (_error) {
    return {}
  }
}

export default {
  namespaced: true,
  state: () => ({
    sidebar: {
      opened: localStorage.getItem('sidebar') !== 'false'
    },
    tableDensity: localStorage.getItem('ui:tableDensity') || 'compact',
    sidebarCollapsed: localStorage.getItem('ui:sidebarCollapsed') === 'true',
    themeMode: 'system',
    language: 'zh-CN'
  }),
  mutations: {
    TOGGLE_SIDEBAR(state) {
      state.sidebar.opened = !state.sidebar.opened
      localStorage.setItem('sidebar', state.sidebar.opened)
      state.sidebarCollapsed = !state.sidebar.opened
      localStorage.setItem('ui:sidebarCollapsed', String(state.sidebarCollapsed))
    },
    SET_TABLE_DENSITY(state, density) {
      const next = density === 'comfortable' ? 'comfortable' : 'compact'
      state.tableDensity = next
      localStorage.setItem('ui:tableDensity', next)
    },
    SET_THEME_MODE(state, mode) {
      const next = ['light', 'dark', 'system'].includes(mode) ? mode : 'system'
      state.themeMode = next
      syncThemeMode(next, getCurrentUserInfo())
    },
    SET_LANGUAGE(state, locale) {
      const next = normalizeLocale(locale)
      state.language = next
      setStoredLanguage(next, getCurrentUserInfo())
      setI18nLocale(next)
    },
    INIT_UI_PREFERENCE(state) {
      const userInfo = getCurrentUserInfo()
      state.themeMode = getStoredThemeMode(userInfo)
      state.language = getStoredLanguage(userInfo)
    }
  },
  getters: {
    tableDensity: (state) => state.tableDensity,
    themeMode: (state) => state.themeMode,
    language: (state) => normalizeLocale(state.language),
    sidebarOpened: (state) => state.sidebar.opened,
    sidebarCollapsed: (state) => state.sidebarCollapsed
  }
}
