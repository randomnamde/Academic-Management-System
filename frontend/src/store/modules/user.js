import Cookies from 'js-cookie'
import { login, getUserInfo } from '@/api/user'
import { getStoredThemeMode, syncThemeMode } from '@/composables/useTheme'
import { getStoredLanguage, normalizeLocale, setStoredLanguage } from '@/i18n/localeManager'
import { setI18nLocale } from '@/i18n'

const initialUserInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')

export default {
  namespaced: true,
  state: () => ({
    token: Cookies.get('token') || '',
    userInfo: initialUserInfo,
    permissions: initialUserInfo.permissions || [],
    roles: initialUserInfo.roles || []
  }),
  mutations: {
    SET_TOKEN(state, token) {
      state.token = token
      Cookies.set('token', token)
    },
    SET_USER_INFO(state, userInfo) {
      state.userInfo = userInfo
      state.permissions = userInfo.permissions || []
      state.roles = userInfo.roles || []
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
      const nextThemeMode = getStoredThemeMode(userInfo)
      syncThemeMode(nextThemeMode, userInfo)
      const nextLanguage = getStoredLanguage(userInfo)
      setI18nLocale(nextLanguage)
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = {}
      state.permissions = []
      state.roles = []
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
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

    logout({ commit, state }) {
      const retainedThemeMode = getStoredThemeMode(state.userInfo)
      const retainedLanguage = getStoredLanguage(state.userInfo)
      commit('CLEAR_USER')
      syncThemeMode(retainedThemeMode, {})
      setStoredLanguage(retainedLanguage, {})
      setI18nLocale(retainedLanguage)
    }
  },
  getters: {
    isLoggedIn: (state) => !!state.token,
    userRole: (state) => state.userInfo?.primaryRole || state.userInfo?.role,
    userRoles: (state) => {
      const merged = new Set(Array.isArray(state.roles) ? state.roles : [])
      if (state.userInfo?.primaryRole) merged.add(state.userInfo.primaryRole)
      if (state.userInfo?.role) merged.add(state.userInfo.role)
      return Array.from(merged)
    },
    account: (state) => state.userInfo?.account || state.userInfo?.username,
    username: (state) => state.userInfo?.account || state.userInfo?.username,
    permissions: (state) => state.permissions
  }
}
