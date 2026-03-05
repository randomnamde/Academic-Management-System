import { computed } from 'vue'
import { useStore } from 'vuex'
import { normalizeLocale } from '@/i18n/localeManager'

export function useLanguage(externalStore) {
  const store = externalStore || useStore()

  const language = computed(() => normalizeLocale(store.getters.language || store.state.uiPreference?.language))

  const setLanguage = (locale) => {
    store.commit('SET_LANGUAGE', normalizeLocale(locale))
  }

  const toggleLanguage = () => {
    setLanguage(language.value === 'zh-CN' ? 'en-US' : 'zh-CN')
  }

  return {
    language,
    setLanguage,
    toggleLanguage
  }
}

