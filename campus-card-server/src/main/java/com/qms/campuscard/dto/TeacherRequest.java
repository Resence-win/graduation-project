package com.qms.campuscard.dto;

import lombok.Data;

@Data
public class TeacherRequest {
    private String teacherNo;
    private String name;
    private String gender;
    private String department;
    private String phone;
    private String password; // 新增密码字段
}
