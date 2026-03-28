package com.qms.campuscard.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.common.LogUtil;
import com.qms.campuscard.common.Result;
import com.qms.campuscard.dto.BorrowRequest;
import com.qms.campuscard.entity.Book;
import com.qms.campuscard.entity.BorrowRecord;
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

    @GetMapping("/book/list")
    public Result<IPage<Book>> getBookList(
            @RequestParam(required = false) String book_name,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        IPage<Book> books = bookService.getBookList(book_name, author, status, page, size);
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