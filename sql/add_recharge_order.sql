-- 支付宝沙箱充值订单表
CREATE TABLE IF NOT EXISTS recharge_order (
    id BIGSERIAL PRIMARY KEY,
    out_trade_no VARCHAR(64),
    card_id BIGINT,
    amount NUMERIC(10,2),
    status VARCHAR(20) DEFAULT 'WAIT_PAY',
    alipay_trade_no VARCHAR(64),
    operator_id BIGINT,
    paid_time TIMESTAMP,
    settled_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted INT DEFAULT 0
);

ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS out_trade_no VARCHAR(64);
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS card_id BIGINT;
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS amount NUMERIC(10,2);
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'WAIT_PAY';
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS alipay_trade_no VARCHAR(64);
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS operator_id BIGINT;
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS paid_time TIMESTAMP;
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS settled_time TIMESTAMP;
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE recharge_order ADD COLUMN IF NOT EXISTS is_deleted INT DEFAULT 0;

UPDATE recharge_order SET status = 'WAIT_PAY' WHERE status IS NULL;
UPDATE recharge_order SET create_time = CURRENT_TIMESTAMP WHERE create_time IS NULL;
UPDATE recharge_order SET update_time = CURRENT_TIMESTAMP WHERE update_time IS NULL;
UPDATE recharge_order SET is_deleted = 0 WHERE is_deleted IS NULL;

COMMENT ON TABLE recharge_order IS '支付宝充值订单表';
COMMENT ON COLUMN recharge_order.out_trade_no IS '商户订单号';
COMMENT ON COLUMN recharge_order.card_id IS '校园卡ID';
COMMENT ON COLUMN recharge_order.amount IS '充值金额';
COMMENT ON COLUMN recharge_order.status IS '订单状态(WAIT_PAY/SETTLED/CLOSED)';
COMMENT ON COLUMN recharge_order.alipay_trade_no IS '支付宝交易号';
COMMENT ON COLUMN recharge_order.operator_id IS '操作人';
COMMENT ON COLUMN recharge_order.paid_time IS '支付时间';
COMMENT ON COLUMN recharge_order.settled_time IS '入账时间';
COMMENT ON COLUMN recharge_order.create_time IS '创建时间';
COMMENT ON COLUMN recharge_order.update_time IS '更新时间';
COMMENT ON COLUMN recharge_order.is_deleted IS '是否删除';

ALTER TABLE recharge_order ALTER COLUMN out_trade_no SET NOT NULL;
ALTER TABLE recharge_order ALTER COLUMN card_id SET NOT NULL;
ALTER TABLE recharge_order ALTER COLUMN amount SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_recharge_order_out_trade_no ON recharge_order(out_trade_no);
CREATE INDEX IF NOT EXISTS idx_recharge_order_card_id ON recharge_order(card_id);
CREATE INDEX IF NOT EXISTS idx_recharge_order_status ON recharge_order(status);
