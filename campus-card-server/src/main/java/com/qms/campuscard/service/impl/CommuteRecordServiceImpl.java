package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CommuteRecord;
import com.qms.campuscard.entity.CommuteRoute;
import com.qms.campuscard.entity.CommuteSchedule;
import com.qms.campuscard.entity.CommuteVehicle;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.CommuteRecordMapper;
import com.qms.campuscard.service.CommuteRouteService;
import com.qms.campuscard.service.CommuteRecordService;
import com.qms.campuscard.service.CommuteScheduleService;
import com.qms.campuscard.service.CommuteVehicleService;
import com.qms.campuscard.service.StudentService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class CommuteRecordServiceImpl implements CommuteRecordService {

    @Resource
    private CommuteRecordMapper commuteRecordMapper;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private CommuteRouteService commuteRouteService;

    @Resource
    private CommuteVehicleService commuteVehicleService;

    @Resource
    private CommuteScheduleService commuteScheduleService;

    @Resource
    private StudentService studentService;

    @Override
    public IPage<CommuteRecord> getCommuteRecords(Long cardId, Long routeId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<CommuteRecord> pageParam = new Page<>(page, size);
        QueryWrapper<CommuteRecord> queryWrapper = new QueryWrapper<>();

        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (routeId != null) {
            queryWrapper.eq("route_id", routeId);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("ride_time");
        return commuteRecordMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public boolean addCommuteRecord(CommuteRecord record) {
        validateCommuteRecord(record);
        record.setIsDeleted(0);
        record.setStatus(1);
        record.setRideTime(LocalDateTime.now());
        return commuteRecordMapper.insert(record) > 0;
    }

    private void validateCommuteRecord(CommuteRecord record) {
        if (record == null) {
            throw new RuntimeException("乘车记录不能为空");
        }
        if (record.getCardId() == null) {
            throw new RuntimeException("校园卡ID不能为空");
        }
        if (record.getRouteId() == null) {
            throw new RuntimeException("线路ID不能为空");
        }
        if (record.getVehicleId() == null) {
            throw new RuntimeException("车辆ID不能为空");
        }
        if (record.getScheduleId() == null) {
            throw new RuntimeException("班次ID不能为空");
        }

        CampusCard card = getAvailableCard(record.getCardId());
        if (card == null) {
            throw new RuntimeException("校园卡不存在或不可用");
        }
        studentService.ensureStudentProfileCompleteByCard(card);

        CommuteRoute route = commuteRouteService.getRouteById(record.getRouteId());
        if (route == null || !Integer.valueOf(1).equals(route.getStatus())) {
            throw new RuntimeException("线路不存在或已停用");
        }

        CommuteVehicle vehicle = commuteVehicleService.getVehicleById(record.getVehicleId());
        if (vehicle == null || !Integer.valueOf(1).equals(vehicle.getStatus())) {
            throw new RuntimeException("车辆不存在或不可用");
        }

        CommuteSchedule schedule = commuteScheduleService.getScheduleById(record.getScheduleId());
        if (schedule == null || !Integer.valueOf(1).equals(schedule.getStatus())) {
            throw new RuntimeException("班次不存在或已停用");
        }
        if (!record.getRouteId().equals(schedule.getRouteId())) {
            throw new RuntimeException("班次不属于所选线路");
        }
        if (!record.getVehicleId().equals(schedule.getVehicleId())) {
            throw new RuntimeException("车辆不属于所选班次");
        }

        LocalDate today = LocalDate.now();
        if ((schedule.getStartDate() != null && today.isBefore(schedule.getStartDate()))
                || (schedule.getEndDate() != null && today.isAfter(schedule.getEndDate()))) {
            throw new RuntimeException("班次不在有效日期内");
        }

        Integer seatNumber = parseSeatNumber(record.getSeatNumber());
        if (seatNumber == null) {
            throw new RuntimeException("座位号格式不正确");
        }
        if (vehicle.getSeatCount() != null && seatNumber > vehicle.getSeatCount()) {
            throw new RuntimeException("座位号不能超过车辆座位数");
        }
        if (hasCheckedIn(record.getCardId(), record.getScheduleId())) {
            throw new RuntimeException("该校园卡今天已登记该班次");
        }
        if (isSeatOccupied(record.getScheduleId(), seatNumber)) {
            throw new RuntimeException("该座位已被占用，请选择其他座位");
        }
        record.setSeatNumber(String.valueOf(seatNumber));
    }

    private CampusCard getAvailableCard(Long cardId) {
        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", cardId);
        queryWrapper.eq("status", 1);
        queryWrapper.eq("is_deleted", 0);
        return campusCardMapper.selectOne(queryWrapper);
    }

    private Integer parseSeatNumber(String seatNumber) {
        if (seatNumber == null || seatNumber.trim().isEmpty()) {
            return null;
        }
        try {
            int parsed = Integer.parseInt(seatNumber.trim());
            return parsed > 0 ? parsed : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isSeatOccupied(Long scheduleId, Integer seatNumber) {
        LocalDate today = LocalDate.now();
        QueryWrapper<CommuteRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("schedule_id", scheduleId);
        queryWrapper.eq("seat_number", String.valueOf(seatNumber));
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.ge("ride_time", today.atStartOfDay());
        queryWrapper.lt("ride_time", today.plusDays(1).atStartOfDay());
        return commuteRecordMapper.selectCount(queryWrapper) > 0;
    }

    private boolean hasCheckedIn(Long cardId, Long scheduleId) {
        LocalDate today = LocalDate.now();
        QueryWrapper<CommuteRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("schedule_id", scheduleId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.ge("ride_time", today.atStartOfDay());
        queryWrapper.lt("ride_time", today.plusDays(1).atStartOfDay());
        return commuteRecordMapper.selectCount(queryWrapper) > 0;
    }
}
