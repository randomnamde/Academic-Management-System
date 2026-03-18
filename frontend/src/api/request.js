import axios from 'axios'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import router from '@/router'
import i18n from '@/i18n'
import ErrorHandler from '@/utils/errorHandler'
import { DEFAULT_CONFIG } from '@/utils/errorConstants'

// 创建请求缓存
const requestCache = new Map()

// 请求重试队列
const retryQueue = new Map()

/**
 * 生成缓存键
 */
function generateCacheKey(config) {
  return `${config.method}_${config.url}_${JSON.stringify(config.params || {})}_${JSON.stringify(config.data || {})}`
}

/**
 * 检查缓存
 */
function checkCache(config) {
  if (!config.enableCache) return null

  const cacheKey = generateCacheKey(config)
  const cached = requestCache.get(cacheKey)

  if (cached && Date.now() - cached.timestamp < config.cacheExpireTime) {
    return cached.data
  }

  requestCache.delete(cacheKey)
  return null
}

/**
 * 设置缓存
 */
function setCache(config, data) {
  if (!config.enableCache) return

  const cacheKey = generateCacheKey(config)
  requestCache.set(cacheKey, {
    data,
    timestamp: Date.now()
  })
}

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: DEFAULT_CONFIG.timeout
})

/**
 * 请求拦截器
 */
service.interceptors.request.use(
  config => {
    // 自动添加 token
    const token = Cookies.get('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }

    // 初始化重试计数
    config.retryCount = 0
    config.maxRetries = config.maxRetries || DEFAULT_CONFIG.maxRetries
    config.retryDelay = config.retryDelay || DEFAULT_CONFIG.retryDelay
    config.retryBackoffMultiplier = config.retryBackoffMultiplier || DEFAULT_CONFIG.retryBackoffMultiplier

    // 检查缓存（仅 GET 请求）
    if (config.method?.toUpperCase() === 'GET' && config.enableCache !== false) {
      const cached = checkCache({
        ...config,
        enableCache: true,
        cacheExpireTime: config.cacheExpireTime || DEFAULT_CONFIG.cacheExpireTime
      })
      if (cached) {
        config.adapter = () => Promise.resolve({
          data: cached,
          status: 200,
          statusText: 'OK',
          headers: {},
          config,
          request: {},
          fromCache: true
        })
      }
    }

    return config
  },
  error => {
    ErrorHandler.logError(error, { phase: 'request' })
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器 - 成功
 */
service.interceptors.response.use(
  response => {
    const res = response.data

    // 如果是缓存数据，直接返回
    if (response.fromCache) {
      return res
    }

    // 缓存成功的 GET 请求
    if (response.config.method?.toUpperCase() === 'GET' && response.config.enableCache !== false) {
      setCache(response.config, res)
    }

    // 业务逻辑错误处理
    if (res.code !== 200) {
      const error = {
        response: {
          status: res.code,
          data: { message: res.message, code: res.code }
        },
        config: response.config
      }

      // 记录错误
      ErrorHandler.logError(error, { phase: 'response', businessError: true })

      // 显示错误提示（除非配置了静默模式）
      if (!response.config?.silent) {
        const userMessage = ErrorHandler.getUserMessage(error, i18n.global)
        ElMessage.error(userMessage)
      }

      // 处理 401 未授权
      if (res.code === 401) {
        Cookies.remove('token')
        localStorage.removeItem('userInfo')
        localStorage.removeItem('userPermissions')
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  async error => {
    const config = error.config

    // 如果没有配置对象或已经取消重试，直接拒绝
    if (!config) {
      ErrorHandler.logError(error, { phase: 'response', noConfig: true })
      return Promise.reject(error)
    }

    // 错误分类和日志记录
    const errorType = ErrorHandler.classifyError(error)
    ErrorHandler.logError(error, { phase: 'response', errorType })

    // 检查是否需要重试
    if (ErrorHandler.isRetryable(error, config.retryCount, config.maxRetries)) {
      config.retryCount++

      // 计算重试延迟（指数退避）
      const delay = ErrorHandler.calculateRetryDelay(
        config.retryCount - 1,
        config.retryDelay,
        config.retryBackoffMultiplier
      )

      // 显示重试提示
      if (!config.silent && config.retryCount === 1) {
        ElMessage.warning({
          message: i18n.global.t('request.error.retryAttempt', { count: config.retryCount, max: config.maxRetries }),
          duration: 2000
        })
      }

      // 延迟后重试
      await new Promise(resolve => setTimeout(resolve, delay))

      return service(config)
    }

    // 不再重试或达到最大重试次数，显示错误信息
    if (!config.silent) {
      const userMessage = ErrorHandler.getUserMessage(error, i18n.global)
      ElMessage.error(userMessage)

      // 如果是 401，清除认证信息并跳转登录
      if (error.response?.status === 401) {
        setTimeout(() => {
          Cookies.remove('token')
          localStorage.removeItem('userInfo')
          localStorage.removeItem('userPermissions')
          router.push('/login')
        }, 1500)
      }
    }

    return Promise.reject(error)
  }
)

/**
 * 清除请求缓存
 */
export function clearCache() {
  requestCache.clear()
}

/**
 * 清除指定 URL 的缓存
 */
export function clearCacheByPattern(pattern) {
  const keys = Array.from(requestCache.keys())
  keys.forEach(key => {
    if (key.includes(pattern)) {
      requestCache.delete(key)
    }
  })
}

export default service
