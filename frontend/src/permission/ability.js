const ROLE_ROUTE_MAP = {
  ADMIN: new Set([
    'Dashboard',
    'Analytics',
    'Student',
    'Teacher',
    'Class',
    'Course',
    'CourseArrangement',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile',
    'System'
  ]),
  TEACHER: new Set([
    'Dashboard',
    'Analytics',
    'Student',
    'Class',
    'Course',
    'CourseArrangement',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile'
  ]),
  STUDENT: new Set([
    'Dashboard',
    'Analytics',
    'Teacher',
    'Class',
    'Course',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile'
  ])
}

const ROLE_ACTION_MAP = {
  ADMIN: new Set([
    'score:create',
    'score:update',
    'score:delete',
    'attendance:create',
    'attendance:update',
    'attendance:delete',
    'announcement:create',
    'announcement:update',
    'announcement:delete',
    'announcement:publish',
    'leave:approve'
  ]),
  TEACHER: new Set([
    'score:create',
    'score:update',
    'score:delete',
    'attendance:create',
    'attendance:update',
    'attendance:delete',
    'announcement:create',
    'announcement:update',
    'announcement:delete',
    'announcement:publish',
    'leave:approve'
  ]),
  STUDENT: new Set([])
}

const normalizeRole = (role) => (role || '').toUpperCase()

const normalizePermissions = (permissions) =>
  Array.isArray(permissions) ? new Set(permissions.filter((item) => typeof item === 'string')) : new Set()

export const canRoute = (role, routeName, permissions = []) => {
  if (!routeName) return true
  const routeSet = ROLE_ROUTE_MAP[normalizeRole(role)] || new Set()
  if (routeSet.has(routeName)) return true

  const permissionSet = normalizePermissions(permissions)
  if (permissionSet.has('*:*') || permissionSet.has('*')) return true
  return permissionSet.has(`route:${routeName}:view`)
}

export const canAction = (role, actionCode, permissions = []) => {
  if (!actionCode) return true
  const actionSet = ROLE_ACTION_MAP[normalizeRole(role)] || new Set()
  if (actionSet.has(actionCode)) return true

  const permissionSet = normalizePermissions(permissions)
  if (permissionSet.has('*:*') || permissionSet.has('*')) return true
  return permissionSet.has(actionCode)
}
