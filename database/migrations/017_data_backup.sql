-- 数据备份记录表
CREATE TABLE IF NOT EXISTS data_backup (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    backup_name VARCHAR(100) NOT NULL COMMENT '备份名称',
    backup_type VARCHAR(20) NOT NULL COMMENT '备份类型: FULL-全量, PARTIAL-增量, SCHEDULE-定时',
    file_path VARCHAR(500) COMMENT '备份文件路径',
    file_size BIGINT COMMENT '文件大小(字节)',
    table_count INT DEFAULT 0 COMMENT '备份表数量',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待执行, RUNNING-执行中, SUCCESS-成功, FAILED-失败',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    operator_no VARCHAR(20) COMMENT '操作人编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据备份记录表';

-- 数据恢复记录表
CREATE TABLE IF NOT EXISTS data_restore (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    backup_id BIGINT NOT NULL COMMENT '关联备份ID',
    restore_name VARCHAR(100) NOT NULL COMMENT '恢复名称',
    backup_path VARCHAR(500) NOT NULL COMMENT '备份文件路径',
    tables_restored INT DEFAULT 0 COMMENT '恢复表数量',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING-待执行, RUNNING-执行中, SUCCESS-成功, FAILED-失败',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    operator_no VARCHAR(20) COMMENT '操作人编号',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_backup_id (backup_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据恢复记录表';

-- 备份策略配置表
CREATE TABLE IF NOT EXISTS backup_strategy (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    strategy_name VARCHAR(100) NOT NULL COMMENT '策略名称',
    backup_type VARCHAR(20) NOT NULL COMMENT '备份类型',
    schedule_cron VARCHAR(50) COMMENT '定时Cron表达式',
    retention_days INT DEFAULT 7 COMMENT '保留天数',
    is_enabled TINYINT DEFAULT 1 COMMENT '是否启用',
    tables_include TEXT COMMENT '包含的表(逗号分隔)',
    tables_exclude TEXT COMMENT '排除的表(逗号分隔)',
    last_execute_time DATETIME COMMENT '上次执行时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_schedule_cron (schedule_cron)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='备份策略配置表';
