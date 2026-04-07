package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.CommuteVehicle;

import java.util.List;

public interface CommuteVehicleService {
    IPage<CommuteVehicle> getVehicleList(String plateNumber, Integer status, Integer page, Integer size);
    List<CommuteVehicle> getAllVehicles();
    CommuteVehicle getVehicleById(Long id);
    boolean addVehicle(CommuteVehicle vehicle);
    boolean updateVehicle(CommuteVehicle vehicle);
    boolean deleteVehicle(Long id);
}
