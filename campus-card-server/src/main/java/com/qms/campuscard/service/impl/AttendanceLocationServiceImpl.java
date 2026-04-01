package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.mapper.AttendanceLocationMapper;
import com.qms.campuscard.service.AttendanceLocationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AttendanceLocationServiceImpl extends ServiceImpl<AttendanceLocationMapper, AttendanceLocation> implements AttendanceLocationService {

    @Override
    public AttendanceLocation createLocation(AttendanceLocation location) {
        save(location);
        return location;
    }

    @Override
    public AttendanceLocation updateLocation(AttendanceLocation location) {
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
        return lambdaQuery()
                .eq(AttendanceLocation::getStatus, 1)
                .list();
    }
}
