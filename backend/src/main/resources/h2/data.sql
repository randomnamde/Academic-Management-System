INSERT INTO sys_user (id, username, password, real_name, phone, email, status, role) VALUES
(1, 'demo_admin', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'Demo Admin', '13800000001', 'admin@school.com', 1, 'ADMIN'),
(2, 'teacher001', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'Teacher One', '13800000002', 'teacher001@school.com', 1, 'TEACHER'),
(3, 'student001', '$2a$10$uwNOzFPaw6z3fyiwMkxuouAgn7y4UCxSY71t8se/G0HpyyTYbYE9y', 'Student One', '13800000003', 'student001@school.com', 1, 'STUDENT');

INSERT INTO teacher (id, user_id, teacher_no, name, gender, phone, email, title, department, hire_date, status) VALUES
(1, 2, 'T2024001', 'Teacher One', 'MALE', '13800000002', 'teacher001@school.com', 'LECTURER', 'Computer Science', DATE '2020-09-01', 1);

INSERT INTO class (id, class_name, class_code, grade, major, teacher_id, room, student_count, status) VALUES
(1, 'SE2301', 'SE2301', 2023, 'Software Engineering', 1, 'B101', 1, 1);

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

INSERT INTO leave_request (id, student_id, course_arrangement_id, leave_type, start_time, end_time, reason, status, approver_id) VALUES
(1, 1, 1, 'SICK', TIMESTAMP '2026-02-26 08:00:00', TIMESTAMP '2026-02-26 10:00:00', 'Fever', 'PENDING', 1);

INSERT INTO announcement (id, title, content, type, target_role, priority, author_id, view_count, is_top, status) VALUES
(1, 'Welcome', 'Welcome to the student management system.', 'NOTICE', 'ALL', 1, 1, 0, 1, 1),
(2, 'Exam Notice', 'Final exam starts next week.', 'IMPORTANT', 'STUDENT', 2, 1, 0, 0, 1);
