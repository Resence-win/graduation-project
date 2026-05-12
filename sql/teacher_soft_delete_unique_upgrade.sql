-- 让软删除数据不再占用教师编号、学号和登录用户名。
-- 执行后，仅 is_deleted = 0 的有效数据需要保持唯一。

ALTER TABLE teacher DROP CONSTRAINT IF EXISTS teacher_teacher_no_key;
ALTER TABLE student DROP CONSTRAINT IF EXISTS student_student_no_key;
ALTER TABLE admin_user DROP CONSTRAINT IF EXISTS uk_admin_username;

CREATE UNIQUE INDEX IF NOT EXISTS uk_teacher_no_active
ON teacher(teacher_no)
WHERE is_deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_student_no_active
ON student(student_no)
WHERE is_deleted = 0;

CREATE UNIQUE INDEX IF NOT EXISTS uk_admin_username_active
ON admin_user(username)
WHERE is_deleted = 0;
