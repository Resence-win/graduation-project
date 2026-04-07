package com.qms.campuscard.controller;

import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.Map;
import java.util.HashMap;

@RestController
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @Resource
    private LogUtil logUtil;

    @GetMapping("/api/dashboard")
    public Result<Map<String, Object>> getDashboardData() {
        Map<String, Object> data = new HashMap<>();
        data.put("studentCount", dashboardService.getStudentCount());
        data.put("teacherCount", dashboardService.getTeacherCount());
        data.put("merchantCount", dashboardService.getMerchantCount());
        data.put("todayConsume", dashboardService.getTodayConsumeAmount());

        // 记录日志
        logUtil.recordLog(1L, "查询", "dashboard", null, "查询仪表盘数据");
        return Result.success(data);
    }
}
