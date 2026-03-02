import { reactive, ref } from 'vue'

export function useTablePage(defaultSearch = {}) {
  const loading = ref(false)
  const page = ref(1)
  const size = ref(10)
  const total = ref(0)
  const records = ref([])
  const search = reactive({ ...defaultSearch })

  async function run(fetcher) {
    loading.value = true
    try {
      const res = await fetcher({
        page: page.value,
        size: size.value,
        ...search
      })
      records.value = res?.data?.records || []
      total.value = Number(res?.data?.total || 0)
    } finally {
      loading.value = false
    }
  }

  function resetSearch() {
    Object.keys(search).forEach((key) => {
      search[key] = defaultSearch[key] ?? ''
    })
    page.value = 1
  }

  return {
    loading,
    page,
    size,
    total,
    records,
    search,
    run,
    resetSearch
  }
}
