package com.qms.campuscard.controller;

import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.CardOperationRequest;
import com.qms.campuscard.dto.OpenCardRequest;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CardChangeRecord;
import com.qms.campuscard.service.CampusCardService;
import java.util.List;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class CampusCardController {

    private final CampusCardService campusCardService;

    public CampusCardController(CampusCardService campusCardService) {
        this.campusCardService = campusCardService;
    }

    @PostMapping("/card/open")
    public Result<CampusCard> openCard(@RequestBody OpenCardRequest request) {
        CampusCard campusCard = campusCardService.openCard(request.getUserId(), request.getUserType());
        return Result.success("开卡成功", campusCard);
    }

    @GetMapping("/card/{cardId}")
    public Result<CampusCard> getCardById(@PathVariable Long cardId) {
        CampusCard campusCard = campusCardService.getCardById(cardId);
        if (campusCard != null) {
            return Result.success(campusCard);
        } else {
            return Result.error("校园卡不存在");
        }
    }

    @PostMapping("/card/loss")
    public Result<Boolean> lossCard(@RequestBody CardOperationRequest request) {
        boolean success = campusCardService.lossCard(request.getCardId());
        if (success) {
            return Result.success("挂失成功", true);
        } else {
            return Result.error("挂失失败");
        }
    }

    @PostMapping("/card/unloss")
    public Result<Boolean> unlossCard(@RequestBody CardOperationRequest request) {
        boolean success = campusCardService.unlossCard(request.getCardId());
        if (success) {
            return Result.success("解挂成功", true);
        } else {
            return Result.error("解挂失败");
        }
    }

    @PostMapping("/card/cancel")
    public Result<Boolean> cancelCard(@RequestBody CardOperationRequest request) {
        boolean success = campusCardService.cancelCard(request.getCardId());
        if (success) {
            return Result.success("注销成功", true);
        } else {
            return Result.error("注销失败");
        }
    }

    @GetMapping("/account/{cardId}")
    public Result<Account> getAccountByCardId(@PathVariable Long cardId) {
        Account account = campusCardService.getAccountByCardId(cardId);
        if (account != null) {
            return Result.success(account);
        } else {
            return Result.error("账户不存在");
        }
    }

    @GetMapping("/account/balance/{cardId}")
    public Result<BigDecimal> getBalance(@PathVariable Long cardId) {
        BigDecimal balance = campusCardService.getBalance(cardId);
        return Result.success(balance);
    }

    @GetMapping("/card/records/{cardId}")
    public Result<List<CardChangeRecord>> getCardOperationRecords(@PathVariable Long cardId) {
        List<CardChangeRecord> records = campusCardService.getCardOperationRecords(cardId);
        return Result.success(records);
    }

    @GetMapping("/account/flow")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<AccountFlow>> getAccountFlow(
            @RequestParam Long account_id,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        com.baomidou.mybatisplus.core.metadata.IPage<AccountFlow> flows = campusCardService.getAccountFlow(account_id, page, size);
        return Result.success(flows);
    }
}
