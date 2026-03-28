package com.qms.campuscard.service.impl;

import com.qms.campuscard.mapper.ConsumeRecordMapper;
import com.qms.campuscard.service.StatisticsService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Resource
    private ConsumeRecordMapper consumeRecordMapper;

    @Override
    public List<Map<String, Object>> getConsumeStatistics(String startDate, String endDate) {
        return consumeRecordMapper.getConsumeStatistics(startDate, endDate);
    }

    @Override
    public List<Map<String, Object>> getUserRank(String startDate, String endDate, Integer limit) {
        return consumeRecordMapper.getUserRank(startDate, endDate, limit);
    }

    @Override
    public List<Map<String, Object>> getMerchantRank(String startDate, String endDate, Integer limit) {
        return consumeRecordMapper.getMerchantRank(startDate, endDate, limit);
    }
}
