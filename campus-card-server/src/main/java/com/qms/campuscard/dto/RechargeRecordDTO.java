package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RechargeRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long accountId;
    private String cardNo;
    private BigDecimal amount;
    private String rechargeType;
    private Long operatorId;
    private LocalDateTime createTime;
}
