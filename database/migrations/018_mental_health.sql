-- 心理健康档案表
CREATE TABLE IF NOT EXISTS mental_health_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    record_date DATE NOT NULL COMMENT '档案日期',
    assessment_type VARCHAR(50) COMMENT '测评类型: SCL90-SCL90症状清单, SDS-抑郁自评, SAS-焦虑自评, GHQ-一般健康问卷',
    total_score DECIMAL(5,2) COMMENT '总分',
    somatic_score DECIMAL(5,2) COMMENT '躯体化得分',
    obsessive_score DECIMAL(5,2) COMMENT '强迫症状得分',
    interpersonal_score DECIMAL(5,2) COMMENT '人际关系敏感得分',
    depression_score DECIMAL(5,2) COMMENT '抑郁得分',
    anxiety_score DECIMAL(5,2) COMMENT '焦虑得分',
    hostility_score DECIMAL(5,2) COMMENT '敌对得分',
    terror_score DECIMAL(5,2) COMMENT '恐怖得分',
    paranoid_score DECIMAL(5,2) COMMENT '偏执得分',
    psychoticism_score DECIMAL(5,2) COMMENT '精神病性得分',
    risk_level VARCHAR(20) COMMENT '风险等级: NORMAL-正常, WARNING-预警, DANGER-危险',
    counselor_note TEXT COMMENT '咨询师备注',
    follow_up_status VARCHAR(20) DEFAULT 'PENDING' COMMENT '跟进状态: PENDING-待跟进, FOLLOWING-跟进中, CLOSED-已关闭',
    follow_up_date DATE COMMENT '跟进日期',
    assessor_no VARCHAR(20) COMMENT '评估教师编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student (student_no),
    INDEX idx_record_date (record_date),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理健康档案表';

-- 心理访谈记录表
CREATE TABLE IF NOT EXISTS mental_interview (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    interview_date DATETIME NOT NULL COMMENT '访谈日期',
    interview_type VARCHAR(50) COMMENT '访谈类型: INITIAL-初次访谈, FOLLOW-跟进访谈, CRISIS-危机访谈',
    interview_mode VARCHAR(20) COMMENT '访谈方式: FACE_TO_FACE-面谈, PHONE-电话, ONLINE-在线',
    topic VARCHAR(200) COMMENT '主题',
    content TEXT COMMENT '访谈内容',
    emotion_state VARCHAR(50) COMMENT '情绪状态',
    risk_indicator VARCHAR(20) COMMENT '风险指标',
    suggestion TEXT COMMENT '建议',
    next_plan TEXT COMMENT '后续计划',
    interviewer_no VARCHAR(20) NOT NULL COMMENT '访谈教师编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student (student_no),
    INDEX idx_interview_date (interview_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理访谈记录表';

-- 心理危机干预记录表
CREATE TABLE IF NOT EXISTS mental_crisis (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    student_no VARCHAR(20) NOT NULL COMMENT '学号',
    crisis_date DATETIME NOT NULL COMMENT '危机发生时间',
    crisis_type VARCHAR(50) COMMENT '危机类型: SUICIDE-自杀倾向, SELF_HARM-自伤, ABUSE-虐待, BULLYING-校园欺凌, OTHER-其他',
    risk_level VARCHAR(20) NOT NULL COMMENT '风险等级: LOW-低风险, MEDIUM-中风险, HIGH-高风险, EXTREME-极高风险',
    description TEXT COMMENT '情况描述',
    intervention_measures TEXT COMMENT '干预措施',
    outcome VARCHAR(50) COMMENT '处理结果: RESOLVED-已化解, MONITORING-持续关注, ESCALATED-已上报',
    related_persons TEXT COMMENT '相关人员',
    follow_up_plan TEXT COMMENT '后续跟进计划',
    reporter_no VARCHAR(20) COMMENT '上报人编号',
    handler_no VARCHAR(20) COMMENT '处理人编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_student (student_no),
    INDEX idx_risk_level (risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理危机干预记录表';
