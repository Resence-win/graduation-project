package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.OperationLog;

import java.util.List;

public interface OperationLogService {

    IPage<OperationLog> getOperationLogs(Long operatorId, String operationType, String targetTable, Integer page, Integer size);

    List<OperationLog> exportOperationLogs(Long operatorId, String operationType, String targetTable);

    void save(OperationLog operationLog);
}
