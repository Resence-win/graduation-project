-- 用途：为门禁扫码增加定位校验能力。
-- 执行后需在门禁点管理中补齐每个门禁点的经纬度和允许通行半径。

ALTER TABLE access_point
    ADD COLUMN IF NOT EXISTS latitude DECIMAL(10, 6),
    ADD COLUMN IF NOT EXISTS longitude DECIMAL(10, 6),
    ADD COLUMN IF NOT EXISTS radius INT DEFAULT 50;

COMMENT ON COLUMN access_point.latitude IS '纬度';
COMMENT ON COLUMN access_point.longitude IS '经度';
COMMENT ON COLUMN access_point.radius IS '允许通行半径(米)';

ALTER TABLE access_record
    ADD COLUMN IF NOT EXISTS actual_latitude DECIMAL(10, 6),
    ADD COLUMN IF NOT EXISTS actual_longitude DECIMAL(10, 6),
    ADD COLUMN IF NOT EXISTS distance DECIMAL(10, 2);

COMMENT ON COLUMN access_record.actual_latitude IS '扫码实际纬度';
COMMENT ON COLUMN access_record.actual_longitude IS '扫码实际经度';
COMMENT ON COLUMN access_record.distance IS '扫码位置距门禁点距离(米)';
