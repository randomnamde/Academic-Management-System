import axios from 'axios'
import Cookies from 'js-cookie'

function parseFileName(disposition, fallbackName) {
  if (!disposition) return fallbackName

  const utf8Match = disposition.match(/filename\*=UTF-8''([^;]+)/i)
  if (utf8Match?.[1]) {
    try {
      return decodeURIComponent(utf8Match[1])
    } catch (_e) {
      return utf8Match[1]
    }
  }

  const normalMatch = disposition.match(/filename="([^"]+)"/i)
  if (normalMatch?.[1]) return normalMatch[1]

  return fallbackName
}

export async function downloadWithAuth(path, params = {}, fallbackName = 'export.csv') {
  const token = Cookies.get('token')
  const baseURL = import.meta.env.VITE_APP_BASE_API || '/api'
  const response = await axios.get(`${baseURL}${path}`, {
    params,
    responseType: 'blob',
    headers: token ? { Authorization: `Bearer ${token}` } : {}
  })

  const contentDisposition = response.headers['content-disposition']
  const fileName = parseFileName(contentDisposition, fallbackName)
  const blob = new Blob([response.data], { type: response.data.type || 'application/octet-stream' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = fileName
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
