package com.qms.campuscard.task;

import com.qms.campuscard.service.AttendanceRecordService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class AttendanceAbsentGenerateTask {

    @Resource
    private AttendanceRecordService attendanceRecordService;

    /**
     * 定期补齐当天已结束打卡点的缺勤记录。
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void generateMissingAttendanceRecords() {
        attendanceRecordService.generateMissingAttendanceRecords();
    }
}
