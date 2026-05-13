-- ========================================
-- 校园一卡通系统全模块优质演示数据
-- ========================================
-- 使用说明：
-- 1. 请先执行 init.sql。
-- 2. 若当前库尚未创建 product 表或消费扩展字段，请先执行 sql/product_consume_upgrade.sql。
-- 3. 本脚本可重复执行；演示数据使用 DEMO/202601xx 等业务编号做幂等判断。
-- 4. 图片文件位于项目根目录 upload/seed/ 与 upload/book/。

BEGIN;

SET search_path TO campus_card;

-- ========================
-- 1. 登录账号、教师、学生
-- ========================
INSERT INTO admin_user (username, password, role, status, create_time, is_deleted)
SELECT 'admin', 'e10adc3949ba59abbe56e057f20f883e', 'admin', 1, CURRENT_TIMESTAMP, 0
WHERE NOT EXISTS (SELECT 1 FROM admin_user WHERE username = 'admin' AND is_deleted = 0);

WITH demo_teacher (teacher_no, name, gender, department, phone) AS (
    VALUES
    ('T20261001', '周明远', '男', '计算机与人工智能学院', '13910010001'),
    ('T20261002', '赵婉宁', '女', '经济管理学院', '13910010002'),
    ('T20261003', '刘景行', '男', '机电工程学院', '13910010003'),
    ('T20261004', '陈若琳', '女', '建筑与艺术设计学院', '13910010004')
)
INSERT INTO teacher (teacher_no, name, gender, department, phone, status, create_time, is_deleted)
SELECT teacher_no, name, gender, department, phone, 1, CURRENT_TIMESTAMP, 0
FROM demo_teacher dt
WHERE NOT EXISTS (
    SELECT 1 FROM teacher t WHERE t.teacher_no = dt.teacher_no AND t.is_deleted = 0
);

WITH demo_teacher (teacher_no) AS (
    VALUES ('T20261001'), ('T20261002'), ('T20261003'), ('T20261004')
)
INSERT INTO admin_user (username, password, role, status, create_time, is_deleted)
SELECT teacher_no, 'e10adc3949ba59abbe56e057f20f883e', 'teacher', 1, CURRENT_TIMESTAMP, 0
FROM demo_teacher dt
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user a WHERE a.username = dt.teacher_no AND a.is_deleted = 0
);

WITH demo_student (student_no, name, gender, college, major, class_name, phone, teacher_no, attendance_mode, attendance_status, internship_company) AS (
    VALUES
    ('20260101', '林亦辰', '男', '计算机与人工智能学院', '软件工程', '软工2301', '13810010001', 'T20261001', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260102', '苏晚晴', '女', '计算机与人工智能学院', '数据科学与大数据技术', '大数据2301', '13810010002', 'T20261001', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260103', '许知远', '男', '计算机与人工智能学院', '网络空间安全', '网安2301', '13810010003', 'T20261001', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260104', '唐若溪', '女', '经济管理学院', '财务管理', '财管2301', '13810010004', 'T20261002', 'CAMPUS', 'LEAVE', NULL),
    ('20260105', '何嘉佑', '男', '机电工程学院', '智能制造工程', '智造2301', '13810010005', 'T20261003', 'INTERNSHIP', 'INTERNSHIP', '青舟智能制造有限公司'),
    ('20260106', '程安琪', '女', '外国语学院', '英语', '英语2302', '13810010006', 'T20261004', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260107', '高牧之', '男', '建筑与艺术设计学院', '视觉传达设计', '视传2301', '13810010007', 'T20261004', 'INTERNSHIP', 'INTERNSHIP', '拾光品牌设计工作室'),
    ('20260108', '梁思宁', '女', '土木与水利工程学院', '工程管理', '工管2301', '13810010008', 'T20261004', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260109', '方泽宇', '男', '自动化与电气工程学院', '自动化', '自动化2301', '13810010009', 'T20261003', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260110', '宋雨桐', '女', '生命科学与工程学院', '食品科学与工程', '食科2301', '13810010010', 'T20261002', 'CAMPUS', 'LEAVE', NULL),
    ('20260111', '邵星河', '男', '法学院', '法学', '法学2301', '13810010011', 'T20261002', 'CAMPUS', 'ON_CAMPUS', NULL),
    ('20260112', '袁清嘉', '女', '微电子现代产业学院', '微电子科学与工程', '微电2301', '13810010012', 'T20261001', 'CAMPUS', 'ON_CAMPUS', NULL)
)
INSERT INTO student (student_no, name, gender, college, major, class_name, phone, teacher_id, attendance_mode, attendance_status, internship_company, status, create_time, is_deleted)
SELECT ds.student_no, ds.name, ds.gender, ds.college, ds.major, ds.class_name, ds.phone,
       t.id, ds.attendance_mode, ds.attendance_status, ds.internship_company, 1, CURRENT_TIMESTAMP, 0
FROM demo_student ds
JOIN teacher t ON t.teacher_no = ds.teacher_no AND t.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM student s WHERE s.student_no = ds.student_no AND s.is_deleted = 0
);

WITH demo_student (student_no) AS (
    VALUES
    ('20260101'), ('20260102'), ('20260103'), ('20260104'), ('20260105'), ('20260106'),
    ('20260107'), ('20260108'), ('20260109'), ('20260110'), ('20260111'), ('20260112')
)
INSERT INTO admin_user (username, password, role, status, create_time, is_deleted)
SELECT student_no, 'e10adc3949ba59abbe56e057f20f883e', 'student', 1, CURRENT_TIMESTAMP, 0
FROM demo_student ds
WHERE NOT EXISTS (
    SELECT 1 FROM admin_user a WHERE a.username = ds.student_no AND a.is_deleted = 0
);

-- ========================
-- 2. 校园卡、账户、卡操作记录
-- ========================
WITH demo_student_card (student_no, card_no, status, issue_offset_days, expire_years) AS (
    VALUES
    ('20260101', 'DEMO-S-20260101', 1, 180, 4),
    ('20260102', 'DEMO-S-20260102', 1, 175, 4),
    ('20260103', 'DEMO-S-20260103', 2, 170, 4),
    ('20260104', 'DEMO-S-20260104', 1, 165, 4),
    ('20260105', 'DEMO-S-20260105', 1, 160, 4),
    ('20260106', 'DEMO-S-20260106', 1, 155, 4),
    ('20260107', 'DEMO-S-20260107', 1, 150, 4),
    ('20260108', 'DEMO-S-20260108', 1, 145, 4),
    ('20260109', 'DEMO-S-20260109', 1, 140, 4),
    ('20260110', 'DEMO-S-20260110', 1, 135, 4),
    ('20260111', 'DEMO-S-20260111', 1, 130, 4),
    ('20260112', 'DEMO-S-20260112', 0, 125, 4)
)
INSERT INTO campus_card (card_no, user_id, user_type, status, issue_date, expire_date, create_time, is_deleted)
SELECT dsc.card_no, s.id, 'student', dsc.status,
       CURRENT_DATE - dsc.issue_offset_days,
       CURRENT_DATE + dsc.expire_years * 365,
       CURRENT_TIMESTAMP, 0
FROM demo_student_card dsc
JOIN student s ON s.student_no = dsc.student_no AND s.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM campus_card cc WHERE cc.card_no = dsc.card_no AND cc.is_deleted = 0
);

