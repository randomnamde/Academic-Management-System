import { createStore } from 'vuex'
import Cookies from 'js-cookie'
import { login, getUserInfo } from '@/api/user'
import { getStoredThemeMode, syncThemeMode } from '@/composables/useTheme'
import { getStoredLanguage, normalizeLocale, setStoredLanguage } from '@/i18n/localeManager'
import { setI18nLocale } from '@/i18n'

const initialUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

const store = createStore({
  state: {
    token: Cookies.get('token') || '',
    userInfo: initialUserInfo,
    sidebar: {
      opened: localStorage.getItem('sidebar') !== 'false'
    },
    uiPreference: {
      tableDensity: localStorage.getItem('ui:tableDensity') || 'compact',
      sidebarCollapsed: localStorage.getItem('ui:sidebarCollapsed') === 'true',
      themeMode: getStoredThemeMode(initialUserInfo),
      language: getStoredLanguage(initialUserInfo)
    }
  },
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      Cookies.set('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      const nextThemeMode = getStoredThemeMode(userInfo)
      state.uiPreference.themeMode = nextThemeMode
      syncThemeMode(nextThemeMode, userInfo)
      const nextLanguage = getStoredLanguage(userInfo)
      state.uiPreference.language = nextLanguage
      setI18nLocale(nextLanguage)
    },
    CLEAR_USER(state) {
      const retainedThemeMode = state.uiPreference.themeMode
      const retainedLanguage = state.uiPreference.language
      state.token = ''
      state.userInfo = {}
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
      state.uiPreference.themeMode = retainedThemeMode
      state.uiPreference.language = retainedLanguage
      syncThemeMode(retainedThemeMode, {})
      setStoredLanguage(retainedLanguage, {})
      setI18nLocale(retainedLanguage)
    },
    TOGGLE_SIDEBAR(state) {
      state.sidebar.opened = !state.sidebar.opened
      localStorage.setItem('sidebar', state.sidebar.opened)
      state.uiPreference.sidebarCollapsed = !state.sidebar.opened
      localStorage.setItem('ui:sidebarCollapsed', String(state.uiPreference.sidebarCollapsed))
    },
    SET_TABLE_DENSITY(state, density) {
      const next = density === 'comfortable' ? 'comfortable' : 'compact'
      state.uiPreference.tableDensity = next
      localStorage.setItem('ui:tableDensity', next)
    },
    SET_THEME_MODE(state, mode) {
      const next = ['light', 'dark', 'system'].includes(mode) ? mode : 'system'
      state.uiPreference.themeMode = next
      syncThemeMode(next, state.userInfo)
    },
    SET_LANGUAGE(state, locale) {
      const next = normalizeLocale(locale)
      state.uiPreference.language = next
      setStoredLanguage(next, state.userInfo)
      setI18nLocale(next)
    }
  },
  actions: {
    async login({ commit }, loginData) {
      const res = await login(loginData)
      if (res.code === 200) {
        commit('SET_TOKEN', res.data.token)
        commit('SET_USER_INFO', res.data)
        return res
      }
      throw new Error(res.message)
    },

    async getUserInfo({ commit }) {
      const res = await getUserInfo()
      if (res.code === 200) {
        commit('SET_USER_INFO', res.data)
        return res
      }
    },

    logout({ commit }) {
      commit('CLEAR_USER')
    }
  },
  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => state.userInfo?.primaryRole || state.userInfo?.role,
    userRoles: (state) => {
      const merged = new Set(Array.isArray(state.userInfo?.roles) ? state.userInfo.roles : [])
      if (state.userInfo?.primaryRole) merged.add(state.userInfo.primaryRole)
      if (state.userInfo?.role) merged.add(state.userInfo.role)
      return Array.from(merged)
    },
    account: (state) => state.userInfo?.account || state.userInfo?.username,
    username: (state) => state.userInfo?.account || state.userInfo?.username,
    tableDensity: (state) => state.uiPreference.tableDensity,
    themeMode: (state) => state.uiPreference.themeMode,
    language: (state) => normalizeLocale(state.uiPreference.language)
  }
})

export default store
