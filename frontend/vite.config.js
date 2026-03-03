import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/echarts') || id.includes('node_modules/zrender') || id.includes('node_modules/vue-echarts')) {
            return 'vendor-echarts'
          }
          if (id.includes('node_modules/element-plus') || id.includes('node_modules/@element-plus/icons-vue')) {
            return 'vendor-element-plus'
          }
          if (id.includes('node_modules/vue-router') || id.includes('node_modules/vuex') || id.includes('node_modules/vue')) {
            return 'vendor-vue'
          }
          if (id.includes('node_modules/axios') || id.includes('node_modules/dayjs') || id.includes('node_modules/js-cookie')) {
            return 'vendor-utils'
          }
          return undefined
        }
      }
    }
  },
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  },
  server: {
    port: 3000,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
