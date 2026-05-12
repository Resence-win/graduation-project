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
import org.springframework.transaction.annotation.Transactional;

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
        normalizeTeacher(teacher);
        validateTeacherBasicInfo(teacher);
        ensureTeacherNoUnique(teacher.getTeacherNo(), null);
        teacher.setStatus(1);
        teacher.setIsDeleted(0);
        teacher.setCreateTime(LocalDateTime.now());
        return teacherMapper.insert(teacher) > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addTeacher(TeacherRequest teacherRequest) {
        if (teacherRequest == null) {
            throw new RuntimeException("教师信息不能为空");
        }
        if (teacherRequest.getPassword() == null || teacherRequest.getPassword().trim().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }

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

        normalizeTeacher(teacher);
        validateTeacherBasicInfo(teacher);
        ensureTeacherNoUnique(teacher.getTeacherNo(), null);
        
        if (teacherMapper.insert(teacher) <= 0) {
            return false;
        }

        return saveTeacherLoginAccount(teacher.getTeacherNo(), teacherRequest.getPassword().trim());
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
        if (teacher == null || teacher.getId() == null) {
            throw new RuntimeException("教师ID不能为空");
        }
        normalizeTeacher(teacher);
        validateTeacherBasicInfo(teacher);
        ensureTeacherNoUnique(teacher.getTeacherNo(), teacher.getId());
        teacher.setUpdateTime(LocalDateTime.now());
        return teacherMapper.updateById(teacher) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacher(Long id) {
        Teacher existingTeacher = getTeacherById(id);
        if (existingTeacher == null) {
            return false;
        }

        Teacher teacher = new Teacher();
        teacher.setId(id);
        teacher.setIsDeleted(1);
        teacher.setUpdateTime(LocalDateTime.now());
        if (teacherMapper.updateById(teacher) <= 0) {
            return false;
        }

        AdminUser adminUser = getActiveAdminUser(existingTeacher.getTeacherNo());
        if (adminUser != null && "teacher".equals(adminUser.getRole())) {
            adminUser.setIsDeleted(1);
            return adminUserMapper.updateById(adminUser) > 0;
        }
        return true;
    }

    private boolean saveTeacherLoginAccount(String teacherNo, String password) {
        AdminUser existingAdminUser = getActiveAdminUser(teacherNo);
        if (existingAdminUser != null) {
            if (!"teacher".equals(existingAdminUser.getRole())) {
                throw new RuntimeException("登录账号已存在且不是教师角色");
            }
            existingAdminUser.setPassword(getMD5(password));
            existingAdminUser.setStatus(1);
            existingAdminUser.setIsDeleted(0);
            return adminUserMapper.updateById(existingAdminUser) > 0;
        }

        // 创建并保存对应的管理员用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(teacherNo);
        adminUser.setPassword(getMD5(password));
        adminUser.setRole("teacher");
        adminUser.setStatus(1);
        adminUser.setIsDeleted(0);
        adminUser.setCreateTime(LocalDateTime.now());

        return adminUserMapper.insert(adminUser) > 0;
    }

    private void normalizeTeacher(Teacher teacher) {
        if (teacher == null) {
            return;
        }
        teacher.setTeacherNo(trimToNull(teacher.getTeacherNo()));
        teacher.setName(trimToNull(teacher.getName()));
        teacher.setGender(trimToNull(teacher.getGender()));
        teacher.setDepartment(trimToNull(teacher.getDepartment()));
        teacher.setPhone(trimToNull(teacher.getPhone()));
    }

    private void validateTeacherBasicInfo(Teacher teacher) {
        if (teacher == null) {
            throw new RuntimeException("教师信息不能为空");
        }
        if (isBlank(teacher.getTeacherNo())) {
            throw new RuntimeException("教师编号不能为空");
        }
        if (isBlank(teacher.getName())) {
            throw new RuntimeException("教师姓名不能为空");
        }
    }

    private void ensureTeacherNoUnique(String teacherNo, Long currentId) {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teacher_no", teacherNo);
        queryWrapper.eq("is_deleted", 0);
        if (currentId != null) {
            queryWrapper.ne("id", currentId);
        }
        if (teacherMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("教师编号已存在");
        }
    }

    private AdminUser getActiveAdminUser(String username) {
        QueryWrapper<AdminUser> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", username);
        queryWrapper.eq("is_deleted", 0);
        return adminUserMapper.selectOne(queryWrapper);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
