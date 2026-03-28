package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("campus_card")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampusCard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("card_no")
    private String cardNo;

    @TableField("user_id")
    private Long userId;

    @TableField("user_type")
    private String userType;

    @TableField("status")
    private Integer status;

    @TableField("issue_date")
    private LocalDate issueDate;

    @TableField("expire_date")
    private LocalDate expireDate;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_time")
    private LocalDateTime updateTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
