package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteVehicle;
import com.qms.campuscard.mapper.CommuteVehicleMapper;
import com.qms.campuscard.service.CommuteVehicleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class CommuteVehicleServiceImpl implements CommuteVehicleService {

    @Resource
    private CommuteVehicleMapper commuteVehicleMapper;

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
    public CommuteVehicle getVehicleById(Long id) {
        return commuteVehicleMapper.selectById(id);
    }

    @Override
    public boolean addVehicle(CommuteVehicle vehicle) {
        vehicle.setIsDeleted(0);
        return commuteVehicleMapper.insert(vehicle) > 0;
    }

    @Override
    public boolean updateVehicle(CommuteVehicle vehicle) {
        return commuteVehicleMapper.updateById(vehicle) > 0;
    }

    @Override
    public boolean deleteVehicle(Long id) {
        CommuteVehicle vehicle = new CommuteVehicle();
        vehicle.setId(id);
        vehicle.setIsDeleted(1);
        return commuteVehicleMapper.updateById(vehicle) > 0;
    }
}
