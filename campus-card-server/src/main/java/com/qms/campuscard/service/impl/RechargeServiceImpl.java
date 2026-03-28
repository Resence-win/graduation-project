package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.RechargeRecord;
import com.qms.campuscard.mapper.AccountFlowMapper;
import com.qms.campuscard.mapper.AccountMapper;
import com.qms.campuscard.mapper.RechargeRecordMapper;
import com.qms.campuscard.service.RechargeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class RechargeServiceImpl implements RechargeService {

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private RechargeRecordMapper rechargeRecordMapper;

    @Resource
    private AccountFlowMapper accountFlowMapper;

    @Override
    @Transactional
    public boolean recharge(Long cardId, BigDecimal amount, String rechargeType) {
        // 查找账户
        QueryWrapper<Account> accountQuery = new QueryWrapper<>();
        accountQuery.eq("card_id", cardId);
        accountQuery.eq("is_deleted", 0);
        Account account = accountMapper.selectOne(accountQuery);
        if (account == null) {
            throw new RuntimeException("账户不存在");
        }

        // 更新余额
        BigDecimal newBalance = account.getBalance().add(amount);
        account.setBalance(newBalance);
        account.setUpdateTime(LocalDateTime.now());
        accountMapper.updateById(account);

        // 创建充值记录
        RechargeRecord rechargeRecord = new RechargeRecord();
        rechargeRecord.setAccountId(account.getId());
        rechargeRecord.setAmount(amount);
        rechargeRecord.setRechargeType(rechargeType);
        rechargeRecord.setOperatorId(null);
        rechargeRecord.setIsDeleted(0);
        rechargeRecord.setCreateTime(LocalDateTime.now());
        rechargeRecordMapper.insert(rechargeRecord);

        // 创建账户流水
        AccountFlow accountFlow = new AccountFlow();
        accountFlow.setAccountId(account.getId());
        accountFlow.setChangeType("充值");
        accountFlow.setAmount(amount);
        accountFlow.setBalanceAfter(newBalance);
        accountFlow.setRelatedId(rechargeRecord.getId());
        accountFlow.setIsDeleted(0);
        accountFlow.setCreateTime(LocalDateTime.now());
        accountFlowMapper.insert(accountFlow);

        return true;
    }

    @Override
    public IPage<RechargeRecord> getRechargeRecords(Long cardId, Integer page, Integer size) {
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

        // 查找充值记录
        Page<RechargeRecord> pageParam = new Page<>(page, size);
        QueryWrapper<RechargeRecord> rechargeQuery = new QueryWrapper<>();
        rechargeQuery.eq("account_id", account.getId());
        rechargeQuery.eq("is_deleted", 0);
        rechargeQuery.orderByDesc("create_time");
        return rechargeRecordMapper.selectPage(pageParam, rechargeQuery);
    }
}
