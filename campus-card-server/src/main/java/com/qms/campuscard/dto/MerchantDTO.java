package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class MerchantDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String merchantName;
    private Long typeId;
    private String typeName;
    private String location;
    private String logo;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
