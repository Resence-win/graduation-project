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

    /**
     * 图书馆统计接口：统计当前逾期、当前借出、可借出图书和禁借剩余天数排行。
     */
    @GetMapping("/api/stat/library-overdue")
    public Result<Map<String, Object>> getLibraryOverdueStatistics(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        Map<String, Object> data = statisticsService.getLibraryOverdueStatistics(start_date, end_date, limit);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询图书馆逾期统计，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    /**
     * 图书统计明细接口：按当前逾期、当前借出、可借出分类查询弹窗表格数据。
     */
    @GetMapping("/api/stat/library-book-details")
    public Result<Map<String, Object>> getLibraryBookDetails(
            @RequestParam(required = false, defaultValue = "overdue") String type,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Map<String, Object> data = statisticsService.getLibraryBookDetails(type, page, size);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询图书统计明细，类型：" + type);
        return Result.success(data);
    }

    /**
     * 校园卡注销统计接口：按注销原因、时间和人员类型聚合注销记录。
     */
    @GetMapping("/api/stat/card-cancel")
    public Result<Map<String, Object>> getCardCancelStatistics(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date) {
        Map<String, Object> data = statisticsService.getCardCancelStatistics(start_date, end_date);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询校园卡注销统计，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    /**
     * 校园卡注销明细接口：查询注销卡弹窗表格。
     */
    @GetMapping("/api/stat/card-cancel/details")
    public Result<Map<String, Object>> getCardCancelDetails(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        Map<String, Object> data = statisticsService.getCardCancelDetails(start_date, end_date, page, size);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询校园卡注销明细，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }

    /**
     * 考勤趋势统计接口：统计指定日期区间或最近七天的正常、迟到、早退、缺勤数量。
     */
    @GetMapping("/api/stat/weekly-attendance")
    public Result<Map<String, Object>> getWeeklyAttendanceStatistics(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date) {
        Map<String, Object> data = statisticsService.getWeeklyAttendanceStatistics(start_date, end_date);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询一周考勤统计");
        return Result.success(data);
    }

    /**
     * 通勤车统计接口：按线路、班次、车辆统计乘车人数和空座率。
     */
    @GetMapping("/api/stat/commute")
    public Result<Map<String, Object>> getCommuteStatistics(
            @RequestParam(required = false, defaultValue = "") String start_date,
            @RequestParam(required = false, defaultValue = "") String end_date) {
        Map<String, Object> data = statisticsService.getCommuteStatistics(start_date, end_date);
        logUtil.recordLog(1L, "查询", "statistics", null, "查询通勤车统计，时间范围：" + (start_date.isEmpty() ? "全部" : start_date) + " 至 " + (end_date.isEmpty() ? "全部" : end_date));
        return Result.success(data);
    }
}
