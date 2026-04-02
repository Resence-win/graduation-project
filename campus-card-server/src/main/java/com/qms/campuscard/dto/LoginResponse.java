package com.qms.campuscard.dto;

import com.qms.campuscard.entity.AdminUser;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private AdminUser user;
    private Long cardId;
    private String cardNo;
}
