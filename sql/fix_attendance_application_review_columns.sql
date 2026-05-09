-- 请假审批字段最小修复脚本
-- 用途：修复旧库缺少 reviewer_id/review_time/review_remark 等列导致的查询报错。
-- 适用数据库：openGauss / PostgreSQL

-- 1. 学生负责老师字段。老师端审批权限根据该字段判断。
ALTER TABLE student ADD COLUMN IF NOT EXISTS teacher_id BIGINT;
COMMENT ON COLUMN student.teacher_id IS '负责老师ID';
CREATE INDEX IF NOT EXISTS idx_student_teacher_id ON student(teacher_id);

-- 2. 请假审批字段。当前报错缺的就是 reviewer_id。
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS reviewer_id BIGINT;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS review_time TIMESTAMP;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS review_remark VARCHAR(1000);

COMMENT ON COLUMN attendance_application.reviewer_id IS '审核人ID';
COMMENT ON COLUMN attendance_application.review_time IS '审核时间';
COMMENT ON COLUMN attendance_application.review_remark IS '审核备注';

-- 3. 考勤申报状态默认值。请假和外出实习均应为待审核。
ALTER TABLE attendance_application ALTER COLUMN status SET DEFAULT 'PENDING';

UPDATE attendance_application
SET status = CASE
    WHEN status IS NULL OR status = '' THEN 'PENDING'
    ELSE UPPER(status)
END;

-- 4. 请假打卡记录需要保存请假信息。
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_application_id BIGINT;
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_reason VARCHAR(1000);
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_start_date DATE;
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_end_date DATE;

COMMENT ON COLUMN attendance_record.leave_application_id IS '请假申请ID';
COMMENT ON COLUMN attendance_record.leave_reason IS '请假原因';
COMMENT ON COLUMN attendance_record.leave_start_date IS '请假开始日期';
COMMENT ON COLUMN attendance_record.leave_end_date IS '请假结束日期';

-- 5. 索引。已有则跳过。
CREATE INDEX IF NOT EXISTS idx_attendance_leave_application_id ON attendance_record(leave_application_id);
CREATE INDEX IF NOT EXISTS idx_attendance_application_status ON attendance_application(status);

-- 6. 核验：应能看到 reviewer_id、review_time、review_remark。
SELECT column_name
FROM information_schema.columns
WHERE table_name = 'attendance_application'
  AND column_name IN ('reviewer_id', 'review_time', 'review_remark')
ORDER BY column_name;
