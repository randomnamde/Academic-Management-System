-- Migrate college PK and FK references from id to college_code
START TRANSACTION;

ALTER TABLE teacher ADD COLUMN college_code_tmp VARCHAR(32) NULL;
UPDATE teacher t JOIN college c ON t.college_id = c.id SET t.college_code_tmp = c.college_code WHERE t.college_id IS NOT NULL;
ALTER TABLE teacher DROP FOREIGN KEY fk_teacher_college;
ALTER TABLE teacher DROP COLUMN college_id;
ALTER TABLE teacher CHANGE COLUMN college_code_tmp college_code VARCHAR(32) NULL;
ALTER TABLE teacher ADD CONSTRAINT fk_teacher_college FOREIGN KEY (college_code) REFERENCES college(college_code) ON DELETE SET NULL;

ALTER TABLE major ADD COLUMN college_code_tmp VARCHAR(32) NULL;
UPDATE major m JOIN college c ON m.college_id = c.id SET m.college_code_tmp = c.college_code WHERE m.college_id IS NOT NULL;
ALTER TABLE major DROP FOREIGN KEY fk_major_college;
ALTER TABLE major DROP COLUMN college_id;
ALTER TABLE major CHANGE COLUMN college_code_tmp college_code VARCHAR(32) NOT NULL;
ALTER TABLE major ADD CONSTRAINT fk_major_college FOREIGN KEY (college_code) REFERENCES college(college_code) ON DELETE CASCADE;

ALTER TABLE class ADD COLUMN college_code_tmp VARCHAR(32) NULL;
UPDATE class cl JOIN college c ON cl.college_id = c.id SET cl.college_code_tmp = c.college_code WHERE cl.college_id IS NOT NULL;
ALTER TABLE class DROP FOREIGN KEY fk_class_college;
ALTER TABLE class DROP COLUMN college_id;
ALTER TABLE class CHANGE COLUMN college_code_tmp college_code VARCHAR(32) NOT NULL;
ALTER TABLE class ADD CONSTRAINT fk_class_college FOREIGN KEY (college_code) REFERENCES college(college_code) ON DELETE RESTRICT;

ALTER TABLE college DROP PRIMARY KEY;
ALTER TABLE college ADD PRIMARY KEY (college_code);
ALTER TABLE college ADD UNIQUE KEY uk_college_id (id);

COMMIT;
