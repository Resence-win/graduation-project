package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.*;
import com.qms.campuscard.mapper.*;
import com.qms.campuscard.service.CampusCardService;
import com.qms.campuscard.util.RedisUtil;
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

    private static final String CARD_INFO_KEY_PREFIX = "card:info:";
    private static final String CARD_INFO_NO_KEY_PREFIX = "card:info:no:";
    private static final String ACCOUNT_BALANCE_KEY_PREFIX = "account:balance:";
    private static final long CACHE_EXPIRE_TIME = 1800;

    @Resource
    private CampusCardMapper campusCardMapper;

    @Resource
    private AccountMapper accountMapper;

    @Resource
    private AccountFlowMapper accountFlowMapper;

    @Resource
    private CardChangeRecordMapper cardChangeRecordMapper;

    @Resource
    private StudentMapper studentMapper;

    @Resource
    private TeacherMapper teacherMapper;

    @Resource
    private RedisUtil redisUtil;

    private void recordCardChange(Long cardId, String operationType, String oldValue, String newValue) {
        CardChangeRecord record = new CardChangeRecord();
        record.setCardId(cardId);
        record.setOperationType(operationType);
        record.setOperatorId(1L); // 默认为系统操作
        // 将 oldValue 和 newValue 合并为 remark
        String remark = "";
        if (oldValue != null) {
            remark += "旧值: " + oldValue;
        }
        if (newValue != null) {
            if (!remark.isEmpty()) {
                remark += ", ";
            }
            remark += "新值: " + newValue;
        }
        record.setRemark(remark);
        record.setIsDeleted(0);
        record.setCreateTime(LocalDateTime.now());
        cardChangeRecordMapper.insert(record);
    }

    @Override
    @Transactional
    public CampusCard openCard(String userNo, String userType) {
        // 校验用户是否存在
        Long userId = null;
        try {
            System.out.println("userNo: " + userNo);
            System.out.println("userType: " + userType);
            if (userType == null) {
                throw new RuntimeException("用户类型不能为空");
            }
            if ("student".equals(userType)) {
                QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
                studentQueryWrapper.eq("student_no", userNo);
                studentQueryWrapper.eq("is_deleted", 0);
                Student student = studentMapper.selectOne(studentQueryWrapper);
                if (student == null) {
                    throw new RuntimeException("学生不存在");
                }
                userId = student.getId();
            } else if ("teacher".equals(userType)) {
                QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
                teacherQueryWrapper.eq("teacher_no", userNo);
                teacherQueryWrapper.eq("is_deleted", 0);
                Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
                if (teacher == null) {
                    throw new RuntimeException("教师不存在");
                }
                userId = teacher.getId();
            } else {
                throw new RuntimeException("用户类型错误，只能是student或teacher");
            }
        } catch (Exception e) {
            System.out.println("开卡异常: " + e.getMessage());
            e.printStackTrace();
            throw e;
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
    public com.qms.campuscard.dto.CampusCardDTO getCardById(Long cardId) {
        String cacheKey = CARD_INFO_KEY_PREFIX + cardId;
        com.qms.campuscard.dto.CampusCardDTO cachedDto = (com.qms.campuscard.dto.CampusCardDTO) redisUtil.get(cacheKey);
        if (cachedDto != null) {
            return cachedDto;
        }

        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("id", cardId);
        queryWrapper.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(queryWrapper);
        
        if (campusCard == null) {
            return null;
        }
        
        // 转换为DTO
        com.qms.campuscard.dto.CampusCardDTO dto = new com.qms.campuscard.dto.CampusCardDTO();
        dto.setId(campusCard.getId());
        dto.setCardNo(campusCard.getCardNo());
        dto.setUserId(campusCard.getUserId());
        dto.setUserType(campusCard.getUserType());
        dto.setStatus(campusCard.getStatus());
        dto.setIssueDate(campusCard.getIssueDate());
        dto.setExpireDate(campusCard.getExpireDate());
        dto.setCreateTime(campusCard.getCreateTime());
        dto.setUpdateTime(campusCard.getUpdateTime());
        dto.setIsDeleted(campusCard.getIsDeleted());
        
        // 获取用户信息
        if ("student".equals(campusCard.getUserType())) {
            QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
            studentQueryWrapper.eq("id", campusCard.getUserId());
            studentQueryWrapper.eq("is_deleted", 0);
            Student student = studentMapper.selectOne(studentQueryWrapper);
            if (student != null) {
                dto.setUserNo(student.getStudentNo());
                dto.setUserName(student.getName());
            }
        } else if ("teacher".equals(campusCard.getUserType())) {
            QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
            teacherQueryWrapper.eq("id", campusCard.getUserId());
            teacherQueryWrapper.eq("is_deleted", 0);
            Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
            if (teacher != null) {
                dto.setUserNo(teacher.getTeacherNo());
                dto.setUserName(teacher.getName());
            }
        }
        
        // 获取账户余额
        Account account = getAccountByCardId(campusCard.getId());
        if (account != null) {
            dto.setBalance(account.getBalance());
        }
        
        redisUtil.set(cacheKey, dto, CACHE_EXPIRE_TIME);
        return dto;
    }

    @Override
    public com.qms.campuscard.dto.CampusCardDTO getCardByUserNo(String userNo, String userType) {
        // 首先根据用户类型和编号获取用户ID
        Long userId = null;
        if ("student".equals(userType)) {
            QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
            studentQueryWrapper.eq("student_no", userNo);
            studentQueryWrapper.eq("is_deleted", 0);
            Student student = studentMapper.selectOne(studentQueryWrapper);
            if (student != null) {
                userId = student.getId();
            }
        } else if ("teacher".equals(userType)) {
            QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
            teacherQueryWrapper.eq("teacher_no", userNo);
            teacherQueryWrapper.eq("is_deleted", 0);
            Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
            if (teacher != null) {
                userId = teacher.getId();
            }
        }
        
        if (userId == null) {
            return null;
        }
        
        // 根据用户ID和类型查询校园卡
        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.eq("user_type", userType);
        queryWrapper.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(queryWrapper);
        
        if (campusCard == null) {
            return null;
        }
        
        // 转换为DTO
        com.qms.campuscard.dto.CampusCardDTO dto = new com.qms.campuscard.dto.CampusCardDTO();
        dto.setId(campusCard.getId());
        dto.setCardNo(campusCard.getCardNo());
        dto.setUserId(campusCard.getUserId());
        dto.setUserType(campusCard.getUserType());
        dto.setUserNo(userNo);
        dto.setStatus(campusCard.getStatus());
        dto.setIssueDate(campusCard.getIssueDate());
        dto.setExpireDate(campusCard.getExpireDate());
        dto.setCreateTime(campusCard.getCreateTime());
        dto.setUpdateTime(campusCard.getUpdateTime());
        dto.setIsDeleted(campusCard.getIsDeleted());
        
        // 获取用户姓名
        if ("student".equals(userType)) {
            Student student = studentMapper.selectById(userId);
            if (student != null) {
                dto.setUserName(student.getName());
            }
        } else if ("teacher".equals(userType)) {
            Teacher teacher = teacherMapper.selectById(userId);
            if (teacher != null) {
                dto.setUserName(teacher.getName());
            }
        }
        
        // 获取账户余额
        Account account = getAccountByCardId(campusCard.getId());
        if (account != null) {
            dto.setBalance(account.getBalance());
        }
        
        return dto;
    }

    @Override
    @Transactional
    public boolean lossCard(Long cardId) {
        CampusCard campusCard = campusCardMapper.selectById(cardId);
        if (campusCard == null || campusCard.getStatus() != 1) {
            return false;
        }

        // 更新校园卡状态
        campusCard.setStatus(2);
        campusCard.setUpdateTime(LocalDateTime.now());
        boolean success = campusCardMapper.updateById(campusCard) > 0;

        if (success) {
            // 更新账户状态
            Account account = getAccountByCardId(cardId);
            if (account != null) {
                account.setStatus(2);
                account.setUpdateTime(LocalDateTime.now());
                accountMapper.updateById(account);
            }
            // 记录挂失操作
            recordCardChange(cardId, "挂失", "正常", "挂失");
            // 清除缓存
            clearCardCache(cardId, campusCard.getCardNo());
        }
        return success;
    }

    @Override
    @Transactional
    public boolean unlossCard(Long cardId) {
        CampusCard campusCard = campusCardMapper.selectById(cardId);
        if (campusCard == null || campusCard.getStatus() != 2) {
            return false;
        }

        // 更新校园卡状态
        campusCard.setStatus(1);
        campusCard.setUpdateTime(LocalDateTime.now());
        boolean success = campusCardMapper.updateById(campusCard) > 0;

        if (success) {
            // 更新账户状态
            Account account = getAccountByCardId(cardId);
            if (account != null) {
                account.setStatus(1);
                account.setUpdateTime(LocalDateTime.now());
                accountMapper.updateById(account);
            }
            // 记录解挂操作
            recordCardChange(cardId, "解挂", "挂失", "正常");
            // 清除缓存
            clearCardCache(cardId, campusCard.getCardNo());
        }
        return success;
    }

    @Override
    @Transactional
    public boolean cancelCard(Long cardId) {
        CampusCard campusCard = campusCardMapper.selectById(cardId);
        if (campusCard == null) {
            return false;
        }

        // 更新校园卡状态
        campusCard.setStatus(0);
        campusCard.setUpdateTime(LocalDateTime.now());
        boolean success = campusCardMapper.updateById(campusCard) > 0;

        if (success) {
            // 更新账户状态
            Account account = getAccountByCardId(cardId);
            if (account != null) {
                account.setStatus(0);
                account.setUpdateTime(LocalDateTime.now());
                accountMapper.updateById(account);
            }
            // 记录注销操作
            recordCardChange(cardId, "注销", null, "校园卡注销");
            // 清除缓存
            clearCardCache(cardId, campusCard.getCardNo());
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
    public Account getAccountByCardNo(String cardNo) {
        // 首先根据卡号查询校园卡
        QueryWrapper<CampusCard> cardQueryWrapper = new QueryWrapper<>();
        cardQueryWrapper.eq("card_no", cardNo);
        cardQueryWrapper.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(cardQueryWrapper);
        
        if (campusCard == null) {
            return null;
        }
        
        // 根据校园卡ID查询账户
        return getAccountByCardId(campusCard.getId());
    }

    @Override
    public com.qms.campuscard.dto.CampusCardDTO getCardByCardNo(String cardNo) {
        String cacheKey = CARD_INFO_NO_KEY_PREFIX + cardNo;
        com.qms.campuscard.dto.CampusCardDTO cachedDto = (com.qms.campuscard.dto.CampusCardDTO) redisUtil.get(cacheKey);
        if (cachedDto != null) {
            return cachedDto;
        }

        // 根据卡号查询校园卡
        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_no", cardNo);
        queryWrapper.eq("is_deleted", 0);
        CampusCard campusCard = campusCardMapper.selectOne(queryWrapper);
        
        if (campusCard == null) {
            return null;
        }
        
        // 转换为DTO
        com.qms.campuscard.dto.CampusCardDTO dto = new com.qms.campuscard.dto.CampusCardDTO();
        dto.setId(campusCard.getId());
        dto.setCardNo(campusCard.getCardNo());
        dto.setUserId(campusCard.getUserId());
        dto.setUserType(campusCard.getUserType());
        dto.setStatus(campusCard.getStatus());
        dto.setIssueDate(campusCard.getIssueDate());
        dto.setExpireDate(campusCard.getExpireDate());
        dto.setCreateTime(campusCard.getCreateTime());
        dto.setUpdateTime(campusCard.getUpdateTime());
        dto.setIsDeleted(campusCard.getIsDeleted());
        
        // 获取用户信息
        if ("student".equals(campusCard.getUserType())) {
            QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
            studentQueryWrapper.eq("id", campusCard.getUserId());
            studentQueryWrapper.eq("is_deleted", 0);
            Student student = studentMapper.selectOne(studentQueryWrapper);
            if (student != null) {
                dto.setUserNo(student.getStudentNo());
                dto.setUserName(student.getName());
            }
        } else if ("teacher".equals(campusCard.getUserType())) {
            QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
            teacherQueryWrapper.eq("id", campusCard.getUserId());
            teacherQueryWrapper.eq("is_deleted", 0);
            Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
            if (teacher != null) {
                dto.setUserNo(teacher.getTeacherNo());
                dto.setUserName(teacher.getName());
            }
        }
        
        // 获取账户余额
        Account account = getAccountByCardId(campusCard.getId());
        if (account != null) {
            dto.setBalance(account.getBalance());
        }
        
        redisUtil.set(cacheKey, dto, CACHE_EXPIRE_TIME);
        return dto;
    }

    @Override
    public BigDecimal getBalance(Long cardId) {
        String cacheKey = ACCOUNT_BALANCE_KEY_PREFIX + cardId;
        BigDecimal cachedBalance = (BigDecimal) redisUtil.get(cacheKey);
        if (cachedBalance != null) {
            return cachedBalance;
        }

        Account account = getAccountByCardId(cardId);
        BigDecimal balance = account != null ? account.getBalance() : BigDecimal.ZERO;
        redisUtil.set(cacheKey, balance, CACHE_EXPIRE_TIME);
        return balance;
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
    public com.baomidou.mybatisplus.core.metadata.IPage<com.qms.campuscard.dto.CampusCardDTO> getCardList(Page<CampusCard> page, String cardNo, Integer status) {
        QueryWrapper<CampusCard> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("is_deleted", 0);
        
        if (cardNo != null && !cardNo.isEmpty()) {
            queryWrapper.like("card_no", cardNo);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.orderByDesc("create_time");
        
        // 查询校园卡列表
        com.baomidou.mybatisplus.core.metadata.IPage<CampusCard> cardPage = campusCardMapper.selectPage(page, queryWrapper);
        
        // 转换为DTO列表
        return cardPage.convert(campusCard -> {
            com.qms.campuscard.dto.CampusCardDTO dto = new com.qms.campuscard.dto.CampusCardDTO();
            dto.setId(campusCard.getId());
            dto.setCardNo(campusCard.getCardNo());
            dto.setUserId(campusCard.getUserId());
            dto.setUserType(campusCard.getUserType());
            dto.setStatus(campusCard.getStatus());
            dto.setIssueDate(campusCard.getIssueDate());
            dto.setExpireDate(campusCard.getExpireDate());
            dto.setCreateTime(campusCard.getCreateTime());
            dto.setUpdateTime(campusCard.getUpdateTime());
            dto.setIsDeleted(campusCard.getIsDeleted());
            
            // 获取用户信息
            if ("student".equals(campusCard.getUserType())) {
                QueryWrapper<Student> studentQueryWrapper = new QueryWrapper<>();
                studentQueryWrapper.eq("id", campusCard.getUserId());
                studentQueryWrapper.eq("is_deleted", 0);
                Student student = studentMapper.selectOne(studentQueryWrapper);
                if (student != null) {
                    dto.setUserNo(student.getStudentNo());
                    dto.setUserName(student.getName());
                }
            } else if ("teacher".equals(campusCard.getUserType())) {
                QueryWrapper<Teacher> teacherQueryWrapper = new QueryWrapper<>();
                teacherQueryWrapper.eq("id", campusCard.getUserId());
                teacherQueryWrapper.eq("is_deleted", 0);
                Teacher teacher = teacherMapper.selectOne(teacherQueryWrapper);
                if (teacher != null) {
                    dto.setUserNo(teacher.getTeacherNo());
                    dto.setUserName(teacher.getName());
                }
            }
            
            return dto;
        });
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

    private void clearCardCache(Long cardId, String cardNo) {
        redisUtil.del(CARD_INFO_KEY_PREFIX + cardId);
        redisUtil.del(CARD_INFO_NO_KEY_PREFIX + cardNo);
        redisUtil.del(ACCOUNT_BALANCE_KEY_PREFIX + cardId);
    }
}
