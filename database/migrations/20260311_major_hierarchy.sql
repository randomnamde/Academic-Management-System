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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Major table';

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'class'
          AND COLUMN_NAME = 'major_code'
    ),
    'SELECT 1',
    'ALTER TABLE class ADD COLUMN major_code VARCHAR(8) NULL'
);
PREPARE ddl_stmt FROM @stmt;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'class'
          AND INDEX_NAME = 'idx_class_major_code'
    ),
    'SELECT 1',
    'CREATE INDEX idx_class_major_code ON class(major_code)'
);
PREPARE ddl_stmt FROM @stmt;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;

INSERT INTO major (major_code, major_name, major_abbreviation, college_id, description, status)
SELECT 'SOFT2301', 'Software Engineering', 'SOFT', 1, 'Migrated seeded major', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM major WHERE major_code = 'SOFT2301');

INSERT INTO major (major_code, major_name, major_abbreviation, college_id, description, status)
SELECT 'CSCI2301', 'Computer Science', 'CSCI', 1, 'Migrated seeded major', 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM major WHERE major_code = 'CSCI2301');

UPDATE class SET major_code = 'SOFT2301' WHERE major_code IS NULL AND class_code = 'SE2301';
UPDATE class SET major_code = 'CSCI2301' WHERE major_code IS NULL AND class_code = 'CS2301';
