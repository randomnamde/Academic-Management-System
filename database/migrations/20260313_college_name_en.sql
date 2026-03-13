SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'college'
          AND COLUMN_NAME = 'college_name_en'
    ),
    'SELECT 1',
    'ALTER TABLE college ADD COLUMN college_name_en VARCHAR(100) NULL AFTER college_name'
);
PREPARE ddl_stmt FROM @stmt;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;

UPDATE college
SET college_name_en = college_name
WHERE (college_name_en IS NULL OR college_name_en = '')
  AND college_name IS NOT NULL;

SET @stmt = IF(
    EXISTS (
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'college'
          AND COLUMN_NAME = 'college_name_en'
          AND IS_NULLABLE = 'YES'
    ),
    'ALTER TABLE college MODIFY COLUMN college_name_en VARCHAR(100) NOT NULL',
    'SELECT 1'
);
PREPARE ddl_stmt FROM @stmt;
EXECUTE ddl_stmt;
DEALLOCATE PREPARE ddl_stmt;
