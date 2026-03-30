package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.RechargeRecord;
import com.qms.campuscard.dto.RechargeRecordDTO;
import com.qms.campuscard.mapper.AccountFlowMapper;
import com.qms.campuscard.mapper.AccountMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
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

    @Resource
    private CampusCardMapper campusCardMapper;

    @Override
    @Transactional
    public boolean recharge(Long cardId, BigDecimal amount, String rechargeType) {
        // 检查校园卡状态
        QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
        cardQuery.eq("id", cardId);
        cardQuery.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
        if (campusCard == null) {
            throw new RuntimeException("校园卡不存在");
        }
        if (campusCard.getStatus() == 0) {
            throw new RuntimeException("校园卡已注销，无法充值");
        }
        if (campusCard.getStatus() == 2) {
            throw new RuntimeException("校园卡已挂失，无法充值");
        }
        
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
    @Transactional
    public boolean rechargeByCardNo(String cardNo, BigDecimal amount, String rechargeType) {
        // 根据卡号查询校园卡
        QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
        cardQuery.eq("card_no", cardNo);
        cardQuery.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
        if (campusCard == null) {
            throw new RuntimeException("校园卡不存在");
        }
        
        // 调用现有的充值方法
        return recharge(campusCard.getId(), amount, rechargeType);
    }

    @Override
    public IPage<RechargeRecordDTO> getRechargeRecords(Long cardId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        // 查找充值记录
        Page<RechargeRecord> pageParam = new Page<>(page, size);
        QueryWrapper<RechargeRecord> rechargeQuery = new QueryWrapper<>();
        rechargeQuery.eq("is_deleted", 0);
        
        if (cardId != null) {
            // 查找账户
            QueryWrapper<Account> accountQuery = new QueryWrapper<>();
            accountQuery.eq("card_id", cardId);
            accountQuery.eq("is_deleted", 0);
            Account account = accountMapper.selectOne(accountQuery);
            if (account != null) {
                rechargeQuery.eq("account_id", account.getId());
            } else {
                // 账户不存在，返回空页
                return new Page<>(page, size);
            }
        }
        
        rechargeQuery.orderByDesc("create_time");
        IPage<RechargeRecord> rechargePage = rechargeRecordMapper.selectPage(pageParam, rechargeQuery);
        
        // 转换为DTO并添加卡号信息
        return rechargePage.convert(rechargeRecord -> {
            RechargeRecordDTO dto = new RechargeRecordDTO();
            dto.setId(rechargeRecord.getId());
            dto.setAccountId(rechargeRecord.getAccountId());
            dto.setAmount(rechargeRecord.getAmount());
            dto.setRechargeType(rechargeRecord.getRechargeType());
            dto.setOperatorId(rechargeRecord.getOperatorId());
            dto.setCreateTime(rechargeRecord.getCreateTime());
            
            // 获取卡号信息
            QueryWrapper<Account> accountQuery = new QueryWrapper<>();
            accountQuery.eq("id", rechargeRecord.getAccountId());
            accountQuery.eq("is_deleted", 0);
            Account account = accountMapper.selectOne(accountQuery);
            if (account != null) {
                QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
                cardQuery.eq("id", account.getCardId());
                cardQuery.eq("is_deleted", 0);
                CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
                if (campusCard != null) {
                    dto.setCardNo(campusCard.getCardNo());
                }
            }
            
            return dto;
        });
    }
}
