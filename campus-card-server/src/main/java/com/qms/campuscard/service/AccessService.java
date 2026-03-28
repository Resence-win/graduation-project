package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.AccessRecord;

public interface AccessService {

    IPage<AccessRecord> getAccessRecords(Long cardId, Integer page, Integer size);
}
