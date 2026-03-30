package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ConsumeRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long accountId;
    private String cardNo;
    private Long merchantId;
    private String merchantName;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Integer status;
    private LocalDateTime consumeTime;
}
