package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteSchedule;
import com.qms.campuscard.mapper.CommuteScheduleMapper;
import com.qms.campuscard.service.CommuteScheduleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class CommuteScheduleServiceImpl implements CommuteScheduleService {

    @Resource
    private CommuteScheduleMapper commuteScheduleMapper;

    @Override
    public IPage<CommuteSchedule> getScheduleList(Long routeId, Long vehicleId, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<CommuteSchedule> pageParam = new Page<>(page, size);
        QueryWrapper<CommuteSchedule> queryWrapper = new QueryWrapper<>();

        if (routeId != null) {
            queryWrapper.eq("route_id", routeId);
        }
        if (vehicleId != null) {
            queryWrapper.eq("vehicle_id", vehicleId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return commuteScheduleMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public CommuteSchedule getScheduleById(Long id) {
        return commuteScheduleMapper.selectById(id);
    }

    @Override
    public boolean addSchedule(CommuteSchedule schedule) {
        schedule.setIsDeleted(0);
        return commuteScheduleMapper.insert(schedule) > 0;
    }

    @Override
    public boolean updateSchedule(CommuteSchedule schedule) {
        return commuteScheduleMapper.updateById(schedule) > 0;
    }

    @Override
    public boolean deleteSchedule(Long id) {
        CommuteSchedule schedule = new CommuteSchedule();
        schedule.setId(id);
        schedule.setIsDeleted(1);
        return commuteScheduleMapper.updateById(schedule) > 0;
    }
}
