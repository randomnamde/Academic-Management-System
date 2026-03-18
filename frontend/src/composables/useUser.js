import { computed } from 'vue'
import store from '@/store'

export function useUserInfo() {
  const userInfo = computed(() => store.state.userInfo)
  const roles = computed(() => store.getters.userRoles)
  const isStudent = computed(() => userInfo.value?.role === 'STUDENT')
  const isTeacher = computed(() => userInfo.value?.role === 'TEACHER')
  const isAdmin = computed(() => ['SCHOOL_ADMIN', 'COLLEGE_ADMIN'].some(r => roles.value.includes(r)))

  return {
    userInfo,
    roles,
    isStudent,
    isTeacher,
    isAdmin
  }
}
