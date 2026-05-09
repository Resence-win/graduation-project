package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("student")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("student_no")
    private String studentNo;

    @TableField("name")
    private String name;

    @TableField("gender")
    private String gender;

    @TableField("college")
    private String college;

    @TableField("major")
    private String major;

    @TableField("class_name")
    private String className;

    @TableField("phone")
    private String phone;

    @TableField("teacher_id")
    private Long teacherId;

    @TableField(exist = false)
    private String teacherName;

    @TableField("attendance_mode")
    private String attendanceMode;

    @TableField("attendance_status")
    private String attendanceStatus;

    @TableField("internship_company")
    private String internshipCompany;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
