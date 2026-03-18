INSERT INTO sys_user (id, username, password, real_name, phone, email, role, status) VALUES
(1, 'admin', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', 'System Admin', '13800000001', 'admin@school.com', 'SCHOOL_ADMIN', 1),
(2, 'T00CS20240001', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', 'Teacher Zhang', '13800000002', 'teacher001@school.com', 'COURSE_TEACHER', 1),
(3, 'T00CS20240002', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', 'Teacher Li', '13800000003', 'teacher002@school.com', 'COURSE_TEACHER', 1),
(4, '2023SO0001', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', 'Student Wang', '13800000004', 'student001@school.com', 'STUDENT', 1),
(5, '2023SO0002', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', 'Student Zhao', '13800000005', 'student002@school.com', 'STUDENT', 1),
(6, 'college_admin_cs', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', 'College Admin', '13800000006', 'college-admin@school.com', 'COLLEGE_ADMIN', 1);

INSERT INTO college (id, college_code, college_name, college_name_en, description, status, admin_user_id) VALUES
(1, 'CS', 'Computer Science College', 'Computer Science', 'Default initialized college', 1, 'college_admin_cs');

INSERT INTO teacher (id, user_id, teacher_no, name, gender, phone, email, title, department, college_code, hire_date, status) VALUES
(1, 'T00CS20240001', 'T00CS20240001', 'Teacher Zhang', 'MALE', '13800000002', 'teacher001@school.com', 'PROFESSOR', 'Computer Science', 'CS', DATE '2015-09-01', 1),
(2, 'T00CS20240002', 'T00CS20240002', 'Teacher Li', 'FEMALE', '13800000003', 'teacher002@school.com', 'ASSOCIATE_PROFESSOR', 'Computer Science', 'CS', DATE '2018-09-01', 1);

INSERT INTO major (major_code, major_name, major_abbreviation, college_code, description, status) VALUES
('SOFT2301', 'Software Engineering', 'SOFT', 'CS', 'Default initialized major', 1),
('CSCI2301', 'Computer Science and Technology', 'CSCI', 'CS', 'Default initialized major', 1);

INSERT INTO class (id, class_name, class_code, grade, major_code, college_code, teacher_no, room, student_count, status) VALUES
(1, 'Software 2301', 'CSXXSOFT20230001', 2023, 'SOFT2301', 'CS', 'T00CS20240001', 'B101', 2, 1),
(2, 'CS 2301', 'CSXXCSCI20230002', 2023, 'CSCI2301', 'CS', 'T00CS20240002', 'B102', 0, 1);

INSERT INTO student (id, user_id, student_no, name, gender, phone, email, class_id, enrollment_date, status) VALUES
(1, '2023SO0001', '2023SO0001', 'Student Wang', 'MALE', '13800000004', 'student001@school.com', 'CSXXSOFT20230001', DATE '2023-09-01', 'ENROLLED'),
(2, '2023SO0002', '2023SO0002', 'Student Zhao', 'FEMALE', '13800000005', 'student002@school.com', 'CSXXSOFT20230001', DATE '2023-09-01', 'ENROLLED');

INSERT INTO course (id, course_name, course_code, credit, hours, category, description, status) VALUES
(1, 'Advanced Mathematics', 'MATH001', 4.0, 64, 'REQUIRED', 'Core mathematics course', 1),
(2, 'College English', 'ENG001', 3.0, 48, 'REQUIRED', 'Core language course', 1),
(3, 'Introduction to Computing', 'CS001', 3.0, 48, 'REQUIRED', 'Introductory CS course', 1),
(4, 'Data Structures', 'CS002', 4.0, 64, 'REQUIRED', 'Core CS course', 1),
(5, 'Web Development', 'CS003', 2.0, 32, 'ELECTIVE', 'Web development technologies', 1);

INSERT INTO course_arrangement (id, arrangement_code, course_code, teacher_no, class_id, semester, schedule, room, capacity, enrolled_count, status) VALUES
(1, 'C202400CS000100', 'MATH001', 'T00CS20240001', 'CSXXSOFT20230001', '2024-2025-1', 'Mon 08:00-09:40', 'A101', 60, 2, 1),
(2, 'C202400CS000101', 'ENG001', 'T00CS20240002', 'CSXXSOFT20230001', '2024-2025-1', 'Tue 08:00-09:40', 'A102', 60, 2, 1),
(3, 'C202400CS000102', 'CS001', 'T00CS20240001', 'CSXXSOFT20230001', '2024-2025-1', 'Wed 08:00-09:40', 'A103', 60, 2, 1),
(4, 'C202400CS000103', 'CS002', 'T00CS20240002', 'CSXXSOFT20230001', '2024-2025-1', 'Thu 08:00-09:40', 'A104', 60, 2, 1);

INSERT INTO score (id, student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status) VALUES
(1, '2023SO0001', 1, 85.00, 88.00, 90.00, 88.00, 3.70, 'NORMAL'),
(2, '2023SO0001', 2, 82.00, 85.00, 87.00, 85.00, 3.50, 'NORMAL'),
(3, '2023SO0002', 1, 90.00, 92.00, 95.00, 92.00, 4.00, 'NORMAL'),
(4, '2023SO0002', 2, 88.00, 90.00, 91.00, 90.00, 4.00, 'NORMAL');

INSERT INTO attendance (id, student_id, course_arrangement_id, attendance_date, status, check_in_time, remark) VALUES
(1, '2023SO0001', 1, DATE '2024-01-08', 'PRESENT', TIME '07:55:00', NULL),
(2, '2023SO0001', 2, DATE '2024-01-08', 'PRESENT', TIME '07:58:00', NULL),
(3, '2023SO0002', 1, DATE '2024-01-08', 'PRESENT', TIME '07:50:00', NULL),
(4, '2023SO0002', 2, DATE '2024-01-08', 'LATE', TIME '08:15:00', 'Traffic jam');

INSERT INTO announcement (id, title, content, type, target_role, priority, author_id, view_count, is_top, status) VALUES
(1, 'Welcome', 'Student management system is online.', 'NOTICE', 'ALL', 1, 'admin', 0, 1, 1);

INSERT INTO sys_user_role (id, user_id, role_code) VALUES
(1, 'admin', 'SCHOOL_ADMIN'),
(2, 'T00CS20240001', 'COURSE_TEACHER'),
(3, 'T00CS20240001', 'HOMEROOM_TEACHER'),
(4, 'T00CS20240002', 'COURSE_TEACHER'),
(5, 'T00CS20240002', 'HOMEROOM_TEACHER'),
(6, '2023SO0001', 'STUDENT'),
(7, '2023SO0002', 'STUDENT'),
(8, 'college_admin_cs', 'COLLEGE_ADMIN');

INSERT INTO sys_config (config_key, config_value, description) VALUES
('currentSemester', '2024-2025-1', 'Current active semester'),
('courseTimeSlots', '["08:00-09:40","10:00-11:40","14:00-15:40","16:00-17:40","19:00-20:40"]', 'Unified time slots for schedule');

INSERT INTO semester (id, semester_code, start_date, end_date, status, remark) VALUES
(1, '2024-2025-1', DATE '2024-09-01', DATE '2025-01-20', 'ACTIVE', 'Default active semester');
