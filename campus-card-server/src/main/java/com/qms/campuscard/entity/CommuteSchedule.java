package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("commute_schedule")
public class CommuteSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long routeId;

    private Long vehicleId;

    private LocalTime departureTime;

    private String frequency;

    private LocalDate startDate;

    private LocalDate endDate;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
