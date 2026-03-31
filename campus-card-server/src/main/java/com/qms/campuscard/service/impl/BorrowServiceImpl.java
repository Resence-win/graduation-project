package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.BorrowRecord;
import com.qms.campuscard.entity.CampusCard;
import com.qms.campuscard.entity.Student;
import com.qms.campuscard.entity.Teacher;
import com.qms.campuscard.entity.Book;
import com.qms.campuscard.entity.BorrowApplication;
import com.qms.campuscard.mapper.BorrowRecordMapper;
import com.qms.campuscard.mapper.CampusCardMapper;
import com.qms.campuscard.mapper.StudentMapper;
import com.qms.campuscard.mapper.TeacherMapper;
import com.qms.campuscard.mapper.BookMapper;
import com.qms.campuscard.mapper.BorrowApplicationMapper;
import com.qms.campuscard.service.BorrowService;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;

@Service
public class BorrowServiceImpl implements BorrowService {

    @Resource
    private BorrowRecordMapper borrowRecordMapper;

    @Resource
    private BookMapper bookMapper;

    @Resource
    private BorrowApplicationMapper borrowApplicationMapper;
    
    @Resource
    private CampusCardMapper campusCardMapper;
    
    @Resource
    private StudentMapper studentMapper;
    
    @Resource
    private TeacherMapper teacherMapper;

