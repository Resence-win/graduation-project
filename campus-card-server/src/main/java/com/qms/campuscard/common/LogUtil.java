package com.qms.campuscard.common;

import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.service.OperationLogService;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

@Component
public class LogUtil {

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
}
