-- 添加create_time字段到borrow_application表
ALTER TABLE borrow_application ADD COLUMN create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 添加字段注释
COMMENT ON COLUMN borrow_application.create_time IS '创建时间';
