package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AttendanceRecord;

import java.util.Map;

public interface AttendanceRecordService {

    IPage<AttendanceRecord> getAttendanceRecords(Long cardId, Long locationId, String status, String startDate, String endDate, Integer page, Integer size);
    
    AttendanceRecord createAttendance(Long cardId, Long locationId, String actualLocation, Double actualLatitude, Double actualLongitude, String deviceInfo,
                                      String attendanceType, String internshipCompany, String internshipLog, String internshipLogDate);
    
    IPage<AttendanceRecord> getAttendanceStatistics(String startDate, String endDate, Integer page, Integer size);

    Map<String, Long> getAttendanceSummary(String startDate, String endDate, Long locationId);
    
    IPage<AttendanceRecord> getAttendanceRecordsByLocationId(Long locationId, Integer page, Integer size);

    void generateMissingAttendanceRecords();
}
