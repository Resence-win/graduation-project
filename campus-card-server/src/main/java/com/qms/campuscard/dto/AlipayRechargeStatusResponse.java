package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AlipayRechargeStatusResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private String outTradeNo;
    private String alipayTradeNo;
    private String status;
    private String tradeStatus;
    private BigDecimal amount;
    private Boolean paid;
    private Boolean settled;
    private String message;
}
