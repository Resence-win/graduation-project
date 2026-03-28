package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.service.AttendanceRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
public class AttendanceRecordController {

    @Resource
    private AttendanceRecordService attendanceRecordService;

    @GetMapping("/api/attendance/list")
    public Result<IPage<AttendanceRecord>> getAttendanceRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AttendanceRecord> records = attendanceRecordService.getAttendanceRecords(card_id, status, page, size);
        return Result.success(records);
    }
}
