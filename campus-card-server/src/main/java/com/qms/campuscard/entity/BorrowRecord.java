package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import java.time.LocalDateTime;

@TableName("borrow_record")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("card_id")
    private Long cardId;

    @TableField("book_id")
    private Long bookId;

    @TableField("application_id")
    private Long applicationId;

    @TableField("borrow_time")
    private LocalDateTime borrowTime;

    @TableField("return_time")
    private LocalDateTime returnTime;

    @TableField("due_time")
    private LocalDateTime dueTime;

    @TableField("status")
    private Integer status;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("is_deleted")
    private Integer isDeleted;
    
    @TableField(exist = false)
    private String bookName;
    
    public String getBookName() {
        return bookName;
    }
    
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
}
