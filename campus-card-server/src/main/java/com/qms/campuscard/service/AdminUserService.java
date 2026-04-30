package com.qms.campuscard.service;

import com.qms.campuscard.entity.AdminUser;

public interface AdminUserService {

    AdminUser login(String username, String password);
    
    boolean changePassword(String username, String oldPassword, String newPassword);
    
    boolean resetPassword(String username, String newPassword);
}