package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.entity.AttendanceRecord;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.AttendanceApplication;
import com.qms.campuscard.mapper.AttendanceApplicationMapper;
import com.qms.campuscard.mapper.AttendanceRecordMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.service.AttendanceLocationService;
import com.qms.campuscard.service.AttendanceRecordService;
import com.qms.campuscard.service.StudentService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    
    @Resource
    private AttendanceLocationService attendanceLocationService;
    
    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private AttendanceApplicationMapper attendanceApplicationMapper;

    @Resource
    private StudentService studentService;

    @Override
    public IPage<AttendanceRecord> getAttendanceRecords(Long cardId, String status, String startDate, String endDate, Integer page, Integer size) {
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
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status);
        }
        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("record_time");
        IPage<AttendanceRecord> result = attendanceRecordMapper.selectPage(pageParam, queryWrapper);
        
        fillRecordExtraInfo(result);
        
        return result;
    }

    @Override
    public AttendanceRecord createAttendance(Long cardId, Long locationId, String actualLocation, Double actualLatitude, Double actualLongitude, String deviceInfo,
                                             String attendanceType, String internshipCompany, String internshipLog, String internshipLogDate) {
        CampusCard campusCard = campusCardMapper.selectById(cardId);
        if (campusCard == null || campusCard.getIsDeleted() == null || campusCard.getIsDeleted() != 0) {
            throw new RuntimeException("校园卡不存在");
        }
        if (campusCard.getStatus() == null || campusCard.getStatus() != 1) {
            throw new RuntimeException("校园卡状态异常，无法打卡");
        }
        studentService.ensureStudentProfileCompleteByCard(campusCard);

        String studentAttendanceMode = "CAMPUS";
        String studentAttendanceStatus = "ON_CAMPUS";
        String studentInternshipCompany = null;
        AttendanceApplication activeLeaveApplication = null;
        if ("student".equalsIgnoreCase(campusCard.getUserType())) {
            Student student = studentMapper.selectById(campusCard.getUserId());
            if (student != null && student.getAttendanceMode() != null && !student.getAttendanceMode().trim().isEmpty()) {
                studentAttendanceMode = student.getAttendanceMode().trim().toUpperCase();
            }
            if (student != null && student.getAttendanceStatus() != null && !student.getAttendanceStatus().trim().isEmpty()) {
                studentAttendanceStatus = student.getAttendanceStatus().trim().toUpperCase();
            }
            if (student != null) {
                studentInternshipCompany = student.getInternshipCompany();
            }
            activeLeaveApplication = getActiveApprovedLeaveApplication(campusCard.getId(), LocalDate.now());
            if (activeLeaveApplication != null) {
                studentAttendanceStatus = "LEAVE";
                if (student != null && !"LEAVE".equals(student.getAttendanceStatus())) {
                    student.setAttendanceStatus("LEAVE");
                    student.setUpdateTime(LocalDateTime.now());
                    studentMapper.updateById(student);
                }
            } else if ("LEAVE".equals(studentAttendanceStatus)) {
                studentAttendanceStatus = "INTERNSHIP".equals(studentAttendanceMode) ? "INTERNSHIP" : "ON_CAMPUS";
                if (student != null) {
                    student.setAttendanceStatus(studentAttendanceStatus);
                    student.setUpdateTime(LocalDateTime.now());
                    studentMapper.updateById(student);
                }
            }
        }

        if ("LEAVE".equals(studentAttendanceStatus)) {
            attendanceType = "LEAVE";
        } else if (attendanceType == null || attendanceType.trim().isEmpty()) {
            attendanceType = "INTERNSHIP".equals(studentAttendanceMode) ? "OFF_CAMPUS_LOCATION" : "CAMPUS_LOCATION";
        }
        attendanceType = attendanceType.trim().toUpperCase();

        if (!"LEAVE".equals(attendanceType)) {
            if ("CAMPUS".equals(studentAttendanceMode) && !"CAMPUS_LOCATION".equals(attendanceType)) {
                throw new RuntimeException("在校学生仅支持校内位置打卡");
            }
            if ("INTERNSHIP".equals(studentAttendanceMode)
                    && !"OFF_CAMPUS_LOCATION".equals(attendanceType)
                    && !"INTERNSHIP_LOG".equals(attendanceType)) {
                throw new RuntimeException("实习学生仅支持校外位置打卡或实习日志上报");
            }
        }
        
        AttendanceRecord attendanceRecord = new AttendanceRecord();
        attendanceRecord.setCardId(cardId);
        attendanceRecord.setLocationId(locationId);
        attendanceRecord.setActualLocation(actualLocation);
        attendanceRecord.setActualLatitude(actualLatitude);
        attendanceRecord.setActualLongitude(actualLongitude);
        attendanceRecord.setDeviceInfo(deviceInfo);
        attendanceRecord.setAttendanceType(attendanceType);
        attendanceRecord.setInternshipCompany(internshipCompany != null && !internshipCompany.trim().isEmpty() ? internshipCompany : studentInternshipCompany);
        attendanceRecord.setInternshipLog(internshipLog);
        if (internshipLogDate != null && !internshipLogDate.trim().isEmpty()) {
            attendanceRecord.setInternshipLogDate(LocalDate.parse(internshipLogDate));
        }
        if (activeLeaveApplication != null) {
            attendanceRecord.setLeaveApplicationId(activeLeaveApplication.getId());
            attendanceRecord.setLeaveReason(activeLeaveApplication.getReason());
            attendanceRecord.setLeaveStartDate(activeLeaveApplication.getStartDate());
            attendanceRecord.setLeaveEndDate(activeLeaveApplication.getEndDate());
        }
        attendanceRecord.setRecordTime(LocalDateTime.now());

        String status;
        if ("LEAVE".equals(studentAttendanceStatus)) {
            if (activeLeaveApplication == null) {
                throw new RuntimeException("未找到已通过且当前有效的请假申请");
            }
            if (locationId != null) {
                AttendanceLocation location = attendanceLocationService.getLocationById(locationId);
                validateActiveLocation(location);
            }
            LocalDate today = LocalDate.now();
            QueryWrapper<AttendanceRecord> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("card_id", cardId);
            checkWrapper.eq("attendance_type", "LEAVE");
            checkWrapper.eq("leave_application_id", activeLeaveApplication.getId());
            checkWrapper.ge("record_time", today.atStartOfDay());
            checkWrapper.lt("record_time", today.plusDays(1).atStartOfDay());
            checkWrapper.eq("is_deleted", 0);
            if (attendanceRecordMapper.selectCount(checkWrapper) > 0) {
                throw new RuntimeException("今日已完成请假考勤记录，请勿重复提交");
            }
            status = "正常";
        } else if ("INTERNSHIP".equals(studentAttendanceMode)) {
            LocalDate today = LocalDate.now();
            QueryWrapper<AttendanceRecord> checkWrapper = new QueryWrapper<>();
            checkWrapper.eq("card_id", cardId);
            checkWrapper.eq("attendance_type", attendanceType);
            checkWrapper.ge("record_time", today.atStartOfDay());
            checkWrapper.lt("record_time", today.plusDays(1).atStartOfDay());
            checkWrapper.eq("is_deleted", 0);
            if (attendanceRecordMapper.selectCount(checkWrapper) > 0) {
                throw new RuntimeException("今日已完成实习打卡，请勿重复提交");
            }

            if (attendanceRecord.getInternshipCompany() == null || attendanceRecord.getInternshipCompany().trim().isEmpty()) {
                throw new RuntimeException("实习单位不能为空");
            }
            if ("INTERNSHIP_LOG".equals(attendanceType) && (internshipLog == null || internshipLog.trim().isEmpty())) {
                throw new RuntimeException("实习日志不能为空");
            }
            if ("OFF_CAMPUS_LOCATION".equals(attendanceType) && (actualLocation == null || actualLocation.trim().isEmpty())) {
                throw new RuntimeException("校外位置打卡必须提交当前位置");
            }
            status = "正常";
        } else {
            if (locationId == null) {
                throw new RuntimeException("在校考勤必须选择打卡位置");
            }

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

            AttendanceLocation location = attendanceLocationService.getLocationById(locationId);
            if (location == null) {
                throw new RuntimeException("打卡位置不存在");
            }

            validateActiveLocation(location);
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(location.getStartTime())) {
                status = "正常";
            } else if (now.isBefore(location.getStartTime().plusMinutes(30))) {
                status = "迟到";
            } else if (now.isBefore(location.getEndTime())) {
                status = "正常";
            } else {
                status = "缺勤";
            }
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
        
        fillRecordExtraInfo(result);
        
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

        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            // 构造正确的时间戳格式
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("record_time");
        IPage<AttendanceRecord> result = attendanceRecordMapper.selectPage(pageParam, queryWrapper);
        
        fillRecordExtraInfo(result);
        
        return result;
    }

    @Override
    public Map<String, Long> getAttendanceSummary(String startDate, String endDate) {
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }

        queryWrapper.eq("is_deleted", 0);

        long total = attendanceRecordMapper.selectCount(queryWrapper);

        Map<String, Long> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("normal", countByStatus(startDate, endDate, "正常"));
        summary.put("late", countByStatus(startDate, endDate, "迟到"));
        summary.put("early", countByStatus(startDate, endDate, "早退"));
        summary.put("absent", countByStatus(startDate, endDate, "缺勤"));

        return summary;
    }

    private Long countByStatus(String startDate, String endDate, String status) {
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }

        queryWrapper.eq("status", status);
        queryWrapper.eq("is_deleted", 0);

        return attendanceRecordMapper.selectCount(queryWrapper);
    }

    private void fillRecordExtraInfo(IPage<AttendanceRecord> result) {
        for (AttendanceRecord record : result.getRecords()) {
            if (record.getLocationId() != null) {
                AttendanceLocation location = attendanceLocationService.getLocationById(record.getLocationId());
                if (location != null) {
                    record.setLocationName(location.getLocationName());
                }
            }
            if (record.getCardId() != null) {
                QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
                cardQuery.eq("id", record.getCardId());
                cardQuery.eq("is_deleted", 0);
                CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
                if (campusCard != null) {
                    record.setCardNo(campusCard.getCardNo());
                    if ("student".equalsIgnoreCase(campusCard.getUserType())) {
                        Student student = studentMapper.selectById(campusCard.getUserId());
                        record.setAttendanceMode(student != null && student.getAttendanceMode() != null ? student.getAttendanceMode() : "CAMPUS");
                    }
                }
            }
        }
    }

    private AttendanceApplication getActiveApprovedLeaveApplication(Long cardId, LocalDate date) {
        QueryWrapper<AttendanceApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("application_type", "LEAVE");
        queryWrapper.eq("status", "APPROVED");
        queryWrapper.le("start_date", date);
        queryWrapper.ge("end_date", date);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        return attendanceApplicationMapper.selectOne(queryWrapper);
    }

    private void validateActiveLocation(AttendanceLocation location) {
        if (location == null) {
            throw new RuntimeException("打卡位置不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (location.getStatus() == null || location.getStatus() != 1
                || location.getStartTime() == null || location.getEndTime() == null
                || now.isBefore(location.getStartTime())
                || now.isAfter(location.getEndTime())) {
            throw new RuntimeException("打卡位置不在有效考勤时间内");
        }
    }
}
