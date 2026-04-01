package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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

    private Long locationId;

    private String status;

    private String actualLocation;

    private Double actualLatitude;

    private Double actualLongitude;

    private String deviceInfo;

    private LocalDateTime recordTime;

    private Integer isDeleted;

    // 非数据库字段，用于存储打卡位置名称
    @TableField(exist = false)
    private String locationName;
}
