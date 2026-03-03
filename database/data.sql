-- 初始化数据
USE student_management;

-- 插入系统用户（密码：123456，使用BCrypt加密）
INSERT INTO sys_user (username, password, real_name, phone, email, role, status) VALUES
('admin', '$2a$10$7JB720yubVSOSv9/DdBNEObud5IS9AbJhB4Va8HGJ5JHhB4Vq7y/6', '管理员', '13800000001', 'admin@school.com', 'ADMIN', 1),
('teacher001', '$2a$10$7JB720yubVSOSv9/DdBNEObud5IS9AbJhB4Va8HGJ5JHhB4Vq7y/6', '张老师', '13800000002', 'teacher001@school.com', 'TEACHER', 1),
('teacher002', '$2a$10$7JB720yubVSOSv9/DdBNEObud5IS9AbJhB4Va8HGJ5JHhB4Vq7y/6', '李老师', '13800000003', 'teacher002@school.com', 'TEACHER', 1),
('student001', '$2a$10$7JB720yubVSOSv9/DdBNEObud5IS9AbJhB4Va8HGJ5JHhB4Vq7y/6', '王同学', '13800000004', 'student001@school.com', 'STUDENT', 1),
('student002', '$2a$10$7JB720yubVSOSv9/DdBNEObud5IS9AbJhB4Va8HGJ5JHhB4Vq7y/6', '李同学', '13800000005', 'student002@school.com', 'STUDENT', 1);

-- 插入教师信息
INSERT INTO teacher (user_id, teacher_no, name, gender, phone, email, title, department, hire_date, status) VALUES
(2, 'T2024001', '张老师', 'MALE', '13800000002', 'teacher001@school.com', 'PROFESSOR', '计算机学院', '2015-09-01', 1),
(3, 'T2024002', '李老师', 'FEMALE', '13800000003', 'teacher002@school.com', 'ASSOCIATE_PROFESSOR', '计算机学院', '2018-09-01', 1);

-- 插入班级信息
INSERT INTO class (class_name, class_code, grade, major, teacher_id, room, student_count, status) VALUES
('软件工程2301班', 'SE2301', 2023, '软件工程', 1, 'B101', 2, 1),
('计算机科学2301班', 'CS2301', 2023, '计算机科学与技术', 2, 'B102', 0, 1);

-- 插入学生信息
INSERT INTO student (user_id, student_no, name, gender, phone, email, class_id, enrollment_date, status) VALUES
(4, '2023010001', '王同学', 'MALE', '13800000004', 'student001@school.com', 1, '2023-09-01', 'ENROLLED'),
(5, '2023010002', '李同学', 'FEMALE', '13800000005', 'student002@school.com', 1, '2023-09-01', 'ENROLLED');

-- 更新班级学生数量
UPDATE class SET student_count = 2 WHERE id = 1;

-- 插入课程信息
INSERT INTO course (course_name, course_code, credit, hours, category, description, status) VALUES
('高等数学', 'MATH001', 4.0, 64, 'REQUIRED', '大学高等数学基础课程', 1),
('大学英语', 'ENG001', 3.0, 48, 'REQUIRED', '大学英语基础课程', 1),
('计算机导论', 'CS001', 3.0, 48, 'REQUIRED', '计算机专业入门课程', 1),
('数据结构', 'CS002', 4.0, 64, 'REQUIRED', '计算机核心专业课程', 1),
('Web开发技术', 'CS003', 2.0, 32, 'ELECTIVE', 'Web前端与后端开发技术', 1);

-- 插入授课安排
INSERT INTO course_arrangement (course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status) VALUES
(1, 1, 1, '2024-2025-1', '周一 8:00-9:40', 'A101', 60, 2, 1),
(2, 2, 1, '2024-2025-1', '周二 8:00-9:40', 'A102', 60, 2, 1),
(3, 1, 1, '2024-2025-1', '周三 8:00-9:40', 'A103', 60, 2, 1),
(4, 2, 1, '2024-2025-1', '周四 8:00-9:40', 'A104', 60, 2, 1);

-- 插入成绩
INSERT INTO score (student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status) VALUES
(1, 1, 85.00, 88.00, 90.00, 88.00, 3.70, 'NORMAL'),
(1, 2, 82.00, 85.00, 87.00, 85.00, 3.50, 'NORMAL'),
(2, 1, 90.00, 92.00, 95.00, 92.00, 4.00, 'NORMAL'),
(2, 2, 88.00, 90.00, 91.00, 90.00, 4.00, 'NORMAL');

-- 插入通知公告
INSERT INTO announcement (title, content, type, target_role, priority, author_id, is_top, status) VALUES
('欢迎使用学生管理系统', '学生管理系统正式上线，欢迎使用！', 'NOTICE', 'ALL', 1, 1, 1, 1),
('期末考试安排通知', '本学期期末考试将于2024年1月15日至20日进行，请各位同学做好准备。', 'IMPORTANT', 'ALL', 2, 1, 0, 1),
('寒假放假通知', '学校将于2024年1月22日开始放寒假，请各位同学安排好行程。', 'NOTICE', 'ALL', 1, 1, 0, 1);

-- 插入考勤记录
INSERT INTO attendance (student_id, course_arrangement_id, attendance_date, status, check_in_time, remark) VALUES
(1, 1, '2024-01-08', 'PRESENT', '07:55:00', NULL),
(1, 2, '2024-01-08', 'PRESENT', '07:58:00', NULL),
(2, 1, '2024-01-08', 'PRESENT', '07:50:00', NULL),
(2, 2, '2024-01-08', 'LATE', '08:15:00', '交通拥堵');

-- ===== 2026-03 College + Multi-role + Leave Workflow seed =====
INSERT INTO college (id, college_code, college_name, description, status, admin_user_id)
VALUES (1, 'CS', 'Computer Science College', 'Default seeded college', 1, 1)
ON DUPLICATE KEY UPDATE college_name = VALUES(college_name), admin_user_id = VALUES(admin_user_id);

UPDATE teacher SET college_id = 1 WHERE college_id IS NULL;
UPDATE class SET college_id = 1 WHERE college_id IS NULL;

INSERT IGNORE INTO sys_user_role (user_id, role_code) VALUES
(1, 'SCHOOL_ADMIN'),
(2, 'COURSE_TEACHER'),
(2, 'HOMEROOM_TEACHER'),
(3, 'STUDENT');

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

INSERT INTO sys_config (config_key, config_value, description)
VALUES ('currentSemester', '2024-2025-1', 'Current semester for workflow and notifications')
ON DUPLICATE KEY UPDATE config_value = VALUES(config_value), description = VALUES(description);
