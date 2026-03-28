package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.ConsumeRecord;
import com.qms.campuscard.mapper.AccountFlowMapper;
import com.qms.campuscard.mapper.AccountMapper;
import com.qms.campuscard.mapper.ConsumeRecordMapper;
import com.qms.campuscard.service.ConsumeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ConsumeServiceImpl implements ConsumeService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private ConsumeRecordMapper consumeRecordMapper;

    @Resource
    private AccountFlowMapper accountFlowMapper;

    @Override
    @Transactional
    public boolean consume(Long cardId, Long merchantId, BigDecimal amount) {
        // 查找账户
        QueryWrapper<Account> accountQuery = new QueryWrapper<>();
        accountQuery.eq("card_id", cardId);
        accountQuery.eq("is_deleted", 0);
        Account account = accountMapper.selectOne(accountQuery);
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }

        // 校验余额
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("余额不足");
        }

        // 扣减余额
        BigDecimal newBalance = account.getBalance().subtract(amount);
        account.setBalance(newBalance);
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);

        // 创建消费记录
        ConsumeRecord consumeRecord = new ConsumeRecord();
        consumeRecord.setAccountId(account.getId());
        consumeRecord.setMerchantId(merchantId);
        consumeRecord.setAmount(amount);
        consumeRecord.setBalanceAfter(newBalance);
        consumeRecord.setStatus(1);
        consumeRecord.setConsumeTime(LocalDateTime.now());
        consumeRecord.setIsDeleted(0);
        consumeRecordMapper.insert(consumeRecord);

        // 创建账户流水
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccountId(account.getId());
        accountFlow.setChangeType("消费");
        accountFlow.setAmount(amount);
        accountFlow.setBalanceAfter(newBalance);
        accountFlow.setRelatedId(consumeRecord.getId());
        accountFlow.setIsDeleted(0);
        accountFlow.setCreateTime(LocalDateTime.now());
        accountFlowMapper.insert(accountFlow);

        return true;
    }

    @Override
    public IPage<ConsumeRecord> getConsumeRecords(Long cardId, Long merchantId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        // 查找账户
        QueryWrapper<Account> accountQuery = new QueryWrapper<>();
        accountQuery.eq("card_id", cardId);
        accountQuery.eq("is_deleted", 0);
        Account account = accountMapper.selectOne(accountQuery);
        if (account == null) {
            return new Page<>(page, size);
        }

        // 查找消费记录
        Page<ConsumeRecord> pageParam = new Page<>(page, size);
        QueryWrapper<ConsumeRecord> consumeQuery = new QueryWrapper<>();
        consumeQuery.eq("account_id", account.getId());
        if (merchantId != null) {
            consumeQuery.eq("merchant_id", merchantId);
        }
        consumeQuery.eq("is_deleted", 0);
        consumeQuery.orderByDesc("consume_time");
        return consumeRecordMapper.selectPage(pageParam, consumeQuery);
    }
}