WITH demo_teacher_card (teacher_no, card_no, status, issue_offset_days, expire_years) AS (
    VALUES
    ('T20261001', 'DEMO-T-T20261001', 1, 220, 6),
    ('T20261002', 'DEMO-T-T20261002', 1, 210, 6),
    ('T20261003', 'DEMO-T-T20261003', 1, 200, 6),
    ('T20261004', 'DEMO-T-T20261004', 0, 190, 6)
)
INSERT INTO campus_card (card_no, user_id, user_type, status, issue_date, expire_date, create_time, is_deleted)
SELECT dtc.card_no, t.id, 'teacher', dtc.status,
       CURRENT_DATE - dtc.issue_offset_days,
       CURRENT_DATE + dtc.expire_years * 365,
       CURRENT_TIMESTAMP, 0
FROM demo_teacher_card dtc
JOIN teacher t ON t.teacher_no = dtc.teacher_no AND t.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM campus_card cc WHERE cc.card_no = dtc.card_no AND cc.is_deleted = 0
);

INSERT INTO account (card_id, balance, status, create_time, is_deleted)
SELECT cc.id, 0.00, 1, CURRENT_TIMESTAMP, 0
FROM campus_card cc
WHERE cc.card_no LIKE 'DEMO-%'
  AND cc.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM account a WHERE a.card_id = cc.id AND a.is_deleted = 0
  );

INSERT INTO card_change_record (card_id, operation_type, operator_id, remark, create_time, is_deleted)
SELECT cc.id, '开卡', 1, '答辩演示数据批量开卡', cc.create_time, 0
FROM campus_card cc
WHERE cc.card_no LIKE 'DEMO-%'
  AND cc.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM card_change_record r
      WHERE r.card_id = cc.id AND r.operation_type = '开卡' AND r.remark = '答辩演示数据批量开卡' AND r.is_deleted = 0
  );

INSERT INTO card_change_record (card_id, operation_type, operator_id, remark, create_time, is_deleted)
SELECT cc.id, '挂失', 1, '新值: 学生反馈校园卡遗失，已临时挂失', CURRENT_DATE - 5 + CURRENT_TIME, 0
FROM campus_card cc
WHERE cc.card_no = 'DEMO-S-20260103'
  AND cc.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM card_change_record r
      WHERE r.card_id = cc.id AND r.operation_type = '挂失' AND r.is_deleted = 0
  );

INSERT INTO card_change_record (card_id, operation_type, operator_id, remark, create_time, is_deleted)
SELECT cc.id, '注销', 1, '新值: 毕业离校办理销户', CURRENT_DATE - 12 + CURRENT_TIME, 0
FROM campus_card cc
WHERE cc.card_no IN ('DEMO-S-20260112', 'DEMO-T-T20261004')
  AND cc.is_deleted = 0
  AND NOT EXISTS (
      SELECT 1 FROM card_change_record r
      WHERE r.card_id = cc.id AND r.operation_type = '注销' AND r.is_deleted = 0
  );

-- ========================
-- 3. 商户、商品、图书
-- ========================
WITH demo_type (type_name) AS (
    VALUES ('餐饮'), ('超市'), ('文创用品'), ('医疗服务'), ('咖啡饮品'), ('生活服务'), ('文印服务'), ('体育服务')
)
INSERT INTO merchant_type (type_name, create_time, is_deleted)
SELECT type_name, CURRENT_TIMESTAMP, 0
FROM demo_type dt
WHERE NOT EXISTS (
    SELECT 1 FROM merchant_type mt WHERE mt.type_name = dt.type_name AND mt.is_deleted = 0
);

WITH demo_merchant (merchant_name, type_name, location, logo, status) AS (
    VALUES
    ('第一食堂', '餐饮', '东区生活广场一层', '/upload/seed/merchant/canteen.svg', 1),
    ('湖畔咖啡', '咖啡饮品', '图书馆二层西侧', '/upload/seed/merchant/cafe.svg', 1),
    ('校园超市', '超市', '学生公寓A区入口', '/upload/seed/merchant/market.svg', 1),
    ('青藤文创', '文创用品', '图书馆一层大厅', '/upload/seed/merchant/stationery.svg', 1),
    ('校医务室', '医疗服务', '综合服务中心二层', '/upload/seed/merchant/clinic.svg', 1),
    ('云印文印', '文印服务', '教学楼B座一层', '/upload/seed/merchant/print.svg', 1),
    ('清洗驿站', '生活服务', '学生公寓C区负一层', '/upload/seed/merchant/laundry.svg', 1),
    ('活力运动馆', '体育服务', '体育中心北门', '/upload/seed/merchant/sports.svg', 1)
)
INSERT INTO merchant (merchant_name, type_id, location, logo, status, create_time, is_deleted)
SELECT dm.merchant_name, mt.id, dm.location, dm.logo, dm.status, CURRENT_TIMESTAMP, 0
FROM demo_merchant dm
JOIN merchant_type mt ON mt.type_name = dm.type_name AND mt.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM merchant m WHERE m.merchant_name = dm.merchant_name AND m.is_deleted = 0
);

