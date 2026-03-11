<template>
  <el-breadcrumb separator="/">
    <el-breadcrumb-item
      v-for="item in breadcrumbItems"
      :key="item.path"
      :to="item.path === route.path ? undefined : item.path"
    >
      {{ item.title }}
    </el-breadcrumb-item>
  </el-breadcrumb>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const normalizePath = (path = '') => {
  if (!path) return ''
  return path.startsWith('/') ? path : `/${path}`
}

const resolveRouteTitle = (record) => {
  if (!record?.meta) return ''
  return record.meta.titleKey ? t(record.meta.titleKey) : (record.meta.title || '')
}

const routeRecordMap = computed(() => {
  const map = new Map()
  const visit = (records = []) => {
    records.forEach((record) => {
      const fullPath = normalizePath(record.path)
      if (fullPath) {
        map.set(fullPath, record)
      }
      if (Array.isArray(record.children) && record.children.length) {
        visit(record.children)
      }
    })
  }
  visit(router.options.routes || [])
  return map
})

const breadcrumbItems = computed(() => {
  const chain = []
  const seen = new Set()

  const appendByPath = (path) => {
    const normalizedPath = normalizePath(path)
    if (!normalizedPath || seen.has(normalizedPath)) return

    const record = routeRecordMap.value.get(normalizedPath)
    if (!record?.meta || (!record.meta.titleKey && !record.meta.title)) return

    const parentPath = record.meta?.breadcrumbParent
    if (parentPath) {
      appendByPath(parentPath)
    }

    seen.add(normalizedPath)
    chain.push({
      path: normalizedPath,
      title: resolveRouteTitle(record)
    })
  }

  route.matched
    .filter((item) => item.path && item.meta && (item.meta.titleKey || item.meta.title))
    .forEach((item) => {
      const parentPath = item.meta?.breadcrumbParent
      if (parentPath) {
        appendByPath(parentPath)
      }

      const itemPath = normalizePath(item.path)
      if (!itemPath || seen.has(itemPath)) return
      seen.add(itemPath)
      chain.push({
        path: itemPath,
        title: resolveRouteTitle(item)
      })
    })

  return chain
})
</script>
