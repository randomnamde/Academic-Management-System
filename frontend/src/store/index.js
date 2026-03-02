import { createStore } from 'vuex'
import Cookies from 'js-cookie'
import { login, getUserInfo } from '@/api/user'

const store = createStore({
  state: {
    token: Cookies.get('token') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}'),
    sidebar: {
      opened: localStorage.getItem('sidebar') !== 'false'
    },
    uiPreference: {
      tableDensity: localStorage.getItem('ui:tableDensity') || 'compact',
      sidebarCollapsed: localStorage.getItem('ui:sidebarCollapsed') === 'true'
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
    },
    CLEAR_USER(state) {
      state.token = ''
      state.userInfo = {}
      Cookies.remove('token')
      localStorage.removeItem('userInfo')
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
    isLoggedIn: state => !!state.token,
    userRole: state => state.userInfo?.role,
    username: state => state.userInfo?.username,
    tableDensity: state => state.uiPreference.tableDensity
  }
})

export default store
