package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AttendanceLocation;

import java.util.List;

public interface AttendanceLocationService {

    AttendanceLocation createLocation(AttendanceLocation location);

    AttendanceLocation updateLocation(AttendanceLocation location);

    void deleteLocation(Long id);

    AttendanceLocation getLocationById(Long id);

    IPage<AttendanceLocation> getLocationsByTeacherId(Long teacherId, Integer page, Integer size);

    IPage<AttendanceLocation> getAllLocations(Integer page, Integer size);

    List<AttendanceLocation> getActiveLocations();
}
