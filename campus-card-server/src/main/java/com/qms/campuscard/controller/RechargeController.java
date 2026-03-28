package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.RechargeRequest;
import com.qms.campuscard.entity.RechargeRecord;
import com.qms.campuscard.service.RechargeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recharge")
public class RechargeController {

    private final RechargeService rechargeService;

    public RechargeController(RechargeService rechargeService) {
        this.rechargeService = rechargeService;
    }

    @PostMapping
    public Result<Boolean> recharge(@RequestBody RechargeRequest request) {
        boolean success = rechargeService.recharge(request.getCardId(), request.getAmount(), request.getRechargeType());
        if (success) {
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
        return Result.success(records);
    }
}
