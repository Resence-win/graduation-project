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

@TableName("access_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("card_id")
    private Long cardId;

    @TableField("direction")
    private String direction;

    @TableField("location")
    private String location;

    @TableField("access_time")
    private LocalDateTime accessTime;

    @TableField("is_deleted")
    private Integer isDeleted;
}
