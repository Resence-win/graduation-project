package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Teacher;

public interface TeacherService {

    boolean addTeacher(Teacher teacher);

    IPage<Teacher> getTeacherList(Page<Teacher> page, String teacherNo, String name);

    Teacher getTeacherById(Long id);

    boolean updateTeacher(Teacher teacher);

    boolean deleteTeacher(Long id);
}