package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.ConsumeRecord;

public interface ConsumeService {

    boolean consume(Long cardId, Long merchantId, java.math.BigDecimal amount);

    IPage<ConsumeRecord> getConsumeRecords(Long cardId, Long merchantId, Integer page, Integer size);
}
