-- 为图书增加所属馆藏地，支持多校区、多图书馆分类管理
ALTER TABLE book
    ADD COLUMN IF NOT EXISTS collection_location VARCHAR(200);

COMMENT ON COLUMN book.collection_location IS '所属馆藏地';
