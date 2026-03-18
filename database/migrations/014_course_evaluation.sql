-- 课程评价表
CREATE TABLE IF NOT EXISTS course_evaluation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    course_arrangement_id BIGINT NOT NULL,
    teacher_no VARCHAR(20) NOT NULL,
    teaching_score DECIMAL(3,1) COMMENT '教学评分(1-5分)',
    content_score DECIMAL(3,1) COMMENT '内容评分',
    method_score DECIMAL(3,1) COMMENT '方法评分',
    overall_score DECIMAL(3,1) COMMENT '总体评分',
    comment TEXT COMMENT '评价内容',
    is_anonymous TINYINT DEFAULT 1 COMMENT '是否匿名',
    status ENUM('PENDING', 'SUBMITTED', 'PUBLISHED') DEFAULT 'PENDING',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_course (student_id, course_arrangement_id),
    INDEX idx_teacher_course (teacher_no, course_arrangement_id),
    INDEX idx_arrangement (course_arrangement_id),
    INDEX idx_status (status),
    CONSTRAINT fk_eval_student FOREIGN KEY (student_id) REFERENCES student(student_no),
    CONSTRAINT fk_eval_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id),
    CONSTRAINT fk_eval_teacher FOREIGN KEY (teacher_no) REFERENCES teacher(teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
