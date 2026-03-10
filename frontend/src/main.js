import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import i18n, { setI18nLocale } from './i18n'

import './styles/tokens.css'
import './styles/base.css'
import './styles/tailwind.css'
import './styles/global.scss'
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
import { initializeTheme } from './composables/useTheme'

const resizeObserverErrorPattern = /ResizeObserver loop (completed with undelivered notifications|limit exceeded)/i

window.addEventListener('error', (event) => {
  if (resizeObserverErrorPattern.test(event.message || '')) {
    event.stopImmediatePropagation()
  }
})

window.addEventListener('unhandledrejection', (event) => {
  const reason = event.reason
  const message = typeof reason === 'string' ? reason : reason?.message
  if (resizeObserverErrorPattern.test(message || '')) {
    event.preventDefault()
  }
})

const app = createApp(App)

app.use(i18n)
setI18nLocale(store.state.uiPreference?.language)
app.use(router)
app.use(store)
initializeTheme(store)

app.mount('#app')


