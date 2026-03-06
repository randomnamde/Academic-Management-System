const STORAGE_KEY = 'ui:language'
const SUPPORTED_LOCALES = ['zh-CN', 'en-US']

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

export function normalizeLocale(locale) {
  return SUPPORTED_LOCALES.includes(locale) ? locale : 'zh-CN'
}

export function getBrowserDefaultLocale() {
  if (typeof navigator === 'undefined') return 'zh-CN'
  const preferred = String(navigator.language || navigator.userLanguage || '').toLowerCase()
  return preferred.startsWith('zh') ? 'zh-CN' : 'en-US'
}

export function getLanguageStorageKey(userInfo) {
  return `${STORAGE_KEY}:${normalizeUserIdentity(userInfo)}`
}

export function setStoredLanguage(locale, userInfo) {
  if (typeof localStorage === 'undefined') return
  localStorage.setItem(getLanguageStorageKey(userInfo), normalizeLocale(locale))
}

export function getStoredLanguage(userInfo) {
  if (typeof localStorage === 'undefined') return getBrowserDefaultLocale()

  const scopedKey = getLanguageStorageKey(userInfo)
  const scopedValue = localStorage.getItem(scopedKey)
  if (scopedValue != null) {
    return normalizeLocale(scopedValue)
  }

  const legacyValue = localStorage.getItem(STORAGE_KEY)
  if (legacyValue != null) {
    const normalizedLegacy = normalizeLocale(legacyValue)
    localStorage.setItem(scopedKey, normalizedLegacy)
    return normalizedLegacy
  }

  return getBrowserDefaultLocale()
}
