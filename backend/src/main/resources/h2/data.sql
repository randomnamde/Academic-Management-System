INSERT INTO sys_user (id, username, password, real_name, phone, email, status, role) VALUES
(1, 'admin', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'Demo Admin', '13800000001', 'admin@school.com', 1, 'SCHOOL_ADMIN'),
(2, 'teacher001', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'Teacher One', '13800000002', 'teacher001@school.com', 1, 'COURSE_TEACHER'),
(3, 'student001', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'Student One', '13800000003', 'student001@school.com', 1, 'STUDENT'),
(4, 'college_admin_cs', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'College Admin', '13800000004', 'college-admin@school.com', 1, 'COLLEGE_ADMIN');

INSERT INTO college (id, college_code, college_name, description, status, admin_user_id) VALUES
(1, 'CS', 'Computer Science College', 'Default seeded college', 1, 4);

INSERT INTO teacher (id, user_id, teacher_no, name, gender, phone, email, title, department, college_id, hire_date, status) VALUES
(1, 2, 'T2024001', 'Teacher One', 'MALE', '13800000002', 'teacher001@school.com', 'LECTURER', 'Computer Science', 1, DATE '2020-09-01', 1);

INSERT INTO class (id, class_name, class_code, grade, major, college_id, teacher_id, room, student_count, status) VALUES
(1, 'SE2301', 'SE2301', 2023, 'Software Engineering', 1, 1, 'B101', 1, 1);

INSERT INTO student (id, user_id, student_no, name, gender, phone, email, class_id, enrollment_date, status) VALUES
(1, 3, '2023010001', 'Student One', 'MALE', '13800000003', 'student001@school.com', 1, DATE '2023-09-01', 'ENROLLED');

INSERT INTO course (id, course_name, course_code, credit, hours, category, description, status) VALUES
(1, 'Data Structures', 'CS002', 4.0, 64, 'REQUIRED', 'Core course', 1);

INSERT INTO course_arrangement (id, course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status) VALUES
(1, 1, 1, 1, '2024-2025-1', 'Mon 08:00-09:40', 'A101', 60, 1, 1);

INSERT INTO score (id, student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status) VALUES
(1, 1, 1, 85.00, 88.00, 90.00, 88.00, 3.70, 'NORMAL');

INSERT INTO attendance (id, student_id, course_arrangement_id, attendance_date, status, check_in_time) VALUES
(1, 1, 1, DATE '2026-02-26', 'PRESENT', TIME '07:55:00');

INSERT INTO leave_request (id, student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, workflow_type, current_node, approver_id) VALUES
(1, 1, 1, 'SICK', TIMESTAMP '2026-02-26 08:00:00', TIMESTAMP '2026-02-26 10:00:00', 'Fever', 'PENDING', 'SHORT', 'PENDING_HOMEROOM_REVIEW', 1);

INSERT INTO announcement (id, title, content, type, target_role, priority, author_id, view_count, is_top, status) VALUES
(1, 'Welcome', 'Welcome to the student management system.', 'NOTICE', 'ALL', 1, 1, 0, 1, 1),
(2, 'Exam Notice', 'Final exam starts next week.', 'IMPORTANT', 'STUDENT', 2, 1, 0, 0, 1);

INSERT INTO sys_user_role (user_id, role_code) VALUES
(1, 'SCHOOL_ADMIN'),
(2, 'COURSE_TEACHER'),
(2, 'HOMEROOM_TEACHER'),
(3, 'STUDENT'),
(4, 'COLLEGE_ADMIN');

INSERT INTO sys_user_role (user_id, role_code)
SELECT t.user_id, 'COURSE_TEACHER'
FROM teacher t
WHERE t.user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = t.user_id AND ur.role_code = 'COURSE_TEACHER'
);

INSERT INTO sys_user_role (user_id, role_code)
SELECT t.user_id, 'HOMEROOM_TEACHER'
FROM class c
JOIN teacher t ON c.teacher_id = t.id
WHERE t.user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = t.user_id AND ur.role_code = 'HOMEROOM_TEACHER'
);

INSERT INTO sys_user_role (user_id, role_code)
SELECT s.user_id, 'STUDENT'
FROM student s
WHERE s.user_id IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM sys_user_role ur
    WHERE ur.user_id = s.user_id AND ur.role_code = 'STUDENT'
);

INSERT INTO sys_config (config_key, config_value, description) VALUES
('currentSemester', '2024-2025-1', 'Current semester for workflow and notifications');
