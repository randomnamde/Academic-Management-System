import { ref } from 'vue'

const message = ref('')
const type = ref('success')
const visible = ref(false)
let timer = null

export function useToast() {
  const show = (text, nextType = 'success', duration = 2200) => {
    message.value = text
    type.value = nextType
    visible.value = true
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      visible.value = false
      timer = null
    }, duration)
  }

  return {
    message,
    type,
    visible,
    show
  }
}
