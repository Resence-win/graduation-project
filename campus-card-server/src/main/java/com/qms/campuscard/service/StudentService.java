package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Student;

public interface StudentService {

    boolean addStudent(Student student);

    IPage<Student> getStudentList(Page<Student> page, String studentNo, String name);

    Student getStudentById(Long id);

    boolean updateStudent(Student student);

    boolean deleteStudent(Long id);
}
