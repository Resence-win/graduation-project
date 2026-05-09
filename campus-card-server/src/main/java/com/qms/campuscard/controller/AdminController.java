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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@Slf4j
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

    /**
     * 用户登录接口：校验账号密码，并根据角色返回用户信息、业务用户ID和校园卡信息。
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("登录请求: {}", loginRequest.getUsername());
        AdminUser adminUser = adminUserService.login(loginRequest.getUsername(), loginRequest.getPassword());
        if (adminUser == null) {
            log.warn("登录失败，用户名或密码错误: {}", loginRequest.getUsername());
            return Result.error("用户名或密码错误");
        }
        
        // 获取数据库中的角色信息
        String actualRole = adminUser.getRole();
        log.info("用户实际角色: {}", actualRole);
        
        // 构建登录响应
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setUser(adminUser);
        
        // 直接使用数据库中的角色进行判断
        String normalizedRole = actualRole;
        if ("SUPER_ADMIN".equals(actualRole)) {
            normalizedRole = "admin";
        }
        
        // 如果是学生或教师，查询对应的校园卡信息
        if ("student".equals(normalizedRole) || "teacher".equals(normalizedRole)) {
            Long userId = null;
            String userType = normalizedRole;
            
            if ("student".equals(normalizedRole)) {
                // 根据学号查询学生信息
                QueryWrapper<Student> studentWrapper = new QueryWrapper<>();
                studentWrapper.eq("student_no", adminUser.getUsername());
                studentWrapper.eq("is_deleted", 0);
                Student student = studentMapper.selectOne(studentWrapper);
                if (student != null) {
                    userId = student.getId();
                    loginResponse.setBusinessUserId(student.getId());
                }
            } else if ("teacher".equals(normalizedRole)) {
                // 根据教师号查询教师信息
                QueryWrapper<Teacher> teacherWrapper = new QueryWrapper<>();
                teacherWrapper.eq("teacher_no", adminUser.getUsername());
                teacherWrapper.eq("is_deleted", 0);
                Teacher teacher = teacherMapper.selectOne(teacherWrapper);
                if (teacher != null) {
                    userId = teacher.getId();
                    loginResponse.setBusinessUserId(teacher.getId());
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
        
        log.info("登录成功: {}, 角色: {}", loginRequest.getUsername(), normalizedRole);
        return Result.success("登录成功", loginResponse);
    }
    
    /**
     * 修改密码接口：登录用户通过旧密码校验后更新自己的登录密码。
     */
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody Map<String, String> params) {
        log.info("修改密码请求，参数: {}", params.keySet());
        String username = params.get("username");
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        
        log.info("修改密码 - 用户名: {}", username);
        
        boolean result = adminUserService.changePassword(username, oldPassword, newPassword);
        if (result) {
            log.info("密码修改成功: {}", username);
            return Result.success("密码修改成功");
        } else {
            log.warn("密码修改失败: {}", username);
            return Result.error("密码修改失败，旧密码错误");
        }
    }
    
    /**
     * 重置密码接口：管理员按用户名重置指定用户的登录密码。
     */
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> params) {
        log.info("重置密码请求，参数: {}", params.keySet());
        String username = params.get("username");
        String newPassword = params.get("newPassword");
        
        log.info("重置密码 - 用户名: {}", username);
        
        boolean result = adminUserService.resetPassword(username, newPassword);
        if (result) {
            log.info("密码重置成功: {}", username);
            return Result.success("密码重置成功");
        } else {
            log.warn("密码重置失败: {}", username);
            return Result.error("密码重置失败，用户不存在");
        }
    }
}
