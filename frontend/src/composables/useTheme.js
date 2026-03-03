import { computed, onBeforeUnmount } from 'vue'
import { useStore } from 'vuex'

const STORAGE_KEY = 'ui:themeMode'
const DARK_MEDIA_QUERY = '(prefers-color-scheme: dark)'
const VALID_MODES = ['system', 'light', 'dark']

let systemMediaQuery = null
let systemChangeHandler = null
const callbacks = new Set()

function normalizeMode(mode) {
  return VALID_MODES.includes(mode) ? mode : 'system'
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

export function persistThemeMode(mode) {
  if (typeof localStorage === 'undefined') return
  localStorage.setItem(STORAGE_KEY, normalizeMode(mode))
}

export function getStoredThemeMode() {
  if (typeof localStorage === 'undefined') return 'system'
  return normalizeMode(localStorage.getItem(STORAGE_KEY))
}

export function syncThemeMode(mode) {
  const normalized = normalizeMode(mode)
  persistThemeMode(normalized)
  applyTheme(resolveTheme(normalized))
}

export function initializeTheme(store) {
  const initialMode = normalizeMode(store?.state?.uiPreference?.themeMode || getStoredThemeMode())
  syncThemeMode(initialMode)

  watchSystemTheme(() => {
    const currentMode = normalizeMode(store?.state?.uiPreference?.themeMode || getStoredThemeMode())
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
    syncThemeMode(normalized)
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
    setThemeMode
  }
}

