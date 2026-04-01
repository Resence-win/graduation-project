package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.*;
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

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer isDeleted;
}
