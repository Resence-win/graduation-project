package com.qms.campuscard.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("borrow_application")
public class BorrowApplication {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long cardId;
    private Long bookId;
    private Integer borrowDays;
    private LocalDateTime applicationTime;
    private Integer status;
    private Long operatorId;
    private LocalDateTime approvalTime;
    private String remark;
    private Integer isDeleted;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCardId() {
        return cardId;
    }

    public void setCardId(Long cardId) {
        this.cardId = cardId;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Integer getBorrowDays() {
        return borrowDays;
    }

    public void setBorrowDays(Integer borrowDays) {
        this.borrowDays = borrowDays;
    }

    public LocalDateTime getApplicationTime() {
        return applicationTime;
    }

    public void setApplicationTime(LocalDateTime applicationTime) {
        this.applicationTime = applicationTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public LocalDateTime getApprovalTime() {
        return approvalTime;
    }

    public void setApprovalTime(LocalDateTime approvalTime) {
        this.approvalTime = approvalTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Integer isDeleted) {
        this.isDeleted = isDeleted;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    // 非数据库字段，用于前端显示
    @TableField(exist = false)
    private String bookName;
    @TableField(exist = false)
    private String cardNo;
    @TableField(exist = false)
    private String userName;
    
    public String getBookName() {
        return bookName;
    }
    
    public void setBookName(String bookName) {
        this.bookName = bookName;
    }
    
    public String getCardNo() {
        return cardNo;
    }
    
    public void setCardNo(String cardNo) {
        this.cardNo = cardNo;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
}
