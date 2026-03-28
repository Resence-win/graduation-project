package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("card_change_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CardChangeRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("card_id")
    private Long cardId;

    @TableField("operation_type")
    private String operationType;

    @TableField("operator_id")
    private Long operatorId;

    @TableField("remark")
    private String remark;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
