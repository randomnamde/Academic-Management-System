import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

import './styles/tokens.css'
import './styles/base.css'
import './styles/tailwind.css'
import './styles/global.scss'

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

for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(ElementPlus)
app.use(router)
app.use(store)

app.mount('#app')

