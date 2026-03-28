package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.CommuteRecord;
import com.qms.campuscard.service.CommuteRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
public class CommuteRecordController {

    @Resource
    private CommuteRecordService commuteRecordService;

    @Resource
    private LogUtil logUtil;

    @GetMapping("/api/commute/list")
    public Result<IPage<CommuteRecord>> getCommuteRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Long route_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<CommuteRecord> records = commuteRecordService.getCommuteRecords(card_id, route_id, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "commute_record", null, "查询通勤记录，卡号：" + (card_id != null ? card_id : "全部"));
        return Result.success(records);
    }
}
