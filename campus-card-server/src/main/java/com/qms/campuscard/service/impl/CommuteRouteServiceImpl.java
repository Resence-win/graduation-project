package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteRoute;
import com.qms.campuscard.mapper.CommuteRouteMapper;
import com.qms.campuscard.service.CommuteRouteService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class CommuteRouteServiceImpl implements CommuteRouteService {

    @Resource
    private CommuteRouteMapper commuteRouteMapper;

    @Override
    public IPage<CommuteRoute> getRouteList(String routeName, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<CommuteRoute> pageParam = new Page<>(page, size);
        QueryWrapper<CommuteRoute> queryWrapper = new QueryWrapper<>();

        if (routeName != null && !routeName.isEmpty()) {
            queryWrapper.like("route_name", routeName);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return commuteRouteMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public CommuteRoute getRouteById(Long id) {
        return commuteRouteMapper.selectById(id);
    }

    @Override
    public boolean addRoute(CommuteRoute route) {
        route.setIsDeleted(0);
        return commuteRouteMapper.insert(route) > 0;
    }

    @Override
    public boolean updateRoute(CommuteRoute route) {
        return commuteRouteMapper.updateById(route) > 0;
    }

    @Override
    public boolean deleteRoute(Long id) {
        // 删除线路
        CommuteRoute route = new CommuteRoute();
        route.setId(id);
        route.setIsDeleted(1);
        return commuteRouteMapper.updateById(route) > 0;
    }
}
