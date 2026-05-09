package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AccountDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long cardId;
    private String cardNo;
    private String studentName;
    private BigDecimal balance;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
