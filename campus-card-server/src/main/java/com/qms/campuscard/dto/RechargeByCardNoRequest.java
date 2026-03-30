package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class RechargeByCardNoRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cardNo;
    private BigDecimal amount;
    private String rechargeType;
}
