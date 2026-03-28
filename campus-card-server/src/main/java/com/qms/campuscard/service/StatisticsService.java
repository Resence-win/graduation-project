package com.qms.campuscard.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    List<Map<String, Object>> getConsumeStatistics(String startDate, String endDate);

    List<Map<String, Object>> getUserRank(String startDate, String endDate, Integer limit);

    List<Map<String, Object>> getMerchantRank(String startDate, String endDate, Integer limit);
}