WITH demo_product (product_name, merchant_name, price, stock, description, image, status) AS (
    VALUES
    ('招牌鸡腿饭', '第一食堂', 16.00, 120, '第一食堂热销套餐，含鸡腿、米饭和时蔬。', '/upload/seed/product/chicken-rice.svg', 1),
    ('番茄牛肉面', '第一食堂', 14.50, 90, '番茄汤底搭配牛肉片，适合午晚餐。', '/upload/seed/product/noodle.svg', 1),
    ('营养便当', '第一食堂', 18.00, 80, '荤素搭配的轻食便当，适合晚自习前补充能量。', '/upload/seed/product/bento.svg', 1),
    ('冷萃咖啡', '湖畔咖啡', 9.90, 70, '低糖冷萃咖啡，适合课间提神。', '/upload/seed/product/coffee.svg', 1),
    ('酸奶杯', '湖畔咖啡', 8.80, 60, '原味酸奶搭配莓果，适合早餐和课间。', '/upload/seed/product/yogurt.svg', 1),
    ('矿泉水', '校园超市', 2.00, 300, '550ml 校园常备饮用水。', '/upload/seed/product/water.svg', 1),
    ('折叠雨伞', '校园超市', 25.00, 75, '轻便折叠伞，适合宿舍和教学楼通勤。', '/upload/seed/product/umbrella.svg', 1),
    ('时令水果盒', '校园超市', 12.00, 65, '每日配送水果盒，适合宿舍分享。', '/upload/seed/product/fruit.svg', 1),
    ('横线笔记本', '青藤文创', 6.50, 160, 'A5 横线笔记本，适合课堂笔记。', '/upload/seed/product/notebook.svg', 1),
    ('考试中性笔', '青藤文创', 3.00, 240, '0.5mm 黑色中性笔，考试与日常书写适用。', '/upload/seed/product/pen.svg', 1),
    ('校园帆布袋', '青藤文创', 19.90, 60, '校园纪念款帆布袋，可放书本和电脑配件。', '/upload/seed/product/tote-bag.svg', 1),
    ('创可贴', '校医务室', 5.00, 100, '便携创可贴，适合轻微擦伤临时处理。', '/upload/seed/product/bandage.svg', 1),
    ('感冒冲剂', '校医务室', 18.00, 50, '常用感冒冲剂，请按医嘱或说明书使用。', '/upload/seed/product/medicine.svg', 1),
    ('彩色打印券', '云印文印', 4.00, 180, 'A4 彩色打印服务券，适合课程报告。', '/upload/seed/product/print-card.svg', 1),
    ('洗衣服务券', '清洗驿站', 6.00, 120, '宿舍区自助洗衣服务券。', '/upload/seed/product/laundry-card.svg', 1),
    ('运动体验券', '活力运动馆', 15.00, 45, '篮球馆与器械区单次体验券。', '/upload/seed/product/sports-pass.svg', 1)
)
INSERT INTO product (product_name, merchant_id, price, stock, description, image, status, create_time, is_deleted)
SELECT dp.product_name, m.id, dp.price, dp.stock, dp.description, dp.image, dp.status, CURRENT_TIMESTAMP, 0
FROM demo_product dp
JOIN merchant m ON m.merchant_name = dp.merchant_name AND m.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM product p
    WHERE p.product_name = dp.product_name AND p.merchant_id = m.id AND p.is_deleted = 0
);

WITH demo_book (book_name, author, collection_location, logo, status) AS (
    VALUES
    ('软件工程：实践者的研究方法', 'Roger S. Pressman', '图书馆三层 T-312 软件工程书架', '/upload/book/seed-software-engineering.png', 1),
    ('数据库系统概念', 'Abraham Silberschatz', '图书馆三层 T-326 数据库书架', '/upload/book/seed-database-system.png', 2),
    ('计算机网络：自顶向下方法', 'James F. Kurose', '图书馆三层 T-331 网络技术书架', '/upload/book/seed-computer-network.png', 1),
    ('人工智能基础', '李德毅', '图书馆四层 T-402 人工智能书架', '/upload/book/seed-ai-basic.png', 2),
    ('财务管理案例教程', '王化成', '图书馆二层 E-218 经管书架', '/upload/book/seed-financial-management.png', 1),
    ('用户体验设计', 'Donald A. Norman', '图书馆四层 A-416 设计书架', '/upload/book/seed-ux-design.png', 1),
    ('校园一卡通系统设计与实现', '教研项目组', '图书馆三层 T-318 信息系统书架', '/upload/book/seed-campus-card-system.svg', 1),
    ('数据可视化分析实战', '陈为', '图书馆四层 T-421 数据分析书架', '/upload/book/seed-data-visualization.svg', 2)
)
INSERT INTO book (book_name, author, collection_location, logo, status, create_time, is_deleted)
SELECT book_name, author, collection_location, logo, status, CURRENT_TIMESTAMP, 0
FROM demo_book db
WHERE NOT EXISTS (
    SELECT 1 FROM book b WHERE b.book_name = db.book_name AND b.author = db.author AND b.is_deleted = 0
);

-- ========================
-- 4. 充值订单、充值记录、消费记录
-- ========================
WITH demo_order (out_trade_no, card_no, amount, status, alipay_trade_no, operator_username, paid_offset_days, settled_offset_days, create_offset_days) AS (
    VALUES
    ('DEMO-ALIPAY-20260101-001', 'DEMO-S-20260101', 100.00, 'SETTLED', '202605010000000001', 'admin', 9, 9, 9),
    ('DEMO-ALIPAY-20260102-001', 'DEMO-S-20260102', 120.00, 'SETTLED', '202605020000000002', 'admin', 8, 8, 8),
    ('DEMO-ALIPAY-20260104-001', 'DEMO-S-20260104', 80.00, 'WAIT_PAY', NULL, 'admin', NULL, NULL, 2),
    ('DEMO-ALIPAY-20260105-001', 'DEMO-S-20260105', 150.00, 'CLOSED', '202605030000000003', 'admin', 7, NULL, 7)
)
INSERT INTO recharge_order (out_trade_no, card_id, amount, status, alipay_trade_no, operator_id, paid_time, settled_time, create_time, update_time, is_deleted)
SELECT d.out_trade_no, cc.id, d.amount, d.status, d.alipay_trade_no, au.id,
       CASE WHEN d.paid_offset_days IS NULL THEN NULL ELSE CURRENT_DATE - d.paid_offset_days + TIME '10:05:00' END,
       CASE WHEN d.settled_offset_days IS NULL THEN NULL ELSE CURRENT_DATE - d.settled_offset_days + TIME '10:08:00' END,
       CURRENT_DATE - d.create_offset_days + TIME '09:58:00',
       CURRENT_TIMESTAMP,
       0
FROM demo_order d
JOIN campus_card cc ON cc.card_no = d.card_no AND cc.is_deleted = 0
LEFT JOIN admin_user au ON au.username = d.operator_username AND au.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM recharge_order ro WHERE ro.out_trade_no = d.out_trade_no AND ro.is_deleted = 0
);

