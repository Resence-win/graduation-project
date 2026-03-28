package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AttendanceRecord;

public interface AttendanceRecordService {

    IPage<AttendanceRecord> getAttendanceRecords(Long cardId, String status, Integer page, Integer size);
}
