/**
 * 图片懒加载指令
 * 用法: v-lazy="imageUrl"
 * 或: v-lazy="{ src: imageUrl, placeholder: placeholderUrl }"
 */

// 存储observer实例
const observerMap = new Map()

export default {
  mounted(el, binding) {
    const { value } = binding

    if (!value) return

    // 处理参数格式
    const src = typeof value === 'string' ? value : value.src
    const placeholder = typeof value === 'object' ? value.placeholder : ''

    // 设置占位图或背景
    if (placeholder) {
      el.src = placeholder
    } else {
      el.style.backgroundColor = '#f5f7fa'
      el.style.backgroundImage = 'url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 24 24\' fill=\'none\' stroke=\'%23c0c4cc\' stroke-width=\'1\'%3E%3Crect x=\'3\' y=\'3\' width=\'18\' height=\'18\' rx=\'2\' ry=\'2\'/%3E%3Ccircle cx=\'8.5\' cy=\'8.5\' r=\'1.5\'/%3E%3Cpolyline points=\'21 15 16 10 5 21\'/%3E%3C/svg%3E")'
      el.style.backgroundRepeat = 'no-repeat'
      el.style.backgroundPosition = 'center'
      el.style.backgroundSize = '40px 40px'
    }

    // 保存原始src
    el.dataset.src = src
    el.dataset.loaded = 'false'

    // 使用IntersectionObserver监听元素
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          if (entry.isIntersecting) {
            const img = entry.target
            const imgSrc = img.dataset.src

            if (imgSrc && img.dataset.loaded === 'false') {
              // 创建新图片来预加载
              const tempImg = new Image()
              tempImg.onload = () => {
                img.src = imgSrc
                img.dataset.loaded = 'true'
                img.style.backgroundImage = 'none'
                img.style.backgroundColor = 'transparent'
              }
              tempImg.onerror = () => {
                // 加载失败使用默认占位
                img.style.backgroundImage = 'url("data:image/svg+xml,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 24 24\' fill=\'none\' stroke=\'%23fab6b6\' stroke-width=\'1\'%3E%3Crect x=\'3\' y=\'3\' width=\'18\' height=\'18\' rx=\'2\' ry=\'2\'/%3E%3Ccircle cx=\'8.5\' cy=\'8.5\' r=\'1.5\'/%3E%3Cpolyline points=\'21 15 16 10 5 21\'/%3E%3C/svg%3E")'
              }
              tempImg.src = imgSrc
            }

            // 停止观察已加载的图片
            observer.unobserve(img)
          }
        })
      },
      {
        rootMargin: '50px 0px',
        threshold: 0.1
      }
    )

    observer.observe(el)
    observerMap.set(el, observer)
  },
  updated(el, binding) {
    const { value } = binding

    if (!value) return

    const src = typeof value === 'string' ? value : value.src

    // 如果src变化，重新设置
    if (el.dataset.src !== src) {
      el.dataset.src = src
      el.dataset.loaded = 'false'

      const observer = observerMap.get(el)
      if (observer) {
        observer.observe(el)
      }
    }
  },
  unmounted(el) {
    const observer = observerMap.get(el)
    if (observer) {
      observer.disconnect()
      observerMap.delete(el)
    }
  }
}
