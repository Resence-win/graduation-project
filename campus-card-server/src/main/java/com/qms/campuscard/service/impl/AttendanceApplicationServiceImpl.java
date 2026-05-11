package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.dto.UploadMaterialResponse;
import com.qms.campuscard.entity.AttendanceApplication;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.mapper.AttendanceApplicationMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.service.AttendanceApplicationService;
import com.qms.campuscard.service.StudentService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class AttendanceApplicationServiceImpl implements AttendanceApplicationService {

    private static final long MAX_MATERIAL_SIZE = 10 * 1024 * 1024L;
    private static final Map<String, String> ALLOWED_MATERIAL_TYPES = new HashMap<>();

    static {
        ALLOWED_MATERIAL_TYPES.put("jpg", "image");
        ALLOWED_MATERIAL_TYPES.put("jpeg", "image");
        ALLOWED_MATERIAL_TYPES.put("png", "image");
        ALLOWED_MATERIAL_TYPES.put("gif", "image");
        ALLOWED_MATERIAL_TYPES.put("webp", "image");
        ALLOWED_MATERIAL_TYPES.put("pdf", "file");
        ALLOWED_MATERIAL_TYPES.put("doc", "file");
        ALLOWED_MATERIAL_TYPES.put("docx", "file");
        ALLOWED_MATERIAL_TYPES.put("xls", "file");
        ALLOWED_MATERIAL_TYPES.put("xlsx", "file");
        ALLOWED_MATERIAL_TYPES.put("ppt", "file");
        ALLOWED_MATERIAL_TYPES.put("pptx", "file");
        ALLOWED_MATERIAL_TYPES.put("txt", "file");
        ALLOWED_MATERIAL_TYPES.put("zip", "file");
        ALLOWED_MATERIAL_TYPES.put("rar", "file");
    }

    @Resource
    private AttendanceApplicationMapper attendanceApplicationMapper;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private StudentService studentService;

    @Value("${file.upload.url-prefix:/upload}")
    private String urlPrefix;

    @Override
    public AttendanceApplication submitInternshipApplication(AttendanceApplication application) {
        Student student = getStudentByCard(application.getCardId());
        studentService.ensureStudentProfileComplete(student);
        if (application.getInternshipCompany() == null || application.getInternshipCompany().trim().isEmpty()) {
            throw new RuntimeException("实习单位不能为空");
        }
        if (application.getStartDate() == null) {
            throw new RuntimeException("实习开始日期不能为空");
        }
        application.setReason(cleanReasonHtml(application.getReason()));
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
        studentService.ensureStudentProfileComplete(student);
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
        application.setReason(cleanReasonHtml(application.getReason()));
        if (!hasReasonContent(application.getReason())) {
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
    public UploadMaterialResponse uploadMaterial(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        if (file.getSize() > MAX_MATERIAL_SIZE) {
            throw new RuntimeException("文件大小不能超过10MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty() || !originalFilename.contains(".")) {
            throw new RuntimeException("文件名不合法");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        String materialType = ALLOWED_MATERIAL_TYPES.get(extension);
        if (materialType == null) {
            throw new RuntimeException("不支持的文件类型");
        }

        try {
            String newFilename = UUID.randomUUID() + "." + extension;
            String relativeDir = "attendance-application";
            String absoluteUploadPath = System.getProperty("user.dir") + File.separator + "upload" + File.separator + relativeDir;
            File uploadDir = new File(absoluteUploadPath);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new RuntimeException("上传目录创建失败");
            }

            File destFile = new File(uploadDir, newFilename);
            file.transferTo(destFile);
            String normalizedUrlPrefix = urlPrefix == null ? "/upload" : urlPrefix.trim();
            String url = normalizedUrlPrefix + "/" + relativeDir + "/" + newFilename;
            return new UploadMaterialResponse(url, originalFilename, materialType);
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败", e);
        }
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
        refreshLeaveStatusesByApplications(result.getRecords());
        fillApplicationExtraInfo(result);
        return result;
    }

    @Override
    public IPage<AttendanceApplication> getApplications(String applicationType, String status, Long teacherId, String requesterRole, Integer page, Integer size) {
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
        if ("teacher".equalsIgnoreCase(requesterRole)) {
            if (teacherId == null) {
                queryWrapper.eq("student_id", -1L);
            } else {
                queryWrapper.inSql("student_id", "SELECT id FROM student WHERE teacher_id = " + teacherId + " AND is_deleted = 0");
            }
        } else if (teacherId != null) {
            queryWrapper.inSql("student_id", "SELECT id FROM student WHERE teacher_id = " + teacherId + " AND is_deleted = 0");
        }
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        IPage<AttendanceApplication> result = attendanceApplicationMapper.selectPage(new Page<>(page, size), queryWrapper);
        refreshLeaveStatusesByApplications(result.getRecords());
        fillApplicationExtraInfo(result);
        return result;
    }

    @Override
    public AttendanceApplication reviewApplication(Long applicationId, String status, Long reviewerId, String reviewerRole, String reviewRemark) {
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
        ensureReviewerPermission(application, reviewerId, reviewerRole);

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

    @Override
    public AttendanceApplication returnInternshipApplication(Long applicationId, Long reviewerId, String reviewerRole, String reviewRemark) {
        if (applicationId == null) {
            throw new RuntimeException("申请ID不能为空");
        }
        AttendanceApplication application = attendanceApplicationMapper.selectById(applicationId);
        if (application == null || application.getIsDeleted() == null || application.getIsDeleted() != 0) {
            throw new RuntimeException("考勤申报不存在");
        }
        if (!"INTERNSHIP".equals(application.getApplicationType())) {
            throw new RuntimeException("仅支持结束实习申报");
        }
        if (!"APPROVED".equals(application.getStatus())) {
            throw new RuntimeException("仅已通过的实习申报可结束实习");
        }
        if (isInternshipReturned(application)) {
            throw new RuntimeException("该实习申报已结束，请勿重复操作");
        }
        ensureReviewerPermission(application, reviewerId, reviewerRole);

        Student student = studentMapper.selectById(application.getStudentId());
        if (student == null || student.getIsDeleted() == null || student.getIsDeleted() != 0) {
            throw new RuntimeException("申请学生不存在");
        }

        student.setAttendanceMode("CAMPUS");
        student.setAttendanceStatus("ON_CAMPUS");
        student.setInternshipCompany(null);
        student.setUpdateTime(LocalDateTime.now());
        studentMapper.updateById(student);

        String returnRemark = "已结束实习/已返校";
        if (reviewRemark != null && !reviewRemark.trim().isEmpty()) {
            returnRemark += "：" + reviewRemark.trim();
        }
        if (application.getReviewRemark() != null && !application.getReviewRemark().trim().isEmpty()) {
            application.setReviewRemark(application.getReviewRemark() + "；" + returnRemark);
        } else {
            application.setReviewRemark(returnRemark);
        }
        application.setReviewerId(reviewerId);
        application.setReviewTime(LocalDateTime.now());
        application.setUpdateTime(LocalDateTime.now());
        attendanceApplicationMapper.updateById(application);

        return application;
    }

    @Override
    public void refreshLeaveStatusByStudentId(Long studentId) {
        if (studentId == null) {
            return;
        }
        Student student = studentMapper.selectById(studentId);
        refreshLeaveStatus(student);
    }

    @Override
    public void refreshAllLeaveStatuses() {
        QueryWrapper<Student> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attendance_status", "LEAVE");
        queryWrapper.eq("is_deleted", 0);
        for (Student student : studentMapper.selectList(queryWrapper)) {
            refreshLeaveStatus(student);
        }
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
        long existingCount = attendanceApplicationMapper.selectList(queryWrapper).stream()
                .filter(existing -> !isInternshipReturned(existing))
                .count();
        if (existingCount > 0) {
            throw new RuntimeException("已有待审核或已通过的实习申报，请勿重复提交");
        }
    }

    private void ensureReviewerPermission(AttendanceApplication application, Long reviewerId, String reviewerRole) {
        if ("teacher".equalsIgnoreCase(reviewerRole)) {
            if (reviewerId == null) {
                throw new RuntimeException("审核老师不能为空");
            }
            Student applicant = studentMapper.selectById(application.getStudentId());
            if (applicant == null || applicant.getIsDeleted() == null || applicant.getIsDeleted() != 0) {
                throw new RuntimeException("申请学生不存在");
            }
            if (applicant.getTeacherId() == null) {
                throw new RuntimeException("该学生未绑定负责老师，暂不能线上审批");
            }
            if (!reviewerId.equals(applicant.getTeacherId())) {
                throw new RuntimeException("当前老师无权审批该学生的申报");
            }
        }
    }

    private boolean isInternshipReturned(AttendanceApplication application) {
        return application.getReviewRemark() != null
                && (application.getReviewRemark().contains("已结束实习")
                || application.getReviewRemark().contains("已返校"));
    }

    private void refreshLeaveStatusesByApplications(List<AttendanceApplication> applications) {
        if (applications == null || applications.isEmpty()) {
            return;
        }
        Set<Long> studentIds = new HashSet<>();
        for (AttendanceApplication application : applications) {
            if (application.getStudentId() != null) {
                studentIds.add(application.getStudentId());
            }
        }
        for (Long studentId : studentIds) {
            refreshLeaveStatusByStudentId(studentId);
        }
    }

    private void refreshLeaveStatus(Student student) {
        if (student == null || student.getIsDeleted() == null || student.getIsDeleted() != 0) {
            return;
        }
        AttendanceApplication activeLeave = getActiveApprovedLeaveApplicationByStudentId(student.getId(), LocalDate.now());
        String currentStatus = normalizeAttendanceStatus(student.getAttendanceStatus());
        String targetStatus = activeLeave != null
                ? "LEAVE"
                : ("LEAVE".equals(currentStatus) ? getDefaultAttendanceStatus(student) : currentStatus);
        if (!Objects.equals(currentStatus, targetStatus)) {
            student.setAttendanceStatus(targetStatus);
            student.setUpdateTime(LocalDateTime.now());
            studentMapper.updateById(student);
        }
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

    private String getDefaultAttendanceStatus(Student student) {
        return "INTERNSHIP".equals(normalizeAttendanceMode(student.getAttendanceMode())) ? "INTERNSHIP" : "ON_CAMPUS";
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

    private String cleanReasonHtml(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return reason;
        }
        Safelist safelist = Safelist.relaxed()
                .preserveRelativeLinks(true)
                .addTags("span")
                .addAttributes(":all", "class")
                .addAttributes("div", "data-material-list")
                .addAttributes("a", "href", "title", "target", "rel", "data-material-card", "data-material-type", "data-material-name", "data-material-url")
                .addAttributes("img", "src", "alt", "title", "width", "height");
        Document.OutputSettings outputSettings = new Document.OutputSettings().prettyPrint(false);
        return Jsoup.clean(reason, "https://campus-card.local", safelist, outputSettings);
    }

    private boolean hasReasonContent(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            return false;
        }
        if (reason.matches("(?is).*<(img|a)\\b.*")) {
            return true;
        }
        return !Jsoup.parse(reason).text().trim().isEmpty();
    }
}