WITH demo_recharge (card_no, amount, recharge_type, operator_username, create_offset_days, create_clock) AS (
    VALUES
    ('DEMO-S-20260101', 100.00, '支付宝', 'admin', 9, TIME '10:10:00'),
    ('DEMO-S-20260101', 50.00, '人工充值', 'admin', 4, TIME '14:20:00'),
    ('DEMO-S-20260102', 120.00, '支付宝', 'admin', 8, TIME '11:15:00'),
    ('DEMO-S-20260104', 60.00, '人工充值', 'admin', 6, TIME '16:30:00'),
    ('DEMO-S-20260105', 200.00, '人工充值', 'admin', 10, TIME '09:35:00'),
    ('DEMO-S-20260106', 80.00, '支付宝', 'admin', 5, TIME '12:05:00'),
    ('DEMO-S-20260107', 150.00, '人工充值', 'admin', 7, TIME '13:10:00'),
    ('DEMO-S-20260108', 90.00, '支付宝', 'admin', 3, TIME '10:45:00'),
    ('DEMO-S-20260109', 110.00, '人工充值', 'admin', 6, TIME '15:50:00'),
    ('DEMO-S-20260110', 70.00, '支付宝', 'admin', 2, TIME '09:25:00'),
    ('DEMO-S-20260111', 130.00, '人工充值', 'admin', 11, TIME '10:40:00'),
    ('DEMO-T-T20261001', 180.00, '人工充值', 'admin', 12, TIME '11:30:00')
)
INSERT INTO recharge_record (account_id, amount, recharge_type, operator_id, create_time, is_deleted)
SELECT a.id, dr.amount, dr.recharge_type, COALESCE(au.id, 1),
       CURRENT_DATE - dr.create_offset_days + dr.create_clock,
       0
FROM demo_recharge dr
JOIN campus_card cc ON cc.card_no = dr.card_no AND cc.is_deleted = 0
JOIN account a ON a.card_id = cc.id AND a.is_deleted = 0
LEFT JOIN admin_user au ON au.username = dr.operator_username AND au.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM recharge_record rr
    WHERE rr.account_id = a.id
      AND rr.amount = dr.amount
      AND rr.recharge_type = dr.recharge_type
      AND rr.create_time = CURRENT_DATE - dr.create_offset_days + dr.create_clock
      AND rr.is_deleted = 0
);

WITH demo_consume (card_no, merchant_name, product_name, quantity, amount, consume_offset_days, consume_clock) AS (
    VALUES
    ('DEMO-S-20260101', '第一食堂', '招牌鸡腿饭', 1, 16.00, 8, TIME '12:20:00'),
    ('DEMO-S-20260101', '湖畔咖啡', '冷萃咖啡', 1, 9.90, 6, TIME '15:30:00'),
    ('DEMO-S-20260101', '校园超市', '时令水果盒', 1, 12.00, 2, TIME '19:05:00'),
    ('DEMO-S-20260102', '第一食堂', '番茄牛肉面', 1, 14.50, 7, TIME '12:10:00'),
    ('DEMO-S-20260102', '青藤文创', '考试中性笔', 2, 6.00, 5, TIME '17:40:00'),
    ('DEMO-S-20260104', '校医务室', '创可贴', 1, 5.00, 3, TIME '10:25:00'),
    ('DEMO-S-20260105', '第一食堂', '营养便当', 1, 18.00, 9, TIME '18:10:00'),
    ('DEMO-S-20260105', '云印文印', '彩色打印券', 3, 12.00, 4, TIME '16:05:00'),
    ('DEMO-S-20260106', '校园超市', '折叠雨伞', 1, 25.00, 4, TIME '09:55:00'),
    ('DEMO-S-20260107', '湖畔咖啡', '酸奶杯', 1, 8.80, 5, TIME '08:50:00'),
    ('DEMO-S-20260107', '活力运动馆', '运动体验券', 1, 15.00, 1, TIME '20:15:00'),
    ('DEMO-S-20260108', '清洗驿站', '洗衣服务券', 2, 12.00, 2, TIME '18:30:00'),
    ('DEMO-S-20260109', '青藤文创', '横线笔记本', 2, 13.00, 6, TIME '14:12:00'),
    ('DEMO-S-20260110', '校医务室', '感冒冲剂', 1, 18.00, 1, TIME '11:40:00'),
    ('DEMO-S-20260111', '校园超市', '矿泉水', 3, 6.00, 10, TIME '13:05:00'),
    ('DEMO-T-T20261001', '湖畔咖啡', '冷萃咖啡', 2, 19.80, 3, TIME '16:18:00')
)
INSERT INTO consume_record (account_id, merchant_id, product_id, product_name, quantity, amount, balance_after, status, consume_time, is_deleted)
SELECT a.id, m.id, p.id, dc.product_name, dc.quantity, dc.amount, 0.00, 1,
       CURRENT_DATE - dc.consume_offset_days + dc.consume_clock,
       0
FROM demo_consume dc
JOIN campus_card cc ON cc.card_no = dc.card_no AND cc.is_deleted = 0
JOIN account a ON a.card_id = cc.id AND a.is_deleted = 0
JOIN merchant m ON m.merchant_name = dc.merchant_name AND m.is_deleted = 0
JOIN product p ON p.product_name = dc.product_name AND p.merchant_id = m.id AND p.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM consume_record cr
    WHERE cr.account_id = a.id
      AND cr.product_id = p.id
      AND cr.consume_time = CURRENT_DATE - dc.consume_offset_days + dc.consume_clock
      AND cr.is_deleted = 0
);

-- ========================
-- 5. 图书借阅申请与借阅记录
-- ========================
WITH demo_borrow_application (card_no, book_name, borrow_days, application_offset_days, status, operator_username, approval_offset_days, remark) AS (
    VALUES
    ('DEMO-S-20260101', '数据库系统概念', 30, 14, 2, 'admin', 13, '课程设计需要，准予借阅。'),
    ('DEMO-S-20260102', '人工智能基础', 30, 8, 2, 'admin', 7, '学习计划清晰，准予借阅。'),
    ('DEMO-S-20260104', '数据可视化分析实战', 21, 36, 2, 'admin', 35, '已通过，提醒按期归还。'),
    ('DEMO-S-20260105', '用户体验设计', 14, 2, 1, NULL, NULL, '等待老师确认项目用途。'),
    ('DEMO-S-20260107', '财务管理案例教程', 14, 5, 3, 'admin', 4, '专业方向不匹配，建议借阅设计类图书。')
)
INSERT INTO borrow_application (card_id, book_id, borrow_days, application_time, status, operator_id, approval_time, remark, create_time, update_time, is_deleted)
SELECT cc.id, b.id, dba.borrow_days,
       CURRENT_DATE - dba.application_offset_days + TIME '10:00:00',
       dba.status,
       au.id,
       CASE WHEN dba.approval_offset_days IS NULL THEN NULL ELSE CURRENT_DATE - dba.approval_offset_days + TIME '15:00:00' END,
       dba.remark,
       CURRENT_DATE - dba.application_offset_days + TIME '10:00:00',
       CURRENT_TIMESTAMP,
       0
