package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.mapper.AttendanceRecordMapper;
import com.qms.campuscard.service.AttendanceLocationService;
import com.qms.campuscard.service.AttendanceRecordService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    
    @Resource
    private AttendanceLocationService attendanceLocationService;

    @Override
    public IPage<AttendanceRecord> getAttendanceRecords(Long cardId, String status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<AttendanceRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("record_time");
        IPage<AttendanceRecord> result = attendanceRecordMapper.selectPage(pageParam, queryWrapper);
        
        // 填充打卡位置名称
        for (AttendanceRecord record : result.getRecords()) {
            if (record.getLocationId() != null) {
                AttendanceLocation location = attendanceLocationService.getLocationById(record.getLocationId());
                if (location != null) {
                    record.setLocationName(location.getLocationName());
                }
            }
        }
        
        return result;
    }

    @Override
    public AttendanceRecord createAttendance(Long cardId, Long locationId, String actualLocation, Double actualLatitude, Double actualLongitude, String deviceInfo) {
        // 检查是否已经打卡
        LocalDate today = LocalDate.now();
        QueryWrapper<AttendanceRecord> checkWrapper = new QueryWrapper<>();
        checkWrapper.eq("card_id", cardId);
        checkWrapper.eq("location_id", locationId);
        checkWrapper.ge("record_time", today.atStartOfDay());
        checkWrapper.lt("record_time", today.plusDays(1).atStartOfDay());
        checkWrapper.eq("is_deleted", 0);
        if (attendanceRecordMapper.selectCount(checkWrapper) > 0) {
            throw new RuntimeException("今日已在该位置打卡，请勿重复打卡");
        }
        
        // 获取打卡位置信息
        AttendanceLocation location = attendanceLocationService.getLocationById(locationId);
        if (location == null) {
            throw new RuntimeException("打卡位置不存在");
        }
        
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setCardId(cardId);
        attendanceRecord.setLocationId(locationId);
        attendanceRecord.setActualLocation(actualLocation);
        attendanceRecord.setActualLatitude(actualLatitude);
        attendanceRecord.setActualLongitude(actualLongitude);
        attendanceRecord.setDeviceInfo(deviceInfo);
        attendanceRecord.setRecordTime(LocalDateTime.now());
        
        // 自动判断考勤状态
        LocalDateTime now = LocalDateTime.now();
        String status;
        if (now.isBefore(location.getStartTime())) {
            status = "正常";
        } else if (now.isBefore(location.getStartTime().plusMinutes(30))) {
            status = "迟到";
        } else if (now.isBefore(location.getEndTime())) {
            status = "正常";
        } else {
            status = "缺勤";
        }
        
        attendanceRecord.setStatus(status);
        attendanceRecord.setIsDeleted(0);
        
        attendanceRecordMapper.insert(attendanceRecord);
        return attendanceRecord;
    }

    @Override
    public IPage<AttendanceRecord> getAttendanceRecordsByLocationId(Long locationId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<AttendanceRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (locationId != null) {
            queryWrapper.eq("location_id", locationId);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("record_time");
        IPage<AttendanceRecord> result = attendanceRecordMapper.selectPage(pageParam, queryWrapper);
        
        // 填充打卡位置名称
        for (AttendanceRecord record : result.getRecords()) {
            if (record.getLocationId() != null) {
                AttendanceLocation location = attendanceLocationService.getLocationById(record.getLocationId());
                if (location != null) {
                    record.setLocationName(location.getLocationName());
                }
            }
        }
        
        return result;
    }

    @Override
    public IPage<AttendanceRecord> getAttendanceStatistics(String startDate, String endDate, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<AttendanceRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (startDate != null) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null) {
            // 构造正确的时间戳格式
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("record_time");
        IPage<AttendanceRecord> result = attendanceRecordMapper.selectPage(pageParam, queryWrapper);
        
        // 填充打卡位置名称
        for (AttendanceRecord record : result.getRecords()) {
            if (record.getLocationId() != null) {
                AttendanceLocation location = attendanceLocationService.getLocationById(record.getLocationId());
                if (location != null) {
                    record.setLocationName(location.getLocationName());
                }
            }
        }
        
        return result;
    }
}
