package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Account;
import com.qms.campuscard.entity.AccountFlow;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.CardChangeRecord;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.mapper.AccountFlowMapper;
import com.qms.campuscard.mapper.AccountMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.CardChangeRecordMapper;
import com.qms.campuscard.service.CampusCardService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class CampusCardServiceImpl implements CampusCardService {

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private com.qms.campuscard.mapper.StudentMapper studentMapper;

    @Resource
    private com.qms.campuscard.mapper.TeacherMapper teacherMapper;

    @Resource
    private CardChangeRecordMapper cardChangeRecordMapper;

    @Resource
    private AccountFlowMapper accountFlowMapper;

    private void recordCardChange(Long cardId, String operationType, Long operatorId, String remark) {
        CardChangeRecord record = new CardChangeRecord();
        record.setCardId(cardId);
        record.setOperationType(operationType);
        record.setOperatorId(operatorId);
        record.setRemark(remark);
        record.setIsDeleted(0);
        record.setCreateTime(LocalDateTime.now());
        cardChangeRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public CampusCard openCard(Long userId, String userType) {
        // 校验用户是否存在
        if ("student".equals(userType)) {
            Student student = studentMapper.selectById(userId);
            if (student == null || student.getIsDeleted() == 1) {
                throw new RuntimeException("学生不存在");
            }
        } else if ("teacher".equals(userType)) {
            Teacher teacher = teacherMapper.selectById(userId);
            if (teacher == null || teacher.getIsDeleted() == 1) {
                throw new RuntimeException("教师不存在");
            }
        } else {
            throw new RuntimeException("用户类型错误，只能是student或teacher");
        }

        // 校验是否已开卡（只有注销状态的卡可以重新开卡）
        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("user_type", userType);
        queryWrapper.eq("is_deleted", 0);
        List<CampusCard> existingCards = campusCardMapper.selectList(queryWrapper);
        
        for (CampusCard card : existingCards) {
            if (card.getStatus() != 0) { // 0表示注销状态
                throw new RuntimeException("该用户已有在用的校园卡，不能重复开卡");
            }
        }

        // 生成卡号：年月日 + 6位随机数
        String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomNum = String.format("%06d", new Random().nextInt(1000000));
        String cardNo = dateStr + randomNum;

        // 创建校园卡
        CampusCard campusCard = new CampusCard();
        campusCard.setCardNo(cardNo);
        campusCard.setUserId(userId);
        campusCard.setUserType(userType);
        campusCard.setStatus(1);
        campusCard.setIssueDate(LocalDate.now());
        campusCard.setExpireDate(LocalDate.now().plusYears(4));
        campusCard.setIsDeleted(0);
        campusCard.setCreateTime(LocalDateTime.now());
        campusCardMapper.insert(campusCard);

        // 创建账户
        Account account = new Account();
        account.setCardId(campusCard.getId());
        account.setBalance(BigDecimal.ZERO);
        account.setStatus(1);
        account.setIsDeleted(0);
        account.setCreateTime(LocalDateTime.now());
        accountMapper.insert(account);

        // 记录开卡操作
        recordCardChange(campusCard.getId(), "开卡", null, "用户ID: " + userId + ", 用户类型: " + userType);

        return campusCard;
    }

    @Override
    public CampusCard getCardById(Long cardId) {
        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", cardId);
        queryWrapper.eq("is_deleted", 0);
        return campusCardMapper.selectOne(queryWrapper);
    }

    @Override
    @Transactional
    public boolean lossCard(Long cardId) {
        CampusCard campusCard = new CampusCard();
        campusCard.setId(cardId);
        campusCard.setStatus(2);
        campusCard.setUpdateTime(LocalDateTime.now());
        boolean success = campusCardMapper.updateById(campusCard) > 0;
        if (success) {
            // 记录挂失操作
            recordCardChange(cardId, "挂失", null, "校园卡挂失");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean unlossCard(Long cardId) {
        CampusCard campusCard = new CampusCard();
        campusCard.setId(cardId);
        campusCard.setStatus(1);
        campusCard.setUpdateTime(LocalDateTime.now());
        boolean success = campusCardMapper.updateById(campusCard) > 0;
        if (success) {
            // 记录解挂操作
            recordCardChange(cardId, "解挂", null, "校园卡解挂");
        }
        return success;
    }

    @Override
    @Transactional
    public boolean cancelCard(Long cardId) {
        CampusCard campusCard = new CampusCard();
        campusCard.setId(cardId);
        campusCard.setStatus(0);
        campusCard.setUpdateTime(LocalDateTime.now());
        boolean success = campusCardMapper.updateById(campusCard) > 0;
        if (success) {
            // 记录注销操作
            recordCardChange(cardId, "注销", null, "校园卡注销");
        }
        return success;
    }

    @Override
    public Account getAccountByCardId(Long cardId) {
        QueryWrapper<Account> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("is_deleted", 0);
        return accountMapper.selectOne(queryWrapper);
    }

    @Override
    public BigDecimal getBalance(Long cardId) {
        Account account = getAccountByCardId(cardId);
        return account != null ? account.getBalance() : BigDecimal.ZERO;
    }

    @Override
    public List<CardChangeRecord> getCardOperationRecords(Long cardId) {
        QueryWrapper<CardChangeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_id", cardId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return cardChangeRecordMapper.selectList(queryWrapper);
    }

    @Override
    public com.baomidou.mybatisplus.core.metadata.IPage<AccountFlow> getAccountFlow(Long accountId, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        Page<AccountFlow> pageParam = new Page<>(page, size);
        QueryWrapper<AccountFlow> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_id", accountId);
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return accountFlowMapper.selectPage(pageParam, queryWrapper);
    }
}
