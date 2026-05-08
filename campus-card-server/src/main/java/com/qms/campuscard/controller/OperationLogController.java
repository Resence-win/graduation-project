package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.service.OperationLogService;
import com.qms.campuscard.util.ExcelUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
import java.util.List;

@RestController
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @Resource
    private LogUtil logUtil;

    @GetMapping("/api/log/list")
    public Result<IPage<OperationLog>> getOperationLogs(
            @RequestParam(required = false) Long operator_id,
            @RequestParam(required = false) String operation_type,
            @RequestParam(required = false) String target_table,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<OperationLog> logs = operationLogService.getOperationLogs(operator_id, operation_type, target_table, page, size);
        return Result.success(logs);
    }

    @GetMapping("/api/log/export")
    public void exportOperationLogs(
            @RequestParam(required = false) Long operator_id,
            @RequestParam(required = false) String operation_type,
            @RequestParam(required = false) String target_table,
            HttpServletResponse response) {
        try {
            List<OperationLog> logs = operationLogService.exportOperationLogs(operator_id, operation_type, target_table);
            ExcelUtil.exportOperationLogs(response, logs);
            logUtil.recordLog(1L, "导出", "operation_log", null, "导出系统日志，共" + logs.size() + "条");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
