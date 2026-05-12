package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qms.campuscard.entity.AttendanceLocation;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.AttendanceLocationMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.service.AttendanceLocationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AttendanceLocationServiceImpl extends ServiceImpl<AttendanceLocationMapper, AttendanceLocation> implements AttendanceLocationService {

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Override
    public AttendanceLocation createLocation(AttendanceLocation location) {
        validateLocation(location);
        validateNewLocation(location);
        location.setIsDeleted(0);
        save(location);
        fillTeacherName(location);
        return location;
    }

    @Override
    public AttendanceLocation updateLocation(AttendanceLocation location) {
        if (location == null || location.getId() == null) {
            throw new RuntimeException("打卡位置ID不能为空");
        }
        AttendanceLocation existing = getById(location.getId());
        if (existing == null) {
            throw new RuntimeException("打卡位置不存在");
        }
        validateLocation(location);
        validateUpdateLocation(existing, location);
        updateById(location);
        return location;
    }

    @Override
    public void deleteLocation(Long id) {
        removeById(id);
    }

    @Override
    public AttendanceLocation getLocationById(Long id) {
        AttendanceLocation location = getById(id);
        fillTeacherName(location);
        return location;
    }

    @Override
    public IPage<AttendanceLocation> getLocationsByTeacherId(Long teacherId, Integer page, Integer size) {
        Page<AttendanceLocation> pagination = new Page<>(page, size);
        IPage<AttendanceLocation> result = lambdaQuery()
                .eq(AttendanceLocation::getTeacherId, teacherId)
                .orderByDesc(AttendanceLocation::getCreateTime)
                .page(pagination);
        fillTeacherName(result.getRecords());
        return result;
    }

    @Override
    public IPage<AttendanceLocation> getAllLocations(Integer page, Integer size) {
        Page<AttendanceLocation> pagination = new Page<>(page, size);
        IPage<AttendanceLocation> result = lambdaQuery()
                .orderByDesc(AttendanceLocation::getCreateTime)
                .page(pagination);
        fillTeacherName(result.getRecords());
        return result;
    }

    @Override
    public List<AttendanceLocation> getActiveLocations(Long cardId) {
        LocalDateTime now = LocalDateTime.now();
        Long teacherId = getStudentTeacherIdByCardId(cardId);
        List<AttendanceLocation> locations = lambdaQuery()
                .eq(AttendanceLocation::getStatus, 1)
                .eq(teacherId != null, AttendanceLocation::getTeacherId, teacherId)
                .le(AttendanceLocation::getStartTime, now)
                .ge(AttendanceLocation::getEndTime, now)
                .orderByAsc(AttendanceLocation::getEndTime)
                .list();
        fillTeacherName(locations);
        return locations;
    }

    private void validateLocation(AttendanceLocation location) {
        if (location == null) {
            throw new RuntimeException("打卡位置不能为空");
        }
        if (location.getLocationName() == null || location.getLocationName().trim().isEmpty()) {
            throw new RuntimeException("位置名称不能为空");
        }
        if (location.getLatitude() == null || location.getLongitude() == null) {
            throw new RuntimeException("经纬度不能为空");
        }
        if (location.getRadius() == null || location.getRadius() <= 0) {
            throw new RuntimeException("打卡半径必须大于0");
        }
        if (location.getStartTime() == null || location.getEndTime() == null) {
            throw new RuntimeException("打卡开始时间和结束时间不能为空");
        }
        if (!location.getEndTime().isAfter(location.getStartTime())) {
            throw new RuntimeException("打卡结束时间必须晚于开始时间");
        }
        if (!location.getStartTime().toLocalDate().equals(location.getEndTime().toLocalDate())) {
            throw new RuntimeException("打卡开始时间和结束时间必须在同一天");
        }
    }

    private void validateNewLocation(AttendanceLocation location) {
        if (location.getStartTime().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("打卡开始时间不能早于当前时间");
        }
    }

    private void validateUpdateLocation(AttendanceLocation existing, AttendanceLocation updated) {
        LocalDateTime now = LocalDateTime.now();
        if (existing.getEndTime() != null && now.isAfter(existing.getEndTime())) {
            throw new RuntimeException("已结束的打卡位置不允许修改");
        }
        if (existing.getStartTime() != null && !now.isBefore(existing.getStartTime())) {
            if (!updated.getStartTime().isEqual(existing.getStartTime())) {
                throw new RuntimeException("已开始的打卡位置不能修改开始时间");
            }
            if (!updated.getEndTime().isAfter(now)) {
                throw new RuntimeException("进行中的打卡位置结束时间必须晚于当前时间");
            }
            return;
        }
        if (updated.getStartTime().isBefore(now)) {
            throw new RuntimeException("打卡开始时间不能早于当前时间");
        }
    }

    private Long getStudentTeacherIdByCardId(Long cardId) {
        if (cardId == null) {
            return null;
        }
        CampusCard campusCard = campusCardMapper.selectById(cardId);
        if (campusCard == null || campusCard.getIsDeleted() == null || campusCard.getIsDeleted() != 0) {
            throw new RuntimeException("校园卡不存在");
        }
        if (!"student".equalsIgnoreCase(campusCard.getUserType())) {
            return null;
        }
        Student student = studentMapper.selectById(campusCard.getUserId());
        if (student == null || student.getIsDeleted() == null || student.getIsDeleted() != 0) {
            throw new RuntimeException("学生不存在");
        }
        if (student.getTeacherId() == null) {
            return -1L;
        }
        return student.getTeacherId();
    }

    private void fillTeacherName(List<AttendanceLocation> locations) {
        if (locations == null || locations.isEmpty()) {
            return;
        }
        for (AttendanceLocation location : locations) {
            fillTeacherName(location);
        }
    }

    private void fillTeacherName(AttendanceLocation location) {
        if (location == null || location.getTeacherId() == null) {
            return;
        }
        QueryWrapper<Teacher> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", location.getTeacherId());
        queryWrapper.eq("is_deleted", 0);
        Teacher teacher = teacherMapper.selectOne(queryWrapper);
        if (teacher != null) {
            location.setTeacherName(teacher.getName());
        }
    }
}
