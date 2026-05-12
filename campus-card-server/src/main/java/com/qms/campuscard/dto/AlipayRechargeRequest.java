package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class AlipayRechargeRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long cardId;
    private String cardNo;
    private BigDecimal amount;
    private Long operatorId;
    private String operatorName;
}
