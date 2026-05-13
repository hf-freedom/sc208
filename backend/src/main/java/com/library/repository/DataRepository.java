package com.library.repository;

import com.library.model.*;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DataRepository {
    public final Map<String, Book> books = new ConcurrentHashMap<>();
    public final Map<String, User> users = new ConcurrentHashMap<>();
    public final Map<String, BorrowRecord> borrowRecords = new ConcurrentHashMap<>();
    public final Map<String, Reservation> reservations = new ConcurrentHashMap<>();
    public final Map<String, TaskLog> taskLogs = new ConcurrentHashMap<>();

    public DataRepository() {
        initData();
    }

    private void initData() {
        Book book1 = new Book();
        book1.setId(UUID.randomUUID().toString());
        book1.setIsbn("9787111544937");
        book1.setTitle("Java编程思想");
        book1.setAuthor("Bruce Eckel");
        book1.setCategory("编程");
        book1.setTotalQuantity(10);
        book1.setAvailableQuantity(10);
        book1.setBorrowedQuantity(0);
        book1.setReservedQuantity(0);
        book1.setBorrowCount(0);
        book1.setCreateTime(LocalDateTime.now());
        book1.setUpdateTime(LocalDateTime.now());
        books.put(book1.getId(), book1);

        Book book2 = new Book();
        book2.setId(UUID.randomUUID().toString());
        book2.setIsbn("9787115279460");
        book2.setTitle("深入理解Java虚拟机");
        book2.setAuthor("周志明");
        book2.setCategory("编程");
        book2.setTotalQuantity(5);
        book2.setAvailableQuantity(5);
        book2.setBorrowedQuantity(0);
        book2.setReservedQuantity(0);
        book2.setBorrowCount(0);
        book2.setCreateTime(LocalDateTime.now());
        book2.setUpdateTime(LocalDateTime.now());
        books.put(book2.getId(), book2);

        Book book3 = new Book();
        book3.setId(UUID.randomUUID().toString());
        book3.setIsbn("9787020002207");
        book3.setTitle("红楼梦");
        book3.setAuthor("曹雪芹");
        book3.setCategory("文学");
        book3.setTotalQuantity(3);
        book3.setAvailableQuantity(3);
        book3.setBorrowedQuantity(0);
        book3.setReservedQuantity(0);
        book3.setBorrowCount(0);
        book3.setCreateTime(LocalDateTime.now());
        book3.setUpdateTime(LocalDateTime.now());
        books.put(book3.getId(), book3);

        User user1 = new User();
        user1.setId(UUID.randomUUID().toString());
        user1.setName("张三");
        user1.setPhone("13800138000");
        user1.setEmail("zhangsan@example.com");
        user1.setLevel(1);
        user1.setMaxBorrowCount(5);
        user1.setIsBlacklisted(false);
        user1.setTotalFine(0.0);
        user1.setUnpaidFine(0.0);
        user1.setBorrowCount(0);
        user1.setCreateTime(LocalDateTime.now());
        user1.setUpdateTime(LocalDateTime.now());
        users.put(user1.getId(), user1);

        User user2 = new User();
        user2.setId(UUID.randomUUID().toString());
        user2.setName("李四");
        user2.setPhone("13900139000");
        user2.setEmail("lisi@example.com");
        user2.setLevel(2);
        user2.setMaxBorrowCount(10);
        user2.setIsBlacklisted(false);
        user2.setTotalFine(0.0);
        user2.setUnpaidFine(0.0);
        user2.setBorrowCount(0);
        user2.setCreateTime(LocalDateTime.now());
        user2.setUpdateTime(LocalDateTime.now());
        users.put(user2.getId(), user2);
    }
}
