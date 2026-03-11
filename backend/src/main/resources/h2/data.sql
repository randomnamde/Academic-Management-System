INSERT INTO sys_user (id, username, password, real_name, phone, email, role, status) VALUES
(1, 'admin', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '管理员', '13800000001', 'admin@school.com', 'SCHOOL_ADMIN', 1),
(2, 'teacher001', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '张老师', '13800000002', 'teacher001@school.com', 'COURSE_TEACHER', 1),
(3, 'teacher002', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '李老师', '13800000003', 'teacher002@school.com', 'COURSE_TEACHER', 1),
(4, 'student001', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '王同学', '13800000004', 'student001@school.com', 'STUDENT', 1),
(5, 'student002', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '李同学', '13800000005', 'student002@school.com', 'STUDENT', 1),
(6, 'college_admin_cs', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '学院管理员', '13800000006', 'college-admin@school.com', 'COLLEGE_ADMIN', 1);

INSERT INTO college (id, college_code, college_name, description, status, admin_user_id) VALUES
(1, 'CS', '计算机学院', '默认初始化学院', 1, 6);

INSERT INTO teacher (id, user_id, teacher_no, name, gender, phone, email, title, department, college_id, hire_date, status) VALUES
(1, 2, 'T2024001', '张老师', 'MALE', '13800000002', 'teacher001@school.com', 'PROFESSOR', '计算机学院', 1, DATE '2015-09-01', 1),
(2, 3, 'T2024002', '李老师', 'FEMALE', '13800000003', 'teacher002@school.com', 'ASSOCIATE_PROFESSOR', '计算机学院', 1, DATE '2018-09-01', 1);

INSERT INTO major (major_code, major_name, major_abbreviation, college_id, description, status) VALUES
('SOFT2301', '软件工程', 'SOFT', 1, '默认初始化专业', 1),
('CSCI2301', '计算机科学与技术', 'CSCI', 1, '默认初始化专业', 1);

INSERT INTO class (id, class_name, class_code, grade, major_code, college_id, teacher_id, room, student_count, status) VALUES
(1, '软件工程2301班', 'CSXXSOFT20230001', 2023, 'SOFT2301', 1, 1, 'B101', 2, 1),
(2, '计算机科学2301班', 'CSXXCSCI20230002', 2023, 'CSCI2301', 1, 2, 'B102', 0, 1);

INSERT INTO student (id, user_id, student_no, name, gender, phone, email, class_id, enrollment_date, status) VALUES
(1, 4, '2023010001', '王同学', 'MALE', '13800000004', 'student001@school.com', 'CSXXSOFT20230001', DATE '2023-09-01', 'ENROLLED'),
(2, 5, '2023010002', '李同学', 'FEMALE', '13800000005', 'student002@school.com', 'CSXXSOFT20230001', DATE '2023-09-01', 'ENROLLED');

INSERT INTO course (id, course_name, course_code, credit, hours, category, description, status) VALUES
(1, '高等数学', 'MATH001', 4.0, 64, 'REQUIRED', '大学高等数学基础课程', 1),
(2, '大学英语', 'ENG001', 3.0, 48, 'REQUIRED', '大学英语基础课程', 1),
(3, '计算机导论', 'CS001', 3.0, 48, 'REQUIRED', '计算机专业入门课程', 1),
(4, '数据结构', 'CS002', 4.0, 64, 'REQUIRED', '计算机核心专业课程', 1),
(5, 'Web开发技术', 'CS003', 2.0, 32, 'ELECTIVE', 'Web前后端开发技术', 1);

INSERT INTO course_arrangement (id, arrangement_code, course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status) VALUES
(1, 'C202400CS000100', 1, 1, 'CSXXSOFT20230001', '2024-2025-1', '周一 8:00-9:40', 'A101', 60, 2, 1),
(2, 'C202400CS000101', 2, 2, 'CSXXSOFT20230001', '2024-2025-1', '周二 8:00-9:40', 'A102', 60, 2, 1),
(3, 'C202400CS000102', 3, 1, 'CSXXSOFT20230001', '2024-2025-1', '周三 8:00-9:40', 'A103', 60, 2, 1),
(4, 'C202400CS000103', 4, 2, 'CSXXSOFT20230001', '2024-2025-1', '周四 8:00-9:40', 'A104', 60, 2, 1);

INSERT INTO score (id, student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status) VALUES
(1, 1, 1, 85.00, 88.00, 90.00, 88.00, 3.70, 'NORMAL'),
(2, 1, 2, 82.00, 85.00, 87.00, 85.00, 3.50, 'NORMAL'),
(3, 2, 1, 90.00, 92.00, 95.00, 92.00, 4.00, 'NORMAL'),
(4, 2, 2, 88.00, 90.00, 91.00, 90.00, 4.00, 'NORMAL');

INSERT INTO announcement (id, title, content, type, target_role, priority, author_id, view_count, is_top, status) VALUES
(1, '欢迎使用学生管理系统', '学生管理系统正式上线，欢迎使用。', 'NOTICE', 'ALL', 1, 1, 0, 1, 1);

INSERT INTO attendance (id, student_id, course_arrangement_id, attendance_date, status, check_in_time, remark) VALUES
(1, 1, 1, DATE '2024-01-08', 'PRESENT', TIME '07:55:00', NULL),
(2, 1, 2, DATE '2024-01-08', 'PRESENT', TIME '07:58:00', NULL),
(3, 2, 1, DATE '2024-01-08', 'PRESENT', TIME '07:50:00', NULL),
(4, 2, 2, DATE '2024-01-08', 'LATE', TIME '08:15:00', '交通拥堵');

INSERT INTO sys_user_role (id, user_id, role_code) VALUES
(1, 1, 'SCHOOL_ADMIN'),
(2, 2, 'COURSE_TEACHER'),
(3, 2, 'HOMEROOM_TEACHER'),
(4, 3, 'COURSE_TEACHER'),
(5, 3, 'HOMEROOM_TEACHER'),
(6, 4, 'STUDENT'),
(7, 5, 'STUDENT'),
(8, 6, 'COLLEGE_ADMIN');

INSERT INTO sys_config (config_key, config_value, description) VALUES
('currentSemester', '2024-2025-1', '当前生效学期'),
('courseTimeSlots', '["08:00-09:40","10:00-11:40","14:00-15:40","16:00-17:40","19:00-20:40"]', '排课和课表统一使用的时间段配置');

INSERT INTO semester (id, semester_code, start_date, end_date, status, remark) VALUES
(1, '2024-2025-1', DATE '2024-09-01', DATE '2025-01-20', 'ACTIVE', '默认激活学期');
