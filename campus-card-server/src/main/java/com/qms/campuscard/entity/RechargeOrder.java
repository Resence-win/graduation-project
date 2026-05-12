package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("recharge_order")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RechargeOrder implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("out_trade_no")
    private String outTradeNo;

    @TableField("card_id")
    private Long cardId;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("status")
    private String status;

    @TableField("alipay_trade_no")
    private String alipayTradeNo;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("paid_time")
    private LocalDateTime paidTime;

    @TableField("settled_time")
    private LocalDateTime settledTime;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
