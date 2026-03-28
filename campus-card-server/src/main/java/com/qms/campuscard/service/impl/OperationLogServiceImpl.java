package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.OperationLog;
import com.qms.campuscard.mapper.OperationLogMapper;
import com.qms.campuscard.service.OperationLogService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public IPage<OperationLog> getOperationLogs(Long operatorId, String operationType, String targetTable, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<OperationLog> pageParam = new Page<>(page, size);
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();

        if (operatorId != null) {
            queryWrapper.eq("operator_id", operatorId);
        }
        if (operationType != null) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (targetTable != null) {
            queryWrapper.eq("target_table", targetTable);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return operationLogMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public void save(OperationLog operationLog) {
        operationLogMapper.insert(operationLog);
    }
}
