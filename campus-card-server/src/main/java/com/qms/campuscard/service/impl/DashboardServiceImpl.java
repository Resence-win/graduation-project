package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.entity.ConsumeRecord;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.mapper.MerchantMapper;
import com.qms.campuscard.mapper.ConsumeRecordMapper;
import com.qms.campuscard.service.DashboardService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private ConsumeRecordMapper consumeRecordMapper;

    @Override
    public long getStudentCount() {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return studentMapper.selectCount(queryWrapper);
    }

    @Override
    public long getTeacherCount() {
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return teacherMapper.selectCount(queryWrapper);
    }

    @Override
    public long getMerchantCount() {
        QueryWrapper<Merchant> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        return merchantMapper.selectCount(queryWrapper);
    }

    @Override
    public BigDecimal getTodayConsumeAmount() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        QueryWrapper<ConsumeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("consume_time", startOfDay);
        queryWrapper.le("consume_time", endOfDay);
        queryWrapper.eq("is_deleted", 0);

        // 使用自定义SQL查询今日消费总额
        return consumeRecordMapper.getTodayConsumeAmount(startOfDay, endOfDay);
    }
}
