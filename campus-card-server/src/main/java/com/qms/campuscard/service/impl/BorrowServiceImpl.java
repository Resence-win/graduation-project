package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.BorrowRecord;
import com.qms.campuscard.entity.Book;
import com.qms.campuscard.mapper.BorrowRecordMapper;
import com.qms.campuscard.mapper.BookMapper;
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
        return borrowRecordMapper.selectPage(pageParam, queryWrapper);
    }

}