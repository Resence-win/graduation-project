-- 考勤请假审批升级脚本
-- 用途：在已有数据库基础上补齐外出实习、请假审批、请假打卡记录所需字段。
-- 适用数据库：PostgreSQL / openGauss

-- 1. 学生考勤状态字段
ALTER TABLE student ADD COLUMN IF NOT EXISTS attendance_mode VARCHAR(30) DEFAULT 'CAMPUS';
ALTER TABLE student ADD COLUMN IF NOT EXISTS attendance_status VARCHAR(30) DEFAULT 'ON_CAMPUS';
ALTER TABLE student ADD COLUMN IF NOT EXISTS internship_company VARCHAR(200);

UPDATE student
SET attendance_mode = COALESCE(NULLIF(attendance_mode, ''), 'CAMPUS'),
    attendance_status = COALESCE(NULLIF(attendance_status, ''), 'ON_CAMPUS')
WHERE attendance_mode IS NULL
   OR attendance_mode = ''
   OR attendance_status IS NULL
   OR attendance_status = '';

COMMENT ON COLUMN student.attendance_mode IS '考勤模式(CAMPUS: 在校考勤, INTERNSHIP: 校外实习)';
COMMENT ON COLUMN student.attendance_status IS '考勤状态(ON_CAMPUS: 在校, INTERNSHIP: 外出实习, LEAVE: 已请假)';
COMMENT ON COLUMN student.internship_company IS '实习单位';

-- 2. 考勤记录字段
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS attendance_type VARCHAR(30) DEFAULT 'CAMPUS_LOCATION';
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS internship_company VARCHAR(200);
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS internship_log TEXT;
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS internship_log_date DATE;
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_application_id BIGINT;
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_reason VARCHAR(1000);
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_start_date DATE;
ALTER TABLE attendance_record ADD COLUMN IF NOT EXISTS leave_end_date DATE;

UPDATE attendance_record
SET attendance_type = COALESCE(NULLIF(attendance_type, ''), 'CAMPUS_LOCATION')
WHERE attendance_type IS NULL OR attendance_type = '';

COMMENT ON COLUMN attendance_record.attendance_type IS '考勤类型(CAMPUS_LOCATION: 校内位置, OFF_CAMPUS_LOCATION: 校外位置, INTERNSHIP_LOG: 实习日志, LEAVE: 已请假)';
COMMENT ON COLUMN attendance_record.internship_company IS '实习单位';
COMMENT ON COLUMN attendance_record.internship_log IS '实习日志内容';
COMMENT ON COLUMN attendance_record.internship_log_date IS '实习日志日期';
COMMENT ON COLUMN attendance_record.leave_application_id IS '请假申请ID';
COMMENT ON COLUMN attendance_record.leave_reason IS '请假原因';
COMMENT ON COLUMN attendance_record.leave_start_date IS '请假开始日期';
COMMENT ON COLUMN attendance_record.leave_end_date IS '请假结束日期';

-- 3. 考勤申报表。INTERNSHIP 和 LEAVE 均需要审核后生效。
CREATE TABLE IF NOT EXISTS attendance_application (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT,
    card_id BIGINT,
    application_type VARCHAR(30),
    internship_company VARCHAR(200),
    reason VARCHAR(1000),
    start_date DATE,
    end_date DATE,
    status VARCHAR(20) DEFAULT 'PENDING',
    reviewer_id BIGINT,
    review_time TIMESTAMP,
    review_remark VARCHAR(1000),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS student_id BIGINT;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS card_id BIGINT;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS application_type VARCHAR(30);
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS internship_company VARCHAR(200);
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS reason VARCHAR(1000);
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS start_date DATE;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS end_date DATE;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'PENDING';
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS reviewer_id BIGINT;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS review_time TIMESTAMP;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS review_remark VARCHAR(1000);
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS update_time TIMESTAMP;
ALTER TABLE attendance_application ADD COLUMN IF NOT EXISTS is_deleted INT DEFAULT 0;

UPDATE attendance_application
SET status = CASE
    WHEN application_type = 'INTERNSHIP' AND (status IS NULL OR status = '') THEN 'APPROVED'
    WHEN status IS NULL OR status = '' THEN 'PENDING'
    ELSE UPPER(status)
END;

UPDATE attendance_application
SET is_deleted = 0
WHERE is_deleted IS NULL;

COMMENT ON TABLE attendance_application IS '考勤申报表';
COMMENT ON COLUMN attendance_application.id IS '主键ID';
COMMENT ON COLUMN attendance_application.student_id IS '学生ID';
COMMENT ON COLUMN attendance_application.card_id IS '卡ID';
COMMENT ON COLUMN attendance_application.application_type IS '申报类型(INTERNSHIP: 外出实习, LEAVE: 请假)';
COMMENT ON COLUMN attendance_application.internship_company IS '实习单位';
COMMENT ON COLUMN attendance_application.reason IS '申报原因或说明';
COMMENT ON COLUMN attendance_application.start_date IS '开始日期';
COMMENT ON COLUMN attendance_application.end_date IS '结束日期';
COMMENT ON COLUMN attendance_application.status IS '状态(PENDING: 待审核, APPROVED: 已通过, REJECTED: 已拒绝)';
COMMENT ON COLUMN attendance_application.reviewer_id IS '审核人ID';
COMMENT ON COLUMN attendance_application.review_time IS '审核时间';
COMMENT ON COLUMN attendance_application.review_remark IS '审核备注';
COMMENT ON COLUMN attendance_application.create_time IS '创建时间';
COMMENT ON COLUMN attendance_application.update_time IS '更新时间';
COMMENT ON COLUMN attendance_application.is_deleted IS '是否删除';

-- 4. 索引
CREATE INDEX IF NOT EXISTS idx_attendance_type ON attendance_record(attendance_type);
CREATE INDEX IF NOT EXISTS idx_attendance_internship_log_date ON attendance_record(internship_log_date);
CREATE INDEX IF NOT EXISTS idx_attendance_leave_application_id ON attendance_record(leave_application_id);
CREATE INDEX IF NOT EXISTS idx_attendance_application_card_id ON attendance_application(card_id);
CREATE INDEX IF NOT EXISTS idx_attendance_application_type ON attendance_application(application_type);
CREATE INDEX IF NOT EXISTS idx_attendance_application_status ON attendance_application(status);
CREATE INDEX IF NOT EXISTS idx_attendance_application_date ON attendance_application(start_date, end_date);

-- 5. 可选核验
-- SELECT column_name FROM information_schema.columns WHERE table_name = 'attendance_application' ORDER BY ordinal_position;
