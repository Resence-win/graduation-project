package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.dto.AccountDTO;
import com.qms.campuscard.dto.BatchOpenCardRequest;
import com.qms.campuscard.dto.BatchOpenCardResult;
import com.qms.campuscard.dto.CampusCardDTO;
import com.qms.campuscard.dto.CardOperationRequest;
import com.qms.campuscard.dto.OpenCardRequest;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CardChangeRecord;
import com.qms.campuscard.service.CampusCardService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CampusCardController {

    private final CampusCardService campusCardService;
    private final LogUtil logUtil;

    public CampusCardController(CampusCardService campusCardService, LogUtil logUtil) {
        this.campusCardService = campusCardService;
        this.logUtil = logUtil;
    }

    /**
     * 校园卡开卡接口：为学生或教师创建校园卡，并同步初始化对应账户。
     */
    @PostMapping("/card/open")
    public Result<CampusCard> openCard(@RequestBody OpenCardRequest request) {
        try {
            String userNo = request.getUserNo();
            String userType = request.getUserType();
            String remark = request.getRemark();
            
            CampusCard campusCard = campusCardService.openCard(userNo, userType, remark);
            // 记录日志
            logUtil.recordLog(1L, "开卡", "campus_card", campusCard.getId(), "开卡成功：" + campusCard.getCardNo() + (remark != null ? "，原因：" + remark : ""));
            return Result.success("开卡成功", campusCard);
        } catch (Exception e) {
            // 记录日志
            logUtil.recordLog(1L, "开卡", "campus_card", null, "开卡失败：" + e.getMessage());
            return Result.error("开卡失败：" + e.getMessage());
        }
    }

    /**
     * 批量开卡接口：为未开卡学生或教师批量创建校园卡和账户。
     */
    @PostMapping("/card/batch-open")
    public Result<BatchOpenCardResult> batchOpenCard(@RequestBody BatchOpenCardRequest request) {
        try {
            if (request == null) {
                return Result.error("批量开卡失败：请求参数不能为空");
            }
            BatchOpenCardResult result = campusCardService.batchOpenCards(request.getUsers(), request.getRemark());
            logUtil.recordLog(1L, "开卡", "campus_card", null, "批量开卡成功：" + result.getSuccessCount() + "人");
            return Result.success("批量开卡成功", result);
        } catch (Exception e) {
            logUtil.recordLog(1L, "开卡", "campus_card", null, "批量开卡失败：" + e.getMessage());
            return Result.error("批量开卡失败：" + e.getMessage());
        }
    }

    /**
     * 校园卡详情接口：根据校园卡ID查询卡片基础信息和关联用户信息。
     */
    @GetMapping("/card/{cardId}")
    public Result<CampusCardDTO> getCardById(@PathVariable Long cardId) {
        CampusCardDTO campusCard = campusCardService.getCardById(cardId);
        if (campusCard != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "campus_card", cardId, "查询校园卡：" + campusCard.getCardNo());
            return Result.success(campusCard);
        } else {
            return Result.error("校园卡不存在");
        }
    }

    /**
     * 用户校园卡查询接口：根据学号或教师号及用户类型查询当前绑定的校园卡。
     */
    @GetMapping("/card/by-user-no/{userNo}/{userType}")
    public Result<CampusCardDTO> getCardByUserNo(@PathVariable String userNo, @PathVariable String userType) {
        CampusCardDTO campusCard = campusCardService.getCardByUserNo(userNo, userType);
        if (campusCard != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "campus_card", null, "根据用户编号查询校园卡：" + userNo);
            return Result.success(campusCard);
        } else {
            return Result.error("校园卡不存在");
        }
    }

    /**
     * 卡号查询接口：根据实体卡号查询校园卡详情，用于充值、消费等场景前置校验。
     */
    @GetMapping("/card/by-card-no/{cardNo}")
    public Result<CampusCardDTO> getCardByCardNo(@PathVariable String cardNo) {
        CampusCardDTO campusCard = campusCardService.getCardByCardNo(cardNo);
        if (campusCard != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "campus_card", campusCard.getId(), "根据卡号查询校园卡：" + cardNo);
            return Result.success(campusCard);
        } else {
            return Result.error("校园卡不存在");
        }
    }

    /**
     * 挂失接口：将校园卡状态改为挂失，阻止后续消费、通行等卡片操作。
     */
    @PostMapping("/card/loss")
    public Result<Boolean> lossCard(@RequestBody CardOperationRequest request) {
        CampusCardDTO campusCard = campusCardService.getCardById(request.getCardId());
        boolean success = campusCardService.lossCard(request.getCardId(), request.getRemark());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "campus_card", request.getCardId(), "挂失校园卡：" + (campusCard != null ? campusCard.getCardNo() : "未知") + (request.getRemark() != null ? "，原因：" + request.getRemark() : ""));
            return Result.success("挂失成功", true);
        } else {
            return Result.error("挂失失败");
        }
    }

    /**
     * 解挂接口：将已挂失校园卡恢复为可用状态。
     */
    @PostMapping("/card/unloss")
    public Result<Boolean> unlossCard(@RequestBody CardOperationRequest request) {
        CampusCardDTO campusCard = campusCardService.getCardById(request.getCardId());
        boolean success = campusCardService.unlossCard(request.getCardId(), request.getRemark());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "campus_card", request.getCardId(), "解挂校园卡：" + (campusCard != null ? campusCard.getCardNo() : "未知") + (request.getRemark() != null ? "，原因：" + request.getRemark() : ""));
            return Result.success("解挂成功", true);
        } else {
            return Result.error("解挂失败");
        }
    }

    /**
     * 注销接口：停用校园卡并记录注销原因，适用于离校、换卡等业务场景。
     */
    @PostMapping("/card/cancel")
    public Result<Boolean> cancelCard(@RequestBody CardOperationRequest request) {
        CampusCardDTO campusCard = campusCardService.getCardById(request.getCardId());
        boolean success = campusCardService.cancelCard(request.getCardId(), request.getRemark());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "campus_card", request.getCardId(), "注销校园卡：" + (campusCard != null ? campusCard.getCardNo() : "未知") + (request.getRemark() != null ? "，原因：" + request.getRemark() : ""));
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

    /**
     * 账户详情接口：根据卡号查询账户余额、卡片和持卡人相关信息。
     */
    @GetMapping("/account/by-card-no/{cardNo}")
    public Result<AccountDTO> getAccountByCardNo(@PathVariable String cardNo) {
        AccountDTO account = campusCardService.getAccountDetailByCardNo(cardNo);
        if (account != null) {
            // 记录日志
            logUtil.recordLog(1L, "查询", "account", account.getId(), "根据卡号查询账户信息：" + cardNo);
            return Result.success(account);
        } else {
            return Result.error("账户不存在");
        }
    }

    /**
     * 余额查询接口：根据校园卡ID查询当前账户余额。
     */
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

    /**
     * 校园卡分页列表接口：按卡号、状态等条件查询后台卡片管理列表。
     */
    @GetMapping("/card/list")
    public Result<com.baomidou.mybatisplus.core.metadata.IPage<CampusCardDTO>> getCardList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String cardNo,
            @RequestParam(required = false) Integer status) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<com.qms.campuscard.entity.CampusCard> pageParam = new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, size);
        com.baomidou.mybatisplus.core.metadata.IPage<CampusCardDTO> cardPage = campusCardService.getCardList(pageParam, cardNo, status);
        // 记录日志
        logUtil.recordLog(1L, "查询", "campus_card", null, "查询校园卡列表");
        return Result.success(cardPage);
    }

    /**
     * 账户流水接口：分页查询指定账户的充值、消费等资金变动记录。
     */
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
