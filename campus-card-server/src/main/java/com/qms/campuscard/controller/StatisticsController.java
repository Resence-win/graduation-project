package com.qms.campuscard.controller;

import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.service.StatisticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
public class StatisticsController {

    @Resource
    private StatisticsService statisticsService;

    @Resource
    private LogUtil logUtil;

    @GetMapping("/api/stat/consume")
    public Result<List<Map<String, Object>>> getConsumeStatistics(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date) {
        List<Map<String, Object>> data = statisticsService.getConsumeStatistics(start_date, end_date);
        // 记录日志
        logUtil.recordLog(1L, "查询", "statistics", null, "查询消费统计，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    @GetMapping("/api/stat/user-rank")
    public Result<List<Map<String, Object>>> getUserRank(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> data = statisticsService.getUserRank(start_date, end_date, limit);
        // 记录日志
        logUtil.recordLog(1L, "查询", "statistics", null, "查询用户消费排行，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    @GetMapping("/api/stat/merchant-rank")
    public Result<List<Map<String, Object>>> getMerchantRank(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        List<Map<String, Object>> data = statisticsService.getMerchantRank(start_date, end_date, limit);
        // 记录日志
        logUtil.recordLog(1L, "查询", "statistics", null, "查询商户消费排行，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }
}
