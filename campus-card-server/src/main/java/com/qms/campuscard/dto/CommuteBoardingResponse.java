package com.qms.campuscard.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class CommuteBoardingResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long recordId;
    private Long routeId;
    private Long vehicleId;
    private Long scheduleId;
    private String seatNumber;
    private LocalDateTime rideTime;
}