FROM demo_borrow_application dba
JOIN campus_card cc ON cc.card_no = dba.card_no AND cc.is_deleted = 0
JOIN book b ON b.book_name = dba.book_name AND b.is_deleted = 0
LEFT JOIN admin_user au ON au.username = dba.operator_username AND au.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM borrow_application ba
    WHERE ba.card_id = cc.id
      AND ba.book_id = b.id
      AND ba.application_time = CURRENT_DATE - dba.application_offset_days + TIME '10:00:00'
      AND ba.is_deleted = 0
);

WITH demo_borrow_record (card_no, book_name, application_offset_days, borrow_offset_days, due_offset_days, return_offset_days, status) AS (
    VALUES
    ('DEMO-S-20260101', '数据库系统概念', 14, 13, 17, NULL, 1),
    ('DEMO-S-20260102', '人工智能基础', 8, 7, 23, NULL, 1),
    ('DEMO-S-20260104', '数据可视化分析实战', 36, 35, -5, NULL, 3),
    ('DEMO-S-20260106', '计算机网络：自顶向下方法', NULL, 20, 10, 3, 2)
)
INSERT INTO borrow_record (card_id, book_id, application_id, borrow_time, return_time, due_time, status, create_time, is_deleted)
SELECT cc.id, b.id, ba.id,
       CURRENT_DATE - dbr.borrow_offset_days + TIME '09:00:00',
       CASE WHEN dbr.return_offset_days IS NULL THEN NULL ELSE CURRENT_DATE - dbr.return_offset_days + TIME '17:00:00' END,
       CURRENT_DATE + dbr.due_offset_days + TIME '23:59:00',
       dbr.status,
       CURRENT_DATE - dbr.borrow_offset_days + TIME '09:00:00',
       0
FROM demo_borrow_record dbr
JOIN campus_card cc ON cc.card_no = dbr.card_no AND cc.is_deleted = 0
JOIN book b ON b.book_name = dbr.book_name AND b.is_deleted = 0
LEFT JOIN borrow_application ba
  ON ba.card_id = cc.id
 AND ba.book_id = b.id
 AND (dbr.application_offset_days IS NOT NULL AND ba.application_time = CURRENT_DATE - dbr.application_offset_days + TIME '10:00:00')
 AND ba.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM borrow_record br
    WHERE br.card_id = cc.id
      AND br.book_id = b.id
      AND br.borrow_time = CURRENT_DATE - dbr.borrow_offset_days + TIME '09:00:00'
      AND br.is_deleted = 0
);

-- ========================
-- 6. 门禁点与门禁记录
-- ========================
WITH demo_access_point (name, location, latitude, longitude, radius, device_id, status) AS (
    VALUES
    ('图书馆东门', '图书馆东门入口', 35.495120, 114.615360, 80, 'AC-LIB-EAST-01', 1),
    ('教学楼B座南门', '教学楼B座南门', 35.497820, 114.617640, 60, 'AC-TEACH-B-S-01', 1),
    ('学生公寓A区门禁', '学生公寓A区入口', 35.492660, 114.612980, 70, 'AC-DORM-A-01', 1),
    ('实验中心北门', '实验中心北门', 35.496540, 114.619120, 50, 'AC-LAB-N-01', 0)
)
INSERT INTO access_point (name, location, latitude, longitude, radius, device_id, status, create_time, is_deleted)
SELECT name, location, latitude, longitude, radius, device_id, status, CURRENT_TIMESTAMP, 0
FROM demo_access_point dap
WHERE NOT EXISTS (
    SELECT 1 FROM access_point ap WHERE ap.device_id = dap.device_id AND ap.is_deleted = 0
);

WITH demo_access_record (card_no, point_device_id, direction, status, latitude, longitude, distance, access_offset_days, access_clock, device_info) AS (
    VALUES
    ('DEMO-S-20260101', 'AC-LIB-EAST-01', '进', '成功', 35.495118, 114.615352, 1.20, 6, TIME '08:05:00', 'iPhone Safari 定位开门'),
    ('DEMO-S-20260101', 'AC-LIB-EAST-01', '出', '成功', 35.495116, 114.615350, 1.50, 6, TIME '11:45:00', 'iPhone Safari 定位开门'),
    ('DEMO-S-20260102', 'AC-TEACH-B-S-01', '进', '成功', 35.497830, 114.617650, 2.10, 5, TIME '07:55:00', 'Chrome Android 定位开门'),
    ('DEMO-S-20260104', 'AC-DORM-A-01', '出', '成功', 35.492670, 114.612970, 2.50, 4, TIME '09:20:00', 'Edge Mobile 定位开门'),
    ('DEMO-S-20260105', 'AC-LIB-EAST-01', '进', '失败', 35.496900, 114.620200, 480.00, 3, TIME '18:12:00', '超出允许范围，系统拒绝'),
    ('DEMO-S-20260108', 'AC-TEACH-B-S-01', '进', '成功', 35.497815, 114.617631, 1.80, 2, TIME '08:10:00', 'Chrome iOS 定位开门'),
    ('DEMO-T-T20261001', 'AC-LIB-EAST-01', '进', '成功', 35.495121, 114.615361, 0.80, 1, TIME '08:00:00', '教师端扫码开门')
)
INSERT INTO access_record (card_id, access_point_id, direction, location, status, actual_latitude, actual_longitude, distance, access_time, device_info, is_deleted)
SELECT cc.id, ap.id, dar.direction, ap.location, dar.status, dar.latitude, dar.longitude, dar.distance,
       CURRENT_DATE - dar.access_offset_days + dar.access_clock,
       dar.device_info,
       0
FROM demo_access_record dar
JOIN campus_card cc ON cc.card_no = dar.card_no AND cc.is_deleted = 0
JOIN access_point ap ON ap.device_id = dar.point_device_id AND ap.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM access_record ar
    WHERE ar.card_id = cc.id
      AND ar.access_point_id = ap.id
      AND ar.access_time = CURRENT_DATE - dar.access_offset_days + dar.access_clock
      AND ar.is_deleted = 0
);

