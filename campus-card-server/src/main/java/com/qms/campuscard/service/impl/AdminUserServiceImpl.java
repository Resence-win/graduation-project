package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.mapper.AdminUserMapper;
import com.qms.campuscard.service.AdminUserService;
import lombok.extern.slf4j.Slf4j;
import java.security.MessageDigest;
import java.math.BigInteger;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
@Slf4j
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserMapper adminUserMapper;

    // 修改AdminUserServiceImpl.java
@Override
public AdminUser login(String username, String password) {
    log.info("尝试登录，用户名: {}", username);
    // 生成密码的MD5值
    String md5Password = getMD5(password);
    
    QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("username", username);
    queryWrapper.eq("password", md5Password);
    queryWrapper.eq("is_deleted", 0);
    AdminUser result = adminUserMapper.selectOne(queryWrapper);
    log.info("登录查询结果: {}", result != null ? "成功" : "失败");
    return result;
}

// 添加MD5加密方法
private String getMD5(String input) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(input.getBytes());
        BigInteger no = new BigInteger(1, messageDigest);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    } catch (Exception e) {
        log.error("MD5加密失败", e);
        throw new RuntimeException(e);
    }
}

@Override
public boolean changePassword(String username, String oldPassword, String newPassword) {
    log.info("修改密码请求 - 用户名: {}", username);
    try {
        // 生成旧密码的MD5值
        String md5OldPassword = getMD5(oldPassword);
        log.debug("旧密码MD5: {}", md5OldPassword);
        
        // 查找用户
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("is_deleted", 0);
        AdminUser adminUser = adminUserMapper.selectOne(queryWrapper);
        
        if (adminUser == null) {
            log.warn("未找到用户: {}", username);
            return false;
        }
        
        log.info("找到用户，数据库密码: {}", adminUser.getPassword());
        
        // 验证旧密码是否匹配
        if (!adminUser.getPassword().equals(md5OldPassword)) {
            log.warn("旧密码不匹配");
            return false;
        }
        
        // 生成新密码的MD5值并更新
        String md5NewPassword = getMD5(newPassword);
        log.debug("新密码MD5: {}", md5NewPassword);
        adminUser.setPassword(md5NewPassword);
        int result = adminUserMapper.updateById(adminUser);
        log.info("密码修改结果: {}", result > 0 ? "成功" : "失败");
        return result > 0;
    } catch (Exception e) {
        log.error("修改密码异常", e);
        throw e;
    }
}

@Override
public boolean resetPassword(String username, String newPassword) {
    log.info("重置密码请求 - 用户名: {}", username);
    try {
        // 查找用户
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("is_deleted", 0);
        AdminUser adminUser = adminUserMapper.selectOne(queryWrapper);
        
        if (adminUser == null) {
            log.warn("未找到用户: {}", username);
            return false;
        }
        
        log.info("找到用户，准备重置密码");
        
        // 生成新密码的MD5值并更新
        String md5NewPassword = getMD5(newPassword);
        log.debug("新密码MD5: {}", md5NewPassword);
        adminUser.setPassword(md5NewPassword);
        int result = adminUserMapper.updateById(adminUser);
        log.info("密码重置结果: {}", result > 0 ? "成功" : "失败");
        return result > 0;
    } catch (Exception e) {
        log.error("重置密码异常", e);
        throw e;
    }
}
}