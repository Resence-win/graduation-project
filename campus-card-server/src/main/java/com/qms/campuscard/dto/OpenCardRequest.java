package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;

@Data
public class OpenCardRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String user_no; // 学号或教师编号
    private String user_type;
    
    // 添加getter方法，与前端保持一致
    public String getUserNo() {
        return user_no;
    }
    
    public String getUserType() {
        return user_type;
    }
}
