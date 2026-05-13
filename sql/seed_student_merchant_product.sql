-- ========================================
-- 学生、商户、商品演示数据
-- ========================================
-- 使用说明：
-- 1. 请先执行 init.sql。
-- 2. 商品数据依赖 product 表，请先执行 sql/product_consume_upgrade.sql。
-- 3. 图片文件已放在项目根目录 upload/seed/ 下，后端从 campus-card-server 启动时可通过 /api/upload/seed/... 访问。

SET search_path TO campus_card;

-- ========================
-- 1. 学生管理演示数据
-- ========================
INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260001', '李明', '男', '计算机与人工智能学院', '软件工程', '软工2301', '13800010001', 'CAMPUS', 'ON_CAMPUS', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260001');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260002', '王雨晴', '女', '计算机与人工智能学院', '数据科学与大数据技术', '大数据2301', '13800010002', 'CAMPUS', 'ON_CAMPUS', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260002');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260003', '张子涵', '男', '计算机与人工智能学院', '物联网工程', '物联2302', '13800010003', 'CAMPUS', 'LEAVE', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260003');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260004', '刘思源', '女', '经济管理学院', '财务管理', '财管2301', '13800010004', 'CAMPUS', 'ON_CAMPUS', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260004');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260005', '陈嘉豪', '男', '机电工程学院', '机械设计制造及其自动化', '机设2301', '13800010005', 'INTERNSHIP', 'INTERNSHIP', '青舟智能制造有限公司', 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260005');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260006', '赵欣怡', '女', '外国语学院', '英语', '英语2302', '13800010006', 'CAMPUS', 'ON_CAMPUS', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260006');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260007', '周浩然', '男', '建筑与艺术设计学院', '视觉传达设计', '视传2301', '13800010007', 'INTERNSHIP', 'INTERNSHIP', '拾光品牌设计工作室', 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260007');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260008', '黄可欣', '女', '土木与水利工程学院', '工程管理', '工管2301', '13800010008', 'CAMPUS', 'ON_CAMPUS', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260008');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260009', '吴亦凡', '男', '计算机与人工智能学院', '通信工程', '通信2301', '13800010009', 'CAMPUS', 'ON_CAMPUS', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260009');

INSERT INTO student (student_no, name, gender, college, major, class_name, phone, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT '20260010', '郑婉婷', '女', '生命科学与工程学院', '食品科学与工程', '食科2301', '13800010010', 'CAMPUS', 'LEAVE', NULL, 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM student WHERE student_no = '20260010');

-- 学生默认登录账号，密码均为 123456 的 MD5。
INSERT INTO admin_user (username, password, role, status, create_time, is_deleted)
SELECT s.student_no, 'e10adc3949ba59abbe56e057f20f883e', 'student', 1, CURRENT_TIMESTAMP, 0
FROM student s
WHERE s.student_no BETWEEN '20260001' AND '20260010'
  AND NOT EXISTS (SELECT 1 FROM admin_user a WHERE a.username = s.student_no);

-- ========================
-- 2. 商户类型与商户演示数据
-- ========================
INSERT INTO merchant_type (type_name, create_time, is_deleted)
SELECT '餐饮', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM merchant_type WHERE type_name = '餐饮');

INSERT INTO merchant_type (type_name, create_time, is_deleted)
SELECT '超市', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM merchant_type WHERE type_name = '超市');

INSERT INTO merchant_type (type_name, create_time, is_deleted)
SELECT '文创用品', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM merchant_type WHERE type_name = '文创用品');

INSERT INTO merchant_type (type_name, create_time, is_deleted)
SELECT '医疗服务', CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM merchant_type WHERE type_name = '医疗服务');

INSERT INTO merchant (merchant_name, type_id, location, logo, status, create_time, is_deleted)
SELECT '第一食堂', mt.id, '东区生活广场一层', '/upload/seed/merchant/canteen.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant_type mt
WHERE mt.type_name = '餐饮'
  AND mt.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM merchant WHERE merchant_name = '第一食堂');

INSERT INTO merchant (merchant_name, type_id, location, logo, status, create_time, is_deleted)
SELECT '校园超市', mt.id, '学生公寓A区入口', '/upload/seed/merchant/market.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant_type mt
WHERE mt.type_name = '超市'
  AND mt.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM merchant WHERE merchant_name = '校园超市');

INSERT INTO merchant (merchant_name, type_id, location, logo, status, create_time, is_deleted)
SELECT '青藤文创', mt.id, '图书馆一层大厅', '/upload/seed/merchant/stationery.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant_type mt
WHERE mt.type_name = '文创用品'
  AND mt.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM merchant WHERE merchant_name = '青藤文创');

INSERT INTO merchant (merchant_name, type_id, location, logo, status, create_time, is_deleted)
SELECT '校医务室', mt.id, '综合服务中心二层', '/upload/seed/merchant/clinic.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant_type mt
WHERE mt.type_name = '医疗服务'
  AND mt.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM merchant WHERE merchant_name = '校医务室');

-- ========================
-- 3. 商品管理演示数据
-- ========================
INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '招牌鸡腿饭', m.id, 16.00, 120, '第一食堂热销套餐，含鸡腿、米饭和时蔬。', '/upload/seed/product/chicken-rice.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '第一食堂'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '招牌鸡腿饭' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '番茄牛肉面', m.id, 14.50, 90, '番茄汤底搭配牛肉片，适合午晚餐。', '/upload/seed/product/noodle.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '第一食堂'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '番茄牛肉面' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '冷萃咖啡', m.id, 9.90, 80, '低糖冷萃咖啡，适合课间提神。', '/upload/seed/product/coffee.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '校园超市'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '冷萃咖啡' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '矿泉水', m.id, 2.00, 300, '550ml 校园常备饮用水。', '/upload/seed/product/water.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '校园超市'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '矿泉水' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '横线笔记本', m.id, 6.50, 160, 'A5 横线笔记本，适合课堂笔记。', '/upload/seed/product/notebook.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '青藤文创'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '横线笔记本' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '考试中性笔', m.id, 3.00, 240, '0.5mm 黑色中性笔，考试与日常书写适用。', '/upload/seed/product/pen.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '青藤文创'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '考试中性笔' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '校园帆布袋', m.id, 19.90, 60, '校园纪念款帆布袋，可放书本和电脑配件。', '/upload/seed/product/tote-bag.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '青藤文创'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '校园帆布袋' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '折叠雨伞', m.id, 25.00, 75, '轻便折叠伞，适合宿舍和教学楼通勤。', '/upload/seed/product/umbrella.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '校园超市'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '折叠雨伞' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '创可贴', m.id, 5.00, 100, '便携创可贴，适合轻微擦伤临时处理。', '/upload/seed/product/bandage.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '校医务室'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '创可贴' AND p.merchant_id = m.id AND p.is_deleted = 0);

INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT '感冒冲剂', m.id, 18.00, 50, '常用感冒冲剂，请按医嘱或说明书使用。', '/upload/seed/product/medicine.svg', 1, CURRENT_TIMESTAMP, 0
FROM merchant m
WHERE m.merchant_name = '校医务室'
  AND m.is_deleted = 0
  AND NOT EXISTS (SELECT 1 FROM product p WHERE p.product_name = '感冒冲剂' AND p.merchant_id = m.id AND p.is_deleted = 0);
