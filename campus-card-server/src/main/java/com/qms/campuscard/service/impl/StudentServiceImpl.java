package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.StudentRequest;
import com.qms.campuscard.entity.AdminUser;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.mapper.AdminUserMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.service.StudentService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.security.MessageDigest;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private AdminUserMapper adminUserMapper;

    @Override
    public boolean addStudent(Student student) {
        student.setStatus(1);
        student.setIsDeleted(0);
        student.setCreateTime(LocalDateTime.now());
        return studentMapper.insert(student) > 0;
    }
    
    @Override
    public boolean addStudent(StudentRequest studentRequest) {
        // 创建并保存学生信息
        Student student = new Student();
        student.setStudentNo(studentRequest.getStudentNo());
        student.setName(studentRequest.getName());
        student.setGender(studentRequest.getGender());
        student.setCollege(studentRequest.getCollege());
        student.setMajor(studentRequest.getMajor());
        student.setClassName(studentRequest.getClassName());
        student.setPhone(studentRequest.getPhone());
        student.setStatus(1);
        student.setIsDeleted(0);
        student.setCreateTime(LocalDateTime.now());
        
        if (studentMapper.insert(student) <= 0) {
            return false;
        }
        
        // 创建并保存对应的管理员用户
        AdminUser adminUser = new AdminUser();
        adminUser.setUsername(studentRequest.getStudentNo()); // 使用学号作为用户名
        adminUser.setPassword(getMD5(studentRequest.getPassword())); // 保存密码的MD5值
        adminUser.setRole("student"); // 设置角色为学生
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
    public IPage<Student> getStudentList(Page<Student> page, String studentNo, String name) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        if (studentNo != null && !studentNo.isEmpty()) {
            queryWrapper.like("student_no", studentNo);
        }
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        
        queryWrapper.orderByDesc("create_time");
        return studentMapper.selectPage(page, queryWrapper);
    }

    @Override
    public Student getStudentById(Long id) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", id);
        queryWrapper.eq("is_deleted", 0);
        return studentMapper.selectOne(queryWrapper);
    }
    
    @Override
    public Student getStudentByStudentNo(String studentNo) {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_no", studentNo);
        queryWrapper.eq("is_deleted", 0);
        return studentMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateStudent(Student student) {
        student.setUpdateTime(LocalDateTime.now());
        return studentMapper.updateById(student) > 0;
    }

    @Override
    public boolean deleteStudent(Long id) {
        Student student = new Student();
        student.setId(id);
        student.setIsDeleted(1);
        student.setUpdateTime(LocalDateTime.now());
        return studentMapper.updateById(student) > 0;
    }

    @Override
    public boolean batchImportStudents(List<Student> students) {
        if (students == null || students.isEmpty()) {
            return false;
        }

        for (Student student : students) {
            // 检查学号是否已存在
            Student existingStudent = getStudentByStudentNo(student.getStudentNo());
            if (existingStudent != null) {
                // 学号已存在，跳过或更新
                continue;
            }

            // 设置默认值
            student.setStatus(1);
            student.setIsDeleted(0);
            student.setCreateTime(LocalDateTime.now());

            // 插入学生信息
            if (studentMapper.insert(student) <= 0) {
                return false;
            }

            // 创建对应的管理员用户（默认密码为123456）
            AdminUser adminUser = new AdminUser();
            adminUser.setUsername(student.getStudentNo());
            adminUser.setPassword(getMD5("123456"));
            adminUser.setRole("student");
            adminUser.setStatus(1);
            adminUser.setIsDeleted(0);
            adminUser.setCreateTime(LocalDateTime.now());

            if (adminUserMapper.insert(adminUser) <= 0) {
                return false;
            }
        }

        return true;
    }

    @Override
    public List<Student> getAllStudents() {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return studentMapper.selectList(queryWrapper);
    }
}
