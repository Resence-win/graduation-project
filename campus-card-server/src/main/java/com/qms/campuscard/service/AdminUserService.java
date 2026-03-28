package com.qms.campuscard.service;

import com.qms.campuscard.entity.AdminUser;

public interface AdminUserService {

    AdminUser login(String username, String password);
}