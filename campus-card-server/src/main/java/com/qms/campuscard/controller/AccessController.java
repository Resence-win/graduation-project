package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.service.AccessService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/list")
    public Result<IPage<AccessRecord>> getAccessRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AccessRecord> records = accessService.getAccessRecords(card_id, page, size);
        return Result.success(records);
    }
}
