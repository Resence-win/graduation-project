package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.StudentRequest;
import com.qms.campuscard.entity.Student;

public interface StudentService {

    boolean addStudent(Student student);
    
    boolean addStudent(StudentRequest studentRequest);

    IPage<Student> getStudentList(Page<Student> page, String studentNo, String name);

    Student getStudentById(Long id);
    
    Student getStudentByStudentNo(String studentNo);

    boolean updateStudent(Student student);

    boolean deleteStudent(Long id);
}
