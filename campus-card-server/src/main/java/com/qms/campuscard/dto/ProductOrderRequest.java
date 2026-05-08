package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProductOrderRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cardNo;
    private Long productId;
    private Integer quantity;
}
