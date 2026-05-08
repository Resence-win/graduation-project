package com.qms.campuscard.common;

import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.service.OperationLogService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class LogUtil {

    private static final Set<String> IMPORTANT_OPERATION_TYPES = new HashSet<>(Arrays.asList(
            "新增", "修改", "删除", "导入", "导出", "上传", "开卡", "操作"
    ));

    @Resource
    private OperationLogService operationLogService;

    /**
     * 记录操作日志
     * @param operatorId 操作人ID
     * @param operationType 操作类型
     * @param targetTable 目标表
     * @param targetId 目标ID
     * @param content 操作内容
     */
    public void recordLog(Long operatorId, String operationType, String targetTable, Long targetId, String content) {
        if (shouldIgnoreLog(operationType, targetTable)) {
            return;
        }

        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperationType(operationType);
        log.setTargetTable(targetTable);
        log.setTargetId(targetId);
        log.setContent(content);
        log.setCreateTime(LocalDateTime.now());
        log.setIsDeleted(0);
        
        // 保存日志到数据库
        operationLogService.save(log);
    }

    private boolean shouldIgnoreLog(String operationType, String targetTable) {
        if (operationType == null) {
            return true;
        }
        if (!IMPORTANT_OPERATION_TYPES.contains(operationType)) {
            return true;
        }
        return "operation_log".equals(targetTable) && !"导出".equals(operationType);
    }
}
