package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
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

    @Resource
    private LogUtil logUtil;

    @GetMapping("/api/attendance/list")
    public Result<IPage<AttendanceRecord>> getAttendanceRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AttendanceRecord> records = attendanceRecordService.getAttendanceRecords(card_id, status, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "attendance_record", null, "查询考勤记录，卡号：" + (card_id != null ? card_id : "全部"));
        return Result.success(records);
    }
}
