package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.mapper.AttendanceLocationMapper;
import com.qms.campuscard.service.AttendanceLocationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceLocationServiceImpl extends ServiceImpl<AttendanceLocationMapper, AttendanceLocation> implements AttendanceLocationService {

    @Override
    public AttendanceLocation createLocation(AttendanceLocation location) {
        validateLocation(location);
        save(location);
        return location;
    }

    @Override
    public AttendanceLocation updateLocation(AttendanceLocation location) {
        validateLocation(location);
        updateById(location);
        return location;
    }

    @Override
    public void deleteLocation(Long id) {
        removeById(id);
    }

    @Override
    public AttendanceLocation getLocationById(Long id) {
        return getById(id);
    }

    @Override
    public IPage<AttendanceLocation> getLocationsByTeacherId(Long teacherId, Integer page, Integer size) {
        Page<AttendanceLocation> pagination = new Page<>(page, size);
        return lambdaQuery()
                .eq(AttendanceLocation::getTeacherId, teacherId)
                .eq(AttendanceLocation::getStatus, 1)
                .orderByDesc(AttendanceLocation::getCreateTime)
                .page(pagination);
    }

    @Override
    public IPage<AttendanceLocation> getAllLocations(Integer page, Integer size) {
        Page<AttendanceLocation> pagination = new Page<>(page, size);
        return lambdaQuery()
                .eq(AttendanceLocation::getStatus, 1)
                .orderByDesc(AttendanceLocation::getCreateTime)
                .page(pagination);
    }

    @Override
    public List<AttendanceLocation> getActiveLocations() {
        LocalDateTime now = LocalDateTime.now();
        return lambdaQuery()
                .eq(AttendanceLocation::getStatus, 1)
                .le(AttendanceLocation::getStartTime, now)
                .ge(AttendanceLocation::getEndTime, now)
                .orderByAsc(AttendanceLocation::getEndTime)
                .list();
    }

    private void validateLocation(AttendanceLocation location) {
        if (location == null) {
            throw new RuntimeException("打卡位置不能为空");
        }
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            throw new RuntimeException("位置名称不能为空");
        }
        if (location.getLatitude() == null || location.getLongitude() == null) {
            throw new RuntimeException("经纬度不能为空");
        }
        if (location.getRadius() == null || location.getRadius() <= 0) {
            throw new RuntimeException("打卡半径必须大于0");
        }
        if (location.getStartTime() == null || location.getEndTime() == null) {
            throw new RuntimeException("打卡开始时间和结束时间不能为空");
        }
        if (!location.getEndTime().isAfter(location.getStartTime())) {
            throw new RuntimeException("打卡结束时间必须晚于开始时间");
        }
    }
}
