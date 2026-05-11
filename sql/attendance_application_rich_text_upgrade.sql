-- 用途：支持请假/实习申报富文本说明，允许保存图片和附件链接等 HTML 内容。

ALTER TABLE attendance_application
  ALTER COLUMN reason TYPE TEXT;

COMMENT ON COLUMN attendance_application.reason IS '申报原因、说明或富文本资料内容';
