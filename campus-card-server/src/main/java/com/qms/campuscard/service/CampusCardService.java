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

    CampusCard openCard(String userNo, String userType);
    CampusCard openCard(String userNo, String userType, String remark);

    com.qms.campuscard.dto.CampusCardDTO getCardById(Long cardId);

    com.qms.campuscard.dto.CampusCardDTO getCardByUserNo(String userNo, String userType);

    com.baomidou.mybatisplus.core.metadata.IPage<com.qms.campuscard.dto.CampusCardDTO> getCardList(Page<com.qms.campuscard.entity.CampusCard> page, String cardNo, Integer status);

    boolean lossCard(Long cardId);
    boolean lossCard(Long cardId, String remark);

    boolean unlossCard(Long cardId);
    boolean unlossCard(Long cardId, String remark);

    boolean cancelCard(Long cardId);
    boolean cancelCard(Long cardId, String remark);

    Account getAccountByCardId(Long cardId);

    Account getAccountByCardNo(String cardNo);

    com.qms.campuscard.dto.CampusCardDTO getCardByCardNo(String cardNo);

    BigDecimal getBalance(Long cardId);

    List<CardChangeRecord> getCardOperationRecords(Long cardId);

    IPage<AccountFlow> getAccountFlow(Long accountId, Integer page, Integer size);
}
