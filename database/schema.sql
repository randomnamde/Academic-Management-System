-- 学生管理系统数据库设计
-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management;

-- 用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    role ENUM('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT') NOT NULL COMMENT '主角色',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 学院表
CREATE TABLE IF NOT EXISTS college (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    college_code VARCHAR(32) NOT NULL UNIQUE COMMENT '学院编码',
    college_name VARCHAR(100) NOT NULL COMMENT '学院名称',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    admin_user_id BIGINT UNIQUE COMMENT '学院管理员用户ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_college_code (college_code),
    CONSTRAINT fk_college_admin_user FOREIGN KEY (admin_user_id) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学院表';

-- 教师信息表
CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    teacher_no VARCHAR(20) NOT NULL UNIQUE COMMENT '教师编号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('MALE', 'FEMALE') NOT NULL COMMENT '性别',
    birthday DATE COMMENT '出生日期',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    title ENUM('LECTURER', 'ASSOCIATE_PROFESSOR', 'PROFESSOR') COMMENT '职称',
    department VARCHAR(100) COMMENT '所属院系',
    college_id BIGINT COMMENT '所属学院ID',
    hire_date DATE COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_teacher_college_id (college_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 专业表
CREATE TABLE IF NOT EXISTS major (
    major_code VARCHAR(8) PRIMARY KEY COMMENT '专业编码',
    major_name VARCHAR(100) NOT NULL COMMENT '专业名称',
    major_abbreviation VARCHAR(4) NOT NULL COMMENT '专业英文简称',
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    description VARCHAR(255) COMMENT '描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_major_college_id (college_id),
    CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='专业表';

-- 班级表
CREATE TABLE IF NOT EXISTS class (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '内部唯一ID',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    class_code VARCHAR(16) NOT NULL COMMENT '班级代码',
    grade YEAR NOT NULL COMMENT '年级',
    major_code VARCHAR(8) NOT NULL COMMENT '专业编码',
    college_id BIGINT NOT NULL COMMENT '所属学院ID',
    teacher_id BIGINT COMMENT '班主任ID',
    room VARCHAR(50) COMMENT '教室',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已毕业，1-在读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (class_code),
    UNIQUE KEY uk_class_id (id),
    INDEX idx_class_code (class_code),
    INDEX idx_class_college_id (college_id),
    INDEX idx_class_major_code (major_code),
    CONSTRAINT fk_class_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_major FOREIGN KEY (major_code) REFERENCES major(major_code) ON DELETE RESTRICT,
    CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 学生信息表
CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    student_no VARCHAR(20) NOT NULL UNIQUE COMMENT '学号',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender ENUM('MALE', 'FEMALE') NOT NULL COMMENT '性别',
    birthday DATE COMMENT '出生日期',
    id_card VARCHAR(18) COMMENT '身份证号',
    phone VARCHAR(20) COMMENT '联系电话',
    email VARCHAR(100) COMMENT '邮箱',
    address VARCHAR(255) COMMENT '家庭地址',
    class_id VARCHAR(16) COMMENT '班级编码',
    enrollment_date DATE COMMENT '入学日期',
    graduation_date DATE COMMENT '毕业日期',
    status ENUM('ENROLLED', 'GRADUATED', 'SUSPENDED', 'DROPPED') DEFAULT 'ENROLLED' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_no (student_no),
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(class_code) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_name VARCHAR(100) NOT NULL COMMENT '课程名称',
    course_code VARCHAR(20) NOT NULL UNIQUE COMMENT '课程代码',
    credit DECIMAL(3,1) NOT NULL COMMENT '学分',
    hours INT COMMENT '课时',
    category ENUM('REQUIRED', 'ELECTIVE', 'PRACTICAL') DEFAULT 'REQUIRED' COMMENT '课程类别',
    description TEXT COMMENT '课程描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-停用，1-启用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_course_code (course_code),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 授课安排表
CREATE TABLE IF NOT EXISTS course_arrangement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    arrangement_code VARCHAR(15) COMMENT '排课编号',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    class_id VARCHAR(16) NOT NULL COMMENT '班级编码',
    semester VARCHAR(20) NOT NULL COMMENT '学期（如：2023-2024-1）',
    schedule VARCHAR(100) COMMENT '上课时间安排',
    room VARCHAR(50) COMMENT '上课教室',
    capacity INT DEFAULT 50 COMMENT '容量',
    enrolled_count INT DEFAULT 0 COMMENT '已选人数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已满，1-可选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_arrangement_code (arrangement_code),
    INDEX idx_course_teacher (course_id, teacher_id),
    INDEX idx_class_semester (class_id, semester),
    CONSTRAINT fk_arrangement_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_arrangement_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
    CONSTRAINT fk_arrangement_class FOREIGN KEY (class_id) REFERENCES class(class_code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='授课安排表';

-- 成绩表
CREATE TABLE IF NOT EXISTS score (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_arrangement_id BIGINT NOT NULL COMMENT '授课安排ID',
    usual_score DECIMAL(5,2) COMMENT '平时成绩',
    midterm_score DECIMAL(5,2) COMMENT '期中成绩',
    final_score DECIMAL(5,2) COMMENT '期末成绩',
    total_score DECIMAL(5,2) COMMENT '总评成绩',
    gpa DECIMAL(3,2) COMMENT '绩点',
    exam_time DATETIME COMMENT '考试时间',
    status ENUM('NORMAL', 'MAKEUP', 'RETAKE') DEFAULT 'NORMAL' COMMENT '考试类型',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_course (student_id, course_arrangement_id),
    INDEX idx_student_id (student_id),
    CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_score_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 考勤表
CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_arrangement_id BIGINT NOT NULL COMMENT '授课安排ID',
    attendance_date DATE NOT NULL COMMENT '考勤日期',
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'LEAVE') NOT NULL COMMENT '考勤状态',
    check_in_time TIME COMMENT '签到时间',
    check_out_time TIME COMMENT '签退时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student_date (student_id, attendance_date),
    INDEX idx_course_date (course_arrangement_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤表';

-- 请假表
CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_arrangement_id BIGINT COMMENT '授课安排ID（可选）',
    leave_type ENUM('SICK', 'PERSONAL', 'OFFICIAL', 'OTHER') NOT NULL COMMENT '请假类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    reason TEXT NOT NULL COMMENT '请假原因',
    attachment VARCHAR(255) COMMENT '附件',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审批状态',
    workflow_type VARCHAR(20) COMMENT '流程类型',
    current_node VARCHAR(50) COMMENT '当前节点',
    final_status VARCHAR(20) COMMENT '最终状态',
    approver_id BIGINT COMMENT '审批教师ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(255) COMMENT '审批备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_status (student_id, status),
    INDEX idx_leave_node_status (current_node, status),
    CONSTRAINT fk_leave_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE SET NULL,
    CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假表';

-- 通知公告表
CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type ENUM('NOTICE', 'NEWS', 'EVENT', 'IMPORTANT') DEFAULT 'NOTICE' COMMENT '类型',
    target_role ENUM('ALL', 'SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT') DEFAULT 'ALL' COMMENT '目标角色',
    target_class_id VARCHAR(16) COMMENT '目标班级编码',
    priority TINYINT DEFAULT 0 COMMENT '优先级：0-普通，1-重要，2-紧急',
    author_id BIGINT NOT NULL COMMENT '发布人ID',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    is_top TINYINT DEFAULT 0 COMMENT '是否置顶：0-否，1-是',
    start_time DATETIME COMMENT '生效时间',
    end_time DATETIME COMMENT '过期时间',
    status TINYINT DEFAULT 1 COMMENT '状态：0-草稿，1-已发布，2-已撤回',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_type_status (type, status),
    INDEX idx_start_end (start_time, end_time),
    CONSTRAINT fk_announcement_author FOREIGN KEY (author_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_announcement_class FOREIGN KEY (target_class_id) REFERENCES class(class_code) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    operation VARCHAR(100) NOT NULL COMMENT '操作描述',
    method VARCHAR(500) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration BIGINT COMMENT '执行时长(ms)',
    status TINYINT COMMENT '操作状态：0-失败，1-成功',
    error_msg TEXT COMMENT '错误信息',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_time (user_id, create_time),
    CONSTRAINT fk_sys_log_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_user_role (user_id, role_code),
    INDEX idx_user_role_user_id (user_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 请假审批记录表
CREATE TABLE IF NOT EXISTS leave_request_approval (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    leave_request_id BIGINT NOT NULL COMMENT '请假单ID',
    node_code VARCHAR(50) COMMENT '流程节点编码',
    approver_user_id BIGINT COMMENT '审批用户ID',
    approver_teacher_id BIGINT COMMENT '审批教师ID',
    decision VARCHAR(20) COMMENT '审批结果',
    remark VARCHAR(255) COMMENT '备注',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    INDEX idx_leave_approval_request (leave_request_id),
    CONSTRAINT fk_leave_approval_request FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_approval_user FOREIGN KEY (approver_user_id) REFERENCES sys_user(id) ON DELETE SET NULL,
    CONSTRAINT fk_leave_approval_teacher FOREIGN KEY (approver_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假审批记录表';

-- 请假抄送记录表
CREATE TABLE IF NOT EXISTS leave_request_cc (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    leave_request_id BIGINT NOT NULL COMMENT '请假单ID',
    receiver_user_id BIGINT NOT NULL COMMENT '接收用户ID',
    receiver_teacher_id BIGINT COMMENT '接收教师ID',
    read_flag TINYINT DEFAULT 0 COMMENT '是否已读：0-未读，1-已读',
    read_time DATETIME COMMENT '阅读时间',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_leave_cc_receiver (receiver_user_id, read_flag),
    CONSTRAINT fk_leave_cc_request FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_cc_user FOREIGN KEY (receiver_user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_cc_teacher FOREIGN KEY (receiver_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假抄送记录表';

-- 系统配置表
CREATE TABLE IF NOT EXISTS sys_config (
    config_key VARCHAR(64) PRIMARY KEY COMMENT '配置键',
    config_value VARCHAR(255) COMMENT '配置值',
    description VARCHAR(255) COMMENT '描述',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统配置表';

-- 学期表
CREATE TABLE IF NOT EXISTS semester (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    semester_code VARCHAR(32) NOT NULL UNIQUE COMMENT '学期编码',
    start_date DATE NOT NULL COMMENT '开始日期',
    end_date DATE NOT NULL COMMENT '结束日期',
    status ENUM('PLANNED', 'ACTIVE', 'ENDED', 'ARCHIVED') NOT NULL DEFAULT 'PLANNED' COMMENT '状态',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_semester_code (semester_code),
    INDEX idx_semester_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学期主数据表';

-- 选课轮次表
CREATE TABLE IF NOT EXISTS selection_round (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    round_name VARCHAR(100) NOT NULL COMMENT '轮次名称',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-关闭，1-开启',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课轮次表';

-- 学生选课记录表
CREATE TABLE IF NOT EXISTS student_course_selection (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_arrangement_id BIGINT NOT NULL COMMENT '授课安排ID',
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'DROPPED') DEFAULT 'PENDING' COMMENT '选课状态',
    remark VARCHAR(255) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_student_arrangement (student_id, course_arrangement_id),
    INDEX idx_student_selection_student_id (student_id),
    CONSTRAINT fk_selection_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    CONSTRAINT fk_selection_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生选课记录表';

-- 分析与趋势查询索引
CREATE INDEX idx_score_total_create ON score(total_score, create_time);
CREATE INDEX idx_score_arrangement_total ON score(course_arrangement_id, total_score);
CREATE INDEX idx_attendance_status_scope_date ON attendance(status, course_arrangement_id, attendance_date);
CREATE INDEX idx_leave_pending_create ON leave_request(status, create_time);
CREATE INDEX idx_leave_arrangement_pending ON leave_request(course_arrangement_id, status, create_time);
