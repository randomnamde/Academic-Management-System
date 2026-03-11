ALTER TABLE class
    MODIFY class_code VARCHAR(16) NOT NULL;

ALTER TABLE class
    DROP PRIMARY KEY,
    ADD UNIQUE KEY uk_class_id (id),
    ADD PRIMARY KEY (class_code);

UPDATE class
SET class_code = 'CSXXSOFT20230001'
WHERE class_code = 'SE2301';

UPDATE class
SET class_code = 'CSXXCSCI20230002'
WHERE class_code = 'CS2301';
