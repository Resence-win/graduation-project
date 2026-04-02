package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteStation;
import com.qms.campuscard.mapper.CommuteStationMapper;
import com.qms.campuscard.service.CommuteStationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class CommuteStationServiceImpl implements CommuteStationService {

    @Resource
    private CommuteStationMapper commuteStationMapper;

    @Override
    public IPage<CommuteStation> getStationList(String stationName, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<CommuteStation> pageParam = new Page<>(page, size);
        QueryWrapper<CommuteStation> queryWrapper = new QueryWrapper<>();

        if (stationName != null && !stationName.isEmpty()) {
            queryWrapper.like("station_name", stationName);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return commuteStationMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public CommuteStation getStationById(Long id) {
        return commuteStationMapper.selectById(id);
    }

    @Override
    public boolean addStation(CommuteStation station) {
        station.setIsDeleted(0);
        return commuteStationMapper.insert(station) > 0;
    }

    @Override
    public boolean updateStation(CommuteStation station) {
        return commuteStationMapper.updateById(station) > 0;
    }

    @Override
    public boolean deleteStation(Long id) {
        CommuteStation station = new CommuteStation();
        station.setId(id);
        station.setIsDeleted(1);
        return commuteStationMapper.updateById(station) > 0;
    }
}
