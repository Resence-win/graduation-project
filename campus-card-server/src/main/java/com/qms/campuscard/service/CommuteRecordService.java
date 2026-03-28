package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.CommuteRecord;

public interface CommuteRecordService {

    IPage<CommuteRecord> getCommuteRecords(Long cardId, Long routeId, Integer page, Integer size);
}
