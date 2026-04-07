package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.entity.CommuteRecord;
import com.qms.campuscard.service.CommuteRecordService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/commute")
public class CommuteRecordController {

    @Resource
    private CommuteRecordService commuteRecordService;

    @Resource
    private LogUtil logUtil;

    @GetMapping("/list")
    public Result<IPage<CommuteRecord>> getCommuteRecords(
            @RequestParam(required = false) Long cardId,
            @RequestParam(required = false) Long routeId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<CommuteRecord> records = commuteRecordService.getCommuteRecords(cardId, routeId, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "commute_record", null, "查询通勤记录，卡号：" + (cardId != null ? cardId : "全部"));
        return Result.success(records);
    }


}
