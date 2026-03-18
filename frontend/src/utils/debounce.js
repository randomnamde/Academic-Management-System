/**
 * 防抖函数
 * @param {Function} fn - 要防抖的函数
 * @param {number} delay - 延迟时间(毫秒)，默认300ms
 * @returns {Function} 防抖后的函数
 */
export function debounce(fn, delay = 300) {
  let timer = null

  const debounced = function (...args) {
    if (timer) {
      clearTimeout(timer)
    }

    timer = setTimeout(() => {
      fn.apply(this, args)
      timer = null
    }, delay)
  }

  // 立即执行版本
  debounced.immediate = function (fn, delay = 300) {
    let timer = null
    return function (...args) {
      if (timer) {
        clearTimeout(timer)
      }

      const callNow = !timer

      timer = setTimeout(() => {
        timer = null
      }, delay)

      if (callNow) {
        fn.apply(this, args)
      }
    }
  }

  // 取消函数
  debounced.cancel = function () {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return debounced
}

/**
 * 节流函数
 * @param {Function} fn - 要节流的函数
 * @param {number} delay - 间隔时间(毫秒)，默认300ms
 * @returns {Function} 节流后的函数
 */
export function throttle(fn, delay = 300) {
  let last = 0
  let timer = null

  const throttled = function (...args) {
    const now = Date.now()

    if (now - last >= delay) {
      last = now
      fn.apply(this, args)
    } else if (!timer) {
      timer = setTimeout(() => {
        last = Date.now()
        timer = null
        fn.apply(this, args)
      }, delay - (now - last))
    }
  }

  // 取消函数
  throttled.cancel = function () {
    if (timer) {
      clearTimeout(timer)
      timer = null
    }
  }

  return throttled
}

export default debounce
