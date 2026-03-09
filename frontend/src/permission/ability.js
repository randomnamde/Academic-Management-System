const ROLE_ROUTE_MAP = {
  SCHOOL_ADMIN: new Set([
    'Dashboard',
    'Analytics',
    'Student',
    'Teacher',
    'Class',
    'CourseCenter',
    'Course',
    'College',
    'CourseArrangement',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile',
    'System',
    'Semester',
    'RBACCenter',
    'RBACUsers',
    'RBACUserAssignments',
    'RBACUserList',
    'RBACRoles',
    'RBACPermissions',
    'RBACAudit'
  ]),
  COLLEGE_ADMIN: new Set([
    'Dashboard',
    'Analytics',
    'Student',
    'Teacher',
    'Class',
    'CourseCenter',
    'Course',
    'College',
    'CourseArrangement',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile',
    'RBACCenter',
    'RBACUsers',
    'RBACUserList'
  ]),
  HOMEROOM_TEACHER: new Set([
    'Dashboard',
    'Analytics',
    'Student',
    'Class',
    'CourseCenter',
    'Course',
    'CourseArrangement',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile'
  ]),
  COURSE_TEACHER: new Set([
    'Dashboard',
    'Analytics',
    'Student',
    'Class',
    'CourseCenter',
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
    'CourseCenter',
    'CourseArrangement',
    'Score',
    'Attendance',
    'LeaveRequest',
    'Announcement',
    'Profile'
  ])
}

const ROLE_ACTION_MAP = {
  SCHOOL_ADMIN: new Set([
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
  COLLEGE_ADMIN: new Set([
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
  HOMEROOM_TEACHER: new Set([
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
  COURSE_TEACHER: new Set([
    'score:create',
    'score:update',
    'score:delete',
    'attendance:create',
    'attendance:update',
    'attendance:delete',
    'announcement:create',
    'announcement:update',
    'announcement:delete',
    'announcement:publish'
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
