package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.RechargeRecord;

public interface RechargeService {

    boolean recharge(Long cardId, java.math.BigDecimal amount, String rechargeType);

    IPage<RechargeRecord> getRechargeRecords(Long cardId, Integer page, Integer size);
}
