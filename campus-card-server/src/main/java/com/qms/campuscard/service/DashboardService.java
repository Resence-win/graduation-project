package com.qms.campuscard.service;

import java.math.BigDecimal;

public interface DashboardService {

    long getStudentCount();

    long getTeacherCount();

    long getMerchantCount();

    BigDecimal getTodayConsumeAmount();
}
