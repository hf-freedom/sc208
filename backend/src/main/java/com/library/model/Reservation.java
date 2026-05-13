package com.library.model;

import java.time.LocalDateTime;

public class Reservation {
    private String id;
    private String userId;
    private String bookId;
    private Integer queuePosition;
    private LocalDateTime reserveTime;
    private LocalDateTime expireTime;
    private Boolean isNotified;
    private Boolean isFulfilled;
    private Boolean isExpired;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getBookId() { return bookId; }
    public void setBookId(String bookId) { this.bookId = bookId; }
    public Integer getQueuePosition() { return queuePosition; }
    public void setQueuePosition(Integer queuePosition) { this.queuePosition = queuePosition; }
    public LocalDateTime getReserveTime() { return reserveTime; }
    public void setReserveTime(LocalDateTime reserveTime) { this.reserveTime = reserveTime; }
    public LocalDateTime getExpireTime() { return expireTime; }
    public void setExpireTime(LocalDateTime expireTime) { this.expireTime = expireTime; }
    public Boolean getIsNotified() { return isNotified; }
    public void setIsNotified(Boolean isNotified) { this.isNotified = isNotified; }
    public Boolean getIsFulfilled() { return isFulfilled; }
    public void setIsFulfilled(Boolean isFulfilled) { this.isFulfilled = isFulfilled; }
    public Boolean getIsExpired() { return isExpired; }
    public void setIsExpired(Boolean isExpired) { this.isExpired = isExpired; }
}
