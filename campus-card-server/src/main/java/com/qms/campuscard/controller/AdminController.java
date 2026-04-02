package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.LoginRequest;
import com.qms.campuscard.dto.LoginResponse;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserService adminUserService;
    private final CampusCardMapper campusCardMapper;
    private final StudentMapper studentMapper;
    private final TeacherMapper teacherMapper;

    public AdminController(AdminUserService adminUserService, CampusCardMapper campusCardMapper, StudentMapper studentMapper, TeacherMapper teacherMapper) {
        this.adminUserService = adminUserService;
        this.campusCardMapper = campusCardMapper;
        this.studentMapper = studentMapper;
        this.teacherMapper = teacherMapper;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        AdminUser adminUser = adminUserService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (adminUser == null) {
            return Result.error("用户名或密码错误");
        }
        
        // 验证用户选择的角色是否与数据库中的角色一致
        String selectedRole = loginRequest.getRole();
        String actualRole = adminUser.getRole();
        
        if (selectedRole == null || selectedRole.isEmpty()) {
            return Result.error("请选择登录角色");
        }
        
        // 角色映射：前端传递的角色值与数据库存储的角色值可能不同
        // 前端：admin, student, teacher
        // 数据库：SUPER_ADMIN, student, teacher
        boolean roleMatch = false;
        if ("admin".equals(selectedRole)) {
            // 管理员角色可以是 SUPER_ADMIN 或 admin
            roleMatch = "SUPER_ADMIN".equals(actualRole) || "admin".equals(actualRole);
        } else if ("student".equals(selectedRole)) {
            roleMatch = "student".equals(actualRole);
        } else if ("teacher".equals(selectedRole)) {
            roleMatch = "teacher".equals(actualRole);
        }
        
        if (!roleMatch) {
            return Result.error("角色不匹配，请使用正确的角色登录");
        }
        
        // 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(adminUser);
        
        // 如果是学生或教师，查询对应的校园卡信息
        if ("student".equals(selectedRole) || "teacher".equals(selectedRole)) {
            Long userId = null;
            String userType = selectedRole;
            
            if ("student".equals(selectedRole)) {
                // 根据学号查询学生信息
                QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
                studentWrapper.eq("student_no", adminUser.getUsername());
                studentWrapper.eq("is_deleted", 0);
                Student student = studentMapper.selectOne(studentWrapper);
                if (student != null) {
                    userId = student.getId();
                }
            } else if ("teacher".equals(selectedRole)) {
                // 根据教师号查询教师信息
                QueryWrapper<Teacher> teacherWrapper = new QueryWrapper<>();
                teacherWrapper.eq("teacher_no", adminUser.getUsername());
                teacherWrapper.eq("is_deleted", 0);
                Teacher teacher = teacherMapper.selectOne(teacherWrapper);
                if (teacher != null) {
                    userId = teacher.getId();
                }
            }
            
            // 根据用户ID和用户类型查询校园卡信息
            if (userId != null) {
                QueryWrapper<CampusCard> cardWrapper = new QueryWrapper<>();
                cardWrapper.eq("user_id", userId);
                cardWrapper.eq("user_type", userType);
                cardWrapper.eq("is_deleted", 0);
                CampusCard campusCard = campusCardMapper.selectOne(cardWrapper);
                if (campusCard != null) {
                    loginResponse.setCardId(campusCard.getId());
                    loginResponse.setCardNo(campusCard.getCardNo());
                }
            }
        }
        
        return Result.success("登录成功", loginResponse);
    }
}
