package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.TeacherRequest;
import com.qms.campuscard.entity.Teacher;

public interface TeacherService {

    boolean addTeacher(Teacher teacher);
    
    boolean addTeacher(TeacherRequest teacherRequest);

    IPage<Teacher> getTeacherList(Page<Teacher> page, String teacherNo, String name);

    Teacher getTeacherById(Long id);
    
    Teacher getTeacherByTeacherNo(String teacherNo);

    boolean updateTeacher(Teacher teacher);

    boolean deleteTeacher(Long id);
}