package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.TeacherService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class TeacherServiceImpl implements TeacherService {

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public boolean addTeacher(Teacher teacher) {
        teacher.setStatus(1);
        teacher.setIsDeleted(0);
        teacher.setCreateTime(LocalDateTime.now());
        return teacherMapper.insert(teacher) > 0;
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