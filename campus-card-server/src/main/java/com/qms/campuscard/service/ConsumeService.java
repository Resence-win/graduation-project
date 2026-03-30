package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.ConsumeRecord;

public interface ConsumeService {

    boolean consume(Long cardId, Long merchantId, java.math.BigDecimal amount);
    boolean consumeByCardNo(String cardNo, Long merchantId, java.math.BigDecimal amount);

    IPage<com.qms.campuscard.dto.ConsumeRecordDTO> getConsumeRecords(String cardId, Long merchantId, Integer page, Integer size);
}
