package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.service.OperationLogService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

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
        // 记录日志
        logUtil.recordLog(1L, "查询", "operation_log", null, "查询操作日志");
        return Result.success(logs);
    }
}
