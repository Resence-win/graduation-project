package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.ConsumeRecord;
import com.qms.campuscard.entity.Merchant;
import com.qms.campuscard.dto.ConsumeRecordDTO;
import com.qms.campuscard.mapper.AccountFlowMapper;
import com.qms.campuscard.mapper.AccountMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.ConsumeRecordMapper;
import com.qms.campuscard.mapper.MerchantMapper;
import com.qms.campuscard.service.ConsumeService;
import com.qms.campuscard.util.RedisUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class ConsumeServiceImpl implements ConsumeService {

    private static final String ACCOUNT_BALANCE_KEY_PREFIX = "account:balance:";

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private ConsumeRecordMapper consumeRecordMapper;

    @Resource
    private AccountFlowMapper accountFlowMapper;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private MerchantMapper merchantMapper;

    @Resource
    private RedisUtil redisUtil;

    @Override
    @Transactional
    public boolean consume(Long cardId, Long merchantId, BigDecimal amount) {
        // 检查校园卡状态
        QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
        cardQuery.eq("id", cardId);
        cardQuery.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
        if (campusCard == null) {
            throw new RuntimeException("校园卡不存在");
        }
        if (campusCard.getStatus() == 0) {
            throw new RuntimeException("校园卡已注销，无法消费");
        }
        if (campusCard.getStatus() == 2) {
            throw new RuntimeException("校园卡已挂失，无法消费");
        }
        
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

        // 清除余额缓存
        redisUtil.del(ACCOUNT_BALANCE_KEY_PREFIX + cardId);

        return true;
    }

    @Override
    @Transactional
    public boolean consumeByCardNo(String cardNo, Long merchantId, BigDecimal amount) {
        // 根据卡号查询校园卡
        QueryWrapper<CampusCard> cardQuery = new QueryWrapper<>();
        cardQuery.eq("card_no", cardNo);
        cardQuery.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(cardQuery);
        if (campusCard == null) {
            throw new RuntimeException("校园卡不存在");
        }
        
        // 调用现有的消费方法
        return consume(campusCard.getId(), merchantId, amount);
    }

    @Override
    public IPage<ConsumeRecordDTO> getConsumeRecords(String cardId, Long merchantId, String startDate, String endDate, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        // 查找消费记录
        Page<ConsumeRecord> pageParam = new Page<>(page, size);
        QueryWrapper<ConsumeRecord> consumeQuery = new QueryWrapper<>();
        consumeQuery.eq("is_deleted", 0);
        
        if (cardId != null && !cardId.isEmpty()) {
            // 直接按卡号查询校园卡
            QueryWrapper<CampusCard> cardQueryByNo = new QueryWrapper<>();
            cardQueryByNo.eq("card_no", cardId);
            cardQueryByNo.eq("is_deleted", 0);
            CampusCard campusCard = campusCardMapper.selectOne(cardQueryByNo);
            
            if (campusCard != null) {
                // 查找账户
                QueryWrapper<Account> accountQuery = new QueryWrapper<>();
                accountQuery.eq("card_id", campusCard.getId());
                accountQuery.eq("is_deleted", 0);
                Account account = accountMapper.selectOne(accountQuery);
                if (account != null) {
                    consumeQuery.eq("account_id", account.getId());
                } else {
                    // 账户不存在，返回空页
                    return new Page<>(page, size);
                }
            } else {
                // 校园卡不存在，返回空页
                return new Page<>(page, size);
            }
        }
        
        if (merchantId != null) {
            consumeQuery.eq("merchant_id", merchantId);
        }
        
        if (startDate != null && !startDate.isEmpty()) {
            consumeQuery.ge("consume_time", startDate + " 00:00:00");
        }
        
        if (endDate != null && !endDate.isEmpty()) {
            consumeQuery.le("consume_time", endDate + " 23:59:59");
        }
        
        consumeQuery.orderByDesc("consume_time");
        IPage<ConsumeRecord> consumePage = consumeRecordMapper.selectPage(pageParam, consumeQuery);
        
        // 转换为DTO并添加卡号和商户名称
        return consumePage.convert(consumeRecord -> {
            ConsumeRecordDTO dto = new ConsumeRecordDTO();
            dto.setId(consumeRecord.getId());
            dto.setAccountId(consumeRecord.getAccountId());
            dto.setMerchantId(consumeRecord.getMerchantId());
            dto.setAmount(consumeRecord.getAmount());
            dto.setBalanceAfter(consumeRecord.getBalanceAfter());
            dto.setStatus(consumeRecord.getStatus());
            dto.setConsumeTime(consumeRecord.getConsumeTime());
            
            // 获取卡号信息
            QueryWrapper<Account> accountQuery = new QueryWrapper<>();
            accountQuery.eq("id", consumeRecord.getAccountId());
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
            
            // 获取商户名称
            QueryWrapper<Merchant> merchantQuery = new QueryWrapper<>();
            merchantQuery.eq("id", consumeRecord.getMerchantId());
            merchantQuery.eq("is_deleted", 0);
            Merchant merchant = merchantMapper.selectOne(merchantQuery);
            if (merchant != null) {
                dto.setMerchantName(merchant.getMerchantName());
            }
            
            return dto;
        });
    }
}
