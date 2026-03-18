/**
 * 权限指令
 * 用法：v-permission="'permission:student:create'"
 * 或：v-permission="['permission:student:create', 'permission:student:edit']"
 */

import { useStore } from 'vuex'

const useUserStore = () => {
  const store = useStore()
  return {
    permissions: store.state.user?.permissions || [],
    roles: store.state.user?.roles || [],
    ...store.state.user
  }
}

/**
 * 检查权限
 * @param {string|string[]} permission - 权限标识或权限数组
 * @param {string} type - 权限类型，可选值：'any'(满足任一)、'all'(满足全部)
 * @returns {boolean} 是否有权限
 */
export function hasPermission(permission, type = 'any') {
  const userStore = useUserStore()
  const permissions = userStore.permissions || []

  if (!permission) return true
  if (!permissions || permissions.length === 0) return false

  // 处理数组权限
  if (Array.isArray(permission)) {
    if (permission.length === 0) return true

    if (type === 'all') {
      return permission.every(perm => permissions.includes(perm))
    } else {
      return permission.some(perm => permissions.includes(perm))
    }
  }

  // 处理单个权限
  return permissions.includes(permission)
}

/**
 * 检查角色
 * @param {string|string[]} role - 角色标识或角色数组
 * @param {string} type - 角色类型，可选值：'any'(满足任一)、'all'(满足全部)
 * @returns {boolean} 是否有角色
 */
export function hasRole(role, type = 'any') {
  const userStore = useUserStore()
  const roles = userStore.roles || []

  if (!role) return true
  if (!roles || roles.length === 0) return false

  // 处理数组角色
  if (Array.isArray(role)) {
    if (role.length === 0) return true

    if (type === 'all') {
      return role.every(r => roles.includes(r))
    } else {
      return role.some(r => roles.includes(r))
    }
  }

  // 处理单个角色
  return roles.includes(role)
}

/**
 * 权限指令实现
 */
export default {
  mounted(el, binding) {
    const { value, modifiers } = binding

    // 如果没有绑定值，不执行权限检查
    if (!value) {
      return
    }

    // 获取权限检查类型
    const type = modifiers.all ? 'all' : 'any'

    // 检查是否有权限
    const hasAuth = hasPermission(value, type)

    if (!hasAuth) {
      // 移除元素
      const parent = el.parentNode
      if (parent) {
        parent.removeChild(el)
      } else {
        el.style.display = 'none'
      }
    }
  },

  updated(el, binding) {
    const { value, modifiers } = binding

    if (!value) {
      return
    }

    const type = modifiers.all ? 'all' : 'any'
    const hasAuth = hasPermission(value, type)

    if (!hasAuth) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

/**
 * 角色指令
 * 用法：v-role="'admin'"
 * 或：v-role="['admin', 'teacher']"
 */
export const roleDirective = {
  mounted(el, binding) {
    const { value, modifiers } = binding

    if (!value) {
      return
    }

    const type = modifiers.all ? 'all' : 'any'
    const hasAuth = hasRole(value, type)

    if (!hasAuth) {
      const parent = el.parentNode
      if (parent) {
        parent.removeChild(el)
      } else {
        el.style.display = 'none'
      }
    }
  },

  updated(el, binding) {
    const { value, modifiers } = binding

    if (!value) {
      return
    }

    const type = modifiers.all ? 'all' : 'any'
    const hasAuth = hasRole(value, type)

    if (!hasAuth) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

/**
 * 只读指令
 * 用法：v-readonly="'permission:student:edit'"
 * 当没有权限时，禁用表单元素
 */
export const readonlyDirective = {
  mounted(el, binding) {
    const { value } = binding

    if (!value) {
      return
    }

    const hasAuth = hasPermission(value)

    if (!hasAuth) {
      // 禁用表单元素
      if (el.tagName === 'INPUT' || el.tagName === 'SELECT' || el.tagName === 'TEXTAREA') {
        el.setAttribute('disabled', 'disabled')
        el.classList.add('is-disabled')
      } else {
        // 查找内部的可编辑元素
        const inputs = el.querySelectorAll('input, select, textarea, button')
        inputs.forEach(input => {
          input.setAttribute('disabled', 'disabled')
          input.classList.add('is-disabled')
        })
      }
    }
  }
}
