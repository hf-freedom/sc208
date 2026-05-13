package com.library.model;

import java.time.LocalDateTime;

public class User {
    private String id;
    private String name;
    private String phone;
    private String email;
    private Integer level;
    private Integer maxBorrowCount;
    private Boolean isBlacklisted;
    private Double totalFine;
    private Double unpaidFine;
    private Integer borrowCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public Integer getMaxBorrowCount() { return maxBorrowCount; }
    public void setMaxBorrowCount(Integer maxBorrowCount) { this.maxBorrowCount = maxBorrowCount; }
    public Boolean getIsBlacklisted() { return isBlacklisted; }
    public void setIsBlacklisted(Boolean isBlacklisted) { this.isBlacklisted = isBlacklisted; }
    public Double getTotalFine() { return totalFine; }
    public void setTotalFine(Double totalFine) { this.totalFine = totalFine; }
    public Double getUnpaidFine() { return unpaidFine; }
    public void setUnpaidFine(Double unpaidFine) { this.unpaidFine = unpaidFine; }
    public Integer getBorrowCount() { return borrowCount; }
    public void setBorrowCount(Integer borrowCount) { this.borrowCount = borrowCount; }
    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}
