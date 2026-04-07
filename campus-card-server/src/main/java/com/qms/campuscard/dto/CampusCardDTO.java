package com.qms.campuscard.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CampusCardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String cardNo;
    private Long userId;
    private String userType;
    private String userNo; // 学号或教师编号
    private String userName; // 学生或教师姓名
    private Integer status;
    private BigDecimal balance; // 账户余额
    private LocalDate issueDate;
    private LocalDate expireDate;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
