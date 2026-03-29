package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.TeacherRequest;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.AdminUserMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.TeacherService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.time.LocalDateTime;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;
    
    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public boolean addTeacher(Teacher teacher) {
        teacher.setStatus(1);
        teacher.setIsDeleted(0);
        teacher.setCreateTime(LocalDateTime.now());
        return teacherMapper.insert(teacher) > 0;
    }
    
    @Override
    public boolean addTeacher(TeacherRequest teacherRequest) {
        // 创建并保存教师信息
        Teacher teacher = new Teacher();
        teacher.setTeacherNo(teacherRequest.getTeacherNo());
        teacher.setName(teacherRequest.getName());
        teacher.setGender(teacherRequest.getGender());
        teacher.setDepartment(teacherRequest.getDepartment());
        teacher.setPhone(teacherRequest.getPhone());
        teacher.setStatus(1);
        teacher.setIsDeleted(0);
        teacher.setCreateTime(LocalDateTime.now());
        
        if (teacherMapper.insert(teacher) <= 0) {
            return false;
        }
        
        // 创建并保存对应的管理员用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(teacherRequest.getTeacherNo()); // 使用教师编号作为用户名
        adminUser.setPassword(getMD5(teacherRequest.getPassword())); // 保存密码的MD5值
        adminUser.setRole("teacher"); // 设置角色为教师
        adminUser.setStatus(1);
        adminUser.setIsDeleted(0);
        adminUser.setCreateTime(LocalDateTime.now());
        
        return adminUserMapper.insert(adminUser) > 0;
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

    @Override
    public IPage<Teacher> getTeacherList(Page<Teacher> page, String teacherNo, String name) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        if (teacherNo != null && !teacherNo.isEmpty()) {
            queryWrapper.like("teacher_no", teacherNo);
        }
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByDesc("create_time");
        return teacherMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Teacher getTeacherById(Long id) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("is_deleted", 0);
        return teacherMapper.selectOne(queryWrapper);
    }
    
    @Override
    public Teacher getTeacherByTeacherNo(String teacherNo) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_no", teacherNo);
        queryWrapper.eq("is_deleted", 0);
        return teacherMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateTeacher(Teacher teacher) {
        teacher.setUpdateTime(LocalDateTime.now());
        return teacherMapper.updateById(teacher) > 0;
    }

    @Override
    public boolean deleteTeacher(Long id) {
        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setIsDeleted(1);
        teacher.setUpdateTime(LocalDateTime.now());
        return teacherMapper.updateById(teacher) > 0;
    }
}