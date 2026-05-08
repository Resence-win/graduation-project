package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class BorrowRestrictionStatus implements Serializable {
    private static final long serialVersionUID = 1L;

    private Boolean restricted;
    private Integer overdueDays;
    private Integer remainingRestrictedDays;
    private LocalDateTime restrictionEndTime;
    private String message;
}
