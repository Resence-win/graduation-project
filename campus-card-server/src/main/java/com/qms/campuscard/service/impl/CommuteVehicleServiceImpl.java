package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteVehicle;
import com.qms.campuscard.mapper.CommuteVehicleMapper;
import com.qms.campuscard.service.CommuteVehicleService;
import com.qms.campuscard.util.RedisUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class CommuteVehicleServiceImpl implements CommuteVehicleService {

    private static final String VEHICLE_INFO_KEY_PREFIX = "commute:vehicle:";
    private static final String VEHICLE_LIST_KEY = "commute:vehicle:list";
    private static final long CACHE_EXPIRE_TIME = 3600;

    @Resource
    private CommuteVehicleMapper commuteVehicleMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    public IPage<CommuteVehicle> getVehicleList(String plateNumber, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<CommuteVehicle> pageParam = new Page<>(page, size);
        QueryWrapper<CommuteVehicle> queryWrapper = new QueryWrapper<>();

        if (plateNumber != null && !plateNumber.isEmpty()) {
            queryWrapper.like("plate_number", plateNumber);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return commuteVehicleMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public List<CommuteVehicle> getAllVehicles() {
        List<CommuteVehicle> cachedList = (List<CommuteVehicle>) redisUtil.get(VEHICLE_LIST_KEY);
        if (cachedList != null) {
            return cachedList;
        }

        QueryWrapper<CommuteVehicle> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        List<CommuteVehicle> list = commuteVehicleMapper.selectList(queryWrapper);
        redisUtil.set(VEHICLE_LIST_KEY, list, CACHE_EXPIRE_TIME);
        return list;
    }

    @Override
    public CommuteVehicle getVehicleById(Long id) {
        String cacheKey = VEHICLE_INFO_KEY_PREFIX + id;
        CommuteVehicle cachedVehicle = (CommuteVehicle) redisUtil.get(cacheKey);
        if (cachedVehicle != null) {
            return cachedVehicle;
        }

        CommuteVehicle vehicle = commuteVehicleMapper.selectById(id);
        if (vehicle != null) {
            redisUtil.set(cacheKey, vehicle, CACHE_EXPIRE_TIME);
        }
        return vehicle;
    }

    @Override
    public boolean addVehicle(CommuteVehicle vehicle) {
        vehicle.setIsDeleted(0);
        boolean result = commuteVehicleMapper.insert(vehicle) > 0;
        if (result) {
            redisUtil.del(VEHICLE_LIST_KEY);
        }
        return result;
    }

    @Override
    public boolean updateVehicle(CommuteVehicle vehicle) {
        boolean result = commuteVehicleMapper.updateById(vehicle) > 0;
        if (result) {
            redisUtil.del(VEHICLE_LIST_KEY);
            redisUtil.del(VEHICLE_INFO_KEY_PREFIX + vehicle.getId());
        }
        return result;
    }

    @Override
    public boolean deleteVehicle(Long id) {
        CommuteVehicle vehicle = new CommuteVehicle();
        vehicle.setId(id);
        vehicle.setIsDeleted(1);
        boolean result = commuteVehicleMapper.updateById(vehicle) > 0;
        if (result) {
            redisUtil.del(VEHICLE_LIST_KEY);
            redisUtil.del(VEHICLE_INFO_KEY_PREFIX + id);
        }
        return result;
    }
}
