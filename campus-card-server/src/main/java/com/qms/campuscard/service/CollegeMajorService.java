package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.CollegeMajor;

import java.util.List;
import java.util.Map;

public interface CollegeMajorService {

    boolean addCollegeMajor(CollegeMajor collegeMajor);

    boolean updateCollegeMajor(CollegeMajor collegeMajor);

    boolean deleteCollegeMajor(Long id);

    IPage<CollegeMajor> getCollegeMajorPage(Page<CollegeMajor> page, String collegeName, String majorName, Integer status);

    List<CollegeMajor> getActiveCollegeMajors();

    Map<String, List<String>> getCollegeMajorOptions();
}
