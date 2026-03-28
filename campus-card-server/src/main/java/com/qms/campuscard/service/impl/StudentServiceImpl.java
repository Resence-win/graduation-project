package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.service.StudentService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class StudentServiceImpl implements StudentService {

    @Resource
    private StudentMapper studentMapper;

    @Override
    public boolean addStudent(Student student) {
        student.setStatus(1);
        student.setIsDeleted(0);
        student.setCreateTime(LocalDateTime.now());
        return studentMapper.insert(student) > 0;
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
}
