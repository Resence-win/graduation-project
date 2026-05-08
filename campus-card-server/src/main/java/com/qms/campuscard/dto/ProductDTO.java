package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String productName;
    private Long merchantId;
    private String merchantName;
    private BigDecimal price;
    private Integer stock;
    private String description;
    private String image;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
