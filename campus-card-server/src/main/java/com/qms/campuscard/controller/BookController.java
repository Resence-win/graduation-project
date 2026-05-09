package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.BorrowRequest;
import com.qms.campuscard.dto.BorrowRestrictionStatus;
import com.qms.campuscard.entity.Book;
import com.qms.campuscard.entity.BorrowRecord;
import com.qms.campuscard.entity.BorrowApplication;
import com.qms.campuscard.service.BookService;
import com.qms.campuscard.service.BorrowService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api")
public class BookController {

    private final BookService bookService;
    private final BorrowService borrowService;

    @Resource
    private LogUtil logUtil;

    public BookController(BookService bookService, BorrowService borrowService) {
        this.bookService = bookService;
        this.borrowService = borrowService;
    }

    /**
     * 图书列表接口：按书名、作者、馆藏位置和状态分页查询图书资源。
     */
    @GetMapping("/book/list")
    public Result<IPage<Book>> getBookList(
            @RequestParam(required = false) String book_name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String collection_location,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<Book> books = bookService.getBookList(book_name, author, collection_location, status, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "book", null, "查询图书列表");
        return Result.success(books);
    }

    @PostMapping("/book/upload-logo")
    public Result<String> uploadLogo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("book_id") Long bookId) {
        String logoUrl = bookService.uploadLogo(bookId, file);
        // 记录日志
        logUtil.recordLog(1L, "上传", "book", bookId, "上传图书封面");
        return Result.success("上传成功", logoUrl);
    }

    /**
     * 直接借阅接口：管理员或后台场景直接生成借阅记录并更新图书库存状态。
     */
    @PostMapping("/borrow")
    public Result<Boolean> borrowBook(@RequestBody BorrowRequest request) {
        boolean success = borrowService.borrowBook(request.getCardId(), request.getBookId());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "borrow_record", null, "借阅图书，卡号：" + request.getCardId() + "，图书ID：" + request.getBookId());
            return Result.success("借阅成功", true);
        } else {
            return Result.error("借阅失败");
        }
    }

    /**
     * 借阅申请接口：学生提交图书借阅申请，等待管理员审批后再正式借出。
     */
    @PostMapping("/borrow/application")
    public Result<Boolean> submitBorrowApplication(@RequestBody BorrowRequest request) {
        boolean success = borrowService.submitBorrowApplication(request.getCardId(), request.getBookId(), request.getBorrowDays());
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "borrow_application", null, "提交借阅申请，卡号：" + request.getCardId() + "，图书ID：" + request.getBookId() + "，借阅天数：" + request.getBorrowDays());
            return Result.success("提交申请成功", true);
        } else {
            return Result.error("提交申请失败");
        }
    }

    /**
     * 借阅审批接口：管理员审核借阅申请，批准时生成借阅记录，拒绝时保留审批结果。
     */
    @PostMapping("/borrow/application/approve")
    public Result<Boolean> approveBorrowApplication(
            @RequestParam("application_id") Long applicationId,
            @RequestParam("status") Integer status,
            @RequestParam("operator_id") Long operatorId,
            @RequestParam(required = false) String remark) {
        boolean success = borrowService.approveBorrowApplication(applicationId, status, operatorId, remark);
        if (success) {
            // 记录日志
            logUtil.recordLog(operatorId, "修改", "borrow_application", applicationId, "审批借阅申请，状态：" + (status == 2 ? "批准" : "拒绝"));
            return Result.success("审批成功", true);
        } else {
            return Result.error("审批失败");
        }
    }

    /**
     * 借阅申请列表接口：按卡片、图书和审批状态分页查询借阅申请。
     */
    @GetMapping("/borrow/application/list")
    public Result<IPage<BorrowApplication>> getBorrowApplications(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Long book_id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<BorrowApplication> applications = borrowService.getBorrowApplications(card_id, book_id, status, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "borrow_application", null, "查询借阅申请列表");
        return Result.success(applications);
    }

    /**
     * 在借数量接口：查询指定校园卡当前未归还的图书数量。
     */
    @GetMapping("/borrow/active-count")
    public Result<Integer> getActiveBorrowCount(@RequestParam("card_id") Long cardId) {
        int count = borrowService.getActiveBorrowCount(cardId);
        // 记录日志
        logUtil.recordLog(1L, "查询", "borrow_record", null, "查询活跃借阅数量，卡号：" + cardId);
        return Result.success(count);
    }

    /**
     * 借阅限制接口：查询指定校园卡是否存在超期、数量上限等借阅限制。
     */
    @GetMapping("/borrow/restriction-status")
    public Result<BorrowRestrictionStatus> getBorrowRestrictionStatus(@RequestParam("card_id") Long cardId) {
        BorrowRestrictionStatus status = borrowService.getBorrowRestrictionStatus(cardId);
        // 记录日志
        logUtil.recordLog(1L, "查询", "borrow_record", null, "查询借阅限制状态，卡号：" + cardId);
        return Result.success(status);
    }

    @PostMapping("/book")
    public Result<Boolean> addBook(@RequestBody Book book) {
        boolean success = bookService.addBook(book);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "新增", "book", book.getId(), "新增图书：" + book.getBookName());
            return Result.success("添加成功", true);
        } else {
            return Result.error("添加失败");
        }
    }

    @PutMapping("/book")
    public Result<Boolean> updateBook(@RequestBody Book book) {
        boolean success = bookService.updateBook(book);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "book", book.getId(), "修改图书：" + book.getBookName());
            return Result.success("更新成功", true);
        } else {
            return Result.error("更新失败");
        }
    }

    @DeleteMapping("/book/{id}")
    public Result<Boolean> deleteBook(@PathVariable("id") Long bookId) {
        Book book = bookService.getBookById(bookId);
        boolean success = bookService.deleteBook(bookId);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "删除", "book", bookId, "删除图书：" + (book != null ? book.getBookName() : "未知"));
            return Result.success("删除成功", true);
        } else {
            return Result.error("删除失败");
        }
    }

    @GetMapping("/book/{id}")
    public Result<Book> getBookById(@PathVariable("id") Long bookId) {
        Book book = bookService.getBookById(bookId);
        // 记录日志
        logUtil.recordLog(1L, "查询", "book", bookId, "查询图书详情：" + (book != null ? book.getBookName() : "未知"));
        return Result.success(book);
    }

    /**
     * 图书归还接口：根据借阅记录ID完成还书，并恢复图书可借状态。
     */
    @PostMapping("/borrow/return")
    public Result<Boolean> returnBook(@RequestParam("borrow_id") Long borrowId) {
        boolean success = borrowService.returnBook(borrowId);
        if (success) {
            // 记录日志
            logUtil.recordLog(1L, "修改", "borrow_record", borrowId, "归还图书");
            return Result.success("归还成功", true);
        } else {
            return Result.error("归还失败");
        }
    }

    /**
     * 借阅记录接口：按卡片、图书和归还状态分页查询借阅历史。
     */
    @GetMapping("/borrow/list")
    public Result<IPage<BorrowRecord>> getBorrowRecords(
            @RequestParam(required = false) Long card_id,
            @RequestParam(required = false) Long book_id,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<BorrowRecord> records = borrowService.getBorrowRecords(card_id, book_id, status, page, size);
        // 记录日志
        logUtil.recordLog(1L, "查询", "borrow_record", null, "查询借阅记录");
        return Result.success(records);
    }

}
