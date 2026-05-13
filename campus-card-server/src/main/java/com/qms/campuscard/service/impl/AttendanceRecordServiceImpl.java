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
import com.qms.campuscard.mapper.AttendanceLocationMapper;
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
import java.util.List;
import java.util.Map;

@Service
public class AttendanceRecordServiceImpl implements AttendanceRecordService {

    private static final long NORMAL_CHECKIN_GRACE_MINUTES = 15;
    private static final String STATISTIC_LOCATION_EXPIRED_MESSAGE = "该考勤点已过期超过1个月，暂不支持统计查看";

    @Resource
    private AttendanceRecordMapper attendanceRecordMapper;
    
    @Resource
    private AttendanceLocationService attendanceLocationService;

    @Resource
    private AttendanceLocationMapper attendanceLocationMapper;
    
    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private AttendanceApplicationMapper attendanceApplicationMapper;

    @Resource
    private StudentService studentService;

    @Override
    public IPage<AttendanceRecord> getAttendanceRecords(Long cardId, Long locationId, String status, String startDate, String endDate, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        AttendanceLocation statisticLocation = getStatisticLocationWithinRange(locationId);
        generateMissingAttendanceRecordsIfEnded(statisticLocation);

        Page<AttendanceRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (locationId != null) {
            queryWrapper.eq("location_id", locationId);
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
        Student student = null;
        AttendanceApplication activeLeaveApplication = null;
        if ("student".equalsIgnoreCase(campusCard.getUserType())) {
            student = studentMapper.selectById(campusCard.getUserId());
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
                validateStudentLocationPermission(campusCard, student, location);
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
            validateStudentLocationPermission(campusCard, student, location);
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime normalDeadline = location.getStartTime().plusMinutes(NORMAL_CHECKIN_GRACE_MINUTES);
            if (!now.isAfter(normalDeadline)) {
                status = "正常";
            } else {
                status = "迟到";
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

        AttendanceLocation statisticLocation = getStatisticLocationWithinRange(locationId);
        generateMissingAttendanceRecordsIfEnded(statisticLocation);

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
    public void generateMissingAttendanceRecords() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = LocalDate.now();
        QueryWrapper<AttendanceLocation> locationQuery = new QueryWrapper<>();
        locationQuery.eq("status", 1);
        locationQuery.ge("end_time", today.atStartOfDay());
        locationQuery.le("end_time", now);
        locationQuery.eq("is_deleted", 0);
        List<AttendanceLocation> locations = attendanceLocationMapper.selectList(locationQuery);
        for (AttendanceLocation location : locations) {
            generateMissingAttendanceRecords(location);
        }
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
    public Map<String, Long> getAttendanceSummary(String startDate, String endDate, Long locationId) {
        AttendanceLocation statisticLocation = getStatisticLocationWithinRange(locationId);
        generateMissingAttendanceRecordsIfEnded(statisticLocation);

        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }
        if (locationId != null) {
            queryWrapper.eq("location_id", locationId);
        }

        queryWrapper.eq("is_deleted", 0);

        long total = attendanceRecordMapper.selectCount(queryWrapper);
        long normal = countByStatus(startDate, endDate, locationId, "正常");
        long late = countByStatus(startDate, endDate, locationId, "迟到");
        long early = countByStatus(startDate, endDate, locationId, "早退");
        long absent = countByStatus(startDate, endDate, locationId, "缺勤");
        long actual = normal + late + early;
        long expected = statisticLocation == null ? total : countExpectedAttendance(statisticLocation);

        Map<String, Long> summary = new HashMap<>();
        summary.put("total", total);
        summary.put("expected", expected);
        summary.put("actual", actual);
        summary.put("normal", normal);
        summary.put("late", late);
        summary.put("early", early);
        summary.put("absent", absent);

        return summary;
    }

    private Long countByStatus(String startDate, String endDate, Long locationId, String status) {
        QueryWrapper<AttendanceRecord> queryWrapper = new QueryWrapper<>();

        if (startDate != null && !startDate.trim().isEmpty()) {
            queryWrapper.ge("record_time", startDate);
        }
        if (endDate != null && !endDate.trim().isEmpty()) {
            queryWrapper.le("record_time", endDate + "T23:59:59");
        }
        if (locationId != null) {
            queryWrapper.eq("location_id", locationId);
        }

        queryWrapper.eq("status", status);
        queryWrapper.eq("is_deleted", 0);

        return attendanceRecordMapper.selectCount(queryWrapper);
    }

    private AttendanceLocation getStatisticLocationWithinRange(Long locationId) {
        if (locationId == null) {
            return null;
        }
        AttendanceLocation location = attendanceLocationService.getLocationById(locationId);
        if (location == null) {
            throw new RuntimeException("打卡位置不存在");
        }
        if (location.getEndTime() == null) {
            throw new RuntimeException("打卡位置未配置结束时间，暂不支持统计查看");
        }
        if (location.getEndTime().isBefore(LocalDateTime.now().minusMonths(1))) {
            throw new RuntimeException(STATISTIC_LOCATION_EXPIRED_MESSAGE);
        }
        return location;
    }

    private void generateMissingAttendanceRecordsIfEnded(AttendanceLocation location) {
        if (location != null && location.getEndTime() != null && !location.getEndTime().isAfter(LocalDateTime.now())) {
            generateMissingAttendanceRecords(location);
        }
    }

    private long countExpectedAttendance(AttendanceLocation location) {
        if (location == null || location.getTeacherId() == null || location.getEndTime() == null) {
            return 0L;
        }
        LocalDate attendanceDate = location.getEndTime().toLocalDate();
        QueryWrapper<Student> studentQuery = new QueryWrapper<>();
        studentQuery.eq("teacher_id", location.getTeacherId());
        studentQuery.eq("is_deleted", 0);
        List<Student> students = studentMapper.selectList(studentQuery);
        long count = 0L;
        for (Student student : students) {
            if (!shouldGenerateAbsentRecord(student, attendanceDate)) {
                continue;
            }
            if (getActiveStudentCard(student.getId()) != null) {
                count++;
            }
        }
        return count;
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

    private void generateMissingAttendanceRecords(AttendanceLocation location) {
        if (location == null || location.getTeacherId() == null || location.getEndTime() == null) {
            return;
        }
        LocalDate attendanceDate = location.getEndTime().toLocalDate();
        QueryWrapper<Student> studentQuery = new QueryWrapper<>();
        studentQuery.eq("teacher_id", location.getTeacherId());
        studentQuery.eq("is_deleted", 0);
        List<Student> students = studentMapper.selectList(studentQuery);
        for (Student student : students) {
            if (!shouldGenerateAbsentRecord(student, attendanceDate)) {
                continue;
            }
            CampusCard campusCard = getActiveStudentCard(student.getId());
            if (campusCard == null) {
                continue;
            }
            try {
                studentService.ensureStudentProfileComplete(student);
            } catch (RuntimeException ignored) {
                continue;
            }
            if (hasLocationRecord(campusCard.getId(), location.getId(), attendanceDate)) {
                continue;
            }
            AttendanceRecord absentRecord = new AttendanceRecord();
            absentRecord.setCardId(campusCard.getId());
            absentRecord.setLocationId(location.getId());
            absentRecord.setStatus("缺勤");
            absentRecord.setActualLocation("未打卡");
            absentRecord.setAttendanceType("CAMPUS_LOCATION");
            absentRecord.setRecordTime(location.getEndTime());
            absentRecord.setIsDeleted(0);
            attendanceRecordMapper.insert(absentRecord);
        }
    }

    private boolean shouldGenerateAbsentRecord(Student student, LocalDate attendanceDate) {
        if (student == null || student.getId() == null) {
            return false;
        }
        String attendanceMode = normalizeAttendanceMode(student.getAttendanceMode());
        String attendanceStatus = normalizeAttendanceStatus(student.getAttendanceStatus());
        if (!"CAMPUS".equals(attendanceMode) || "INTERNSHIP".equals(attendanceStatus) || "LEAVE".equals(attendanceStatus)) {
            return false;
        }
        return getActiveApprovedLeaveApplicationByStudentId(student.getId(), attendanceDate) == null;
    }

    private CampusCard getActiveStudentCard(Long studentId) {
        QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
        cardQuery.eq("user_id", studentId);
        cardQuery.eq("user_type", "student");
        cardQuery.eq("status", 1);
        cardQuery.eq("is_deleted", 0);
        cardQuery.last("LIMIT 1");
        return campusCardMapper.selectOne(cardQuery);
    }

    private boolean hasLocationRecord(Long cardId, Long locationId, LocalDate attendanceDate) {
        QueryWrapper<AttendanceRecord> recordQuery = new QueryWrapper<>();
        recordQuery.eq("card_id", cardId);
        recordQuery.eq("location_id", locationId);
        recordQuery.ge("record_time", attendanceDate.atStartOfDay());
        recordQuery.lt("record_time", attendanceDate.plusDays(1).atStartOfDay());
        recordQuery.eq("is_deleted", 0);
        return attendanceRecordMapper.selectCount(recordQuery) > 0;
    }

    private AttendanceApplication getActiveApprovedLeaveApplicationByStudentId(Long studentId, LocalDate date) {
        QueryWrapper<AttendanceApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("student_id", studentId);
        queryWrapper.eq("application_type", "LEAVE");
        queryWrapper.eq("status", "APPROVED");
        queryWrapper.le("start_date", date);
        queryWrapper.ge("end_date", date);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        queryWrapper.last("LIMIT 1");
        return attendanceApplicationMapper.selectOne(queryWrapper);
    }

    private void validateStudentLocationPermission(CampusCard campusCard, Student student, AttendanceLocation location) {
        if (campusCard == null || !"student".equalsIgnoreCase(campusCard.getUserType())) {
            return;
        }
        if (student == null || student.getTeacherId() == null) {
            throw new RuntimeException("当前学生未绑定负责老师，暂不能打卡");
        }
        if (location.getTeacherId() == null || !student.getTeacherId().equals(location.getTeacherId())) {
            throw new RuntimeException("当前打卡位置不属于负责老师发布，无法打卡");
        }
    }

    private String normalizeAttendanceMode(String attendanceMode) {
        if (attendanceMode == null || attendanceMode.trim().isEmpty()) {
            return "CAMPUS";
        }
        return attendanceMode.trim().toUpperCase();
    }

    private String normalizeAttendanceStatus(String attendanceStatus) {
        if (attendanceStatus == null || attendanceStatus.trim().isEmpty()) {
            return "ON_CAMPUS";
        }
        return attendanceStatus.trim().toUpperCase();
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
