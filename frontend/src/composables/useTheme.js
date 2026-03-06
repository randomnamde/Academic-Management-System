import { computed, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'

const STORAGE_KEY = 'ui:themeMode'
const DARK_MEDIA_QUERY = '(prefers-color-scheme: dark)'
const VALID_MODES = ['system', 'light', 'dark']
const MODE_SEQUENCE = ['system', 'light', 'dark']

let systemMediaQuery = null
let systemChangeHandler = null
const callbacks = new Set()

function normalizeMode(mode) {
  return VALID_MODES.includes(mode) ? mode : 'system'
}

function normalizeUserIdentity(userInfo) {
  if (userInfo && userInfo.id != null && String(userInfo.id).trim() !== '') {
    return `id:${String(userInfo.id).trim()}`
  }
  if (userInfo && userInfo.account && String(userInfo.account).trim()) {
    return `account:${String(userInfo.account).trim().toLowerCase()}`
  }
  if (userInfo && userInfo.username && String(userInfo.username).trim()) {
    return `username:${String(userInfo.username).trim().toLowerCase()}`
  }
  return 'guest'
}

export function getThemeModeStorageKey(userInfo) {
  return `${STORAGE_KEY}:${normalizeUserIdentity(userInfo)}`
}

function getSystemTheme() {
  if (typeof window === 'undefined' || !window.matchMedia) return 'light'
  return window.matchMedia(DARK_MEDIA_QUERY).matches ? 'dark' : 'light'
}

export function resolveTheme(mode) {
  const normalized = normalizeMode(mode)
  return normalized === 'system' ? getSystemTheme() : normalized
}

export function applyTheme(theme) {
  if (typeof document === 'undefined') return
  const resolved = theme === 'dark' ? 'dark' : 'light'
  const root = document.documentElement
  root.setAttribute('data-theme', resolved)
  root.style.colorScheme = resolved
}

function emitSystemTheme() {
  const theme = getSystemTheme()
  callbacks.forEach((callback) => callback(theme))
}

function ensureSystemListener() {
  if (typeof window === 'undefined' || !window.matchMedia || systemMediaQuery) return
  systemMediaQuery = window.matchMedia(DARK_MEDIA_QUERY)
  systemChangeHandler = () => emitSystemTheme()
  systemMediaQuery.addEventListener('change', systemChangeHandler)
}

function cleanupSystemListenerIfIdle() {
  if (!systemMediaQuery || callbacks.size) return
  systemMediaQuery.removeEventListener('change', systemChangeHandler)
  systemMediaQuery = null
  systemChangeHandler = null
}

export function watchSystemTheme(callback) {
  if (typeof window === 'undefined' || !window.matchMedia) return () => {}
  callbacks.add(callback)
  ensureSystemListener()

  return () => {
    callbacks.delete(callback)
    cleanupSystemListenerIfIdle()
  }
}

export function persistThemeMode(mode, userInfo) {
  if (typeof localStorage === 'undefined') return
  localStorage.setItem(getThemeModeStorageKey(userInfo), normalizeMode(mode))
}

export function getStoredThemeMode(userInfo) {
  if (typeof localStorage === 'undefined') return 'system'
  const scopedKey = getThemeModeStorageKey(userInfo)
  const scopedValue = localStorage.getItem(scopedKey)
  if (scopedValue != null) {
    return normalizeMode(scopedValue)
  }

  // Backward compatibility: migrate from legacy global key.
  const legacyValue = localStorage.getItem(STORAGE_KEY)
  const normalized = normalizeMode(legacyValue)
  if (legacyValue != null) {
    localStorage.setItem(scopedKey, normalized)
  }
  return normalized
}

export function syncThemeMode(mode, userInfo) {
  const normalized = normalizeMode(mode)
  persistThemeMode(normalized, userInfo)
  applyTheme(resolveTheme(normalized))
}

export function initializeTheme(store) {
  const userInfo = store?.state?.userInfo || {}
  const initialMode = normalizeMode(store?.state?.uiPreference?.themeMode || getStoredThemeMode(userInfo))
  syncThemeMode(initialMode, userInfo)

  watchSystemTheme(() => {
    const currentUserInfo = store?.state?.userInfo || {}
    const currentMode = normalizeMode(store?.state?.uiPreference?.themeMode || getStoredThemeMode(currentUserInfo))
    if (currentMode === 'system') {
      applyTheme(resolveTheme(currentMode))
    }
  })
}

export function useTheme(externalStore) {
  const store = externalStore || useStore()
  const mode = computed(() => normalizeMode(store.getters.themeMode || store.state.uiPreference?.themeMode))
  const activeTheme = computed(() => resolveTheme(mode.value))

  const setThemeMode = (nextMode) => {
    const normalized = normalizeMode(nextMode)
    store.commit('SET_THEME_MODE', normalized)
  }

  const cycleThemeMode = () => {
    const index = MODE_SEQUENCE.indexOf(mode.value)
    const next = MODE_SEQUENCE[(index + 1) % MODE_SEQUENCE.length]
    setThemeMode(next)
  }

  const stop = watchSystemTheme(() => {
    if (mode.value === 'system') {
      applyTheme(resolveTheme('system'))
    }
  })

  onBeforeUnmount(() => {
    stop?.()
  })

  return {
    mode,
    activeTheme,
    setThemeMode,
    cycleThemeMode
  }
}
