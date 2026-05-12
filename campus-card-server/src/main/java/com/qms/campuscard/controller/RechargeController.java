package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.config.AlipaySandboxProperties;
import com.qms.campuscard.dto.AlipayPagePayResponse;
import com.qms.campuscard.dto.AlipayRechargeRequest;
import com.qms.campuscard.dto.AlipayRechargeStatusResponse;
import com.qms.campuscard.dto.RechargeRequest;
import com.qms.campuscard.dto.RechargeByCardNoRequest;
import com.qms.campuscard.service.AlipayRechargeService;
import com.qms.campuscard.service.RechargeService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargeService rechargeService;
    private final AlipayRechargeService alipayRechargeService;
    private final AlipaySandboxProperties alipayProperties;

    @Resource
    private LogUtil logUtil;

    public RechargeController(RechargeService rechargeService, AlipayRechargeService alipayRechargeService, AlipaySandboxProperties alipayProperties) {
        this.rechargeService = rechargeService;
        this.alipayRechargeService = alipayRechargeService;
        this.alipayProperties = alipayProperties;
    }

    /**
     * 充值接口：根据校园卡ID为账户增加余额，并写入充值记录和资金流水。
     */
    @PostMapping
    public Result<Boolean> recharge(@RequestBody RechargeRequest request) {
        try {
            boolean success = rechargeService.recharge(request.getCardId(), request.getAmount(), request.getRechargeType(), request.getOperatorId(), request.getOperatorName());
            if (success) {
                // 记录日志
                logUtil.recordLog(request.getOperatorId() != null ? request.getOperatorId() : 1L, "新增", "recharge_record", null, "充值成功，卡号：" + request.getCardId() + "，金额：" + request.getAmount());
                return Result.success("充值成功", true);
            } else {
                return Result.error("充值失败");
            }
        } catch (Exception e) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "recharge_record", null, "充值失败：" + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 卡号充值接口：根据实体卡号完成充值，适合收银台或人工充值场景。
     */
    @PostMapping("/by-card-no")
    public Result<Boolean> rechargeByCardNo(@RequestBody RechargeByCardNoRequest request) {
        try {
            boolean success = rechargeService.rechargeByCardNo(request.getCardNo(), request.getAmount(), request.getRechargeType(), request.getOperatorId(), request.getOperatorName());
            if (success) {
                // 记录日志
                logUtil.recordLog(request.getOperatorId() != null ? request.getOperatorId() : 1L, "新增", "recharge_record", null, "充值成功，卡号：" + request.getCardNo() + "，金额：" + request.getAmount());
                return Result.success("充值成功", true);
            } else {
                return Result.error("充值失败");
            }
        } catch (Exception e) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "recharge_record", null, "充值失败：" + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付宝沙箱电脑网站支付：创建本地充值订单，并返回支付宝自动提交表单。
     */
    @PostMapping("/alipay/page-pay")
    public Result<AlipayPagePayResponse> createAlipayPagePay(@RequestBody AlipayRechargeRequest request) {
        try {
            AlipayPagePayResponse response = alipayRechargeService.createPagePay(request);
            logUtil.recordLog(request.getOperatorId() != null ? request.getOperatorId() : 1L, "新增", "recharge_order", null, "创建支付宝充值订单：" + response.getOutTradeNo());
            return Result.success("支付宝支付订单创建成功", response);
        } catch (Exception e) {
            logUtil.recordLog(1L, "新增", "recharge_order", null, "创建支付宝充值订单失败：" + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付宝沙箱订单查询：主动查询支付宝交易状态，支付成功后完成一次性入账。
     */
    @GetMapping("/alipay/status/{outTradeNo}")
    public Result<AlipayRechargeStatusResponse> getAlipayRechargeStatus(@PathVariable String outTradeNo) {
        try {
            AlipayRechargeStatusResponse response = alipayRechargeService.queryAndSettle(outTradeNo);
            return Result.success(response);
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 支付宝同步回跳：验签后重定向回前端页面，由前端继续主动查询订单状态。
     */
    @GetMapping("/alipay/return")
    public void alipayReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, String> params = collectRequestParams(request);
        String outTradeNo = params.get("out_trade_no");
        boolean verified = false;
        try {
            verified = alipayRechargeService.verifyReturn(params);
        } catch (Exception e) {
            logUtil.recordLog(1L, "查询", "recharge_order", null, "支付宝同步回跳验签失败：" + e.getMessage());
        }

        String redirectUrl = buildFrontendRedirectUrl(outTradeNo, verified);
        response.sendRedirect(redirectUrl);
    }

    /**
     * 充值记录接口：按卡ID或卡号分页查询充值明细。
     */
    @GetMapping("/list")
    public Result<IPage<com.qms.campuscard.dto.RechargeRecordDTO>> getRechargeRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) String card_no,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<com.qms.campuscard.dto.RechargeRecordDTO> records;
        if (card_no != null && !card_no.isEmpty()) {
            records = rechargeService.getRechargeRecordsByCardNo(card_no, page, size);
        } else {
            records = rechargeService.getRechargeRecords(card_id, page, size);
        }
        // 记录日志
        logUtil.recordLog(1L, "查询", "recharge_record", null, "查询充值记录，卡号：" + (card_id != null ? card_id : (card_no != null ? card_no : "全部")));
        return Result.success(records);
    }

    private Map<String, String> collectRequestParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private String buildFrontendRedirectUrl(String outTradeNo, boolean verified) {
        String baseUrl = alipayProperties.getFrontendReturnUrl();
        String separator = baseUrl.contains("?") ? "&" : "?";
        String encodedOutTradeNo = URLEncoder.encode(outTradeNo != null ? outTradeNo : "", StandardCharsets.UTF_8);
        return baseUrl + separator + "alipayOutTradeNo=" + encodedOutTradeNo + "&alipayReturn=" + (verified ? "success" : "failed");
    }
}
