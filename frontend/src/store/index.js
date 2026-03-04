import { createStore } from 'vuex'
import Cookies from 'js-cookie'
import { login, getUserInfo } from '@/api/user'
import { getStoredThemeMode, syncThemeMode } from '@/composables/useTheme'

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
      themeMode: getStoredThemeMode(initialUserInfo)
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
    },
    CLEAR_USER(state) {
      const retainedThemeMode = state.uiPreference.themeMode
      state.token = ''
      state.userInfo = {}
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
      state.uiPreference.themeMode = retainedThemeMode
      syncThemeMode(retainedThemeMode, {})
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
    username: (state) => state.userInfo?.username,
    tableDensity: (state) => state.uiPreference.tableDensity,
    themeMode: (state) => state.uiPreference.themeMode
  }
})

export default store
