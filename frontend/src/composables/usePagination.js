import { ref } from 'vue'

export function usePagination() {
  const page = ref(1)
  const size = ref(10)
  const total = ref(0)
  const loading = ref(false)
  const tableData = ref([])

  function handlePageChange(newPage) {
    page.value = newPage
  }

  function handleSizeChange(newSize) {
    size.value = newSize
    page.value = 1
  }

  return {
    page,
    size,
    total,
    loading,
    tableData,
    handlePageChange,
    handleSizeChange
  }
}
