package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.AccessPoint;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.mapper.AccessPointMapper;
import com.qms.campuscard.mapper.AccessRecordMapper;
import com.qms.campuscard.service.AccessService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AccessServiceImpl implements AccessService {

    @Resource
    private AccessRecordMapper accessRecordMapper;

    @Resource
    private AccessPointMapper accessPointMapper;

    // 门禁点管理
    @Override
    public IPage<AccessPoint> getAccessPoints(String name, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        Page<AccessPoint> pageParam = new Page<>(page, size);
        QueryWrapper<AccessPoint> queryWrapper = new QueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            queryWrapper.like("name", name);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return accessPointMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public AccessPoint addAccessPoint(AccessPoint accessPoint) {
        validateAccessPoint(accessPoint);
        if (accessPoint.getStatus() == null) {
            accessPoint.setStatus(1);
        }
        accessPoint.setCreateTime(LocalDateTime.now());
        accessPoint.setIsDeleted(0);
        accessPointMapper.insert(accessPoint);
        return accessPoint;
    }

    @Override
    public AccessPoint updateAccessPoint(AccessPoint accessPoint) {
        validateAccessPoint(accessPoint);
        accessPoint.setUpdateTime(LocalDateTime.now());
        accessPointMapper.updateById(accessPoint);
        return accessPoint;
    }

    @Override
    public void deleteAccessPoint(Long id) {
        AccessPoint accessPoint = new AccessPoint();
        accessPoint.setId(id);
        accessPoint.setIsDeleted(1);
        accessPoint.setUpdateTime(LocalDateTime.now());
        accessPointMapper.updateById(accessPoint);
    }

    @Override
    public AccessPoint getAccessPointById(Long id) {
        return accessPointMapper.selectById(id);
    }

    // 门禁记录管理
    @Override
    public IPage<AccessRecord> getAccessRecords(Long cardId, Long accessPointId, LocalDateTime startDate, LocalDateTime endDate, String status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        Page<AccessRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AccessRecord> queryWrapper = new QueryWrapper<>();
        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (accessPointId != null) {
            queryWrapper.eq("access_point_id", accessPointId);
        }
        if (startDate != null) {
            queryWrapper.ge("access_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("access_time", endDate);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("access_time");
        return accessRecordMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public IPage<AccessRecord> getMyAccessRecords(Long cardId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        Page<AccessRecord> pageParam = new Page<>(page, size);
        QueryWrapper<AccessRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("access_time");
        return accessRecordMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public AccessRecord createQRAccess(Long cardId, Long accessPointId, String qrCode, Double actualLatitude, Double actualLongitude, String deviceInfo) {
        // 验证二维码有效性（这里简化处理，实际应使用JWT等方式验证）
        AccessPoint accessPoint = accessPointMapper.selectById(accessPointId);
        if (accessPoint == null || Integer.valueOf(1).equals(accessPoint.getIsDeleted())) {
            throw new RuntimeException("门禁点不存在");
        }
        if (!Integer.valueOf(1).equals(accessPoint.getStatus())) {
            throw new RuntimeException("门禁点已禁用，暂不能通行");
        }
        if (actualLatitude == null || actualLongitude == null) {
            throw new RuntimeException("请先授权浏览器定位后再扫码开门");
        }
        if (accessPoint.getLatitude() == null || accessPoint.getLongitude() == null || accessPoint.getRadius() == null) {
            throw new RuntimeException("该门禁点未配置定位坐标或允许半径，请联系管理员完善配置");
        }

        double distance = calculateDistance(actualLatitude, actualLongitude, accessPoint.getLatitude(), accessPoint.getLongitude());
        if (distance > accessPoint.getRadius()) {
            throw new RuntimeException(String.format("当前位置距离门禁点约%.1f米，已超出%d米允许范围", distance, accessPoint.getRadius()));
        }
        
        // 智能判断进出方向
        // 查询该学生在该门禁点的最后一次通行记录
        QueryWrapper<AccessRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("access_point_id", accessPointId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("access_time");
        queryWrapper.last("LIMIT 1");
        
        AccessRecord lastRecord = accessRecordMapper.selectOne(queryWrapper);
        
        // 判断方向：如果最后一次是"进"，则本次为"出"；否则为"进"
        String direction;
        if (lastRecord != null && "进".equals(lastRecord.getDirection())) {
            direction = "出";
        } else {
            direction = "进";
        }
        
        AccessRecord accessRecord = new AccessRecord();
        accessRecord.setCardId(cardId);
        accessRecord.setAccessPointId(accessPointId);
        accessRecord.setDirection(direction);
        accessRecord.setLocation(accessPoint.getLocation());
        accessRecord.setStatus("成功");
        accessRecord.setActualLatitude(actualLatitude);
        accessRecord.setActualLongitude(actualLongitude);
        accessRecord.setDistance(distance);
        accessRecord.setAccessTime(LocalDateTime.now());
        accessRecord.setDeviceInfo(buildDeviceInfo(deviceInfo, distance, accessPoint.getRadius()));
        accessRecord.setIsDeleted(0);
        
        accessRecordMapper.insert(accessRecord);
        return accessRecord;
    }

    @Override
    public List<Map<String, Object>> getAccessStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        // 这里简化处理，实际应使用SQL聚合查询
        QueryWrapper<AccessRecord> queryWrapper = new QueryWrapper<>();
        if (startDate != null) {
            queryWrapper.ge("access_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("access_time", endDate);
        }
        queryWrapper.eq("is_deleted", 0);
        
        List<AccessRecord> records = accessRecordMapper.selectList(queryWrapper);
        
        // 按日期统计
        Map<String, Integer> dateCountMap = new HashMap<>();
        for (AccessRecord record : records) {
            String date = record.getAccessTime().toLocalDate().toString();
            dateCountMap.put(date, dateCountMap.getOrDefault(date, 0) + 1);
        }
        
        // 转换为返回格式
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : dateCountMap.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", entry.getKey());
            item.put("total_count", entry.getValue());
            result.add(item);
        }
        
        return result;
    }

    @Override
    public List<AccessRecord> exportAccessRecords(Long cardId, LocalDateTime startDate, LocalDateTime endDate) {
        QueryWrapper<AccessRecord> queryWrapper = new QueryWrapper<>();
        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (startDate != null) {
            queryWrapper.ge("access_time", startDate);
        }
        if (endDate != null) {
            queryWrapper.le("access_time", endDate);
        }
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("access_time");
        return accessRecordMapper.selectList(queryWrapper);
    }

    private void validateAccessPoint(AccessPoint accessPoint) {
        if (accessPoint == null) {
            throw new RuntimeException("门禁点信息不能为空");
        }
        if (accessPoint.getName() == null || accessPoint.getName().trim().isEmpty()) {
            throw new RuntimeException("门禁点名称不能为空");
        }
        if (accessPoint.getLocation() == null || accessPoint.getLocation().trim().isEmpty()) {
            throw new RuntimeException("门禁点位置不能为空");
        }
        if (accessPoint.getDeviceId() == null || accessPoint.getDeviceId().trim().isEmpty()) {
            throw new RuntimeException("设备ID不能为空");
        }
        if (accessPoint.getLatitude() == null || accessPoint.getLatitude() < -90 || accessPoint.getLatitude() > 90) {
            throw new RuntimeException("门禁点纬度必须在-90到90之间");
        }
        if (accessPoint.getLongitude() == null || accessPoint.getLongitude() < -180 || accessPoint.getLongitude() > 180) {
            throw new RuntimeException("门禁点经度必须在-180到180之间");
        }
        if (accessPoint.getRadius() == null || accessPoint.getRadius() <= 0) {
            throw new RuntimeException("允许通行半径必须大于0米");
        }
    }

    private double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        double earthRadius = 6371000D;
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double startLat = Math.toRadians(lat1);
        double endLat = Math.toRadians(lat2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(startLat) * Math.cos(endLat)
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadius * c;
    }

    private String buildDeviceInfo(String deviceInfo, double distance, Integer radius) {
        String source = deviceInfo == null || deviceInfo.trim().isEmpty() ? "浏览器定位扫码" : deviceInfo.trim();
        if (source.length() > 180) {
            source = source.substring(0, 180);
        }
        return source + String.format("；定位校验通过，距离%.1f米/半径%d米", distance, radius);
    }
}
