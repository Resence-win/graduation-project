package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteSchedule;
import com.qms.campuscard.mapper.CommuteScheduleMapper;
import com.qms.campuscard.service.CommuteScheduleService;
import com.qms.campuscard.util.RedisUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class CommuteScheduleServiceImpl implements CommuteScheduleService {

    private static final String SCHEDULE_INFO_KEY_PREFIX = "commute:schedule:";
    private static final String SCHEDULE_LIST_KEY = "commute:schedule:list";
    private static final String SCHEDULE_ROUTE_KEY_PREFIX = "commute:schedule:route:";
    private static final long CACHE_EXPIRE_TIME = 3600;

    @Resource
    private CommuteScheduleMapper commuteScheduleMapper;

    @Resource
    private RedisUtil redisUtil;

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
    public List<CommuteSchedule> getAllSchedules() {
        List<CommuteSchedule> cachedList = (List<CommuteSchedule>) redisUtil.get(SCHEDULE_LIST_KEY);
        if (cachedList != null) {
            return cachedList;
        }

        QueryWrapper<CommuteSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        List<CommuteSchedule> list = commuteScheduleMapper.selectList(queryWrapper);
        redisUtil.set(SCHEDULE_LIST_KEY, list, CACHE_EXPIRE_TIME);
        return list;
    }

    @Override
    public List<CommuteSchedule> getSchedulesByRouteId(Long routeId) {
        String cacheKey = SCHEDULE_ROUTE_KEY_PREFIX + routeId;
        List<CommuteSchedule> cachedList = (List<CommuteSchedule>) redisUtil.get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        QueryWrapper<CommuteSchedule> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("route_id", routeId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        List<CommuteSchedule> list = commuteScheduleMapper.selectList(queryWrapper);
        redisUtil.set(cacheKey, list, CACHE_EXPIRE_TIME);
        return list;
    }

    @Override
    public CommuteSchedule getScheduleById(Long id) {
        String cacheKey = SCHEDULE_INFO_KEY_PREFIX + id;
        CommuteSchedule cachedSchedule = (CommuteSchedule) redisUtil.get(cacheKey);
        if (cachedSchedule != null) {
            return cachedSchedule;
        }

        CommuteSchedule schedule = commuteScheduleMapper.selectById(id);
        if (schedule != null) {
            redisUtil.set(cacheKey, schedule, CACHE_EXPIRE_TIME);
        }
        return schedule;
    }

    @Override
    public boolean addSchedule(CommuteSchedule schedule) {
        schedule.setIsDeleted(0);
        boolean result = commuteScheduleMapper.insert(schedule) > 0;
        if (result) {
            redisUtil.del(SCHEDULE_LIST_KEY);
            if (schedule.getRouteId() != null) {
                redisUtil.del(SCHEDULE_ROUTE_KEY_PREFIX + schedule.getRouteId());
            }
        }
        return result;
    }

    @Override
    public boolean updateSchedule(CommuteSchedule schedule) {
        CommuteSchedule oldSchedule = getScheduleById(schedule.getId());
        boolean result = commuteScheduleMapper.updateById(schedule) > 0;
        if (result) {
            redisUtil.del(SCHEDULE_LIST_KEY);
            redisUtil.del(SCHEDULE_INFO_KEY_PREFIX + schedule.getId());
            if (schedule.getRouteId() != null) {
                redisUtil.del(SCHEDULE_ROUTE_KEY_PREFIX + schedule.getRouteId());
            }
            if (oldSchedule != null && oldSchedule.getRouteId() != null && 
                !oldSchedule.getRouteId().equals(schedule.getRouteId())) {
                redisUtil.del(SCHEDULE_ROUTE_KEY_PREFIX + oldSchedule.getRouteId());
            }
        }
        return result;
    }

    @Override
    public boolean deleteSchedule(Long id) {
        CommuteSchedule oldSchedule = getScheduleById(id);
        CommuteSchedule schedule = new CommuteSchedule();
        schedule.setId(id);
        schedule.setIsDeleted(1);
        boolean result = commuteScheduleMapper.updateById(schedule) > 0;
        if (result) {
            redisUtil.del(SCHEDULE_LIST_KEY);
            redisUtil.del(SCHEDULE_INFO_KEY_PREFIX + id);
            if (oldSchedule != null && oldSchedule.getRouteId() != null) {
                redisUtil.del(SCHEDULE_ROUTE_KEY_PREFIX + oldSchedule.getRouteId());
            }
        }
        return result;
    }
}
