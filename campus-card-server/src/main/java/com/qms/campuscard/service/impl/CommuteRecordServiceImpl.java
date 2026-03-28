package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteRecord;
import com.qms.campuscard.mapper.CommuteRecordMapper;
import com.qms.campuscard.service.CommuteRecordService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class CommuteRecordServiceImpl implements CommuteRecordService {

    @Resource
    private CommuteRecordMapper commuteRecordMapper;

    @Override
    public IPage<CommuteRecord> getCommuteRecords(Long cardId, Long routeId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<CommuteRecord> pageParam = new Page<>(page, size);
        QueryWrapper<CommuteRecord> queryWrapper = new QueryWrapper<>();

        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (routeId != null) {
            queryWrapper.eq("route_id", routeId);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("ride_time");
        return commuteRecordMapper.selectPage(pageParam, queryWrapper);
    }
}
