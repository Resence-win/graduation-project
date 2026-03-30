package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.ConsumeRequest;
import com.qms.campuscard.dto.ConsumeByCardNoRequest;
import com.qms.campuscard.entity.ConsumeRecord;
import com.qms.campuscard.service.ConsumeService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/consume")
public class ConsumeController {

    private final ConsumeService consumeService;

    @Resource
    private LogUtil logUtil;

    public ConsumeController(ConsumeService consumeService) {
        this.consumeService = consumeService;
    }

    @PostMapping
    public Result<Boolean> consume(@RequestBody ConsumeRequest request) {
        try {
            boolean success = consumeService.consume(request.getCardId(), request.getMerchantId(), request.getAmount());
            if (success) {
                // 记录日志
                logUtil.recordLog(1L, "新增", "consume_record", null, "消费成功，卡号：" + request.getCardId() + "，商户：" + request.getMerchantId() + "，金额：" + request.getAmount());
                return Result.success("消费成功", true);
            } else {
                return Result.error("消费失败");
            }
        } catch (Exception e) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "consume_record", null, "消费失败：" + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/by-card-no")
    public Result<Boolean> consumeByCardNo(@RequestBody ConsumeByCardNoRequest request) {
        try {
            boolean success = consumeService.consumeByCardNo(request.getCardNo(), request.getMerchantId(), request.getAmount());
            if (success) {
                // 记录日志
                logUtil.recordLog(1L, "新增", "consume_record", null, "消费成功，卡号：" + request.getCardNo() + "，商户：" + request.getMerchantId() + "，金额：" + request.getAmount());
                return Result.success("消费成功", true);
            } else {
                return Result.error("消费失败");
            }
        } catch (Exception e) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "consume_record", null, "消费失败：" + e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/list")
    public Result<IPage<com.qms.campuscard.dto.ConsumeRecordDTO>> getConsumeRecords(
            @RequestParam(required = false) String card_id,
            @RequestParam(required = false) Long merchant_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<com.qms.campuscard.dto.ConsumeRecordDTO> records = consumeService.getConsumeRecords(card_id, merchant_id, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "consume_record", null, "查询消费记录，卡号：" + (card_id != null ? card_id : "全部") + "，商户：" + (merchant_id != null ? merchant_id : "全部"));
        return Result.success(records);
    }
}
