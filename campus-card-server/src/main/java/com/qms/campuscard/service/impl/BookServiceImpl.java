package com.qms.campuscard.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qms.campuscard.entity.Book;
import com.qms.campuscard.mapper.BookMapper;
import com.qms.campuscard.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class BookServiceImpl implements BookService {

    @Resource
    private BookMapper bookMapper;

    @Value("${file.upload.path:./upload}")
    private String uploadPath;

    @Override
    public IPage<Book> getBookList(String bookName, String author, Integer status, Integer page, Integer size) {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1) {
            size = 10;
        }
        
        Page<Book> pageParam = new Page<>(page, size);
        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        
        if (bookName != null && !bookName.isEmpty()) {
            queryWrapper.like("book_name", bookName);
        }
        if (author != null && !author.isEmpty()) {
            queryWrapper.like("author", author);
        }
        if (status != null) {
            queryWrapper.eq("status", status);
        }
        
        queryWrapper.eq("is_deleted", 0);
        queryWrapper.orderByDesc("create_time");
        return bookMapper.selectPage(pageParam, queryWrapper);
    }

    @Override
    public String uploadLogo(Long bookId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }

        // 查询图书是否存在
        Book existingBook = bookMapper.selectById(bookId);
        if (existingBook == null || existingBook.getIsDeleted() == 1) {
            throw new RuntimeException("图书不存在");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = ".png"; // 默认扩展名
        if (originalFilename != null && originalFilename.lastIndexOf(".") > 0) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String filename = UUID.randomUUID().toString() + extension;

        // 确保使用绝对路径
        String projectRoot = System.getProperty("user.dir");
        String bookUploadPath = projectRoot + "/upload/book";
        File uploadDir = new File(bookUploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (!created) {
                throw new RuntimeException("无法创建上传目录");
            }
        }

        try {
            String filePath = bookUploadPath + "/" + filename;
            file.transferTo(new File(filePath));

            // 更新 logo 字段
            existingBook.setLogo("/upload/book/" + filename);
            bookMapper.updateById(existingBook);

            return "/upload/book/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean addBook(Book book) {
        book.setStatus(1); // 默认可借阅
        book.setIsDeleted(0);
        book.setCreateTime(LocalDateTime.now());
        return bookMapper.insert(book) > 0;
    }

    @Override
    public boolean updateBook(Book book) {
        Book existingBook = bookMapper.selectById(book.getId());
        if (existingBook == null || existingBook.getIsDeleted() == 1) {
            throw new RuntimeException("图书不存在");
        }
        return bookMapper.updateById(book) > 0;
    }

    @Override
    public boolean deleteBook(Long bookId) {
        Book book = new Book();
        book.setId(bookId);
        book.setIsDeleted(1);
        return bookMapper.updateById(book) > 0;
    }

    @Override
    public Book getBookById(Long bookId) {
        Book book = bookMapper.selectById(bookId);
        if (book == null || book.getIsDeleted() == 1) {
            throw new RuntimeException("图书不存在");
        }
        return book;
    }

}