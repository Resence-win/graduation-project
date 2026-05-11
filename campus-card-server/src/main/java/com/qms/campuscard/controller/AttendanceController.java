package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.UploadMaterialResponse;
import com.qms.campuscard.entity.AttendanceApplication;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.service.AttendanceApplicationService;
import com.qms.campuscard.service.AttendanceLocationService;
import com.qms.campuscard.service.AttendanceRecordService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    @Resource
    private AttendanceRecordService attendanceRecordService;
    
    @Resource
    private AttendanceLocationService attendanceLocationService;

    @Resource
    private AttendanceApplicationService attendanceApplicationService;

    // 打卡位置相关接口
    /**
     * 创建考勤点接口：教师或管理员配置可打卡的位置、经纬度和有效范围。
     */
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

    /**
     * 教师考勤点接口：分页查询指定教师创建或负责的考勤地点。
     */
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

    /**
     * 可用考勤点接口：查询当前启用的考勤地点，供学生打卡时选择。
     */
    @GetMapping("/location/active")
    public Result<?> getActiveLocations() {
        return Result.success(attendanceLocationService.getActiveLocations());
    }

    // 考勤记录相关接口
    /**
     * 考勤记录接口：按校园卡、状态和日期范围分页查询学生打卡记录。
     */
    @GetMapping("/list")
    public Result<IPage<AttendanceRecord>> getAttendanceRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceRecord> attendanceRecords = attendanceRecordService.getAttendanceRecords(card_id, status, start_date, end_date, page, size);
        return Result.success(attendanceRecords);
    }

    /**
     * 学生打卡接口：记录学生当前位置、设备和考勤类型，并判断打卡状态。
     */
    @PostMapping("/checkin")
    public Result<AttendanceRecord> createAttendance(
            @RequestParam Long card_id,
            @RequestParam(required = false) Long location_id,
            @RequestParam String actual_location,
            @RequestParam Double actual_latitude,
            @RequestParam Double actual_longitude,
            @RequestParam(required = false) String device_info,
            @RequestParam(required = false) String attendance_type,
            @RequestParam(required = false) String internship_company,
            @RequestParam(required = false) String internship_log,
            @RequestParam(required = false) String internship_log_date) {
        AttendanceRecord attendanceRecord = attendanceRecordService.createAttendance(card_id, location_id, actual_location, actual_latitude, actual_longitude, device_info,
                attendance_type, internship_company, internship_log, internship_log_date);
        return Result.success(attendanceRecord);
    }

    /**
     * 考勤统计接口：按时间范围分页汇总考勤数据，供后台统计页面展示。
     */
    @GetMapping("/statistics")
    public Result<IPage<AttendanceRecord>> getAttendanceStatistics(
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceRecord> attendanceStatistics = attendanceRecordService.getAttendanceStatistics(start_date, end_date, page, size);
        return Result.success(attendanceStatistics);
    }

    /**
     * 考勤概览接口：按时间范围汇总正常、异常等考勤状态数量。
     */
    @GetMapping("/summary")
    public Result<Map<String, Long>> getAttendanceSummary(
            @RequestParam(required = false) String start_date,
            @RequestParam(required = false) String end_date) {
        Map<String, Long> summary = attendanceRecordService.getAttendanceSummary(start_date, end_date);
        return Result.success(summary);
    }

    @GetMapping("/location/{locationId}/records")
    public Result<IPage<AttendanceRecord>> getAttendanceRecordsByLocationId(
            @PathVariable Long locationId,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        IPage<AttendanceRecord> records = attendanceRecordService.getAttendanceRecordsByLocationId(locationId, page, size);
        return Result.success(records);
    }

    /**
     * 实习申请接口：学生提交实习考勤申请，用于变更后续考勤方式。
     */
    @PostMapping("/application/internship")
    public Result<AttendanceApplication> submitInternshipApplication(@RequestBody AttendanceApplication application) {
        return Result.success(attendanceApplicationService.submitInternshipApplication(application));
    }

    /**
     * 请假申请接口：学生提交请假申请，等待教师或管理员审核。
     */
    @PostMapping("/application/leave")
    public Result<AttendanceApplication> submitLeaveApplication(@RequestBody AttendanceApplication application) {
        return Result.success(attendanceApplicationService.submitLeaveApplication(application));
    }

    /**
     * 考勤申报资料上传接口：学生在请假或实习申报富文本中上传图片、PDF、Office文档等佐证资料。
     */
    @PostMapping("/application/material/upload")
    public Result<UploadMaterialResponse> uploadApplicationMaterial(@RequestParam("file") MultipartFile file) {
        return Result.success("上传成功", attendanceApplicationService.uploadMaterial(file));
    }

    /**
     * 我的申请接口：按校园卡分页查询当前学生提交的请假或实习申请。
     */
    @GetMapping("/application/my")
    public Result<IPage<AttendanceApplication>> getMyApplications(
            @RequestParam Long card_id,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(attendanceApplicationService.getApplicationsByCardId(card_id, page, size));
    }

    /**
     * 考勤申请列表接口：按申请类型、状态、审核人等条件分页查询申请记录。
     */
    @GetMapping("/application/list")
    public Result<IPage<AttendanceApplication>> getApplications(
            @RequestParam(required = false) String application_type,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long teacher_id,
            @RequestParam(required = false) String requester_role,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size) {
        return Result.success(attendanceApplicationService.getApplications(application_type, status, teacher_id, requester_role, page, size));
    }

    /**
     * 考勤申请审核接口：教师或管理员审批请假、实习申请并写入审核意见。
     */
    @PostMapping("/application/review")
    public Result<AttendanceApplication> reviewApplication(
            @RequestParam Long application_id,
            @RequestParam String status,
            @RequestParam(required = false) Long reviewer_id,
            @RequestParam(required = false) String reviewer_role,
            @RequestParam(required = false) String review_remark) {
        return Result.success(attendanceApplicationService.reviewApplication(application_id, status, reviewer_id, reviewer_role, review_remark));
    }

    /**
     * 实习返校接口：老师或管理员确认学生结束实习，恢复在校考勤。
     */
    @PostMapping("/application/internship/return")
    public Result<AttendanceApplication> returnInternshipApplication(
            @RequestParam Long application_id,
            @RequestParam(required = false) Long reviewer_id,
            @RequestParam(required = false) String reviewer_role,
            @RequestParam(required = false) String review_remark) {
        return Result.success(attendanceApplicationService.returnInternshipApplication(application_id, reviewer_id, reviewer_role, review_remark));
    }
}
