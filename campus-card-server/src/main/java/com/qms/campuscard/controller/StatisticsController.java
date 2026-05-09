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

    /**
     * 综合统计接口：按时间范围汇总用户、交易、充值等核心运营指标。
     */
    @GetMapping("/api/stat/overview")
    public Result<Map<String, Object>> getOverview(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date) {
        Map<String, Object> data = statisticsService.getOverview(start_date, end_date);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询综合数据概览，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    /**
     * 消费趋势接口：按时间范围统计消费金额和消费次数，用于图表展示。
     */
    @GetMapping("/api/stat/consume")
    public Result<List<Map<String, Object>>> getConsumeStatistics(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date) {
        List<Map<String, Object>> data = statisticsService.getConsumeStatistics(start_date, end_date);
        // 记录日志
        logUtil.recordLog(1L, "查询", "statistics", null, "查询消费统计，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    /**
     * 用户消费排行接口：按时间范围查询消费金额靠前的用户。
     */
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

    /**
     * 商户消费排行接口：按时间范围查询营业额靠前的商户。
     */
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