-- ========================
-- 7. 考勤位置、申报、考勤记录
-- ========================
WITH demo_attendance_location (teacher_no, location_name, location, latitude, longitude, radius, start_offset_days, start_clock, end_offset_days, end_clock, status) AS (
    VALUES
    ('T20261001', '软件工程晨读打卡', '教学楼B座 302 教室', 35.497820, 114.617640, 80, 0, TIME '08:00:00', 0, TIME '09:00:00', 1),
    ('T20261002', '经管学院晚自习打卡', '图书馆二层研讨区', 35.495120, 114.615360, 100, 1, TIME '19:00:00', 1, TIME '21:00:00', 1),
    ('T20261003', '智能制造实训签到', '实验中心北门', 35.496540, 114.619120, 60, 2, TIME '14:00:00', 2, TIME '16:00:00', 1),
    ('T20261004', '设计学院采风集合', '艺术楼一层大厅', 35.494860, 114.616420, 90, 3, TIME '09:00:00', 3, TIME '11:00:00', 1)
)
INSERT INTO attendance_location (teacher_id, location_name, location, latitude, longitude, radius, start_time, end_time, status, create_time, is_deleted)
SELECT t.id, dal.location_name, dal.location, dal.latitude, dal.longitude, dal.radius,
       CURRENT_DATE - dal.start_offset_days + dal.start_clock,
       CURRENT_DATE - dal.end_offset_days + dal.end_clock,
       dal.status,
       CURRENT_TIMESTAMP,
       0
FROM demo_attendance_location dal
JOIN teacher t ON t.teacher_no = dal.teacher_no AND t.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_location al
    WHERE al.teacher_id = t.id AND al.location_name = dal.location_name AND al.is_deleted = 0
);

WITH demo_attendance_application (student_no, card_no, application_type, internship_company, reason, start_offset_days, end_offset_days, status, reviewer_no, review_offset_days, review_remark, create_offset_days) AS (
    VALUES
    ('20260105', 'DEMO-S-20260105', 'INTERNSHIP', '青舟智能制造有限公司', '<p>参与智能产线数据采集与设备调试，企业导师已确认接收。</p>', 20, 40, 'APPROVED', 'T20261003', 18, '企业信息完整，准予校外实习。', 21),
    ('20260107', 'DEMO-S-20260107', 'INTERNSHIP', '拾光品牌设计工作室', '<p>参与校园品牌视觉改版项目，需按周提交实习日志。</p>', 16, 35, 'APPROVED', 'T20261004', 15, '实习内容与专业方向一致。', 17),
    ('20260104', 'DEMO-S-20260104', 'LEAVE', NULL, '<p>因身体不适需复诊，请假两天并已上传门诊材料。</p>', 0, 1, 'APPROVED', 'T20261002', 1, '材料完整，同意请假。', 2),
    ('20260110', 'DEMO-S-20260110', 'LEAVE', NULL, '<p>感冒发热，申请休息一天并按时补交课程任务。</p>', 0, 0, 'APPROVED', 'T20261002', 1, '注意休息，按时返校。', 1),
    ('20260102', 'DEMO-S-20260102', 'LEAVE', NULL, '<p>参加校外竞赛路演，申请事假一天。</p>', 5, 5, 'REJECTED', 'T20261001', 4, '与课程考核冲突，暂不通过。', 6),
    ('20260108', 'DEMO-S-20260108', 'INTERNSHIP', '北辰工程咨询有限公司', '<p>已获得实习意向，等待学院确认。</p>', 3, 28, 'PENDING', NULL, NULL, NULL, 1)
)
INSERT INTO attendance_application (student_id, card_id, application_type, internship_company, reason, start_date, end_date, status, reviewer_id, review_time, review_remark, create_time, update_time, is_deleted)
SELECT s.id, cc.id, daa.application_type, daa.internship_company, daa.reason,
       CURRENT_DATE - daa.start_offset_days,
       CURRENT_DATE + daa.end_offset_days,
       daa.status,
       rt.id,
       CASE WHEN daa.review_offset_days IS NULL THEN NULL ELSE CURRENT_DATE - daa.review_offset_days + TIME '16:00:00' END,
       daa.review_remark,
       CURRENT_DATE - daa.create_offset_days + TIME '09:00:00',
       CURRENT_TIMESTAMP,
       0
FROM demo_attendance_application daa
JOIN student s ON s.student_no = daa.student_no AND s.is_deleted = 0
JOIN campus_card cc ON cc.card_no = daa.card_no AND cc.is_deleted = 0
LEFT JOIN teacher rt ON rt.teacher_no = daa.reviewer_no AND rt.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_application aa
    WHERE aa.card_id = cc.id
      AND aa.application_type = daa.application_type
      AND aa.create_time = CURRENT_DATE - daa.create_offset_days + TIME '09:00:00'
      AND aa.is_deleted = 0
);

WITH demo_attendance_record (card_no, location_name, status, actual_location, latitude, longitude, device_info, attendance_type, internship_company, internship_log, log_offset_days, leave_create_offset_days, record_offset_days, record_clock) AS (
    VALUES
    ('DEMO-S-20260101', '软件工程晨读打卡', '正常', '教学楼B座 302 教室', 35.497822, 114.617641, 'iPhone 15 Safari', 'CAMPUS_LOCATION', NULL, NULL, NULL, NULL, 0, TIME '08:05:00'),
    ('DEMO-S-20260102', '软件工程晨读打卡', '迟到', '教学楼B座 302 教室', 35.497829, 114.617650, 'Chrome Android', 'CAMPUS_LOCATION', NULL, NULL, NULL, NULL, 0, TIME '08:25:00'),
    ('DEMO-S-20260109', '智能制造实训签到', '正常', '实验中心北门', 35.496542, 114.619118, 'Edge Mobile', 'CAMPUS_LOCATION', NULL, NULL, NULL, NULL, 2, TIME '14:02:00'),
    ('DEMO-S-20260108', '设计学院采风集合', '缺勤', '未在规定时间打卡', NULL, NULL, '系统自动生成', 'CAMPUS_LOCATION', NULL, NULL, NULL, NULL, 3, TIME '11:30:00'),
    ('DEMO-S-20260104', '经管学院晚自习打卡', '正常', '校医院复诊', 35.493880, 114.613920, '请假状态记录', 'LEAVE', NULL, NULL, NULL, 2, 0, TIME '19:05:00'),
    ('DEMO-S-20260105', NULL, '正常', '青舟智能制造有限公司研发楼', 34.746600, 113.625400, '企业 WiFi 定位', 'OFF_CAMPUS_LOCATION', '青舟智能制造有限公司', NULL, NULL, NULL, 1, TIME '09:15:00'),
    ('DEMO-S-20260107', NULL, '正常', '拾光品牌设计工作室', 35.861700, 104.195400, '实习日志提交', 'INTERNSHIP_LOG', '拾光品牌设计工作室', '完成品牌调研问卷整理，输出校园导视色彩规范初稿。', 1, NULL, 1, TIME '18:40:00')
)
INSERT INTO attendance_record (card_id, location_id, status, actual_location, actual_latitude, actual_longitude, device_info, attendance_type, internship_company, internship_log, internship_log_date, leave_application_id, leave_reason, leave_start_date, leave_end_date, record_time, is_deleted)
SELECT cc.id, al.id, dar.status, dar.actual_location, dar.latitude, dar.longitude, dar.device_info, dar.attendance_type,
       dar.internship_company, dar.internship_log,
       CASE WHEN dar.log_offset_days IS NULL THEN NULL ELSE CURRENT_DATE - dar.log_offset_days END,
       la.id,
       la.reason,
       la.start_date,
       la.end_date,
       CURRENT_DATE - dar.record_offset_days + dar.record_clock,
       0
