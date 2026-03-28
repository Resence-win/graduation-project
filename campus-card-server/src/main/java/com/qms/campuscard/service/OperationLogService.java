package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.OperationLog;

public interface OperationLogService {

    IPage<OperationLog> getOperationLogs(Long operatorId, String operationType, String targetTable, Integer page, Integer size);

    void save(OperationLog operationLog);
}
