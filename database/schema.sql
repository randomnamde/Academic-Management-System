-- 学生管理系统数据库设计
-- 创建数据库
CREATE DATABASE IF NOT EXISTS student_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management;

-- 用户表
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码',
    real_name VARCHAR(50) COMMENT '真实姓名',
    avatar VARCHAR(255) COMMENT '头像URL',
    phone VARCHAR(20) COMMENT '手机号码',
    email VARCHAR(100) COMMENT '邮箱',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    role ENUM('ADMIN', 'TEACHER', 'STUDENT') NOT NULL COMMENT '角色',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 班级表
CREATE TABLE class (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    class_name VARCHAR(50) NOT NULL COMMENT '班级名称',
    class_code VARCHAR(20) NOT NULL UNIQUE COMMENT '班级代码',
    grade YEAR NOT NULL COMMENT '年级',
    major VARCHAR(100) COMMENT '专业',
    teacher_id BIGINT COMMENT '班主任ID',
    room VARCHAR(50) COMMENT '教室',
    student_count INT DEFAULT 0 COMMENT '学生人数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已毕业，1-在读',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_class_code (class_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 教师信息表
CREATE TABLE teacher (
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
    hire_date DATE COMMENT '入职日期',
    status TINYINT DEFAULT 1 COMMENT '状态：0-离职，1-在职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_teacher_no (teacher_no),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师信息表';

-- 添加班级表的外键约束（放在教师表之后）
ALTER TABLE class ADD CONSTRAINT fk_class_teacher 
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL;

-- 学生信息表
CREATE TABLE student (
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
    class_id BIGINT COMMENT '班级ID',
    enrollment_date DATE COMMENT '入学日期',
    graduation_date DATE COMMENT '毕业日期',
    status ENUM('ENROLLED', 'GRADUATED', 'SUSPENDED', 'DROPPED') DEFAULT 'ENROLLED' COMMENT '状态',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_no (student_no),
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生信息表';

-- 课程表
CREATE TABLE course (
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
CREATE TABLE course_arrangement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    teacher_id BIGINT NOT NULL COMMENT '教师ID',
    class_id BIGINT NOT NULL COMMENT '班级ID',
    semester VARCHAR(20) NOT NULL COMMENT '学期（如：2023-2024-1）',
    schedule VARCHAR(100) COMMENT '上课时间安排',
    room VARCHAR(50) COMMENT '上课教室',
    capacity INT DEFAULT 50 COMMENT '容量',
    enrolled_count INT DEFAULT 0 COMMENT '已选人数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-已满，1-可选',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_course_teacher (course_id, teacher_id),
    INDEX idx_class_semester (class_id, semester),
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
    FOREIGN KEY (class_id) REFERENCES class(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='授课安排表';

-- 成绩表
CREATE TABLE score (
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
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 考勤表
CREATE TABLE attendance (
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
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='考勤表';

-- 请假表
CREATE TABLE leave_request (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    student_id BIGINT NOT NULL COMMENT '学生ID',
    course_arrangement_id BIGINT COMMENT '授课安排ID（可选）',
    leave_type ENUM('SICK', 'PERSONAL', 'OFFICIAL', 'OTHER') NOT NULL COMMENT '请假类型',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME NOT NULL COMMENT '结束时间',
    reason TEXT NOT NULL COMMENT '请假原因',
    attachment VARCHAR(255) COMMENT '附件（病假条等）',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING' COMMENT '审批状态',
    approver_id BIGINT COMMENT '审批人ID',
    approve_time DATETIME COMMENT '审批时间',
    approve_remark VARCHAR(255) COMMENT '审批备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_student_status (student_id, status),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE SET NULL,
    FOREIGN KEY (approver_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='请假表';

-- 通知公告表
CREATE TABLE announcement (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content TEXT NOT NULL COMMENT '内容',
    type ENUM('NOTICE', 'NEWS', 'EVENT', 'IMPORTANT') DEFAULT 'NOTICE' COMMENT '类型',
    target_role ENUM('ALL', 'STUDENT', 'TEACHER', 'ADMIN') DEFAULT 'ALL' COMMENT '目标角色',
    target_class_id BIGINT COMMENT '目标班级（为空时全员可见）',
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
    FOREIGN KEY (author_id) REFERENCES sys_user(id) ON DELETE CASCADE,
    FOREIGN KEY (target_class_id) REFERENCES class(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知公告表';

-- 操作日志表
CREATE TABLE sys_log (
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
    FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';
