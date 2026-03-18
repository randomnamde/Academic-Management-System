/**
 * 错误处理工具类
 */
import { ERROR_TYPES, STATUS_CODE_MAP, RETRYABLE_STATUS_CODES, RETRYABLE_ERROR_CODES } from './errorConstants'

/**
 * 错误分类器
 */
class ErrorHandler {
  /**
   * 根据错误信息分类错误类型
   */
  static classifyError(error) {
    if (!error) return ERROR_TYPES.UNKNOWN

    // 网络错误
    if (error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT') {
      return ERROR_TYPES.TIMEOUT
    }

    if (
      error.message?.includes('Network Error') ||
      RETRYABLE_ERROR_CODES.includes(error.code)
    ) {
      return ERROR_TYPES.NETWORK
    }

    // HTTP 状态码错误
    if (error.response) {
      const status = error.response.status

      if (status === 401) {
        return ERROR_TYPES.AUTH
      }

      if (status === 422) {
        return ERROR_TYPES.VALIDATION
      }

      if (status >= 500) {
        return ERROR_TYPES.SERVER
      }
    }

    return ERROR_TYPES.UNKNOWN
  }

  /**
   * 获取用户友好的错误消息
   */
  static getUserMessage(error, i18n) {
    const errorType = this.classifyError(error)

    // 优先使用服务器返回的消息
    if (error.response?.data?.message) {
      return error.response.data.message
    }

    // 优先使用服务器返回的消息
    if (error.response?.data?.error) {
      return error.response.data.error
    }

    // 根据 HTTP 状态码获取消息
    if (error.response?.status && STATUS_CODE_MAP[error.response.status]) {
      return STATUS_CODE_MAP[error.response.status]
    }

    // 根据错误类型获取默认消息
    const defaultMessages = {
      [ERROR_TYPES.NETWORK]: i18n?.t('request.error.networkError') || '网络连接失败',
      [ERROR_TYPES.TIMEOUT]: i18n?.t('request.error.timeout') || '请求超时',
      [ERROR_TYPES.AUTH]: i18n?.t('request.error.unauthorized') || '登录已过期，请重新登录',
      [ERROR_TYPES.VALIDATION]: i18n?.t('request.error.validation') || '数据验证失败',
      [ERROR_TYPES.SERVER]: i18n?.t('request.error.serverError') || '服务器错误',
      [ERROR_TYPES.UNKNOWN]: i18n?.t('request.error.unknown') || '未知错误'
    }

    return defaultMessages[errorType] || '操作失败'
  }

  /**
   * 判断是否可以重试
   */
  static isRetryable(error, retryCount = 0, maxRetries = 3) {
    if (retryCount >= maxRetries) {
      return false
    }

    const errorType = this.classifyError(error)

    // 可重试的错误类型
    const retryableTypes = [ERROR_TYPES.NETWORK, ERROR_TYPES.TIMEOUT, ERROR_TYPES.SERVER]

    if (retryableTypes.includes(errorType)) {
      return true
    }

    // 可重试的 HTTP 状态码
    if (error.response?.status && RETRYABLE_STATUS_CODES.includes(error.response.status)) {
      return true
    }

    return false
  }

  /**
   * 计算重试延迟时间（指数退避）
   */
  static calculateRetryDelay(retryCount, baseDelay = 1000, multiplier = 2) {
    return baseDelay * Math.pow(multiplier, retryCount)
  }

  /**
   * 记录错误日志
   */
  static logError(error, context = {}) {
    const errorLog = {
      timestamp: new Date().toISOString(),
      type: this.classifyError(error),
      message: error.message,
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method,
      context
    }

    // 开发环境输出详细日志
    if (import.meta.env.DEV) {
      console.error('[ErrorHandler]', errorLog)
      console.error('[ErrorHandler] Full error:', error)
    }

    // 生产环境可以将错误日志上报到监控系统
    // 这里可以集成 Sentry 或其他监控工具
    if (import.meta.env.PROD && typeof window !== 'undefined') {
      // 示例：上报到监控系统
      // this.reportToMonitoring(errorLog)
    }

    return errorLog
  }

  /**
   * 上报错误到监控系统（预留接口）
   */
  static reportToMonitoring(errorLog) {
    // 实现错误上报逻辑
    // 可以集成 Sentry、LogRocket 等监控工具
  }

  /**
   * 格式化验证错误
   */
  static formatValidationErrors(errors) {
    if (!errors || typeof errors !== 'object') {
      return []
    }

    const formattedErrors = []

    // 处理对象形式的验证错误 { field: 'error message' }
    if (!Array.isArray(errors)) {
      Object.keys(errors).forEach(field => {
        formattedErrors.push({
          field,
          message: errors[field]
        })
      })
    } else {
      // 处理数组形式的验证错误
      errors.forEach(err => {
        if (typeof err === 'string') {
          formattedErrors.push({
            field: 'general',
            message: err
          })
        } else if (typeof err === 'object') {
          formattedErrors.push(err)
        }
      })
    }

    return formattedErrors
  }
}

export default ErrorHandler
