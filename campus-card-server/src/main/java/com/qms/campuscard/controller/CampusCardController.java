package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.CardOperationRequest;
import com.qms.campuscard.dto.OpenCardRequest;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CardChangeRecord;
import com.qms.campuscard.service.CampusCardService;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CampusCardController {

    private final CampusCardService campusCardService;

    @Resource
    private LogUtil logUtil;

    public CampusCardController(CampusCardService campusCardService) {
        this.campusCardService = campusCardService;
    }

    @PostMapping("/card/open")
    public Result<CampusCard> openCard(@RequestBody OpenCardRequest request) {
        CampusCard campusCard = campusCardService.openCard(request.getUserId(), request.getUserType());
        // 记录日志
        logUtil.recordLog(1L, "新增", "campus_card", campusCard.getId(), "开卡成功，卡号：" + campusCard.getCardNo());
        return Result.success("开卡成功", campusCard);
    }

    @GetMapping("/card/{cardId}")
    public Result<CampusCard> getCardById(@PathVariable Long cardId) {
        CampusCard campusCard = campusCardService.getCardById(cardId);
        if (campusCard != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "campus_card", cardId, "查询校园卡：" + campusCard.getCardNo());
            return Result.success(campusCard);
        } else {
            return Result.error("校园卡不存在");
        }
    }

    @PostMapping("/card/loss")
    public Result<Boolean> lossCard(@RequestBody CardOperationRequest request) {
        CampusCard campusCard = campusCardService.getCardById(request.getCardId());
        boolean success = campusCardService.lossCard(request.getCardId());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "campus_card", request.getCardId(), "挂失校园卡：" + (campusCard != null ? campusCard.getCardNo() : "未知"));
            return Result.success("挂失成功", true);
        } else {
            return Result.error("挂失失败");
        }
    }

    @PostMapping("/card/unloss")
    public Result<Boolean> unlossCard(@RequestBody CardOperationRequest request) {
        CampusCard campusCard = campusCardService.getCardById(request.getCardId());
        boolean success = campusCardService.unlossCard(request.getCardId());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "campus_card", request.getCardId(), "解挂校园卡：" + (campusCard != null ? campusCard.getCardNo() : "未知"));
            return Result.success("解挂成功", true);
        } else {
            return Result.error("解挂失败");
        }
    }

    @PostMapping("/card/cancel")
    public Result<Boolean> cancelCard(@RequestBody CardOperationRequest request) {
        CampusCard campusCard = campusCardService.getCardById(request.getCardId());
        boolean success = campusCardService.cancelCard(request.getCardId());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "campus_card", request.getCardId(), "注销校园卡：" + (campusCard != null ? campusCard.getCardNo() : "未知"));
            return Result.success("注销成功", true);
        } else {
            return Result.error("注销失败");
        }
    }

    @GetMapping("/account/{cardId}")
    public Result<Account> getAccountByCardId(@PathVariable Long cardId) {
        Account account = campusCardService.getAccountByCardId(cardId);
        if (account != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "account", account.getId(), "查询账户信息");
            return Result.success(account);
        } else {
            return Result.error("账户不存在");
        }
    }

    @GetMapping("/account/balance/{cardId}")
    public Result<BigDecimal> getBalance(@PathVariable Long cardId) {
        BigDecimal balance = campusCardService.getBalance(cardId);
        // 记录日志
        logUtil.recordLog(1L, "查询", "account", null, "查询账户余额");
        return Result.success(balance);
    }

    @GetMapping("/card/records/{cardId}")
    public Result<List<CardChangeRecord>> getCardOperationRecords(@PathVariable Long cardId) {
        List<CardChangeRecord> records = campusCardService.getCardOperationRecords(cardId);
        // 记录日志
        logUtil.recordLog(1L, "查询", "card_change_record", null, "查询校园卡操作记录");
        return Result.success(records);
    }

    @GetMapping("/account/flow")
    public Result<IPage<AccountFlow>> getAccountFlow(
            @RequestParam Long account_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<AccountFlow> flows = campusCardService.getAccountFlow(account_id, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "account_flow", null, "查询账户流水");
        return Result.success(flows);
    }
}
