package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.dto.UploadMaterialResponse;
import com.qms.campuscard.entity.AttendanceApplication;
import org.springframework.web.multipart.MultipartFile;

public interface AttendanceApplicationService {

    AttendanceApplication submitInternshipApplication(AttendanceApplication application);

    AttendanceApplication submitLeaveApplication(AttendanceApplication application);

    IPage<AttendanceApplication> getApplicationsByCardId(Long cardId, Integer page, Integer size);

    IPage<AttendanceApplication> getApplications(String applicationType, String status, Long teacherId, String requesterRole, Integer page, Integer size);

    AttendanceApplication reviewApplication(Long applicationId, String status, Long reviewerId, String reviewerRole, String reviewRemark);

    AttendanceApplication returnInternshipApplication(Long applicationId, Long reviewerId, String reviewerRole, String reviewRemark);

    void refreshLeaveStatusByStudentId(Long studentId);

    void refreshAllLeaveStatuses();

    UploadMaterialResponse uploadMaterial(MultipartFile file);
}
