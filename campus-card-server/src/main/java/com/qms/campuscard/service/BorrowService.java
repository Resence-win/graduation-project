package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.BorrowRecord;
import com.qms.campuscard.entity.BorrowApplication;

public interface BorrowService {

    boolean borrowBook(Long cardId, Long bookId);

    boolean returnBook(Long borrowId);

    IPage<BorrowRecord> getBorrowRecords(Long cardId, Long bookId, Integer status, Integer page, Integer size);

    boolean submitBorrowApplication(Long cardId, Long bookId, Integer borrowDays);

    boolean approveBorrowApplication(Long applicationId, Integer status, Long operatorId, String remark);

    IPage<BorrowApplication> getBorrowApplications(Long cardId, Long bookId, Integer status, Integer page, Integer size);

    int getActiveBorrowCount(Long cardId);
}
