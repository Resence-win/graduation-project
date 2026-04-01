package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AccessPoint;
import com.qms.campuscard.entity.AccessRecord;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface AccessService {

    // 门禁点管理
    IPage<AccessPoint> getAccessPoints(String name, Integer status, Integer page, Integer size);
    AccessPoint addAccessPoint(AccessPoint accessPoint);
    AccessPoint updateAccessPoint(AccessPoint accessPoint);
    void deleteAccessPoint(Long id);
    AccessPoint getAccessPointById(Long id);

    // 门禁记录管理
    IPage<AccessRecord> getAccessRecords(Long cardId, Long accessPointId, LocalDateTime startDate, LocalDateTime endDate, String status, Integer page, Integer size);
    IPage<AccessRecord> getMyAccessRecords(Long cardId, Integer page, Integer size);
    AccessRecord createQRAccess(Long cardId, Long accessPointId, String qrCode);
    List<Map<String, Object>> getAccessStatistics(LocalDateTime startDate, LocalDateTime endDate);
    List<AccessRecord> exportAccessRecords(Long cardId, LocalDateTime startDate, LocalDateTime endDate);
}
