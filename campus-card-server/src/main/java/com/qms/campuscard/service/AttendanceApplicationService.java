package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AttendanceApplication;

public interface AttendanceApplicationService {

    AttendanceApplication submitInternshipApplication(AttendanceApplication application);

    AttendanceApplication submitLeaveApplication(AttendanceApplication application);

    IPage<AttendanceApplication> getApplicationsByCardId(Long cardId, Integer page, Integer size);

    IPage<AttendanceApplication> getApplications(String applicationType, String status, Integer page, Integer size);

    AttendanceApplication reviewApplication(Long applicationId, String status, Long reviewerId, String reviewRemark);
}
