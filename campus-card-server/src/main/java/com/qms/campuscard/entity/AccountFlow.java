package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("account_flow")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountFlow implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("account_id")
    private Long accountId;

    @TableField("change_type")
    private String changeType;

    @TableField("amount")
    private BigDecimal amount;

    @TableField("balance_after")
    private BigDecimal balanceAfter;

    @TableField("related_id")
    private Long relatedId;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
