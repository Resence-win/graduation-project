package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class BatchOpenCardResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer totalCount = 0;
    private Integer successCount = 0;
    private Integer failureCount = 0;
    private List<CampusCardDTO> cards = new ArrayList<>();
}
