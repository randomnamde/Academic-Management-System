CREATE DATABASE IF NOT EXISTS student_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE student_management;

CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    real_name VARCHAR(50),
    avatar VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(100),
    status TINYINT DEFAULT 1,
    role ENUM('SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT') NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (username),
    UNIQUE KEY uk_sys_user_id (id),
    INDEX idx_username (username),
    INDEX idx_role (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS college (
    id BIGINT NOT NULL AUTO_INCREMENT,
    college_code VARCHAR(32) NOT NULL UNIQUE,
    college_name VARCHAR(100) NOT NULL,
    college_name_en VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    admin_user_id VARCHAR(50) UNIQUE,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_college_id (id),
    INDEX idx_college_code (college_code),
    CONSTRAINT fk_college_admin_user FOREIGN KEY (admin_user_id) REFERENCES sys_user(username) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS teacher (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    teacher_no VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    birthday DATE,
    phone VARCHAR(20),
    email VARCHAR(100),
    title ENUM('LECTURER', 'ASSOCIATE_PROFESSOR', 'PROFESSOR'),
    department VARCHAR(100),
    college_id BIGINT,
    hire_date DATE,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (teacher_no),
    UNIQUE KEY uk_teacher_id (id),
    INDEX idx_teacher_no (teacher_no),
    INDEX idx_teacher_college_id (college_id),
    CONSTRAINT fk_teacher_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE CASCADE,
    CONSTRAINT fk_teacher_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS major (
    major_code VARCHAR(8) PRIMARY KEY,
    major_name VARCHAR(100) NOT NULL,
    major_abbreviation VARCHAR(4) NOT NULL,
    college_id BIGINT NOT NULL,
    description VARCHAR(255),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_major_college_id (college_id),
    CONSTRAINT fk_major_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS class (
    id BIGINT NOT NULL AUTO_INCREMENT,
    class_name VARCHAR(50) NOT NULL,
    class_code VARCHAR(16) NOT NULL,
    grade YEAR NOT NULL,
    major_code VARCHAR(8) NOT NULL,
    college_id BIGINT NOT NULL,
    teacher_id BIGINT,
    room VARCHAR(50),
    student_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (class_code),
    UNIQUE KEY uk_class_id (id),
    INDEX idx_class_code (class_code),
    INDEX idx_class_college_id (college_id),
    INDEX idx_class_major_code (major_code),
    CONSTRAINT fk_class_college FOREIGN KEY (college_id) REFERENCES college(id) ON DELETE RESTRICT,
    CONSTRAINT fk_class_major FOREIGN KEY (major_code) REFERENCES major(major_code) ON DELETE RESTRICT,
    CONSTRAINT fk_class_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    student_no VARCHAR(20) NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    birthday DATE,
    id_card VARCHAR(18),
    phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(255),
    class_id VARCHAR(16),
    enrollment_date DATE,
    graduation_date DATE,
    status ENUM('ENROLLED', 'GRADUATED', 'SUSPENDED', 'DROPPED') DEFAULT 'ENROLLED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (student_no),
    UNIQUE KEY uk_student_id (id),
    INDEX idx_class_id (class_id),
    INDEX idx_status (status),
    CONSTRAINT fk_student_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE CASCADE,
    CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(class_code) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course (
    id BIGINT NOT NULL AUTO_INCREMENT,
    course_name VARCHAR(100) NOT NULL,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    credit DECIMAL(3,1) NOT NULL,
    hours INT,
    category ENUM('REQUIRED', 'ELECTIVE', 'PRACTICAL') DEFAULT 'REQUIRED',
    description TEXT,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_id (id),
    INDEX idx_course_code (course_code),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS course_arrangement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    arrangement_code VARCHAR(15),
    course_id BIGINT NOT NULL,
    teacher_id BIGINT NOT NULL,
    class_id VARCHAR(16) NOT NULL,
    semester VARCHAR(20) NOT NULL,
    schedule VARCHAR(100),
    room VARCHAR(50),
    capacity INT DEFAULT 50,
    enrolled_count INT DEFAULT 0,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_course_arrangement_id (id),
    UNIQUE KEY uk_arrangement_code (arrangement_code),
    INDEX idx_course_teacher (course_id, teacher_id),
    INDEX idx_class_semester (class_id, semester),
    CONSTRAINT fk_arrangement_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    CONSTRAINT fk_arrangement_teacher FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE CASCADE,
    CONSTRAINT fk_arrangement_class FOREIGN KEY (class_id) REFERENCES class(class_code) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS score (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_arrangement_id BIGINT NOT NULL,
    usual_score DECIMAL(5,2),
    midterm_score DECIMAL(5,2),
    final_score DECIMAL(5,2),
    total_score DECIMAL(5,2),
    gpa DECIMAL(3,2),
    exam_time DATETIME,
    status ENUM('NORMAL', 'MAKEUP', 'RETAKE') DEFAULT 'NORMAL',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_score_id (id),
    UNIQUE KEY uk_student_course (student_id, course_arrangement_id),
    INDEX idx_student_id (student_id),
    CONSTRAINT fk_score_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE,
    CONSTRAINT fk_score_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS attendance (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_arrangement_id BIGINT NOT NULL,
    attendance_date DATE NOT NULL,
    status ENUM('PRESENT', 'ABSENT', 'LATE', 'LEAVE') NOT NULL,
    check_in_time TIME,
    check_out_time TIME,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_attendance_id (id),
    INDEX idx_student_date (student_id, attendance_date),
    INDEX idx_course_date (course_arrangement_id, attendance_date),
    CONSTRAINT fk_attendance_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE,
    CONSTRAINT fk_attendance_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS leave_request (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_arrangement_id BIGINT,
    leave_type ENUM('SICK', 'PERSONAL', 'OFFICIAL', 'OTHER') NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    reason TEXT NOT NULL,
    attachment VARCHAR(255),
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    workflow_type VARCHAR(20),
    current_node VARCHAR(50),
    final_status VARCHAR(20),
    approver_id BIGINT,
    approve_time DATETIME,
    approve_remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_leave_request_id (id),
    INDEX idx_student_status (student_id, status),
    INDEX idx_leave_node_status (current_node, status),
    CONSTRAINT fk_leave_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE,
    CONSTRAINT fk_leave_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE SET NULL,
    CONSTRAINT fk_leave_approver FOREIGN KEY (approver_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS announcement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    type ENUM('NOTICE', 'NEWS', 'EVENT', 'IMPORTANT') DEFAULT 'NOTICE',
    target_role ENUM('ALL', 'SCHOOL_ADMIN', 'COLLEGE_ADMIN', 'HOMEROOM_TEACHER', 'COURSE_TEACHER', 'STUDENT') DEFAULT 'ALL',
    target_class_id VARCHAR(16),
    priority TINYINT DEFAULT 0,
    author_id VARCHAR(50) NOT NULL,
    view_count INT DEFAULT 0,
    is_top TINYINT DEFAULT 0,
    start_time DATETIME,
    end_time DATETIME,
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_announcement_id (id),
    INDEX idx_type_status (type, status),
    INDEX idx_start_end (start_time, end_time),
    CONSTRAINT fk_announcement_author FOREIGN KEY (author_id) REFERENCES sys_user(username) ON DELETE CASCADE,
    CONSTRAINT fk_announcement_class FOREIGN KEY (target_class_id) REFERENCES class(class_code) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50),
    operation VARCHAR(100) NOT NULL,
    method VARCHAR(500),
    params TEXT,
    ip VARCHAR(50),
    duration BIGINT,
    status TINYINT,
    error_msg TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_log_id (id),
    INDEX idx_user_time (user_id, create_time),
    CONSTRAINT fk_sys_log_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_user_role (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id VARCHAR(50) NOT NULL,
    role_code VARCHAR(50) NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sys_user_role_id (id),
    UNIQUE KEY uk_user_role (user_id, role_code),
    INDEX idx_user_role_user_id (user_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES sys_user(username) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS leave_request_approval (
    id BIGINT NOT NULL AUTO_INCREMENT,
    leave_request_id BIGINT NOT NULL,
    node_code VARCHAR(50),
    approver_user_id VARCHAR(50),
    approver_teacher_id BIGINT,
    decision VARCHAR(20),
    remark VARCHAR(255),
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_leave_request_approval_id (id),
    INDEX idx_leave_approval_request (leave_request_id),
    CONSTRAINT fk_leave_approval_request FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_approval_user FOREIGN KEY (approver_user_id) REFERENCES sys_user(username) ON DELETE SET NULL,
    CONSTRAINT fk_leave_approval_teacher FOREIGN KEY (approver_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 请假抄送记录表
CREATE TABLE IF NOT EXISTS leave_request_cc (
    id BIGINT NOT NULL AUTO_INCREMENT,
    leave_request_id BIGINT NOT NULL,
    receiver_user_id VARCHAR(50) NOT NULL,
    receiver_teacher_id BIGINT,
    read_flag TINYINT DEFAULT 0,
    read_time DATETIME,
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_leave_request_cc_id (id),
    INDEX idx_leave_cc_receiver (receiver_user_id, read_flag),
    CONSTRAINT fk_leave_cc_request FOREIGN KEY (leave_request_id) REFERENCES leave_request(id) ON DELETE CASCADE,
    CONSTRAINT fk_leave_cc_user FOREIGN KEY (receiver_user_id) REFERENCES sys_user(username) ON DELETE CASCADE,
    CONSTRAINT fk_leave_cc_teacher FOREIGN KEY (receiver_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_config (
    config_key VARCHAR(64) PRIMARY KEY,
    config_value VARCHAR(255),
    description VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS semester (
    id BIGINT NOT NULL AUTO_INCREMENT,
    semester_code VARCHAR(32) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status ENUM('PLANNED', 'ACTIVE', 'ENDED', 'ARCHIVED') NOT NULL DEFAULT 'PLANNED',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_semester_id (id),
    INDEX idx_semester_code (semester_code),
    INDEX idx_semester_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS selection_round (
    id BIGINT NOT NULL AUTO_INCREMENT,
    round_name VARCHAR(100) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status TINYINT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_selection_round_id (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student_course_selection (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_arrangement_id BIGINT NOT NULL,
    status ENUM('PENDING', 'SUCCESS', 'FAILED', 'DROPPED') DEFAULT 'PENDING',
    remark VARCHAR(255),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_student_course_selection_id (id),
    UNIQUE KEY uk_student_arrangement (student_id, course_arrangement_id),
    INDEX idx_student_selection_student_id (student_id),
    CONSTRAINT fk_selection_student FOREIGN KEY (student_id) REFERENCES student(student_no) ON DELETE CASCADE,
    CONSTRAINT fk_selection_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Analysis indexes
CREATE INDEX idx_score_total_create ON score(total_score, create_time);
CREATE INDEX idx_score_arrangement_total ON score(course_arrangement_id, total_score);
CREATE INDEX idx_attendance_status_scope_date ON attendance(status, course_arrangement_id, attendance_date);
CREATE INDEX idx_leave_pending_create ON leave_request(status, create_time);
CREATE INDEX idx_leave_arrangement_pending ON leave_request(course_arrangement_id, status, create_time);



