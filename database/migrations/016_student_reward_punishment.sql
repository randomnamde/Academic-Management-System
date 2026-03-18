-- 学生奖惩记录表
CREATE TABLE IF NOT EXISTS student_reward_punishment (
    id BIGINT NOT NULL AUTO_INCREMENT,
    student_id VARCHAR(20) NOT NULL,
    type ENUM('REWARD', 'PUNISHMENT') NOT NULL,
    category VARCHAR(50) NOT NULL COMMENT '奖励/惩罚类别',
    reason TEXT NOT NULL,
    amount DECIMAL(10,2) COMMENT '金额（奖学金/罚款）',
    status ENUM('PENDING', 'APPROVED', 'REJECTED') DEFAULT 'PENDING',
    approver_no VARCHAR(20),
    approve_time DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_student_type (student_id, type),
    INDEX idx_student_status (student_id, status),
    INDEX idx_category (category),
    CONSTRAINT fk_rp_student FOREIGN KEY (student_id) REFERENCES student(student_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
