package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.AccessPoint;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.service.AccessService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    private final AccessService accessService;

    @Resource
    private LogUtil logUtil;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    // 门禁点管理
    @GetMapping("/point/list")
    public Result<IPage<AccessPoint>> getAccessPoints(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AccessPoint> points = accessService.getAccessPoints(name, status, page, size);
        logUtil.recordLog(1L, "查询", "access_point", null, "查询门禁点列表");
        return Result.success(points);
    }

    @PostMapping("/point")
    public Result<AccessPoint> addAccessPoint(@RequestBody AccessPoint accessPoint) {
        AccessPoint result = accessService.addAccessPoint(accessPoint);
        logUtil.recordLog(1L, "新增", "access_point", result.getId(), "新增门禁点：" + accessPoint.getName());
        return Result.success(result);
    }

    @PutMapping("/point")
    public Result<AccessPoint> updateAccessPoint(@RequestBody AccessPoint accessPoint) {
        AccessPoint result = accessService.updateAccessPoint(accessPoint);
        logUtil.recordLog(1L, "修改", "access_point", accessPoint.getId(), "修改门禁点：" + accessPoint.getName());
        return Result.success(result);
    }

    @DeleteMapping("/point/{id}")
    public Result<Void> deleteAccessPoint(@PathVariable Long id) {
        accessService.deleteAccessPoint(id);
        logUtil.recordLog(1L, "删除", "access_point", id, "删除门禁点");
        return Result.success();
    }

    // 门禁记录管理
    @GetMapping("/list")
    public Result<IPage<AccessRecord>> getAccessRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Long access_point_id,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (start_date != null && !start_date.isEmpty()) {
            startDate = LocalDateTime.parse(start_date + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (end_date != null && !end_date.isEmpty()) {
            endDate = LocalDateTime.parse(end_date + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        IPage<AccessRecord> records = accessService.getAccessRecords(card_id, access_point_id, startDate, endDate, status, page, size);
        logUtil.recordLog(1L, "查询", "access_record", null, "查询门禁记录");
        return Result.success(records);
    }

    @GetMapping("/my")
    public Result<IPage<AccessRecord>> getMyAccessRecords(
            @RequestParam(required = true) Long card_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AccessRecord> records = accessService.getMyAccessRecords(card_id, page, size);
        return Result.success(records);
    }

    @PostMapping("/qr")
    public Result<AccessRecord> createQRAccess(
            @RequestParam Long card_id,
            @RequestParam Long access_point_id,
            @RequestParam String qr_code) {
        AccessRecord record = accessService.createQRAccess(card_id, access_point_id, qr_code);
        logUtil.recordLog(1L, "操作", "access_record", record.getId(), "二维码开门");
        return Result.success(record);
    }

    @GetMapping("/stat")
    public Result<List<Map<String, Object>>> getAccessStatistics(
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date) {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (start_date != null && !start_date.isEmpty()) {
            startDate = LocalDateTime.parse(start_date + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (end_date != null && !end_date.isEmpty()) {
            endDate = LocalDateTime.parse(end_date + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        List<Map<String, Object>> statistics = accessService.getAccessStatistics(startDate, endDate);
        return Result.success(statistics);
    }

    @GetMapping("/export")
    public void exportAccessRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            HttpServletResponse response) throws IOException {
        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        if (start_date != null && !start_date.isEmpty()) {
            startDate = LocalDateTime.parse(start_date + " 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        if (end_date != null && !end_date.isEmpty()) {
            endDate = LocalDateTime.parse(end_date + " 23:59:59", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
        List<AccessRecord> records = accessService.exportAccessRecords(card_id, startDate, endDate);
        
        // 设置响应头
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=access_records.csv");
        
        // 写入CSV内容
        PrintWriter writer = response.getWriter();
        writer.println("ID,卡号,门禁点ID,方向,位置,状态,通行时间,设备信息");
        for (AccessRecord record : records) {
            writer.println(record.getId() + "," + record.getCardId() + "," + 
                          (record.getAccessPointId() != null ? record.getAccessPointId() : "") + "," +
                          record.getDirection() + "," + record.getLocation() + "," + 
                          record.getStatus() + "," + record.getAccessTime() + "," + 
                          record.getDeviceInfo());
        }
        writer.flush();
        writer.close();
        
        logUtil.recordLog(1L, "导出", "access_record", null, "导出门禁记录");
    }
}
