package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RechargeRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cardId;
    private BigDecimal amount;
    private String rechargeType;
    private Long operatorId;
    private String operatorName;
}
