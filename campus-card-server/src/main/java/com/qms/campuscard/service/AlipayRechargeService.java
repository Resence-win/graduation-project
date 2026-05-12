package com.qms.campuscard.service;

import com.qms.campuscard.dto.AlipayPagePayResponse;
import com.qms.campuscard.dto.AlipayRechargeRequest;
import com.qms.campuscard.dto.AlipayRechargeStatusResponse;

import java.util.Map;

public interface AlipayRechargeService {

    AlipayPagePayResponse createPagePay(AlipayRechargeRequest request);

    AlipayRechargeStatusResponse queryAndSettle(String outTradeNo);

    boolean verifyReturn(Map<String, String> params);
}
