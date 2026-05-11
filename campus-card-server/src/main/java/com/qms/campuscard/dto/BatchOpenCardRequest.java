package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class BatchOpenCardRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<OpenCardRequest> users;
    private String remark;
}
