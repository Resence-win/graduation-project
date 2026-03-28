package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.mapper.AttendanceRecordMapper;
import com.qms.campuscard.service.AttendanceRecordService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;

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
        return attendanceRecordMapper.selectPage(pageParam, queryWrapper);
    }
}
