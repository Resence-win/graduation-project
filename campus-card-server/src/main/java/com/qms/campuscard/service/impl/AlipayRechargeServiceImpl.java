package com.qms.campuscard.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qms.campuscard.config.AlipaySandboxProperties;
import com.qms.campuscard.dto.AlipayPagePayResponse;
import com.qms.campuscard.dto.AlipayRechargeRequest;
import com.qms.campuscard.dto.AlipayRechargeStatusResponse;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.RechargeOrder;
import com.qms.campuscard.mapper.AccountMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.RechargeOrderMapper;
import com.qms.campuscard.service.AlipayRechargeService;
import com.qms.campuscard.service.RechargeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AlipayRechargeServiceImpl implements AlipayRechargeService {

    private static final String STATUS_WAIT_PAY = "WAIT_PAY";
    private static final String STATUS_SETTLED = "SETTLED";
    private static final String STATUS_CLOSED = "CLOSED";
    private static final String ALIPAY_TRADE_SUCCESS = "TRADE_SUCCESS";
    private static final String ALIPAY_TRADE_FINISHED = "TRADE_FINISHED";
    private static final String ALIPAY_TRADE_CLOSED = "TRADE_CLOSED";

    @Resource
    private AlipayClient alipayClient;

    @Resource
    private AlipaySandboxProperties alipayProperties;

    @Resource
    private RechargeOrderMapper rechargeOrderMapper;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private RechargeService rechargeService;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional
    public AlipayPagePayResponse createPagePay(AlipayRechargeRequest request) {
        ensureConfigured();
        BigDecimal amount = normalizeAmount(request.getAmount());
        CampusCard campusCard = resolveRechargeCard(request);
        ensureAccountExists(campusCard.getId());

        LocalDateTime now = LocalDateTime.now();
        RechargeOrder order = new RechargeOrder();
        order.setOutTradeNo(generateOutTradeNo());
        order.setCardId(campusCard.getId());
        order.setAmount(amount);
        order.setStatus(STATUS_WAIT_PAY);
        order.setOperatorId(request.getOperatorId() != null ? request.getOperatorId() : 1L);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        order.setIsDeleted(0);
        rechargeOrderMapper.insert(order);

        AlipayTradePagePayRequest payRequest = new AlipayTradePagePayRequest();
        payRequest.setReturnUrl(alipayProperties.getReturnUrl());
        payRequest.setBizContent(buildPagePayBizContent(order, campusCard, amount));

        try {
            AlipayTradePagePayResponse response = alipayClient.pageExecute(payRequest);
            if (response.getBody() == null || response.getBody().trim().isEmpty()) {
                throw new RuntimeException("支付宝支付表单生成失败");
            }
            return new AlipayPagePayResponse(order.getOutTradeNo(), response.getBody());
        } catch (AlipayApiException e) {
            throw new RuntimeException("调用支付宝沙箱失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public AlipayRechargeStatusResponse queryAndSettle(String outTradeNo) {
        ensureConfigured();
        RechargeOrder order = getOrder(outTradeNo);
        AlipayRechargeStatusResponse result = buildStatusResponse(order);
        if (STATUS_SETTLED.equals(order.getStatus())) {
            result.setPaid(true);
            result.setSettled(true);
            result.setMessage("充值已入账");
            return result;
        }

        AlipayTradeQueryResponse alipayResponse = queryAlipay(outTradeNo);
        result.setTradeStatus(alipayResponse.getTradeStatus());
        result.setAlipayTradeNo(alipayResponse.getTradeNo());

        if (!alipayResponse.isSuccess()) {
            result.setPaid(false);
            result.setSettled(false);
            result.setMessage(alipayResponse.getSubMsg() != null ? alipayResponse.getSubMsg() : "支付尚未完成");
            return result;
        }

        String tradeStatus = alipayResponse.getTradeStatus();
        if (ALIPAY_TRADE_SUCCESS.equals(tradeStatus) || ALIPAY_TRADE_FINISHED.equals(tradeStatus)) {
            RechargeOrder settledOrder = settleOrder(order, alipayResponse.getTradeNo());
            result.setStatus(STATUS_SETTLED);
            result.setAlipayTradeNo(settledOrder.getAlipayTradeNo());
            result.setPaid(true);
            result.setSettled(true);
            result.setMessage("充值成功，已入账");
        } else if (ALIPAY_TRADE_CLOSED.equals(tradeStatus)) {
            order.setStatus(STATUS_CLOSED);
            order.setUpdateTime(LocalDateTime.now());
            rechargeOrderMapper.updateById(order);
            result.setStatus(STATUS_CLOSED);
            result.setPaid(false);
            result.setSettled(false);
            result.setMessage("交易已关闭");
        } else {
            result.setPaid(false);
            result.setSettled(false);
            result.setMessage("支付处理中");
        }
        return result;
    }

    @Override
    public boolean verifyReturn(Map<String, String> params) {
        ensureConfigured();
        try {
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType()
            );
        } catch (AlipayApiException e) {
            throw new RuntimeException("支付宝回跳验签失败：" + e.getMessage());
        }
    }

    private RechargeOrder settleOrder(RechargeOrder order, String alipayTradeNo) {
        if (STATUS_SETTLED.equals(order.getStatus())) {
            return order;
        }
        LocalDateTime now = LocalDateTime.now();
        UpdateWrapper<RechargeOrder> update = new UpdateWrapper<>();
        update.eq("id", order.getId());
        update.eq("status", STATUS_WAIT_PAY);
        update.eq("is_deleted", 0);
        update.set("status", STATUS_SETTLED);
        update.set("alipay_trade_no", alipayTradeNo);
        update.set("paid_time", now);
        update.set("settled_time", now);
        update.set("update_time", now);
        int updated = rechargeOrderMapper.update(null, update);
        RechargeOrder latestOrder = rechargeOrderMapper.selectById(order.getId());
        if (updated <= 0) {
            if (latestOrder != null && STATUS_SETTLED.equals(latestOrder.getStatus())) {
                return latestOrder;
            }
            throw new RuntimeException("充值订单状态已变更，请重新查询支付结果");
        }
        rechargeService.recharge(order.getCardId(), order.getAmount(), "支付宝", order.getOperatorId(), "支付宝沙箱");
        return latestOrder != null ? latestOrder : order;
    }

    private AlipayTradeQueryResponse queryAlipay(String outTradeNo) {
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + outTradeNo + "\"}");
        try {
            return alipayClient.execute(request);
        } catch (AlipayApiException e) {
            throw new RuntimeException("查询支付宝订单失败：" + e.getMessage());
        }
    }

    private String buildPagePayBizContent(RechargeOrder order, CampusCard campusCard, BigDecimal amount) {
        Map<String, String> bizContent = new HashMap<>();
        bizContent.put("out_trade_no", order.getOutTradeNo());
        bizContent.put("total_amount", amount.toPlainString());
        bizContent.put("subject", "校园卡充值-" + campusCard.getCardNo());
        bizContent.put("product_code", "FAST_INSTANT_TRADE_PAY");
        try {
            return objectMapper.writeValueAsString(bizContent);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("支付宝支付参数生成失败");
        }
    }

    private AlipayRechargeStatusResponse buildStatusResponse(RechargeOrder order) {
        AlipayRechargeStatusResponse response = new AlipayRechargeStatusResponse();
        response.setOutTradeNo(order.getOutTradeNo());
        response.setAlipayTradeNo(order.getAlipayTradeNo());
        response.setStatus(order.getStatus());
        response.setAmount(order.getAmount());
        response.setPaid(STATUS_SETTLED.equals(order.getStatus()));
        response.setSettled(STATUS_SETTLED.equals(order.getStatus()));
        response.setMessage("支付处理中");
        return response;
    }

    private RechargeOrder getOrder(String outTradeNo) {
        if (outTradeNo == null || outTradeNo.trim().isEmpty()) {
            throw new RuntimeException("商户订单号不能为空");
        }
        QueryWrapper<RechargeOrder> query = new QueryWrapper<>();
        query.eq("out_trade_no", outTradeNo);
        query.eq("is_deleted", 0);
        RechargeOrder order = rechargeOrderMapper.selectOne(query);
        if (order == null) {
            throw new RuntimeException("充值订单不存在");
        }
        return order;
    }

    private CampusCard resolveRechargeCard(AlipayRechargeRequest request) {
        QueryWrapper<CampusCard> query = new QueryWrapper<>();
        if (request.getCardId() != null) {
            query.eq("id", request.getCardId());
        } else if (request.getCardNo() != null && !request.getCardNo().trim().isEmpty()) {
            query.eq("card_no", request.getCardNo());
        } else {
            throw new RuntimeException("校园卡ID或卡号不能为空");
        }
        query.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(query);
        if (campusCard == null) {
            throw new RuntimeException("校园卡不存在");
        }
        if (campusCard.getStatus() == 0) {
            throw new RuntimeException("校园卡已注销，无法充值");
        }
        if (campusCard.getStatus() == 2) {
            throw new RuntimeException("校园卡已挂失，无法充值");
        }
        return campusCard;
    }

    private void ensureAccountExists(Long cardId) {
        QueryWrapper<Account> accountQuery = new QueryWrapper<>();
        accountQuery.eq("card_id", cardId);
        accountQuery.eq("is_deleted", 0);
        if (accountMapper.selectCount(accountQuery) <= 0) {
            throw new RuntimeException("账户不存在");
        }
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String generateOutTradeNo() {
        return "RC" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    }

    private void ensureConfigured() {
        if (!alipayProperties.isConfigured()) {
            throw new RuntimeException("请先配置支付宝沙箱参数：app-id、private-key、alipay-public-key");
        }
    }
}
