package com.qms.campuscard.service;

import java.util.List;
import java.util.Map;

public interface StatisticsService {

    Map<String, Object> getOverview(String startDate, String endDate);

    List<Map<String, Object>> getConsumeStatistics(String startDate, String endDate);

    List<Map<String, Object>> getUserRank(String startDate, String endDate, Integer limit);

    List<Map<String, Object>> getMerchantRank(String startDate, String endDate, Integer limit);

    Map<String, Object> getLibraryOverdueStatistics(String startDate, String endDate, Integer limit);

    Map<String, Object> getLibraryBookDetails(String type, Integer page, Integer size);

    Map<String, Object> getCardCancelStatistics(String startDate, String endDate);

    Map<String, Object> getCardCancelDetails(String startDate, String endDate, Integer page, Integer size);

    Map<String, Object> getWeeklyAttendanceStatistics(String startDate, String endDate);

    Map<String, Object> getCommuteStatistics(String startDate, String endDate);
}
