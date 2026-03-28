package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("commute_record")
public class CommuteRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long cardId;

    private Long routeId;

    private String seatNumber;

    private LocalDateTime rideTime;

    private Integer status;

    private Integer isDeleted;
}
