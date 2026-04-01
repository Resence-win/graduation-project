package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AttendanceRecord;

public interface AttendanceRecordService {

    IPage<AttendanceRecord> getAttendanceRecords(Long cardId, String status, Integer page, Integer size);
    
    AttendanceRecord createAttendance(Long cardId, Long locationId, String actualLocation, Double actualLatitude, Double actualLongitude, String deviceInfo);
    
    IPage<AttendanceRecord> getAttendanceStatistics(String startDate, String endDate, Integer page, Integer size);
    
    IPage<AttendanceRecord> getAttendanceRecordsByLocationId(Long locationId, Integer page, Integer size);
}
