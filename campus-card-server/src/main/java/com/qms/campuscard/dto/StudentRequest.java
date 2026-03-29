package com.qms.campuscard.dto;

import lombok.Data;

@Data
public class StudentRequest {
    private String studentNo;
    private String name;
    private String gender;
    private String college;
    private String major;
    private String className;
    private String phone;
    private String password; // 新增密码字段
}