    @Override
    @Transactional
    public boolean borrowBook(Long cardId, Long bookId) {
        // 查询图书
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getIsDeleted() == 1) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStatus() != 1) {
            throw new RuntimeException("图书不可借阅");
        }

        // 创建借阅记录
        BorrowRecord borrowRecord = new BorrowRecord();
        borrowRecord.setCardId(cardId);
        borrowRecord.setBookId(bookId);
        borrowRecord.setBorrowTime(LocalDateTime.now());
        borrowRecord.setStatus(1);
        borrowRecord.setIsDeleted(0);
        borrowRecord.setCreateTime(LocalDateTime.now());
        
        // 插入借阅记录
        int insertResult = borrowRecordMapper.insert(borrowRecord);
        if (insertResult <= 0) {
            return false;
        }
        
        // 更新图书状态为已借出
        book.setStatus(2);
        int updateResult = bookMapper.updateById(book);
        if (updateResult <= 0) {
            throw new RuntimeException("更新图书状态失败");
        }
        
        return true;
    }

    @Override
    @Transactional
    public boolean returnBook(Long borrowId) {
        // 查询借阅记录
        BorrowRecord borrowRecord = borrowRecordMapper.selectById(borrowId);
        if (borrowRecord == null || borrowRecord.getIsDeleted() == 1) {
            throw new RuntimeException("借阅记录不存在");
        }
        if (borrowRecord.getStatus() != 1) {
            throw new RuntimeException("图书已经归还");
        }

        // 更新借阅记录
        borrowRecord.setStatus(2); // 已归还
        borrowRecord.setReturnTime(LocalDateTime.now());
        int updateResult = borrowRecordMapper.updateById(borrowRecord);
        if (updateResult <= 0) {
            return false;
        }

        // 更新图书状态为可借阅
        Book book = bookMapper.selectById(borrowRecord.getBookId());
        if (book == null || book.getIsDeleted() == 1) {
            throw new RuntimeException("图书不存在");
        }
        book.setStatus(1); // 可借阅
        int bookUpdateResult = bookMapper.updateById(book);
        if (bookUpdateResult <= 0) {
            throw new RuntimeException("更新图书状态失败");
        }

        return true;
    }

    @Override
    public IPage<BorrowRecord> getBorrowRecords(Long cardId, Long bookId, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<BorrowRecord> pageParam = new Page<>(page, size);
        QueryWrapper<BorrowRecord> queryWrapper = new QueryWrapper<>();

        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (bookId != null) {
            queryWrapper.eq("book_id", bookId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        
        IPage<BorrowRecord> result = borrowRecordMapper.selectPage(pageParam, queryWrapper);
        
        // 关联查询图书信息，填充书名
        for (BorrowRecord record : result.getRecords()) {
            Book book = bookMapper.selectById(record.getBookId());
            if (book != null) {
                record.setBookName(book.getBookName());
            }
        }
        
        return result;
    }

    @Override
    @Transactional
    public boolean submitBorrowApplication(Long cardId, Long bookId, Integer borrowDays) {
        // 检查用户当前借阅数量（包括正在申请中的）
        int activeCount = getActiveBorrowCount(cardId);
        if (activeCount >= 3) {
            throw new RuntimeException("您当前已借阅或正在申请的图书数量已达上限，无法继续借阅");
        }

        // 查询图书
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getIsDeleted() == 1) {
            throw new RuntimeException("图书不存在");
        }
        if (book.getStatus() != 1) {
            throw new RuntimeException("图书不可借阅");
        }

        // 检查是否已存在待审批的借阅申请
        QueryWrapper<BorrowApplication> applicationQuery = new QueryWrapper<>();
        applicationQuery.eq("card_id", cardId);
        applicationQuery.eq("book_id", bookId);
        applicationQuery.eq("status", 1); // 待审批
        applicationQuery.eq("is_deleted", 0);
        if (borrowApplicationMapper.selectCount(applicationQuery) > 0) {
            throw new RuntimeException("您已提交该图书的借阅申请，正在审批中");
        }

        // 创建借阅申请
        BorrowApplication application = new BorrowApplication();
        application.setCardId(cardId);
        application.setBookId(bookId);
        application.setBorrowDays(borrowDays);
        application.setApplicationTime(LocalDateTime.now());
        application.setStatus(1); // 待审批
        application.setIsDeleted(0);
        application.setCreateTime(LocalDateTime.now());

        // 插入借阅申请
        int insertResult = borrowApplicationMapper.insert(application);
        if (insertResult <= 0) {
            return false;
        }

        return true;
    }

    @Override
    @Transactional
    public boolean approveBorrowApplication(Long applicationId, Integer status, Long operatorId, String remark) {
        // 查询借阅申请
        BorrowApplication application = borrowApplicationMapper.selectById(applicationId);
        if (application == null || application.getIsDeleted() == 1) {
            throw new RuntimeException("借阅申请不存在");
        }
        if (application.getStatus() != 1) {
            throw new RuntimeException("该借阅申请已处理");
        }

        // 更新借阅申请状态
        application.setStatus(status);
        application.setOperatorId(operatorId);
        application.setApprovalTime(LocalDateTime.now());
        application.setRemark(remark);
        int updateResult = borrowApplicationMapper.updateById(application);
        if (updateResult <= 0) {
            return false;
        }

        // 如果审批通过，创建借阅记录
        if (status == 2) {
            // 查询图书
            Book book = bookMapper.selectById(application.getBookId());
            if (book == null || book.getIsDeleted() == 1) {
                throw new RuntimeException("图书不存在");
            }
            if (book.getStatus() != 1) {
                throw new RuntimeException("图书不可借阅");
            }

            // 创建借阅记录
            BorrowRecord borrowRecord = new BorrowRecord();
            borrowRecord.setCardId(application.getCardId());
            borrowRecord.setBookId(application.getBookId());
            borrowRecord.setApplicationId(application.getId());
            borrowRecord.setBorrowTime(LocalDateTime.now());
            borrowRecord.setDueTime(LocalDateTime.now().plusDays(application.getBorrowDays()));
            borrowRecord.setStatus(1); // 借阅中
            borrowRecord.setIsDeleted(0);
            borrowRecord.setCreateTime(LocalDateTime.now());

            // 插入借阅记录
            int insertResult = borrowRecordMapper.insert(borrowRecord);
            if (insertResult <= 0) {
                throw new RuntimeException("创建借阅记录失败");
            }

            // 更新图书状态为已借出
            book.setStatus(2);
            int bookUpdateResult = bookMapper.updateById(book);
            if (bookUpdateResult <= 0) {
                throw new RuntimeException("更新图书状态失败");
            }
        }

        return true;
    }

    @Override
    public IPage<BorrowApplication> getBorrowApplications(Long cardId, Long bookId, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }

        Page<BorrowApplication> pageParam = new Page<>(page, size);
        QueryWrapper<BorrowApplication> queryWrapper = new QueryWrapper<>();

        if (cardId != null) {
            queryWrapper.eq("card_id", cardId);
        }
        if (bookId != null) {
            queryWrapper.eq("book_id", bookId);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }

        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("application_time");
        
        IPage<BorrowApplication> result = borrowApplicationMapper.selectPage(pageParam, queryWrapper);
        
        // 关联查询图书信息、校园卡信息和用户信息
        for (BorrowApplication application : result.getRecords()) {
            // 查询图书信息
            Book book = bookMapper.selectById(application.getBookId());
            if (book != null) {
                application.setBookName(book.getBookName());
            }
            
            // 查询校园卡信息
            CampusCard campusCard = campusCardMapper.selectById(application.getCardId());
            if (campusCard != null) {
                application.setCardNo(campusCard.getCardNo());
                
                // 根据用户类型查询用户信息
                if ("student".equals(campusCard.getUserType())) {
                    Student student = studentMapper.selectById(campusCard.getUserId());
                    if (student != null) {
                        application.setUserName(student.getStudentNo() + " - " + student.getName());
                    }
                } else if ("teacher".equals(campusCard.getUserType())) {
                    Teacher teacher = teacherMapper.selectById(campusCard.getUserId());
                    if (teacher != null) {
                        application.setUserName(teacher.getTeacherNo() + " - " + teacher.getName());
                    }
                }
            }
        }
        
        return result;
    }

    @Override
    public int getActiveBorrowCount(Long cardId) {
        // 计算已批准且借阅中的图书数量
        QueryWrapper<BorrowRecord> recordQueryWrapper = new QueryWrapper<>();
        recordQueryWrapper.eq("card_id", cardId);
        recordQueryWrapper.eq("status", 1); // 借阅中
        recordQueryWrapper.eq("is_deleted", 0);
        Long recordCount = borrowRecordMapper.selectCount(recordQueryWrapper);
        int activeCount = recordCount != null ? recordCount.intValue() : 0;
        
        // 计算正在申请中的借阅数量
        QueryWrapper<BorrowApplication> applicationQueryWrapper = new QueryWrapper<>();
        applicationQueryWrapper.eq("card_id", cardId);
        applicationQueryWrapper.eq("status", 1); // 待审批
        applicationQueryWrapper.eq("is_deleted", 0);
        Long applicationCount = borrowApplicationMapper.selectCount(applicationQueryWrapper);
        int pendingCount = applicationCount != null ? applicationCount.intValue() : 0;
        
        // 总活跃数量 = 已借阅数量 + 正在申请数量
        return activeCount + pendingCount;
    }

}