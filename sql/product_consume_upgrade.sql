CREATE TABLE IF NOT EXISTS product (
    id BIGSERIAL PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    merchant_id BIGINT NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    stock INT DEFAULT 0,
    description VARCHAR(500),
    image VARCHAR(255),
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted SMALLINT DEFAULT 0
);

COMMENT ON TABLE product IS '商品表';
COMMENT ON COLUMN product.product_name IS '商品名称';
COMMENT ON COLUMN product.merchant_id IS '所属商户ID';
COMMENT ON COLUMN product.price IS '商品单价';
COMMENT ON COLUMN product.stock IS '库存';
COMMENT ON COLUMN product.description IS '商品描述';
COMMENT ON COLUMN product.image IS '商品图片';
COMMENT ON COLUMN product.status IS '状态(1上架0下架)';

CREATE INDEX IF NOT EXISTS idx_product_merchant_id ON product(merchant_id);
CREATE INDEX IF NOT EXISTS idx_product_status ON product(status);
CREATE INDEX IF NOT EXISTS idx_product_name ON product(product_name);

ALTER TABLE consume_record ADD COLUMN IF NOT EXISTS product_id BIGINT;
ALTER TABLE consume_record ADD COLUMN IF NOT EXISTS product_name VARCHAR(100);
ALTER TABLE consume_record ADD COLUMN IF NOT EXISTS quantity INT DEFAULT 1;

COMMENT ON COLUMN consume_record.product_id IS '商品ID';
COMMENT ON COLUMN consume_record.product_name IS '商品名称快照';
COMMENT ON COLUMN consume_record.quantity IS '消费数量';

CREATE INDEX IF NOT EXISTS idx_consume_product_id ON consume_record(product_id);
