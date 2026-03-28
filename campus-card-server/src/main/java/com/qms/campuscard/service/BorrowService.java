package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.BorrowRecord;

public interface BorrowService {

    boolean borrowBook(Long cardId, Long bookId);

    boolean returnBook(Long borrowId);

    IPage<BorrowRecord> getBorrowRecords(Long cardId, Long bookId, Integer status, Integer page, Integer size);
}
