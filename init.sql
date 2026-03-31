-- ========================================
-- 校园一卡通系统数据库初始化脚本（最终版）
-- ========================================

-- ========================
-- 1. 学生表
-- ========================
DROP TABLE IF EXISTS student;
CREATE TABLE student (
    id BIGSERIAL PRIMARY KEY,
    student_no VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(50),
    gender VARCHAR(10),
    college VARCHAR(100),
    major VARCHAR(100),
    class_name VARCHAR(50),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE student IS '学生信息表';
COMMENT ON COLUMN student.id IS '主键ID';
COMMENT ON COLUMN student.student_no IS '学号';
COMMENT ON COLUMN student.name IS '姓名';
COMMENT ON COLUMN student.gender IS '性别';
COMMENT ON COLUMN student.college IS '学院';
COMMENT ON COLUMN student.major IS '专业';
COMMENT ON COLUMN student.class_name IS '班级';
COMMENT ON COLUMN student.phone IS '手机号';
COMMENT ON COLUMN student.status IS '状态(1正常)';
COMMENT ON COLUMN student.create_time IS '创建时间';
COMMENT ON COLUMN student.update_time IS '更新时间';
COMMENT ON COLUMN student.is_deleted IS '是否删除(0否1是)';

-- ========================
-- 2. 教师表
-- ========================
DROP TABLE IF EXISTS teacher;
CREATE TABLE teacher (
    id BIGSERIAL PRIMARY KEY,
    teacher_no VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(50),
    gender VARCHAR(10),
    department VARCHAR(100),
    phone VARCHAR(20),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE teacher IS '教师信息表';
COMMENT ON COLUMN teacher.id IS '主键ID';
COMMENT ON COLUMN teacher.teacher_no IS '教师编号';
COMMENT ON COLUMN teacher.name IS '姓名';
COMMENT ON COLUMN teacher.gender IS '性别';
COMMENT ON COLUMN teacher.department IS '所属学院';
COMMENT ON COLUMN teacher.phone IS '手机号';
COMMENT ON COLUMN teacher.status IS '状态(1正常)';
COMMENT ON COLUMN teacher.create_time IS '创建时间';
COMMENT ON COLUMN teacher.update_time IS '更新时间';
COMMENT ON COLUMN teacher.is_deleted IS '是否删除(0否1是)';

-- ========================
-- 3. 管理员表
-- ========================
DROP TABLE IF EXISTS admin_user;
CREATE TABLE admin_user (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50),
    password VARCHAR(100),
    role VARCHAR(20),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE admin_user IS '管理员表';
COMMENT ON COLUMN admin_user.id IS '主键ID';
COMMENT ON COLUMN admin_user.username IS '用户名';
COMMENT ON COLUMN admin_user.password IS '密码';
COMMENT ON COLUMN admin_user.role IS '角色';
COMMENT ON COLUMN admin_user.status IS '状态';
COMMENT ON COLUMN admin_user.create_time IS '创建时间';
COMMENT ON COLUMN admin_user.is_deleted IS '是否删除';

-- ========================
-- 4. 校园卡表
-- ========================
DROP TABLE IF EXISTS campus_card;
CREATE TABLE campus_card (
    id BIGSERIAL PRIMARY KEY,
    card_no VARCHAR(50) UNIQUE NOT NULL,
    user_id BIGINT,
    user_type VARCHAR(20),
    status INT DEFAULT 1,
    issue_date DATE,
    expire_date DATE,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE campus_card IS '校园卡表';
COMMENT ON COLUMN campus_card.id IS '主键ID';
COMMENT ON COLUMN campus_card.card_no IS '卡号';
COMMENT ON COLUMN campus_card.user_id IS '用户ID';
COMMENT ON COLUMN campus_card.user_type IS '用户类型(student/teacher)';
COMMENT ON COLUMN campus_card.status IS '状态(1正常2挂失0注销)';
COMMENT ON COLUMN campus_card.issue_date IS '发卡日期';
COMMENT ON COLUMN campus_card.expire_date IS '过期日期';
COMMENT ON COLUMN campus_card.create_time IS '创建时间';
COMMENT ON COLUMN campus_card.update_time IS '更新时间';
COMMENT ON COLUMN campus_card.is_deleted IS '是否删除';

-- ========================
-- 5. 卡操作记录
-- ========================
DROP TABLE IF EXISTS card_change_record;
CREATE TABLE card_change_record (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    operation_type VARCHAR(20),
    operator_id BIGINT,
    remark VARCHAR(200),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE card_change_record IS '校园卡操作记录表';
COMMENT ON COLUMN card_change_record.id IS '主键ID';
COMMENT ON COLUMN card_change_record.card_id IS '卡ID';
COMMENT ON COLUMN card_change_record.operation_type IS '操作类型(开卡/挂失/解挂/注销)';
COMMENT ON COLUMN card_change_record.operator_id IS '操作人';
COMMENT ON COLUMN card_change_record.remark IS '备注';
COMMENT ON COLUMN card_change_record.create_time IS '创建时间';
COMMENT ON COLUMN card_change_record.is_deleted IS '是否删除';

-- ========================
-- 6. 账户表
-- ========================
DROP TABLE IF EXISTS account;
CREATE TABLE account (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    balance NUMERIC(10,2) DEFAULT 0,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE account IS '账户表';
COMMENT ON COLUMN account.id IS '主键ID';
COMMENT ON COLUMN account.card_id IS '卡ID';
COMMENT ON COLUMN account.balance IS '余额';
COMMENT ON COLUMN account.status IS '状态';
COMMENT ON COLUMN account.create_time IS '创建时间';
COMMENT ON COLUMN account.update_time IS '更新时间';
COMMENT ON COLUMN account.is_deleted IS '是否删除';

-- ========================
-- 7. 账户流水
-- ========================
DROP TABLE IF EXISTS account_flow;
CREATE TABLE account_flow (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT,
    change_type VARCHAR(20),
    amount NUMERIC(10,2),
    balance_after NUMERIC(10,2),
    related_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE account_flow IS '账户流水表';
COMMENT ON COLUMN account_flow.id IS '主键ID';
COMMENT ON COLUMN account_flow.account_id IS '账户ID';
COMMENT ON COLUMN account_flow.change_type IS '变动类型(充值/消费)';
COMMENT ON COLUMN account_flow.amount IS '变动金额';
COMMENT ON COLUMN account_flow.balance_after IS '变动后余额';
COMMENT ON COLUMN account_flow.related_id IS '关联业务ID';
COMMENT ON COLUMN account_flow.create_time IS '创建时间';
COMMENT ON COLUMN account_flow.is_deleted IS '是否删除';

-- ========================
-- 8. 充值记录
-- ========================
DROP TABLE IF EXISTS recharge_record;
CREATE TABLE recharge_record (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT,
    amount NUMERIC(10,2),
    recharge_type VARCHAR(20),
    operator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE recharge_record IS '充值记录表';
COMMENT ON COLUMN recharge_record.id IS '主键ID';
COMMENT ON COLUMN recharge_record.account_id IS '账户ID';
COMMENT ON COLUMN recharge_record.amount IS '充值金额';
COMMENT ON COLUMN recharge_record.recharge_type IS '充值方式';
COMMENT ON COLUMN recharge_record.operator_id IS '操作人';
COMMENT ON COLUMN recharge_record.create_time IS '创建时间';
COMMENT ON COLUMN recharge_record.is_deleted IS '是否删除';

-- ========================
-- 9. 消费记录
-- ========================
DROP TABLE IF EXISTS consume_record;
CREATE TABLE consume_record (
    id BIGSERIAL PRIMARY KEY,
    account_id BIGINT,
    merchant_id BIGINT,
    amount NUMERIC(10,2),
    balance_after NUMERIC(10,2),
    status INT DEFAULT 1,
    consume_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE consume_record IS '消费记录表';
COMMENT ON COLUMN consume_record.id IS '主键ID';
COMMENT ON COLUMN consume_record.account_id IS '账户ID';
COMMENT ON COLUMN consume_record.merchant_id IS '商户ID';
COMMENT ON COLUMN consume_record.amount IS '消费金额';
COMMENT ON COLUMN consume_record.balance_after IS '消费后余额';
COMMENT ON COLUMN consume_record.status IS '状态';
COMMENT ON COLUMN consume_record.consume_time IS '消费时间';
COMMENT ON COLUMN consume_record.is_deleted IS '是否删除';

-- ========================
-- 10. 商户类型
-- ========================
DROP TABLE IF EXISTS merchant_type;
CREATE TABLE merchant_type (
    id BIGSERIAL PRIMARY KEY,
    type_name VARCHAR(50),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE merchant_type IS '商户类型表';
COMMENT ON COLUMN merchant_type.id IS '主键ID';
COMMENT ON COLUMN merchant_type.type_name IS '类型名称';
COMMENT ON COLUMN merchant_type.create_time IS '创建时间';
COMMENT ON COLUMN merchant_type.is_deleted IS '是否删除';

-- ========================
-- 11. 商户表
-- ========================
DROP TABLE IF EXISTS merchant;
CREATE TABLE merchant (
    id BIGSERIAL PRIMARY KEY,
    merchant_name VARCHAR(100),
    type_id BIGINT,
    location VARCHAR(200),
    logo VARCHAR(255),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE merchant IS '商户表';
COMMENT ON COLUMN merchant.id IS '主键ID';
COMMENT ON COLUMN merchant.merchant_name IS '商户名称';
COMMENT ON COLUMN merchant.type_id IS '商户类型ID';
COMMENT ON COLUMN merchant.location IS '位置';
COMMENT ON COLUMN merchant.logo IS '商户Logo';
COMMENT ON COLUMN merchant.status IS '状态';
COMMENT ON COLUMN merchant.create_time IS '创建时间';
COMMENT ON COLUMN merchant.update_time IS '更新时间';
COMMENT ON COLUMN merchant.is_deleted IS '是否删除';

-- ========================
-- 12. 门禁记录
-- ========================
DROP TABLE IF EXISTS access_record;
CREATE TABLE access_record (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    direction VARCHAR(10),
    location VARCHAR(100),
    access_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE access_record IS '门禁记录表';
COMMENT ON COLUMN access_record.id IS '主键ID';
COMMENT ON COLUMN access_record.card_id IS '卡ID';
COMMENT ON COLUMN access_record.direction IS '进出方向';
COMMENT ON COLUMN access_record.location IS '位置';
COMMENT ON COLUMN access_record.access_time IS '通行时间';
COMMENT ON COLUMN access_record.is_deleted IS '是否删除';

-- ========================
-- 13. 图书表
-- ========================
DROP TABLE IF EXISTS book;
CREATE TABLE book (
    id BIGSERIAL PRIMARY KEY,
    book_name VARCHAR(100),
    author VARCHAR(100),
    logo VARCHAR(255),
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE book IS '图书表';
COMMENT ON COLUMN book.id IS '主键ID';
COMMENT ON COLUMN book.book_name IS '书名';
COMMENT ON COLUMN book.author IS '作者';
COMMENT ON COLUMN book.logo IS '图书封面';
COMMENT ON COLUMN book.status IS '状态';
COMMENT ON COLUMN book.create_time IS '创建时间';
COMMENT ON COLUMN book.is_deleted IS '是否删除';

-- ========================
-- 14. 借阅申请表
-- ========================
DROP TABLE IF EXISTS borrow_application;
CREATE TABLE borrow_application (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    book_id BIGINT,
    borrow_days INT,
    application_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1, -- 1: 待审批, 2: 已批准, 3: 已拒绝
    operator_id BIGINT,
    approval_time TIMESTAMP,
    remark VARCHAR(200),
    is_deleted INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP
);

COMMENT ON TABLE borrow_application IS '借阅申请表';
COMMENT ON COLUMN borrow_application.id IS '主键ID';
COMMENT ON COLUMN borrow_application.card_id IS '卡ID';
COMMENT ON COLUMN borrow_application.book_id IS '图书ID';
COMMENT ON COLUMN borrow_application.borrow_days IS '借阅天数';
COMMENT ON COLUMN borrow_application.application_time IS '申请时间';
COMMENT ON COLUMN borrow_application.status IS '状态(1: 待审批, 2: 已批准, 3: 已拒绝)';
COMMENT ON COLUMN borrow_application.operator_id IS '操作人ID';
COMMENT ON COLUMN borrow_application.approval_time IS '审批时间';
COMMENT ON COLUMN borrow_application.remark IS '备注';
COMMENT ON COLUMN borrow_application.is_deleted IS '是否删除';
COMMENT ON COLUMN borrow_application.create_time IS '创建时间';
COMMENT ON COLUMN borrow_application.update_time IS '更新时间';

-- ========================
-- 15. 借阅记录
-- ========================
DROP TABLE IF EXISTS borrow_record;
CREATE TABLE borrow_record (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    book_id BIGINT,
    application_id BIGINT,
    borrow_time TIMESTAMP,
    return_time TIMESTAMP,
    due_time TIMESTAMP,
    status INT DEFAULT 1, -- 1: 借阅中, 2: 已归还, 3: 超期
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE borrow_record IS '借阅记录表';
COMMENT ON COLUMN borrow_record.id IS '主键ID';
COMMENT ON COLUMN borrow_record.card_id IS '卡ID';
COMMENT ON COLUMN borrow_record.book_id IS '图书ID';
COMMENT ON COLUMN borrow_record.application_id IS '申请ID';
COMMENT ON COLUMN borrow_record.borrow_time IS '借阅时间';
COMMENT ON COLUMN borrow_record.return_time IS '归还时间';
COMMENT ON COLUMN borrow_record.due_time IS '到期时间';
COMMENT ON COLUMN borrow_record.status IS '状态(1: 借阅中, 2: 已归还, 3: 超期)';
COMMENT ON COLUMN borrow_record.create_time IS '创建时间';
COMMENT ON COLUMN borrow_record.is_deleted IS '是否删除';

-- ========================
-- 16. 考勤记录
-- ========================
DROP TABLE IF EXISTS attendance_record;
CREATE TABLE attendance_record (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    status VARCHAR(20),
    record_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE attendance_record IS '考勤记录表';
COMMENT ON COLUMN attendance_record.id IS '主键ID';
COMMENT ON COLUMN attendance_record.card_id IS '卡ID';
COMMENT ON COLUMN attendance_record.status IS '考勤状态';
COMMENT ON COLUMN attendance_record.record_time IS '记录时间';
COMMENT ON COLUMN attendance_record.is_deleted IS '是否删除';

-- ========================
-- 17. 通勤车记录
-- ========================
DROP TABLE IF EXISTS commute_record;
CREATE TABLE commute_record (
    id BIGSERIAL PRIMARY KEY,
    card_id BIGINT,
    route_id BIGINT,
    seat_number VARCHAR(10),
    ride_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status INT DEFAULT 1,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE commute_record IS '通勤车记录表';
COMMENT ON COLUMN commute_record.id IS '主键ID';
COMMENT ON COLUMN commute_record.card_id IS '卡ID';
COMMENT ON COLUMN commute_record.route_id IS '路线ID';
COMMENT ON COLUMN commute_record.seat_number IS '座位号';
COMMENT ON COLUMN commute_record.ride_time IS '乘车时间';
COMMENT ON COLUMN commute_record.status IS '状态';
COMMENT ON COLUMN commute_record.is_deleted IS '是否删除';

-- ========================
-- 18. 操作日志
-- ========================
DROP TABLE IF EXISTS operation_log;
CREATE TABLE operation_log (
    id BIGSERIAL PRIMARY KEY,
    operator_id BIGINT,
    operation_type VARCHAR(50),
    target_table VARCHAR(50),
    target_id BIGINT,
    content VARCHAR(500),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE operation_log IS '系统操作日志表';
COMMENT ON COLUMN operation_log.id IS '主键ID';
COMMENT ON COLUMN operation_log.operator_id IS '操作人ID';
COMMENT ON COLUMN operation_log.operation_type IS '操作类型';
COMMENT ON COLUMN operation_log.target_table IS '操作表';
COMMENT ON COLUMN operation_log.target_id IS '目标ID';
COMMENT ON COLUMN operation_log.content IS '操作内容';
COMMENT ON COLUMN operation_log.create_time IS '创建时间';
COMMENT ON COLUMN operation_log.is_deleted IS '是否删除';

-- ========================================
-- 唯一约束（防止业务数据异常）
-- ========================================

-- 1. 一个校园卡只能对应一个账户（极其重要）
ALTER TABLE account 
ADD CONSTRAINT uk_account_card UNIQUE(card_id);

-- 2. 卡号唯一（虽然你写了 UNIQUE，这里是规范写法补充说明）
-- 已存在：campus_card.card_no UNIQUE

-- 3. 学号唯一（已存在 UNIQUE，这里补充规范说明）
-- 已存在：student.student_no UNIQUE

-- 4. 教师编号唯一（已存在 UNIQUE）
-- 已存在：teacher.teacher_no UNIQUE

-- 5. 商户类型名称唯一（防止重复类型）
ALTER TABLE merchant_type 
ADD CONSTRAINT uk_merchant_type_name UNIQUE(type_name);

-- 6. 商户名称唯一（可选，但强烈建议）
ALTER TABLE merchant 
ADD CONSTRAINT uk_merchant_name UNIQUE(merchant_name);

-- 7. 管理员用户名唯一（必须）
ALTER TABLE admin_user 
ADD CONSTRAINT uk_admin_username UNIQUE(username);

-- ========================
-- 索引
-- ========================
-- 校园卡相关索引
CREATE INDEX idx_card_no ON campus_card(card_no);
CREATE INDEX idx_campus_card_user_id ON campus_card(user_id);
CREATE INDEX idx_campus_card_status ON campus_card(status);

-- 账户相关索引
CREATE INDEX idx_account_card_id ON account(card_id);
CREATE INDEX idx_account_status ON account(status);

-- 充值记录索引
CREATE INDEX idx_recharge_account_id ON recharge_record(account_id);
CREATE INDEX idx_recharge_time ON recharge_record(create_time);

-- 消费记录索引
CREATE INDEX idx_consume_account_id ON consume_record(account_id);
CREATE INDEX idx_consume_merchant_id ON consume_record(merchant_id);
CREATE INDEX idx_consume_time ON consume_record(consume_time);
CREATE INDEX idx_consume_status ON consume_record(status);

-- 账户流水索引
CREATE INDEX idx_account_flow_account_id ON account_flow(account_id);
CREATE INDEX idx_account_flow_change_type ON account_flow(change_type);
CREATE INDEX idx_account_flow_time ON account_flow(create_time);

-- 用户相关索引
CREATE INDEX idx_student_no ON student(student_no);
CREATE INDEX idx_teacher_no ON teacher(teacher_no);

-- 商户相关索引
CREATE INDEX idx_merchant_type_id ON merchant(type_id);
CREATE INDEX idx_merchant_status ON merchant(status);

-- 辅助模块索引
CREATE INDEX idx_access_card_id ON access_record(card_id);
CREATE INDEX idx_access_time ON access_record(access_time);
CREATE INDEX idx_borrow_card_id ON borrow_record(card_id);
CREATE INDEX idx_borrow_book_id ON borrow_record(book_id);
CREATE INDEX idx_borrow_status ON borrow_record(status);
CREATE INDEX idx_borrow_due_time ON borrow_record(due_time);
CREATE INDEX idx_borrow_application_card_id ON borrow_application(card_id);
CREATE INDEX idx_borrow_application_book_id ON borrow_application(book_id);
CREATE INDEX idx_borrow_application_status ON borrow_application(status);
CREATE INDEX idx_borrow_application_time ON borrow_application(application_time);
CREATE INDEX idx_attendance_card_id ON attendance_record(card_id);
CREATE INDEX idx_attendance_time ON attendance_record(record_time);
CREATE INDEX idx_commute_card_id ON commute_record(card_id);
CREATE INDEX idx_commute_route_id ON commute_record(route_id);
CREATE INDEX idx_commute_time ON commute_record(ride_time);

-- 系统日志索引
CREATE INDEX idx_operation_log_operator_id ON operation_log(operator_id);
CREATE INDEX idx_operation_log_type ON operation_log(operation_type);
CREATE INDEX idx_operation_log_time ON operation_log(create_time);