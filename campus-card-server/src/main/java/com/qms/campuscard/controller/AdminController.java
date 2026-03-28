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
        if (adminUser != null) {
            return Result.success("登录成功", adminUser);
        } else {
            return Result.error("用户名或密码错误");
        }
    }
}