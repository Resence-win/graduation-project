package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.RechargeRequest;
import com.qms.campuscard.entity.RechargeRecord;
import com.qms.campuscard.service.RechargeService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargeService rechargeService;

    @Resource
    private LogUtil logUtil;

    public RechargeController(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    @PostMapping
    public Result<Boolean> recharge(@RequestBody RechargeRequest request) {
        boolean success = rechargeService.recharge(request.getCardId(), request.getAmount(), request.getRechargeType());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "recharge_record", null, "充值成功，卡号：" + request.getCardId() + "，金额：" + request.getAmount());
            return Result.success("充值成功", true);
        } else {
            return Result.error("充值失败");
        }
    }

    @GetMapping("/list")
    public Result<IPage<RechargeRecord>> getRechargeRecords(
            @RequestParam Long card_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<RechargeRecord> records = rechargeService.getRechargeRecords(card_id, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "recharge_record", null, "查询充值记录，卡号：" + card_id);
        return Result.success(records);
    }
}
