package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("commute_route")
public class CommuteRoute {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String routeName;

    private String startStation;

    private String endStation;

    private Double totalDistance;

    private Integer totalTime;

    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;
}
