export function buildRoleOptions(t) {
  return [
    { value: 'SCHOOL_ADMIN', label: t('roles.schoolAdmin') },
    { value: 'COLLEGE_ADMIN', label: t('roles.collegeAdmin') },
    { value: 'HOMEROOM_TEACHER', label: t('roles.homeroomTeacher') },
    { value: 'COURSE_TEACHER', label: t('roles.courseTeacher') },
    { value: 'STUDENT', label: t('roles.student') }
  ]
}

export function roleLabel(t, role) {
  if (role === 'SCHOOL_ADMIN') return t('roles.schoolAdmin')
  if (role === 'COLLEGE_ADMIN') return t('roles.collegeAdmin')
  if (role === 'HOMEROOM_TEACHER') return t('roles.homeroomTeacher')
  if (role === 'COURSE_TEACHER') return t('roles.courseTeacher')
  if (role === 'STUDENT') return t('roles.student')
  return role || '-'
}

export function roleBadgeType(role) {
  if (role === 'SCHOOL_ADMIN') return 'danger'
  if (role === 'COLLEGE_ADMIN') return 'warning'
  if (role === 'HOMEROOM_TEACHER' || role === 'COURSE_TEACHER') return 'info'
  if (role === 'STUDENT') return 'success'
  return 'info'
}
