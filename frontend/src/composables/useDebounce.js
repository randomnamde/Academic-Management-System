import { ref, watch, onUnmounted } from 'vue'
import { debounce, throttle } from '@/utils/debounce'

/**
 * 使用防抖值
 * @param {Ref|Function} source - Vue ref或getter函数
 * @param {number} delay - 延迟时间(毫秒)
 * @returns {Ref} 防抖后的值
 */
export function useDebouncedRef(source, delay = 300) {
  const value = ref(typeof source === 'function' ? source() : source.value)
  let debouncedFn = null

  if (typeof source === 'function') {
    debouncedFn = debounce(() => {
      value.value = source()
    }, delay)
  } else {
    debouncedFn = debounce(() => {
      value.value = source.value
    }, delay)
  }

  if (typeof source === 'function') {
    watch(source, debouncedFn)
  } else {
    watch(() => source.value, debouncedFn)
  }

  onUnmounted(() => {
    debouncedFn.cancel()
  })

  return value
}

/**
 * 使用防抖搜索
 * @param {Function} searchFn - 搜索函数
 * @param {number} delay - 延迟时间(毫秒)，默认300ms
 * @returns {Object} { search, loading, cancel }
 */
export function useDebouncedSearch(searchFn, delay = 300) {
  const loading = ref(false)
  const cancel = ref(null)

  const search = debounce(async (...args) => {
    loading.value = true
    try {
      await searchFn(...args)
    } finally {
      loading.value = false
    }
  }, delay)

  const cancelSearch = () => {
    search.cancel()
    loading.value = false
  }

  onUnmounted(() => {
    search.cancel()
  })

  return {
    search,
    loading,
    cancel: cancelSearch
  }
}

/**
 * 使用节流
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 间隔时间(毫秒)
 * @returns {Function} 节流后的函数
 */
export function useThrottle(fn, delay = 300) {
  const throttledFn = throttle(fn, delay)

  onUnmounted(() => {
    throttledFn.cancel()
  })

  return throttledFn
}

export default useDebouncedSearch
