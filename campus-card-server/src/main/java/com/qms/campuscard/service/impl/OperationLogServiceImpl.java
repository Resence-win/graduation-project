package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.AdminUserMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.OperationLogMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.OperationLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;
    
    @Resource
    private AdminUserMapper adminUserMapper;
    
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private TeacherMapper teacherMapper;
    
    @Resource
    private CampusCardMapper campusCardMapper;

    @Override
    public IPage<OperationLog> getOperationLogs(Long operatorId, String operationType, String targetTable, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<OperationLog> pageParam = new Page<>(page, size);
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();

        if (operatorId != null) {
            queryWrapper.eq("operator_id", operatorId);
        }
        if (operationType != null) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (targetTable != null) {
            queryWrapper.eq("target_table", targetTable);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        IPage<OperationLog> result = operationLogMapper.selectPage(pageParam, queryWrapper);
        
        // 填充操作人名称和目标名称
        for (OperationLog log : result.getRecords()) {
            // 填充操作人名称
            if (log.getOperatorId() != null) {
                // 尝试从管理员表查询
                QueryWrapper<AdminUser> adminQuery = new QueryWrapper<>();
                adminQuery.eq("id", log.getOperatorId());
                adminQuery.eq("is_deleted", 0);
                AdminUser adminUser = adminUserMapper.selectOne(adminQuery);
                if (adminUser != null) {
                    log.setOperatorName(adminUser.getUsername());
                } else {
                    // 尝试从学生表查询
                    QueryWrapper<Student> studentQuery = new QueryWrapper<>();
                    studentQuery.eq("id", log.getOperatorId());
                    studentQuery.eq("is_deleted", 0);
                    Student student = studentMapper.selectOne(studentQuery);
                    if (student != null) {
                        log.setOperatorName(student.getName());
                    } else {
                        // 尝试从教师表查询
                        QueryWrapper<Teacher> teacherQuery = new QueryWrapper<>();
                        teacherQuery.eq("id", log.getOperatorId());
                        teacherQuery.eq("is_deleted", 0);
                        Teacher teacher = teacherMapper.selectOne(teacherQuery);
                        if (teacher != null) {
                            log.setOperatorName(teacher.getName());
                        }
                    }
                }
            }
            
            // 填充目标名称
            if (log.getTargetTable() != null && log.getTargetId() != null) {
                switch (log.getTargetTable()) {
                    case "campus_card":
                        // 查询校园卡卡号
                        QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
                        cardQuery.eq("id", log.getTargetId());
                        cardQuery.eq("is_deleted", 0);
                        CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
                        if (campusCard != null) {
                            log.setTargetName(campusCard.getCardNo());
                        }
                        break;
                    case "student":
                        // 查询学生姓名
                        QueryWrapper<Student> studentQuery = new QueryWrapper<>();
                        studentQuery.eq("id", log.getTargetId());
                        studentQuery.eq("is_deleted", 0);
                        Student student = studentMapper.selectOne(studentQuery);
                        if (student != null) {
                            log.setTargetName(student.getName());
                        }
                        break;
                    case "teacher":
                        // 查询教师姓名
                        QueryWrapper<Teacher> teacherQuery = new QueryWrapper<>();
                        teacherQuery.eq("id", log.getTargetId());
                        teacherQuery.eq("is_deleted", 0);
                        Teacher teacher = teacherMapper.selectOne(teacherQuery);
                        if (teacher != null) {
                            log.setTargetName(teacher.getName());
                        }
                        break;
                    case "admin_user":
                        // 查询管理员姓名
                        QueryWrapper<AdminUser> adminQuery = new QueryWrapper<>();
                        adminQuery.eq("id", log.getTargetId());
                        adminQuery.eq("is_deleted", 0);
                        AdminUser adminUser = adminUserMapper.selectOne(adminQuery);
                        if (adminUser != null) {
                            log.setTargetName(adminUser.getUsername());
                        }
                        break;
                    default:
                        // 其他表暂时不处理
                        break;
                }
            }
        }
        
        return result;
    }

    @Override
    public void save(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }
}
