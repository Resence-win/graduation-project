package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.service.AttendanceLocationService;
import com.qms.campuscard.service.AttendanceRecordService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Resource
    private AttendanceRecordService attendanceRecordService;
    
    @Resource
    private AttendanceLocationService attendanceLocationService;

    // 打卡位置相关接口
    @PostMapping("/location")
    public Result<AttendanceLocation> createLocation(@RequestBody AttendanceLocation location) {
        AttendanceLocation createdLocation = attendanceLocationService.createLocation(location);
        return Result.success(createdLocation);
    }

    @PutMapping("/location")
    public Result<AttendanceLocation> updateLocation(@RequestBody AttendanceLocation location) {
        AttendanceLocation updatedLocation = attendanceLocationService.updateLocation(location);
        return Result.success(updatedLocation);
    }

    @DeleteMapping("/location/{id}")
    public Result<Void> deleteLocation(@PathVariable Long id) {
        attendanceLocationService.deleteLocation(id);
        return Result.success();
    }

    @GetMapping("/location/{id}")
    public Result<AttendanceLocation> getLocationById(@PathVariable Long id) {
        AttendanceLocation location = attendanceLocationService.getLocationById(id);
        return Result.success(location);
    }

    @GetMapping("/location/teacher/{teacherId}")
    public Result<IPage<AttendanceLocation>> getLocationsByTeacherId(
            @PathVariable Long teacherId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceLocation> locations = attendanceLocationService.getLocationsByTeacherId(teacherId, page, size);
        return Result.success(locations);
    }

    @GetMapping("/location/list")
    public Result<IPage<AttendanceLocation>> getAllLocations(
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceLocation> locations = attendanceLocationService.getAllLocations(page, size);
        return Result.success(locations);
    }

    @GetMapping("/location/active")
    public Result<?> getActiveLocations() {
        return Result.success(attendanceLocationService.getActiveLocations());
    }

    // 考勤记录相关接口
    @GetMapping("/list")
    public Result<IPage<AttendanceRecord>> getAttendanceRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceRecord> attendanceRecords = attendanceRecordService.getAttendanceRecords(card_id, status, page, size);
        return Result.success(attendanceRecords);
    }

    @PostMapping("/checkin")
    public Result<AttendanceRecord> createAttendance(
            @RequestParam Long card_id,
            @RequestParam Long location_id,
            @RequestParam String actual_location,
            @RequestParam Double actual_latitude,
            @RequestParam Double actual_longitude,
            @RequestParam(required = false) String device_info) {
        AttendanceRecord attendanceRecord = attendanceRecordService.createAttendance(card_id, location_id, actual_location, actual_latitude, actual_longitude, device_info);
        return Result.success(attendanceRecord);
    }

    @GetMapping("/statistics")
    public Result<IPage<AttendanceRecord>> getAttendanceStatistics(
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceRecord> attendanceStatistics = attendanceRecordService.getAttendanceStatistics(start_date, end_date, page, size);
        return Result.success(attendanceStatistics);
    }

    @GetMapping("/location/{locationId}/records")
    public Result<IPage<AttendanceRecord>> getAttendanceRecordsByLocationId(
            @PathVariable Long locationId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceRecord> records = attendanceRecordService.getAttendanceRecordsByLocationId(locationId, page, size);
        return Result.success(records);
    }
}
