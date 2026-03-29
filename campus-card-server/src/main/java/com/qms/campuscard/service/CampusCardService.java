package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CardChangeRecord;

import java.math.BigDecimal;
import java.util.List;

public interface CampusCardService {

    CampusCard openCard(Long userId, String userType);

    CampusCard getCardById(Long cardId);

    IPage<CampusCard> getCardList(Page<CampusCard> page, String cardNo, Integer status);

    boolean lossCard(Long cardId);

    boolean unlossCard(Long cardId);

    boolean cancelCard(Long cardId);

    Account getAccountByCardId(Long cardId);

    BigDecimal getBalance(Long cardId);

    List<CardChangeRecord> getCardOperationRecords(Long cardId);

    IPage<AccountFlow> getAccountFlow(Long accountId, Integer page, Integer size);
}
