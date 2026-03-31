-- 添加update_time字段到borrow_application表
ALTER TABLE borrow_application ADD COLUMN update_time TIMESTAMP;

-- 添加字段注释
COMMENT ON COLUMN borrow_application.update_time IS '更新时间';
