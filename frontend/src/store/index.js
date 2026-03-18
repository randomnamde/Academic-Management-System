import { createStore } from 'vuex'
import user from './modules/user'
import ui from './modules/ui'

const store = createStore({
  modules: {
    user,
    ui
  },
  // 保留旧的状态访问方式，保持向后兼容
  // 这些状态会代理到对应的模块
  state: () => ({}),
  mutations: {
    // 代理 user 模块的 mutations
    SET_TOKEN(state, token) {
      store.commit('user/SET_TOKEN', token)
    },
    SET_USER_INFO(state, userInfo) {
      store.commit('user/SET_USER_INFO', userInfo)
    },
    CLEAR_USER(state) {
      store.commit('user/CLEAR_USER')
    },
    // 代理 ui 模块的 mutations
    TOGGLE_SIDEBAR(state) {
      store.commit('ui/TOGGLE_SIDEBAR')
    },
    SET_TABLE_DENSITY(state, density) {
      store.commit('ui/SET_TABLE_DENSITY', density)
    },
    SET_THEME_MODE(state, mode) {
      store.commit('ui/SET_THEME_MODE', mode)
    },
    SET_LANGUAGE(state, locale) {
      store.commit('ui/SET_LANGUAGE', locale)
    }
  },
  actions: {
    async login({ commit }, loginData) {
      return store.dispatch('user/login', loginData)
    },
    async getUserInfo({ commit }) {
      return store.dispatch('user/getUserInfo')
    },
    logout({ commit }) {
      store.dispatch('user/logout')
    }
  },
  getters: {
    isLoggedIn: (state, getters, rootState) => !!rootState.user?.token,
    userRole: (state, getters, rootState) => rootState.user?.userInfo?.primaryRole || rootState.user?.userInfo?.role,
    userRoles: (state, getters, rootState) => {
      const merged = new Set(Array.isArray(rootState.user?.roles) ? rootState.user.roles : [])
      if (rootState.user?.userInfo?.primaryRole) merged.add(rootState.user.userInfo.primaryRole)
      if (rootState.user?.userInfo?.role) merged.add(rootState.user.userInfo.role)
      return Array.from(merged)
    },
    account: (state, getters, rootState) => rootState.user?.userInfo?.account || rootState.user?.userInfo?.username,
    username: (state, getters, rootState) => rootState.user?.userInfo?.account || rootState.user?.userInfo?.username,
    tableDensity: (state, getters, rootState) => rootState.ui?.tableDensity || 'compact',
    themeMode: (state, getters, rootState) => rootState.ui?.themeMode || 'system',
    language: (state, getters, rootState) => rootState.ui?.language || 'zh-CN',
    permissions: (state, getters, rootState) => rootState.user?.permissions || [],
    sidebarOpened: (state, getters, rootState) => rootState.ui?.sidebar?.opened !== false,
    sidebarCollapsed: (state, getters, rootState) => rootState.ui?.sidebarCollapsed || false,
    // 兼容旧的访问方式
    userInfo: (state, getters, rootState) => rootState.user?.userInfo || {},
    token: (state, getters, rootState) => rootState.user?.token || '',
    sidebar: (state, getters, rootState) => rootState.ui?.sidebar || { opened: true },
    uiPreference: (state, getters, rootState) => ({
      tableDensity: rootState.ui?.tableDensity || 'compact',
      sidebarCollapsed: rootState.ui?.sidebarCollapsed || false,
      themeMode: rootState.ui?.themeMode || 'system',
      language: rootState.ui?.language || 'zh-CN'
    })
  }
})

export default store
