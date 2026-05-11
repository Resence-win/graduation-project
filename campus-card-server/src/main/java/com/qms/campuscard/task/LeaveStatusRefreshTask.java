package com.qms.campuscard.task;

import com.qms.campuscard.service.AttendanceApplicationService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

@Component
public class LeaveStatusRefreshTask {

    @Resource
    private AttendanceApplicationService attendanceApplicationService;

    /**
     * 每天凌晨刷新仍处于请假状态的学生，避免请假到期后状态滞留。
     */
    @Scheduled(cron = "0 5 0 * * ?")
    public void refreshLeaveStatuses() {
        attendanceApplicationService.refreshAllLeaveStatuses();
    }
}
