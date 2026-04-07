package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CommuteRoute;
import com.qms.campuscard.mapper.CommuteRouteMapper;
import com.qms.campuscard.service.CommuteRouteService;
import com.qms.campuscard.util.RedisUtil;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class CommuteRouteServiceImpl implements CommuteRouteService {

    private static final String ROUTE_INFO_KEY_PREFIX = "commute:route:";
    private static final String ROUTE_LIST_KEY = "commute:route:list";
    private static final long CACHE_EXPIRE_TIME = 3600;

    @Resource
    private CommuteRouteMapper commuteRouteMapper;

    @Resource
    private RedisUtil redisUtil;

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
    public List<CommuteRoute> getAllRoutes() {
        List<CommuteRoute> cachedList = (List<CommuteRoute>) redisUtil.get(ROUTE_LIST_KEY);
        if (cachedList != null) {
            return cachedList;
        }

        QueryWrapper<CommuteRoute> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        List<CommuteRoute> list = commuteRouteMapper.selectList(queryWrapper);
        redisUtil.set(ROUTE_LIST_KEY, list, CACHE_EXPIRE_TIME);
        return list;
    }

    @Override
    public CommuteRoute getRouteById(Long id) {
        String cacheKey = ROUTE_INFO_KEY_PREFIX + id;
        CommuteRoute cachedRoute = (CommuteRoute) redisUtil.get(cacheKey);
        if (cachedRoute != null) {
            return cachedRoute;
        }

        CommuteRoute route = commuteRouteMapper.selectById(id);
        if (route != null) {
            redisUtil.set(cacheKey, route, CACHE_EXPIRE_TIME);
        }
        return route;
    }

    @Override
    public boolean addRoute(CommuteRoute route) {
        route.setIsDeleted(0);
        boolean result = commuteRouteMapper.insert(route) > 0;
        if (result) {
            redisUtil.del(ROUTE_LIST_KEY);
        }
        return result;
    }

    @Override
    public boolean updateRoute(CommuteRoute route) {
        boolean result = commuteRouteMapper.updateById(route) > 0;
        if (result) {
            redisUtil.del(ROUTE_LIST_KEY);
            redisUtil.del(ROUTE_INFO_KEY_PREFIX + route.getId());
        }
        return result;
    }

    @Override
    public boolean deleteRoute(Long id) {
        // 删除线路
        CommuteRoute route = new CommuteRoute();
        route.setId(id);
        route.setIsDeleted(1);
        boolean result = commuteRouteMapper.updateById(route) > 0;
        if (result) {
            redisUtil.del(ROUTE_LIST_KEY);
            redisUtil.del(ROUTE_INFO_KEY_PREFIX + id);
        }
        return result;
    }
}
