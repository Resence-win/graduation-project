package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.RechargeRecord;

public interface RechargeService {

    boolean recharge(Long cardId, java.math.BigDecimal amount, String rechargeType, Long operatorId, String operatorName);
    boolean rechargeByCardNo(String cardNo, java.math.BigDecimal amount, String rechargeType, Long operatorId, String operatorName);

    IPage<com.qms.campuscard.dto.RechargeRecordDTO> getRechargeRecords(Long cardId, Integer page, Integer size);
    IPage<com.qms.campuscard.dto.RechargeRecordDTO> getRechargeRecordsByCardNo(String cardNo, Integer page, Integer size);
}
