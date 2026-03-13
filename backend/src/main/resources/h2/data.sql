INSERT INTO sys_user (id, username, password, real_name, phone, email, role, status) VALUES
(1, 'admin', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '绠＄悊鍛?, '13800000001', 'admin@school.com', 'SCHOOL_ADMIN', 1),
(2, 'T00CS20240001', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '寮犺€佸笀', '13800000002', 'teacher001@school.com', 'COURSE_TEACHER', 1),
(3, 'T00CS20240002', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '鏉庤€佸笀', '13800000003', 'teacher002@school.com', 'COURSE_TEACHER', 1),
(4, '2023SO0001', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '鐜嬪悓瀛?, '13800000004', 'student001@school.com', 'STUDENT', 1),
(5, '2023SO0002', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '鏉庡悓瀛?, '13800000005', 'student002@school.com', 'STUDENT', 1),
(6, 'college_admin_cs', '$2a$10$1ofEFy00WoJy6s65cvoJl..X8b8wS70ioX8CLNJqTTy2M.ctovW2O', '瀛﹂櫌绠＄悊鍛?, '13800000006', 'college-admin@school.com', 'COLLEGE_ADMIN', 1);

INSERT INTO college (id, college_code, college_name, college_name_en, description, status, admin_user_id) VALUES
(1, 'CS', '璁＄畻鏈哄闄?, '榛樿鍒濆鍖栧闄?, 1, 'college_admin_cs');

INSERT INTO teacher (id, user_id, teacher_no, name, gender, phone, email, title, department, college_id, hire_date, status) VALUES
(1, 'T00CS20240001', 'T00CS20240001', '寮犺€佸笀', 'MALE', '13800000002', 'teacher001@school.com', 'PROFESSOR', '璁＄畻鏈哄闄?, 1, DATE '2015-09-01', 1),
(2, 'T00CS20240002', 'T00CS20240002', '鏉庤€佸笀', 'FEMALE', '13800000003', 'teacher002@school.com', 'ASSOCIATE_PROFESSOR', '璁＄畻鏈哄闄?, 1, DATE '2018-09-01', 1);

INSERT INTO major (major_code, major_name, major_abbreviation, college_id, description, status) VALUES
('SOFT2301', '杞欢宸ョ▼', 'SOFT', 1, '榛樿鍒濆鍖栦笓涓?, 1),
('CSCI2301', '璁＄畻鏈虹瀛︿笌鎶€鏈?, 'CSCI', 1, '榛樿鍒濆鍖栦笓涓?, 1);

INSERT INTO class (id, class_name, class_code, grade, major_code, college_id, teacher_id, room, student_count, status) VALUES
(1, '杞欢宸ョ▼2301鐝?, 'CSXXSOFT20230001', 2023, 'SOFT2301', 1, 1, 'B101', 2, 1),
(2, '璁＄畻鏈虹瀛?301鐝?, 'CSXXCSCI20230002', 2023, 'CSCI2301', 1, 2, 'B102', 0, 1);

INSERT INTO student (id, user_id, student_no, name, gender, phone, email, class_id, enrollment_date, status) VALUES
(1, '2023SO0001', '2023SO0001', '鐜嬪悓瀛?, 'MALE', '13800000004', 'student001@school.com', 'CSXXSOFT20230001', DATE '2023-09-01', 'ENROLLED'),
(2, '2023SO0002', '2023SO0002', '鏉庡悓瀛?, 'FEMALE', '13800000005', 'student002@school.com', 'CSXXSOFT20230001', DATE '2023-09-01', 'ENROLLED');

INSERT INTO course (id, course_name, course_code, credit, hours, category, description, status) VALUES
(1, '楂樼瓑鏁板', 'MATH001', 4.0, 64, 'REQUIRED', '澶у楂樼瓑鏁板鍩虹璇剧▼', 1),
(2, '澶у鑻辫', 'ENG001', 3.0, 48, 'REQUIRED', '澶у鑻辫鍩虹璇剧▼', 1),
(3, '璁＄畻鏈哄璁?, 'CS001', 3.0, 48, 'REQUIRED', '璁＄畻鏈轰笓涓氬叆闂ㄨ绋?, 1),
(4, '鏁版嵁缁撴瀯', 'CS002', 4.0, 64, 'REQUIRED', '璁＄畻鏈烘牳蹇冧笓涓氳绋?, 1),
(5, 'Web寮€鍙戞妧鏈?, 'CS003', 2.0, 32, 'ELECTIVE', 'Web鍓嶅悗绔紑鍙戞妧鏈?, 1);

INSERT INTO course_arrangement (id, arrangement_code, course_id, teacher_id, class_id, semester, schedule, room, capacity, enrolled_count, status) VALUES
(1, 'C202400CS000100', 1, 1, 'CSXXSOFT20230001', '2024-2025-1', '鍛ㄤ竴 8:00-9:40', 'A101', 60, 2, 1),
(2, 'C202400CS000101', 2, 2, 'CSXXSOFT20230001', '2024-2025-1', '鍛ㄤ簩 8:00-9:40', 'A102', 60, 2, 1),
(3, 'C202400CS000102', 3, 1, 'CSXXSOFT20230001', '2024-2025-1', '鍛ㄤ笁 8:00-9:40', 'A103', 60, 2, 1),
(4, 'C202400CS000103', 4, 2, 'CSXXSOFT20230001', '2024-2025-1', '鍛ㄥ洓 8:00-9:40', 'A104', 60, 2, 1);

INSERT INTO score (id, student_id, course_arrangement_id, usual_score, midterm_score, final_score, total_score, gpa, status) VALUES
(1, '2023010001', 1, 85.00, 88.00, 90.00, 88.00, 3.70, 'NORMAL'),
(2, '2023010001', 2, 82.00, 85.00, 87.00, 85.00, 3.50, 'NORMAL'),
(3, '2023010002', 1, 90.00, 92.00, 95.00, 92.00, 4.00, 'NORMAL'),
(4, '2023010002', 2, 88.00, 90.00, 91.00, 90.00, 4.00, 'NORMAL');

INSERT INTO announcement (id, title, content, type, target_role, priority, author_id, view_count, is_top, status) VALUES
(1, '娆㈣繋浣跨敤瀛︾敓绠＄悊绯荤粺', '瀛︾敓绠＄悊绯荤粺姝ｅ紡涓婄嚎锛屾杩庝娇鐢ㄣ€?, 'NOTICE', 'ALL', 1, 'admin', 0, 1, 1);

INSERT INTO attendance (id, student_id, course_arrangement_id, attendance_date, status, check_in_time, remark) VALUES
(1, '2023010001', 1, DATE '2024-01-08', 'PRESENT', TIME '07:55:00', NULL),
(2, '2023010001', 2, DATE '2024-01-08', 'PRESENT', TIME '07:58:00', NULL),
(3, '2023010002', 1, DATE '2024-01-08', 'PRESENT', TIME '07:50:00', NULL),
(4, '2023010002', 2, DATE '2024-01-08', 'LATE', TIME '08:15:00', '浜ら€氭嫢鍫?);

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
('currentSemester', '2024-2025-1', '褰撳墠鐢熸晥瀛︽湡'),
('courseTimeSlots', '["08:00-09:40","10:00-11:40","14:00-15:40","16:00-17:40","19:00-20:40"]', '鎺掕鍜岃琛ㄧ粺涓€浣跨敤鐨勬椂闂存閰嶇疆');

INSERT INTO semester (id, semester_code, start_date, end_date, status, remark) VALUES
(1, '2024-2025-1', DATE '2024-09-01', DATE '2025-01-20', 'ACTIVE', '榛樿婵€娲诲鏈?);

