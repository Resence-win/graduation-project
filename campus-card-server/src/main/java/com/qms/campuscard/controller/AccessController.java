package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.AccessRecord;
import com.qms.campuscard.service.AccessService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/access")
public class AccessController {

    private final AccessService accessService;

    @Resource
    private LogUtil logUtil;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping("/list")
    public Result<IPage<AccessRecord>> getAccessRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AccessRecord> records = accessService.getAccessRecords(card_id, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "access_record", null, "查询门禁记录，卡号：" + (card_id != null ? card_id : "全部"));
        return Result.success(records);
    }
}
