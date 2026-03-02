import axios from 'axios'
import { ElMessage } from 'element-plus'
import Cookies from 'js-cookie'
import router from '@/router'

const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API || '/api',
  timeout: 30000
})

service.interceptors.request.use(
  config => {
    const token = Cookies.get('token')
    if (token) {
      config.headers.Authorization = 'Bearer ' + token
    }
    return config
  },
  error => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  response => {
    const res = response.data

    if (res.code !== 200) {
      if (!response.config?.silent) {
        ElMessage.error(res.message || '操作失败')
      }

      if (res.code === 401) {
        Cookies.remove('token')
        router.push('/login')
      }

      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  error => {
    console.error('Response error:', error)
    const message = error.response?.data?.message || '网络错误，请稍后重试'
    if (!error.config?.silent) {
      ElMessage.error(message)
    }

    if (error.response?.status === 401) {
      Cookies.remove('token')
      router.push('/login')
    }

    return Promise.reject(error)
  }
)

export default service
