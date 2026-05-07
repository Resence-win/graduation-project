package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AttendanceApplication;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.mapper.AttendanceApplicationMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.service.AttendanceApplicationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.Arrays;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AttendanceApplicationServiceImpl implements AttendanceApplicationService {

    @Resource
    private AttendanceApplicationMapper attendanceApplicationMapper;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private StudentMapper studentMapper;

    @Override
    public AttendanceApplication submitInternshipApplication(AttendanceApplication application) {
        Student student = getStudentByCard(application.getCardId());
        if (application.getInternshipCompany() == null || application.getInternshipCompany().trim().isEmpty()) {
            throw new RuntimeException("实习单位不能为空");
        }
        if (application.getStartDate() == null) {
            throw new RuntimeException("实习开始日期不能为空");
        }
        application.setStudentId(student.getId());
        application.setApplicationType("INTERNSHIP");
        ensureNoOverlappingInternshipApplication(application);
        application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        application.setIsDeleted(0);
        attendanceApplicationMapper.insert(application);

        return application;
    }

    @Override
    public AttendanceApplication submitLeaveApplication(AttendanceApplication application) {
        Student student = getStudentByCard(application.getCardId());
        if (application.getStartDate() == null || application.getEndDate() == null) {
            throw new RuntimeException("请假开始日期和结束日期不能为空");
        }
        if (application.getEndDate().isBefore(application.getStartDate())) {
            throw new RuntimeException("请假结束日期不能早于开始日期");
        }
        long leaveDays = ChronoUnit.DAYS.between(application.getStartDate(), application.getEndDate()) + 1;
        if (leaveDays < 1 || leaveDays > 3) {
            throw new RuntimeException("线上请假仅支持1-3天，超过3天请走线下流程办理");
        }
        if (application.getReason() == null || application.getReason().trim().isEmpty()) {
            throw new RuntimeException("请假原因不能为空");
        }

        application.setStudentId(student.getId());
        application.setApplicationType("LEAVE");
        ensureNoOverlappingLeaveApplication(application);
        application.setStatus("PENDING");
        application.setCreateTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        application.setIsDeleted(0);
        attendanceApplicationMapper.insert(application);

        return application;
    }

    @Override
    public IPage<AttendanceApplication> getApplicationsByCardId(Long cardId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        QueryWrapper<AttendanceApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        IPage<AttendanceApplication> result = attendanceApplicationMapper.selectPage(new Page<>(page, size), queryWrapper);
        fillApplicationExtraInfo(result);
        return result;
    }

    @Override
    public IPage<AttendanceApplication> getApplications(String applicationType, String status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        QueryWrapper<AttendanceApplication> queryWrapper = new QueryWrapper<>();
        if (applicationType != null && !applicationType.trim().isEmpty()) {
            queryWrapper.eq("application_type", applicationType.trim().toUpperCase());
        }
        if (status != null && !status.trim().isEmpty()) {
            queryWrapper.eq("status", status.trim().toUpperCase());
        }
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        IPage<AttendanceApplication> result = attendanceApplicationMapper.selectPage(new Page<>(page, size), queryWrapper);
        fillApplicationExtraInfo(result);
        return result;
    }

    @Override
    public AttendanceApplication reviewApplication(Long applicationId, String status, Long reviewerId, String reviewRemark) {
        if (applicationId == null) {
            throw new RuntimeException("申请ID不能为空");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new RuntimeException("审核状态不能为空");
        }
        String reviewStatus = status.trim().toUpperCase();
        if (!"APPROVED".equals(reviewStatus) && !"REJECTED".equals(reviewStatus)) {
            throw new RuntimeException("审核状态仅支持通过或拒绝");
        }

        AttendanceApplication application = attendanceApplicationMapper.selectById(applicationId);
        if (application == null || application.getIsDeleted() == null || application.getIsDeleted() != 0) {
            throw new RuntimeException("考勤申报不存在");
        }
        if (!"LEAVE".equals(application.getApplicationType()) && !"INTERNSHIP".equals(application.getApplicationType())) {
            throw new RuntimeException("仅支持审核请假或实习申报");
        }
        if (!"PENDING".equals(application.getStatus())) {
            throw new RuntimeException("该申请已审核，请勿重复操作");
        }

        application.setStatus(reviewStatus);
        application.setReviewerId(reviewerId);
        application.setReviewRemark(reviewRemark);
        application.setReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        attendanceApplicationMapper.updateById(application);

        if ("APPROVED".equals(reviewStatus) && "LEAVE".equals(application.getApplicationType())) {
            Student student = studentMapper.selectById(application.getStudentId());
            LocalDate today = LocalDate.now();
            if (student != null && !today.isBefore(application.getStartDate()) && !today.isAfter(application.getEndDate())) {
                student.setAttendanceStatus("LEAVE");
                student.setUpdateTime(LocalDateTime.now());
                studentMapper.updateById(student);
            }
        } else if ("APPROVED".equals(reviewStatus) && "INTERNSHIP".equals(application.getApplicationType())) {
            Student student = studentMapper.selectById(application.getStudentId());
            if (student != null) {
                student.setAttendanceMode("INTERNSHIP");
                student.setAttendanceStatus("INTERNSHIP");
                student.setInternshipCompany(application.getInternshipCompany());
                student.setUpdateTime(LocalDateTime.now());
                studentMapper.updateById(student);
            }
        }

        return application;
    }

    private Student getStudentByCard(Long cardId) {
        CampusCard campusCard = campusCardMapper.selectById(cardId);
        if (campusCard == null || campusCard.getIsDeleted() == null || campusCard.getIsDeleted() != 0) {
            throw new RuntimeException("校园卡不存在");
        }
        if (!"student".equalsIgnoreCase(campusCard.getUserType())) {
            throw new RuntimeException("仅学生支持考勤申报");
        }
        Student student = studentMapper.selectById(campusCard.getUserId());
        if (student == null || student.getIsDeleted() == null || student.getIsDeleted() != 0) {
            throw new RuntimeException("学生不存在");
        }
        return student;
    }

    private void ensureNoOverlappingLeaveApplication(AttendanceApplication application) {
        QueryWrapper<AttendanceApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", application.getCardId());
        queryWrapper.eq("application_type", "LEAVE");
        queryWrapper.in("status", Arrays.asList("PENDING", "APPROVED"));
        queryWrapper.le("start_date", application.getEndDate());
        queryWrapper.ge("end_date", application.getStartDate());
        queryWrapper.eq("is_deleted", 0);
        if (attendanceApplicationMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("已有待审核或已通过的请假申请，请勿重复提交");
        }
    }

    private void ensureNoOverlappingInternshipApplication(AttendanceApplication application) {
        QueryWrapper<AttendanceApplication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", application.getCardId());
        queryWrapper.eq("application_type", "INTERNSHIP");
        queryWrapper.in("status", Arrays.asList("PENDING", "APPROVED"));
        queryWrapper.eq("is_deleted", 0);
        if (attendanceApplicationMapper.selectCount(queryWrapper) > 0) {
            throw new RuntimeException("已有待审核或已通过的实习申报，请勿重复提交");
        }
    }

    private void fillApplicationExtraInfo(IPage<AttendanceApplication> result) {
        for (AttendanceApplication application : result.getRecords()) {
            if (application.getStudentId() != null) {
                Student student = studentMapper.selectById(application.getStudentId());
                if (student != null) {
                    application.setStudentName(student.getName());
                    application.setStudentNo(student.getStudentNo());
                }
            }
            if (application.getCardId() != null) {
                CampusCard campusCard = campusCardMapper.selectById(application.getCardId());
                if (campusCard != null) {
                    application.setCardNo(campusCard.getCardNo());
                }
            }
        }
    }
}
