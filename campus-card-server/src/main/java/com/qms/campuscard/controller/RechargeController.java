package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.RechargeRequest;
import com.qms.campuscard.dto.RechargeByCardNoRequest;
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

    @GetMapping("/list")
    public Result<IPage<com.qms.campuscard.dto.RechargeRecordDTO>> getRechargeRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<com.qms.campuscard.dto.RechargeRecordDTO> records = rechargeService.getRechargeRecords(card_id, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "recharge_record", null, "查询充值记录，卡号：" + (card_id != null ? card_id : "全部"));
        return Result.success(records);
    }
}
