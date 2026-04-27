package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("operation_log")
public class OperationLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long operatorId;

    private String operationType;

    private String targetTable;

    private Long targetId;

    private String content;

    private LocalDateTime createTime;

    private Integer isDeleted;

    // 非数据库字段，用于存储操作人名称
    @TableField(exist = false)
    private String operatorName;

    // 非数据库字段，用于存储目标名称
    @TableField(exist = false)
    private String targetName;
}
