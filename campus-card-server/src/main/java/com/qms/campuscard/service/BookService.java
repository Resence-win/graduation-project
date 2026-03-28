package com.qms.campuscard.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qms.campuscard.entity.Book;
import org.springframework.web.multipart.MultipartFile;

public interface BookService {

    IPage<Book> getBookList(String bookName, String author, Integer status, Integer page, Integer size);

    String uploadLogo(Long bookId, MultipartFile file);

    boolean addBook(Book book);

    boolean updateBook(Book book);

    boolean deleteBook(Long bookId);

    Book getBookById(Long bookId);
}
