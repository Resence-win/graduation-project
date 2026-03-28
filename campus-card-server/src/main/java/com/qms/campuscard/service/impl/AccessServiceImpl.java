package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.mapper.AccessRecordMapper;
import com.qms.campuscard.service.AccessService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private AccessRecordMapper accessRecordMapper;

    @Override
    public IPage<AccessRecord> getAccessRecords(Long cardId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        Page<AccessRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AccessRecord> queryWrapper = new QueryWrapper<>();
        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("access_time");
        return accessRecordMapper.selectPage(pageParam, queryWrapper);
    }
}
