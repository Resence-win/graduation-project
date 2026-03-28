package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("attendance_record")
public class AttendanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cardId;

    private String status;

    private LocalDateTime recordTime;

    private Integer isDeleted;
}