FROM demo_attendance_record dar
JOIN campus_card cc ON cc.card_no = dar.card_no AND cc.is_deleted = 0
LEFT JOIN attendance_location al ON al.location_name = dar.location_name AND al.is_deleted = 0
LEFT JOIN attendance_application la
  ON la.card_id = cc.id
 AND la.application_type = 'LEAVE'
 AND dar.leave_create_offset_days IS NOT NULL
 AND la.create_time = CURRENT_DATE - dar.leave_create_offset_days + TIME '09:00:00'
 AND la.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM attendance_record ar
    WHERE ar.card_id = cc.id
      AND ar.record_time = CURRENT_DATE - dar.record_offset_days + dar.record_clock
      AND ar.attendance_type = dar.attendance_type
      AND ar.is_deleted = 0
);

-- ========================
-- 8. 通勤车站点、线路、车辆、班次、乘车记录
-- ========================
WITH demo_station (station_name, location, latitude, longitude) AS (
    VALUES
    ('东区校门', '东区校门公交港湾', 35.490429, 114.609707),
    ('图书馆站', '图书馆东侧广场', 35.495000, 114.615000),
    ('实验中心站', '实验中心北门停车区', 35.496540, 114.619120),
    ('老校区南门', '老校区南门候车点', 35.500000, 114.620000),
    ('高铁东站', '高铁东站高校专线候车区', 35.507800, 114.635200)
)
INSERT INTO commute_station (station_name, location, latitude, longitude, status, create_time, is_deleted)
SELECT station_name, location, latitude, longitude, 1, CURRENT_TIMESTAMP, 0
FROM demo_station ds
WHERE NOT EXISTS (
    SELECT 1 FROM commute_station cs WHERE cs.station_name = ds.station_name AND cs.is_deleted = 0
);

WITH demo_vehicle (plate_number, vehicle_type, seat_count, status) AS (
    VALUES
    ('豫A-D001', '新能源大巴', 45, 1),
    ('豫A-D002', '中巴车', 30, 1),
    ('豫A-D003', '新能源大巴', 45, 2)
)
INSERT INTO commute_vehicle (plate_number, vehicle_type, seat_count, status, create_time, is_deleted)
SELECT plate_number, vehicle_type, seat_count, status, CURRENT_TIMESTAMP, 0
FROM demo_vehicle dv
WHERE NOT EXISTS (
    SELECT 1 FROM commute_vehicle cv WHERE cv.plate_number = dv.plate_number AND cv.is_deleted = 0
);

WITH demo_route (route_name, start_station, end_station, total_distance, total_time, status) AS (
    VALUES
    ('D1 东区-老校区早班线', '东区校门', '老校区南门', 12.80, 35, 1),
    ('D2 图书馆-实验中心循环线', '图书馆站', '实验中心站', 3.60, 15, 1),
    ('D3 校园-高铁东站周末线', '东区校门', '高铁东站', 18.50, 42, 1)
)
INSERT INTO commute_route (route_name, start_station, end_station, total_distance, total_time, status, create_time, is_deleted)
SELECT route_name, start_station, end_station, total_distance, total_time, status, CURRENT_TIMESTAMP, 0
FROM demo_route dr
WHERE NOT EXISTS (
    SELECT 1 FROM commute_route cr WHERE cr.route_name = dr.route_name AND cr.is_deleted = 0
);

WITH demo_schedule (route_name, plate_number, departure_time, frequency, start_offset_days, end_offset_days, status) AS (
    VALUES
    ('D1 东区-老校区早班线', '豫A-D001', TIME '07:20:00', '工作日', 20, 60, 1),
    ('D1 东区-老校区早班线', '豫A-D002', TIME '17:40:00', '工作日', 20, 60, 1),
    ('D2 图书馆-实验中心循环线', '豫A-D002', TIME '09:00:00', '每天', 20, 60, 1),
    ('D3 校园-高铁东站周末线', '豫A-D001', TIME '14:30:00', '周末', 20, 60, 1)
)
INSERT INTO commute_schedule (route_id, vehicle_id, departure_time, frequency, start_date, end_date, status, create_time, is_deleted)
SELECT cr.id, cv.id, ds.departure_time, ds.frequency,
       CURRENT_DATE - ds.start_offset_days,
       CURRENT_DATE + ds.end_offset_days,
       ds.status, CURRENT_TIMESTAMP, 0
FROM demo_schedule ds
JOIN commute_route cr ON cr.route_name = ds.route_name AND cr.is_deleted = 0
JOIN commute_vehicle cv ON cv.plate_number = ds.plate_number AND cv.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM commute_schedule cs
    WHERE cs.route_id = cr.id
      AND cs.vehicle_id = cv.id
      AND cs.departure_time = ds.departure_time
      AND cs.is_deleted = 0
);

WITH demo_commute_record (card_no, route_name, plate_number, departure_time, seat_number, ride_offset_days, ride_clock) AS (
    VALUES
    ('DEMO-S-20260101', 'D1 东区-老校区早班线', '豫A-D001', TIME '07:20:00', '1', 5, TIME '07:18:00'),
    ('DEMO-S-20260102', 'D1 东区-老校区早班线', '豫A-D001', TIME '07:20:00', '2', 5, TIME '07:19:00'),
    ('DEMO-S-20260105', 'D3 校园-高铁东站周末线', '豫A-D001', TIME '14:30:00', '8', 4, TIME '14:25:00'),
    ('DEMO-S-20260107', 'D3 校园-高铁东站周末线', '豫A-D001', TIME '14:30:00', '9', 4, TIME '14:26:00'),
    ('DEMO-S-20260108', 'D2 图书馆-实验中心循环线', '豫A-D002', TIME '09:00:00', '5', 2, TIME '08:58:00'),
    ('DEMO-S-20260109', 'D1 东区-老校区早班线', '豫A-D002', TIME '17:40:00', '12', 1, TIME '17:35:00')
)
INSERT INTO commute_record (card_id, route_id, vehicle_id, schedule_id, seat_number, ride_time, status, is_deleted)
SELECT cc.id, cr.id, cv.id, cs.id, dcr.seat_number,
       CURRENT_DATE - dcr.ride_offset_days + dcr.ride_clock,
       1,
       0
