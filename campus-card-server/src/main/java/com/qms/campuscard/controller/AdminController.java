package com.qms.campuscard.controller;

import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.LoginRequest;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.service.AdminUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminUserService adminUserService;

    public AdminController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @PostMapping("/login")
    public Result<AdminUser> login(@RequestBody LoginRequest loginRequest) {
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
        
        return Result.success("登录成功", adminUser);
    }
}
