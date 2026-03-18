-- 考试安排表
CREATE TABLE IF NOT EXISTS exam_arrangement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exam_code VARCHAR(20) UNIQUE NOT NULL,
    course_arrangement_id BIGINT NOT NULL,
    exam_type ENUM('MIDTERM', 'FINAL', 'MAKEUP', 'RETAKE') NOT NULL,
    exam_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    room VARCHAR(50) NOT NULL,
    capacity INT,
    enrolled_count INT DEFAULT 0,
    status ENUM('SCHEDULED', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_exam_date (exam_date),
    INDEX idx_room_date (room, exam_date),
    INDEX idx_course (course_arrangement_id),
    CONSTRAINT fk_exam_arrangement FOREIGN KEY (course_arrangement_id) REFERENCES course_arrangement(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 监考安排表
CREATE TABLE IF NOT EXISTS invigilator_arrangement (
    id BIGINT NOT NULL AUTO_INCREMENT,
    exam_arrangement_id BIGINT NOT NULL,
    teacher_no VARCHAR(20) NOT NULL,
    role ENUM('PRIMARY', 'SECONDARY') DEFAULT 'PRIMARY',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_exam_teacher (exam_arrangement_id, teacher_no),
    INDEX idx_exam (exam_arrangement_id),
    INDEX idx_teacher (teacher_no),
    CONSTRAINT fk_invigilator_exam FOREIGN KEY (exam_arrangement_id) REFERENCES exam_arrangement(id),
    CONSTRAINT fk_invigilator_teacher FOREIGN KEY (teacher_no) REFERENCES teacher(teacher_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
