package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.mapper.AdminUserMapper;
import com.qms.campuscard.service.AdminUserService;
import java.security.MessageDigest;
import java.math.BigInteger;

import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    @Resource
    private AdminUserMapper adminUserMapper;

    // 修改AdminUserServiceImpl.java
@Override
public AdminUser login(String username, String password) {
    // 生成密码的MD5值
    String md5Password = getMD5(password);
    
    QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
    queryWrapper.eq("username", username);
    queryWrapper.eq("password", md5Password);
    queryWrapper.eq("is_deleted", 0);
    return adminUserMapper.selectOne(queryWrapper);
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
        throw new RuntimeException(e);
    }
}
}