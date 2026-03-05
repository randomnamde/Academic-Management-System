import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'
import { getBrowserDefaultLocale, normalizeLocale } from './localeManager'

const messages = {
  'zh-CN': zhCN,
  'en-US': enUS
}

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: normalizeLocale(getBrowserDefaultLocale()),
  fallbackLocale: 'zh-CN',
  messages,
  missingWarn: false,
  fallbackWarn: false
})

export function setI18nLocale(locale) {
  i18n.global.locale.value = normalizeLocale(locale)
}

export default i18n

