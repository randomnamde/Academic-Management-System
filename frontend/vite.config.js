import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import path from 'path'

const normalizePath = (id) => id.replace(/\\/g, '/')

const chunkFromSegment = (id, marker, prefix) => {
  const normalizedId = normalizePath(id)
  const index = normalizedId.indexOf(marker)
  if (index === -1) return null

  const rest = normalizedId.slice(index + marker.length)
  const [segment] = rest.split('/')
  return segment ? `${prefix}${segment}` : null
}

const echartsChunk = (id) => {
  const normalizedId = normalizePath(id)

  if (normalizedId.includes('/node_modules/vue-echarts/')) {
    return 'vendor-echarts-vue'
  }

  const chartChunk = chunkFromSegment(normalizedId, '/node_modules/echarts/charts/', 'vendor-echarts-chart-')
  if (chartChunk) return chartChunk

  const componentChunk = chunkFromSegment(normalizedId, '/node_modules/echarts/components/', 'vendor-echarts-component-')
  if (componentChunk) return componentChunk

  const featureChunk = chunkFromSegment(normalizedId, '/node_modules/echarts/features/', 'vendor-echarts-feature-')
  if (featureChunk) return featureChunk

  const rendererChunk = chunkFromSegment(normalizedId, '/node_modules/echarts/renderers/', 'vendor-echarts-renderer-')
  if (rendererChunk) return rendererChunk

  if (normalizedId.includes('/node_modules/echarts/core')) {
    return 'vendor-echarts-core'
  }

  if (normalizedId.includes('/node_modules/zrender/')) {
    return 'vendor-zrender'
  }

  if (normalizedId.includes('/node_modules/echarts/')) {
    return 'vendor-echarts-core'
  }

  return null
}

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      dts: false,
      resolvers: [
        ElementPlusResolver({
          directives: true,
          importStyle: 'css'
        })
      ]
    }),
    Components({
      dts: false,
      resolvers: [
        ElementPlusResolver({
          directives: true,
          importStyle: 'css'
        })
      ]
    })
  ],
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          const echarts = echartsChunk(id)
          if (echarts) {
            return echarts
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
