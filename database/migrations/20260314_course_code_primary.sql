-- Migrate course PK and FK references from id to course_code
START TRANSACTION;

ALTER TABLE course_arrangement ADD COLUMN course_code_tmp VARCHAR(20) NULL;
UPDATE course_arrangement ca JOIN course c ON ca.course_id = c.id SET ca.course_code_tmp = c.course_code WHERE ca.course_id IS NOT NULL;
ALTER TABLE course_arrangement DROP FOREIGN KEY fk_arrangement_course;
ALTER TABLE course_arrangement DROP COLUMN course_id;
ALTER TABLE course_arrangement CHANGE COLUMN course_code_tmp course_code VARCHAR(20) NOT NULL;
ALTER TABLE course_arrangement ADD CONSTRAINT fk_arrangement_course FOREIGN KEY (course_code) REFERENCES course(course_code) ON DELETE CASCADE;

ALTER TABLE course DROP PRIMARY KEY;
ALTER TABLE course ADD PRIMARY KEY (course_code);
ALTER TABLE course ADD UNIQUE KEY uk_course_id (id);

COMMIT;
