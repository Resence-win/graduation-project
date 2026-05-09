-- 学院专业字典升级脚本

CREATE TABLE IF NOT EXISTS college_major (
    id BIGSERIAL PRIMARY KEY,
    college_name VARCHAR(100) NOT NULL,
    major_name VARCHAR(100) NOT NULL,
    status INT DEFAULT 1,
    sort_order INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP,
    is_deleted INT DEFAULT 0
);

COMMENT ON TABLE college_major IS '学院专业字典表';
COMMENT ON COLUMN college_major.id IS '主键ID';
COMMENT ON COLUMN college_major.college_name IS '学院名称';
COMMENT ON COLUMN college_major.major_name IS '专业名称';
COMMENT ON COLUMN college_major.status IS '状态(1启用0停用)';
COMMENT ON COLUMN college_major.sort_order IS '排序值';
COMMENT ON COLUMN college_major.create_time IS '创建时间';
COMMENT ON COLUMN college_major.update_time IS '更新时间';
COMMENT ON COLUMN college_major.is_deleted IS '是否删除(0否1是)';

CREATE UNIQUE INDEX IF NOT EXISTS uk_college_major_active
ON college_major(college_name, major_name)
WHERE is_deleted = 0;

CREATE INDEX IF NOT EXISTS idx_college_major_college ON college_major(college_name);
CREATE INDEX IF NOT EXISTS idx_college_major_status ON college_major(status);

INSERT INTO college_major (college_name, major_name, sort_order)
SELECT source_data.college_name, source_data.major_name, source_data.sort_order
FROM (
    VALUES
    ('材料科学与工程学院', '材料成型及控制工程', 1),
    ('材料科学与工程学院', '焊接技术与工程', 2),
    ('材料科学与工程学院', '功能材料', 3),
    ('材料科学与工程学院', '金属材料工程', 4),
    ('材料科学与工程学院', '高分子材料与工程', 5),
    ('材料科学与工程学院', '无机非金属材料工程', 6),
    ('机电工程学院', '机械设计制造及其自动化', 7),
    ('机电工程学院', '工业工程', 8),
    ('机电工程学院', '智能制造工程', 9),
    ('石油化工学院', '过程装备与控制工程', 10),
    ('石油化工学院', '化学工程与工艺', 11),
    ('石油化工学院', '应用化学', 12),
    ('石油化工学院', '安全工程', 13),
    ('石油化工学院', '油气储运工程', 14),
    ('能源与动力工程学院', '能源与动力工程', 15),
    ('能源与动力工程学院', '机械电子工程', 16),
    ('能源与动力工程学院', '水利水电工程', 17),
    ('能源与动力工程学院', '测控技术与仪器', 18),
    ('自动化与电气工程学院', '自动化', 19),
    ('自动化与电气工程学院', '电气工程及其自动化', 20),
    ('自动化与电气工程学院', '机器人工程', 21),
    ('计算机与人工智能学院', '计算机科学与技术', 22),
    ('计算机与人工智能学院', '通信工程', 23),
    ('计算机与人工智能学院', '软件工程', 24),
    ('计算机与人工智能学院', '物联网工程', 25),
    ('计算机与人工智能学院', '网络空间安全', 26),
    ('计算机与人工智能学院', '数据科学与大数据技术', 27),
    ('土木与水利工程学院', '土木工程', 28),
    ('土木与水利工程学院', '智能建造', 29),
    ('土木与水利工程学院', '道路桥梁与渡河工程', 30),
    ('土木与水利工程学院', '工程管理', 31),
    ('土木与水利工程学院', '建筑环境与能源应用工程', 32),
    ('土木与水利工程学院', '给排水科学与工程', 33),
    ('土木与水利工程学院', '测绘工程', 34),
    ('冶金与环境学院', '冶金工程', 35),
    ('冶金与环境学院', '环境工程', 36),
    ('微电子现代产业学院', '电子信息科学与技术', 37),
    ('微电子现代产业学院', '微电子科学与工程', 38),
    ('绿色能源与储能学院', '新能源科学与工程', 39),
    ('经济管理学院', '工商管理', 40),
    ('经济管理学院', '会计学', 41),
    ('经济管理学院', '财务管理', 42),
    ('经济管理学院', '国际经济与贸易', 43),
    ('经济管理学院', '金融学', 44),
    ('经济管理学院', '市场营销', 45),
    ('经济管理学院', '大数据管理与应用', 46),
    ('建筑与艺术设计学院', '建筑学', 47),
    ('建筑与艺术设计学院', '城乡规划', 48),
    ('建筑与艺术设计学院', '工业设计', 49),
    ('建筑与艺术设计学院', '视觉传达设计', 50),
    ('建筑与艺术设计学院', '产品设计', 51),
    ('外国语学院', '英语', 52),
    ('外国语学院', '俄语', 53),
    ('理学院', '工程力学', 54),
    ('理学院', '应用物理学', 55),
    ('理学院', '信息与计算科学', 56),
    ('生命科学与工程学院', '生物工程', 57),
    ('生命科学与工程学院', '制药工程', 58),
    ('生命科学与工程学院', '食品科学与工程', 59),
    ('生命科学与工程学院', '生物制药', 60),
    ('法学院', '法学', 61),
    ('法学院', '知识产权', 62),
    ('文学院', '汉语言文学', 63)
) AS source_data(college_name, major_name, sort_order)
WHERE NOT EXISTS (
    SELECT 1 FROM college_major
    WHERE college_name = source_data.college_name
      AND major_name = source_data.major_name
      AND is_deleted = 0
);
