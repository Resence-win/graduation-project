package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class CommuteBoardingRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long cardId;
    private Long routeId;
    private Long vehicleId;
    private Long scheduleId;
}
