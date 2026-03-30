package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ConsumeByCardNoRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String cardNo;
    private Long merchantId;
    private BigDecimal amount;
}
