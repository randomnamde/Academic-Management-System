/**
 * 权限控制 Composable
 * 提供权限相关的组合式 API
 */
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'
import { hasPermission as checkPermission, hasRole as checkRole } from '@/directives/permission'

/**
 * 权限控制 Hook
 */
export function usePermission() {
  const userStore = useUserStore()

  /**
   * 用户信息
   */
  const userInfo = computed(() => userStore.userInfo)
  const roles = computed(() => userStore.roles || [])
  const permissions = computed(() => userStore.permissions || [])

  /**
   * 检查权限
   */
  const hasPermission = (permission, type = 'any') => {
    return checkPermission(permission, type)
  }

  /**
   * 检查角色
   */
  const hasRole = (role, type = 'any') => {
    return checkRole(role, type)
  }

  /**
   * 是否是管理员
   */
  const isAdmin = computed(() => {
    return roles.value.includes('admin')
  })

  /**
   * 是否是教师
   */
  const isTeacher = computed(() => {
    return roles.value.includes('teacher')
  })

  /**
   * 是否是学生
   */
  const isStudent = computed(() => {
    return roles.value.includes('student')
  })

  /**
   * 是否是辅导员
   */
  const isCounselor = computed(() => {
    return roles.value.includes('counselor')
  })

  /**
   * 是否有任意一个指定权限
   */
  const hasAnyPermission = (permissionList) => {
    return checkPermission(permissionList, 'any')
  }

  /**
   * 是否有全部指定权限
   */
  const hasAllPermissions = (permissionList) => {
    return checkPermission(permissionList, 'all')
  }

  /**
   * 是否有任意一个指定角色
   */
  const hasAnyRole = (roleList) => {
    return checkRole(roleList, 'any')
  }

  /**
   * 是否有全部指定角色
   */
  const hasAllRoles = (roleList) => {
    return checkRole(roleList, 'all')
  }

  /**
   * 检查是否可以访问指定资源
   * @param {string} resource - 资源类型，如 'student', 'course' 等
   * @param {string} action - 操作类型，如 'create', 'read', 'update', 'delete'
   */
  const canAccess = (resource, action) => {
    const permission = `permission:${resource}:${action}`
    return hasPermission(permission)
  }

  return {
    // 用户信息
    userInfo,
    roles,
    permissions,

    // 角色判断
    isAdmin,
    isTeacher,
    isStudent,
    isCounselor,

    // 权限检查方法
    hasPermission,
    hasRole,
    hasAnyPermission,
    hasAllPermissions,
    hasAnyRole,
    hasAllRoles,
    canAccess
  }
}

export default usePermission