FROM demo_commute_record dcr
JOIN campus_card cc ON cc.card_no = dcr.card_no AND cc.is_deleted = 0
JOIN commute_route cr ON cr.route_name = dcr.route_name AND cr.is_deleted = 0
JOIN commute_vehicle cv ON cv.plate_number = dcr.plate_number AND cv.is_deleted = 0
JOIN commute_schedule cs ON cs.route_id = cr.id AND cs.vehicle_id = cv.id AND cs.departure_time = dcr.departure_time AND cs.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM commute_record rr
    WHERE rr.card_id = cc.id
      AND rr.schedule_id = cs.id
      AND rr.ride_time = CURRENT_DATE - dcr.ride_offset_days + dcr.ride_clock
      AND rr.is_deleted = 0
);

-- ========================
-- 9. 账户流水与余额回算
-- ========================
WITH demo_accounts AS (
    SELECT a.id AS account_id
    FROM account a
    JOIN campus_card cc ON cc.id = a.card_id
    WHERE cc.card_no LIKE 'DEMO-%' AND cc.is_deleted = 0 AND a.is_deleted = 0
),
demo_tx AS (
    SELECT rr.account_id, '充值' AS change_type, rr.amount, rr.id AS related_id, rr.create_time AS event_time, 1 AS tx_order
    FROM recharge_record rr
    JOIN demo_accounts da ON da.account_id = rr.account_id
    WHERE rr.is_deleted = 0
    UNION ALL
    SELECT cr.account_id, '消费' AS change_type, cr.amount, cr.id AS related_id, cr.consume_time AS event_time, 2 AS tx_order
    FROM consume_record cr
    JOIN demo_accounts da ON da.account_id = cr.account_id
    WHERE cr.is_deleted = 0
),
running_tx AS (
    SELECT account_id, change_type, amount, related_id, event_time,
           SUM(CASE WHEN change_type = '充值' THEN amount ELSE -amount END)
           OVER (PARTITION BY account_id ORDER BY event_time, tx_order, related_id) AS balance_after
    FROM demo_tx
)
INSERT INTO account_flow (account_id, change_type, amount, balance_after, related_id, create_time, is_deleted)
SELECT account_id, change_type, amount, balance_after, related_id, event_time, 0
FROM running_tx rt
WHERE NOT EXISTS (
    SELECT 1 FROM account_flow af
    WHERE af.account_id = rt.account_id
      AND af.change_type = rt.change_type
      AND af.related_id = rt.related_id
      AND af.is_deleted = 0
);

UPDATE consume_record cr
SET balance_after = af.balance_after
FROM account_flow af
WHERE af.change_type = '消费'
  AND af.related_id = cr.id
  AND cr.is_deleted = 0
  AND af.is_deleted = 0
  AND cr.account_id IN (
      SELECT a.id
      FROM account a
      JOIN campus_card cc ON cc.id = a.card_id
      WHERE cc.card_no LIKE 'DEMO-%' AND cc.is_deleted = 0 AND a.is_deleted = 0
  );

UPDATE account a
SET balance = COALESCE((
        SELECT SUM(CASE WHEN tx.change_type = '充值' THEN tx.amount ELSE -tx.amount END)
        FROM (
            SELECT rr.account_id, '充值' AS change_type, rr.amount
            FROM recharge_record rr
            WHERE rr.account_id = a.id AND rr.is_deleted = 0
            UNION ALL
            SELECT cr.account_id, '消费' AS change_type, cr.amount
            FROM consume_record cr
            WHERE cr.account_id = a.id AND cr.is_deleted = 0
        ) tx
    ), 0),
    update_time = CURRENT_TIMESTAMP
WHERE a.id IN (
    SELECT a2.id
    FROM account a2
    JOIN campus_card cc ON cc.id = a2.card_id
    WHERE cc.card_no LIKE 'DEMO-%' AND cc.is_deleted = 0 AND a2.is_deleted = 0
);

-- ========================
-- 10. 操作日志
-- ========================
WITH demo_log (operator_username, operation_type, target_table, target_key, content, create_offset_days, create_clock) AS (
    VALUES
    ('admin', '新增', 'student', '20260101', '导入答辩演示学生林亦辰', 12, TIME '09:00:00'),
    ('admin', '开卡', 'campus_card', 'DEMO-S-20260101', '为学生林亦辰办理校园卡', 12, TIME '09:20:00'),
    ('admin', '充值', 'recharge_record', 'DEMO-S-20260101', '为学生校园卡充值100元', 9, TIME '10:15:00'),
    ('T20261002', '审批', 'attendance_application', '20260104', '审核通过唐若溪请假申请', 1, TIME '16:00:00'),
    ('admin', '挂失', 'campus_card', 'DEMO-S-20260103', '处理许知远校园卡挂失', 5, TIME '10:00:00'),
    ('admin', '维护', 'commute_schedule', 'D1 东区-老校区早班线', '维护东区到老校区早班线路', 3, TIME '15:00:00')
)
INSERT INTO operation_log (operator_id, operation_type, target_table, target_id, content, create_time, is_deleted)
SELECT au.id, dl.operation_type, dl.target_table,
       CASE
           WHEN dl.target_table = 'student' THEN (SELECT s.id FROM student s WHERE s.student_no = dl.target_key AND s.is_deleted = 0)
           WHEN dl.target_table = 'campus_card' THEN (SELECT cc.id FROM campus_card cc WHERE cc.card_no = dl.target_key AND cc.is_deleted = 0)
           WHEN dl.target_table = 'attendance_application' THEN (SELECT aa.id FROM attendance_application aa JOIN student s ON s.id = aa.student_id WHERE s.student_no = dl.target_key AND aa.is_deleted = 0 ORDER BY aa.create_time DESC LIMIT 1)
           WHEN dl.target_table = 'commute_schedule' THEN (SELECT cs.id FROM commute_schedule cs JOIN commute_route cr ON cr.id = cs.route_id WHERE cr.route_name = dl.target_key AND cs.is_deleted = 0 ORDER BY cs.departure_time LIMIT 1)
           ELSE NULL
       END,
       dl.content,
       CURRENT_DATE - dl.create_offset_days + dl.create_clock,
       0
FROM demo_log dl
JOIN admin_user au ON au.username = dl.operator_username AND au.is_deleted = 0
WHERE NOT EXISTS (
    SELECT 1 FROM operation_log ol
    WHERE ol.operation_type = dl.operation_type
      AND ol.target_table = dl.target_table
      AND ol.content = dl.content
      AND ol.is_deleted = 0
);

COMMIT;
