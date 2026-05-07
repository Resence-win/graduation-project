package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("attendance_application")
public class AttendanceApplication {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long studentId;

    private Long cardId;

    private String applicationType;

    private String internshipCompany;

    private String reason;

    private LocalDate startDate;

    private LocalDate endDate;

    private String status;

    private Long reviewerId;

    private LocalDateTime reviewTime;

    private String reviewRemark;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    @TableField(exist = false)
    private String studentName;

    @TableField(exist = false)
    private String studentNo;

    @TableField(exist = false)
    private String cardNo;
}
