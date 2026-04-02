package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.CommuteRoute;

public interface CommuteRouteService {
    IPage<CommuteRoute> getRouteList(String routeName, Integer status, Integer page, Integer size);
    CommuteRoute getRouteById(Long id);
    boolean addRoute(CommuteRoute route);
    boolean updateRoute(CommuteRoute route);
    boolean deleteRoute(Long id);
}
