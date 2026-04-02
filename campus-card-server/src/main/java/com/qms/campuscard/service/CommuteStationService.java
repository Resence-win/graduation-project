package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.CommuteStation;

public interface CommuteStationService {
    IPage<CommuteStation> getStationList(String stationName, Integer status, Integer page, Integer size);
    CommuteStation getStationById(Long id);
    boolean addStation(CommuteStation station);
    boolean updateStation(CommuteStation station);
    boolean deleteStation(Long id);
}
