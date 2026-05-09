package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CollegeMajor;
import com.qms.campuscard.mapper.CollegeMajorMapper;
import com.qms.campuscard.service.CollegeMajorService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CollegeMajorServiceImpl implements CollegeMajorService {

    @Resource
    private CollegeMajorMapper collegeMajorMapper;

    @Override
    public boolean addCollegeMajor(CollegeMajor collegeMajor) {
        collegeMajor.setCollegeName(trim(collegeMajor.getCollegeName()));
        collegeMajor.setMajorName(trim(collegeMajor.getMajorName()));
        if (collegeMajor.getStatus() == null) {
            collegeMajor.setStatus(1);
        }
        if (collegeMajor.getSortOrder() == null) {
            collegeMajor.setSortOrder(0);
        }
        collegeMajor.setIsDeleted(0);
        collegeMajor.setCreateTime(LocalDateTime.now());
        return collegeMajorMapper.insert(collegeMajor) > 0;
    }

    @Override
    public boolean updateCollegeMajor(CollegeMajor collegeMajor) {
        collegeMajor.setCollegeName(trim(collegeMajor.getCollegeName()));
        collegeMajor.setMajorName(trim(collegeMajor.getMajorName()));
        collegeMajor.setUpdateTime(LocalDateTime.now());
        return collegeMajorMapper.updateById(collegeMajor) > 0;
    }

    @Override
    public boolean deleteCollegeMajor(Long id) {
        CollegeMajor collegeMajor = new CollegeMajor();
        collegeMajor.setId(id);
        collegeMajor.setIsDeleted(1);
        collegeMajor.setUpdateTime(LocalDateTime.now());
        return collegeMajorMapper.updateById(collegeMajor) > 0;
    }

    @Override
    public IPage<CollegeMajor> getCollegeMajorPage(Page<CollegeMajor> page, String collegeName, String majorName, Integer status) {
        QueryWrapper<CollegeMajor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        if (collegeName != null && !collegeName.trim().isEmpty()) {
            queryWrapper.like("college_name", collegeName.trim());
        }
        if (majorName != null && !majorName.trim().isEmpty()) {
            queryWrapper.like("major_name", majorName.trim());
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        queryWrapper.orderByAsc("sort_order").orderByAsc("college_name").orderByAsc("major_name");
        return collegeMajorMapper.selectPage(page, queryWrapper);
    }

    @Override
    public List<CollegeMajor> getActiveCollegeMajors() {
        QueryWrapper<CollegeMajor> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("sort_order").orderByAsc("college_name").orderByAsc("major_name");
        return collegeMajorMapper.selectList(queryWrapper);
    }

    @Override
    public Map<String, List<String>> getCollegeMajorOptions() {
        return getActiveCollegeMajors().stream()
                .collect(Collectors.groupingBy(
                        CollegeMajor::getCollegeName,
                        LinkedHashMap::new,
                        Collectors.mapping(CollegeMajor::getMajorName, Collectors.toList())
                ));
    }

    private String trim(String value) {
        return value == null ? null : value.trim();
    }
}
