package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("attendance_location")
public class AttendanceLocation {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long teacherId;

    private String locationName;

    private String location;

    private Double latitude;

    private Double longitude;

    private Integer radius;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Shanghai")
    private LocalDateTime endTime;

    private Integer status;

    @TableField(exist = false)
    private String teacherName;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
