package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.ConsumeRequest;
import com.qms.campuscard.entity.ConsumeRecord;
import com.qms.campuscard.service.ConsumeService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consume")
public class ConsumeController {

    private final ConsumeService consumeService;

    public ConsumeController(ConsumeService consumeService) {
        this.consumeService = consumeService;
    }

    @PostMapping
    public Result<Boolean> consume(@RequestBody ConsumeRequest request) {
        boolean success = consumeService.consume(request.getCardId(), request.getMerchantId(), request.getAmount());
        if (success) {
            return Result.success("消费成功", true);
        } else {
            return Result.error("消费失败");
        }
    }

    @GetMapping("/list")
    public Result<IPage<ConsumeRecord>> getConsumeRecords(
            @RequestParam Long card_id,
            @RequestParam(required = false) Long merchant_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<ConsumeRecord> records = consumeService.getConsumeRecords(card_id, merchant_id, page, size);
        return Result.success(records);
    }
}
