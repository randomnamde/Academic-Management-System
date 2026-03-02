import { reactive, ref } from 'vue'

export function useDialogForm(initialValue = {}) {
  const visible = ref(false)
  const isEdit = ref(false)
  const form = reactive({ ...initialValue })

  function reset() {
    Object.keys(form).forEach((key) => {
      form[key] = initialValue[key] ?? null
    })
  }

  function openCreate() {
    isEdit.value = false
    reset()
    visible.value = true
  }

  function openEdit(payload = {}) {
    isEdit.value = true
    reset()
    Object.assign(form, payload)
    visible.value = true
  }

  function close() {
    visible.value = false
  }

  return {
    visible,
    isEdit,
    form,
    reset,
    openCreate,
    openEdit,
    close
  }
}
