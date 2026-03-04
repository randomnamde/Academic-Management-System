-- 2026-03-04: unify role model to five runtime roles
-- Runtime roles:
-- SCHOOL_ADMIN, COLLEGE_ADMIN, HOMEROOM_TEACHER, COURSE_TEACHER, STUDENT

-- 1) Temporarily extend enum to include both legacy and target values.
ALTER TABLE sys_user
    MODIFY COLUMN role ENUM(
        'ADMIN',
        'TEACHER',
        'STUDENT',
        'SCHOOL_ADMIN',
        'COLLEGE_ADMIN',
        'HOMEROOM_TEACHER',
        'COURSE_TEACHER'
    ) NOT NULL;

-- 2) Ensure relation-table role entries exist for users who only had legacy role.
INSERT INTO sys_user_role (user_id, role_code)
SELECT u.id,
       CASE u.role
           WHEN 'ADMIN' THEN 'SCHOOL_ADMIN'
           WHEN 'TEACHER' THEN 'COURSE_TEACHER'
           WHEN 'STUDENT' THEN 'STUDENT'
       END AS role_code
FROM sys_user u
WHERE u.role IN ('ADMIN', 'TEACHER', 'STUDENT')
  AND NOT EXISTS (
      SELECT 1
      FROM sys_user_role ur
      WHERE ur.user_id = u.id
        AND ur.role_code = CASE u.role
            WHEN 'ADMIN' THEN 'SCHOOL_ADMIN'
            WHEN 'TEACHER' THEN 'COURSE_TEACHER'
            WHEN 'STUDENT' THEN 'STUDENT'
        END
  );

-- 3) Backfill sys_user.role from sys_user_role with fixed primary-role priority.
UPDATE sys_user u
LEFT JOIN (
    SELECT sur.user_id,
           SUBSTRING_INDEX(
               GROUP_CONCAT(
                   sur.role_code
                   ORDER BY FIELD(
                       sur.role_code,
                       'SCHOOL_ADMIN',
                       'COLLEGE_ADMIN',
                       'HOMEROOM_TEACHER',
                       'COURSE_TEACHER',
                       'STUDENT'
                   )
               ),
               ',',
               1
           ) AS primary_role
    FROM sys_user_role sur
    GROUP BY sur.user_id
) pr ON pr.user_id = u.id
SET u.role = COALESCE(
    pr.primary_role,
    CASE u.role
        WHEN 'ADMIN' THEN 'SCHOOL_ADMIN'
        WHEN 'TEACHER' THEN 'COURSE_TEACHER'
        WHEN 'STUDENT' THEN 'STUDENT'
        ELSE u.role
    END
);

-- 4) Remove legacy role codes from relation table.
DELETE FROM sys_user_role WHERE role_code IN ('ADMIN', 'TEACHER');

-- 5) Finalize sys_user.role enum to target set only.
ALTER TABLE sys_user
    MODIFY COLUMN role ENUM(
        'SCHOOL_ADMIN',
        'COLLEGE_ADMIN',
        'HOMEROOM_TEACHER',
        'COURSE_TEACHER',
        'STUDENT'
    ) NOT NULL;

-- 6) Temporarily extend announcement target-role enum for migration.
ALTER TABLE announcement
    MODIFY COLUMN target_role ENUM(
        'ALL',
        'STUDENT',
        'TEACHER',
        'ADMIN',
        'SCHOOL_ADMIN',
        'COLLEGE_ADMIN',
        'HOMEROOM_TEACHER',
        'COURSE_TEACHER'
    ) DEFAULT 'ALL';

-- 7) Split legacy teacher-target announcements to both teacher roles.
INSERT INTO announcement (
    title,
    content,
    type,
    target_role,
    target_class_id,
    priority,
    author_id,
    view_count,
    is_top,
    start_time,
    end_time,
    status,
    create_time,
    update_time
)
SELECT
    a.title,
    a.content,
    a.type,
    'HOMEROOM_TEACHER',
    a.target_class_id,
    a.priority,
    a.author_id,
    a.view_count,
    a.is_top,
    a.start_time,
    a.end_time,
    a.status,
    a.create_time,
    a.update_time
FROM announcement a
WHERE a.target_role = 'TEACHER';

INSERT INTO announcement (
    title,
    content,
    type,
    target_role,
    target_class_id,
    priority,
    author_id,
    view_count,
    is_top,
    start_time,
    end_time,
    status,
    create_time,
    update_time
)
SELECT
    a.title,
    a.content,
    a.type,
    'COURSE_TEACHER',
    a.target_class_id,
    a.priority,
    a.author_id,
    a.view_count,
    a.is_top,
    a.start_time,
    a.end_time,
    a.status,
    a.create_time,
    a.update_time
FROM announcement a
WHERE a.target_role = 'TEACHER';

-- 8) Split legacy admin-target announcements to both admin roles.
INSERT INTO announcement (
    title,
    content,
    type,
    target_role,
    target_class_id,
    priority,
    author_id,
    view_count,
    is_top,
    start_time,
    end_time,
    status,
    create_time,
    update_time
)
SELECT
    a.title,
    a.content,
    a.type,
    'SCHOOL_ADMIN',
    a.target_class_id,
    a.priority,
    a.author_id,
    a.view_count,
    a.is_top,
    a.start_time,
    a.end_time,
    a.status,
    a.create_time,
    a.update_time
FROM announcement a
WHERE a.target_role = 'ADMIN';

INSERT INTO announcement (
    title,
    content,
    type,
    target_role,
    target_class_id,
    priority,
    author_id,
    view_count,
    is_top,
    start_time,
    end_time,
    status,
    create_time,
    update_time
)
SELECT
    a.title,
    a.content,
    a.type,
    'COLLEGE_ADMIN',
    a.target_class_id,
    a.priority,
    a.author_id,
    a.view_count,
    a.is_top,
    a.start_time,
    a.end_time,
    a.status,
    a.create_time,
    a.update_time
FROM announcement a
WHERE a.target_role = 'ADMIN';

-- 9) Remove legacy announcement target-role records.
DELETE FROM announcement WHERE target_role IN ('TEACHER', 'ADMIN');

-- 10) Finalize announcement target-role enum to target set only.
ALTER TABLE announcement
    MODIFY COLUMN target_role ENUM(
        'ALL',
        'SCHOOL_ADMIN',
        'COLLEGE_ADMIN',
        'HOMEROOM_TEACHER',
        'COURSE_TEACHER',
        'STUDENT'
    ) DEFAULT 'ALL';
