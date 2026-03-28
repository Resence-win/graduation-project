package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OpenCardRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String userType;
}
