ALTER TABLE student
    ADD COLUMN class_code_tmp VARCHAR(16) NULL;

UPDATE student s
JOIN class c ON s.class_id = c.id
SET s.class_code_tmp = c.class_code
WHERE s.class_id IS NOT NULL;

ALTER TABLE student DROP FOREIGN KEY fk_student_class;
ALTER TABLE student DROP COLUMN class_id;
ALTER TABLE student CHANGE COLUMN class_code_tmp class_id VARCHAR(16) NULL;
ALTER TABLE student
    ADD CONSTRAINT fk_student_class FOREIGN KEY (class_id) REFERENCES class(class_code) ON DELETE SET NULL;

ALTER TABLE course_arrangement
    ADD COLUMN class_code_tmp VARCHAR(16) NULL;

UPDATE course_arrangement ca
JOIN class c ON ca.class_id = c.id
SET ca.class_code_tmp = c.class_code;

ALTER TABLE course_arrangement DROP FOREIGN KEY fk_arrangement_class;
ALTER TABLE course_arrangement DROP COLUMN class_id;
ALTER TABLE course_arrangement CHANGE COLUMN class_code_tmp class_id VARCHAR(16) NOT NULL;
ALTER TABLE course_arrangement
    ADD CONSTRAINT fk_arrangement_class FOREIGN KEY (class_id) REFERENCES class(class_code) ON DELETE CASCADE;

ALTER TABLE announcement
    ADD COLUMN target_class_code_tmp VARCHAR(16) NULL;

UPDATE announcement a
JOIN class c ON a.target_class_id = c.id
SET a.target_class_code_tmp = c.class_code
WHERE a.target_class_id IS NOT NULL;

ALTER TABLE announcement DROP FOREIGN KEY fk_announcement_class;
ALTER TABLE announcement DROP COLUMN target_class_id;
ALTER TABLE announcement CHANGE COLUMN target_class_code_tmp target_class_id VARCHAR(16) NULL;
ALTER TABLE announcement
    ADD CONSTRAINT fk_announcement_class FOREIGN KEY (target_class_id) REFERENCES class(class_code) ON DELETE SET NULL;
