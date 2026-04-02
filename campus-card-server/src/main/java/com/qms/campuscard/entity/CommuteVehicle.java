package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("commute_vehicle")
public class CommuteVehicle {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String plateNumber;

    private String vehicleType;

    private Integer seatCount;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
