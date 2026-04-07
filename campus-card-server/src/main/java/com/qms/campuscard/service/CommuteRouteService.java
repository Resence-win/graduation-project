package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.CommuteRoute;

import java.util.List;

public interface CommuteRouteService {
    IPage<CommuteRoute> getRouteList(String routeName, Integer status, Integer page, Integer size);
    List<CommuteRoute> getAllRoutes();
    CommuteRoute getRouteById(Long id);
    boolean addRoute(CommuteRoute route);
    boolean updateRoute(CommuteRoute route);
    boolean deleteRoute(Long id);
}
