package com.library.service;

import com.library.model.*;
import com.library.repository.DataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LibraryService {

    @Autowired
    private DataRepository dataRepository;

    @Value("${library.fine-rate:1.0}")
    private Double fineRate;

    @Value("${library.max-borrow-days:30}")
    private Integer maxBorrowDays;

    @Value("${library.reservation-expire-hours:24}")
    private Integer reservationExpireHours;

    public List<Book> getAllBooks() {
        return new ArrayList<>(dataRepository.books.values());
    }

    public Book getBookById(String id) {
        return dataRepository.books.get(id);
    }

    public Book addBook(Book book) {
        book.setId(UUID.randomUUID().toString());
        book.setCreateTime(LocalDateTime.now());
        book.setUpdateTime(LocalDateTime.now());
        book.setBorrowedQuantity(0);
        book.setReservedQuantity(0);
        book.setBorrowCount(0);
        dataRepository.books.put(book.getId(), book);
        return book;
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(dataRepository.users.values());
    }

    public User getUserById(String id) {
        return dataRepository.users.get(id);
    }

    public User addUser(User user) {
        user.setId(UUID.randomUUID().toString());
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        user.setIsBlacklisted(false);
        user.setTotalFine(0.0);
        user.setUnpaidFine(0.0);
        user.setBorrowCount(0);
        if (user.getLevel() == null) user.setLevel(1);
        if (user.getMaxBorrowCount() == null) user.setMaxBorrowCount(5);
        dataRepository.users.put(user.getId(), user);
        return user;
    }

    public BorrowRecord borrowBook(String userId, String bookId) {
        User user = dataRepository.users.get(userId);
        Book book = dataRepository.books.get(bookId);

        if (user == null || book == null) {
            throw new RuntimeException("用户或图书不存在");
        }

        if (user.getIsBlacklisted()) {
            throw new RuntimeException("用户在黑名单中，无法借书");
        }

        if (user.getUnpaidFine() > 0) {
            throw new RuntimeException("用户有未缴纳的罚款，无法借书");
        }

        long overdueCount = dataRepository.borrowRecords.values().stream()
                .filter(r -> r.getUserId().equals(userId) && !r.getIsReturned() && r.getIsOverdue())
                .count();
        if (overdueCount > 0) {
            throw new RuntimeException("用户有逾期未还图书，无法借书");
        }

        long currentBorrowCount = dataRepository.borrowRecords.values().stream()
                .filter(r -> r.getUserId().equals(userId) && !r.getIsReturned())
                .count();
        if (currentBorrowCount >= user.getMaxBorrowCount()) {
            throw new RuntimeException("用户借阅数量已达上限");
        }

        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("图书库存不足");
        }

        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        book.setBorrowedQuantity(book.getBorrowedQuantity() + 1);
        book.setBorrowCount(book.getBorrowCount() + 1);
        book.setUpdateTime(LocalDateTime.now());

        user.setBorrowCount(user.getBorrowCount() + 1);
        user.setUpdateTime(LocalDateTime.now());

        BorrowRecord record = new BorrowRecord();
        record.setId(UUID.randomUUID().toString());
        record.setUserId(userId);
        record.setBookId(bookId);
        record.setBorrowTime(LocalDateTime.now());
        record.setDueTime(LocalDateTime.now().plusDays(maxBorrowDays));
        record.setIsReturned(false);
        record.setIsOverdue(false);
        record.setFineAmount(0.0);
        record.setIsFinePaid(true);
        record.setIsLost(false);

        dataRepository.borrowRecords.put(record.getId(), record);

        return record;
    }

    public BorrowRecord returnBook(String borrowRecordId) {
        BorrowRecord record = dataRepository.borrowRecords.get(borrowRecordId);
        if (record == null || record.getIsReturned()) {
            throw new RuntimeException("借阅记录不存在或已归还");
        }

        Book book = dataRepository.books.get(record.getBookId());
        User user = dataRepository.users.get(record.getUserId());

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(record.getDueTime())) {
            long overdueDays = ChronoUnit.DAYS.between(record.getDueTime(), now);
            double fine = overdueDays * fineRate;
            record.setIsOverdue(true);
            record.setFineAmount(fine);
            record.setIsFinePaid(false);
            user.setTotalFine(user.getTotalFine() + fine);
            user.setUnpaidFine(user.getUnpaidFine() + fine);
        }

        record.setReturnTime(now);
        record.setIsReturned(true);

        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        book.setBorrowedQuantity(book.getBorrowedQuantity() - 1);
        book.setUpdateTime(now);
        user.setUpdateTime(now);

        notifyNextReservation(book.getId());

        return record;
    }

    private void notifyNextReservation(String bookId) {
        List<Reservation> reservations = dataRepository.reservations.values().stream()
                .filter(r -> r.getBookId().equals(bookId) && !r.getIsFulfilled() && !r.getIsExpired())
                .sorted(Comparator.comparing(Reservation::getQueuePosition))
                .collect(Collectors.toList());

        if (!reservations.isEmpty()) {
            Reservation next = reservations.get(0);
            next.setIsNotified(true);
            
            Book book = dataRepository.books.get(bookId);
            book.setUpdateTime(LocalDateTime.now());
        }
    }

    public Reservation reserveBook(String userId, String bookId) {
        User user = dataRepository.users.get(userId);
        Book book = dataRepository.books.get(bookId);

        if (user == null || book == null) {
            throw new RuntimeException("用户或图书不存在");
        }

        if (user.getIsBlacklisted()) {
            throw new RuntimeException("用户在黑名单中，无法预约");
        }

        boolean hasExisting = dataRepository.reservations.values().stream()
                .anyMatch(r -> r.getUserId().equals(userId) && r.getBookId().equals(bookId) 
                        && !r.getIsFulfilled() && !r.getIsExpired());
        if (hasExisting) {
            throw new RuntimeException("用户已预约该图书");
        }

        long queuePosition = dataRepository.reservations.values().stream()
                .filter(r -> r.getBookId().equals(bookId) && !r.getIsFulfilled() && !r.getIsExpired())
                .count() + 1;

        if (book.getAvailableQuantity() <= 0) {
            throw new RuntimeException("图书库存不足，无法预约");
        }

        book.setReservedQuantity(book.getReservedQuantity() + 1);
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        book.setUpdateTime(LocalDateTime.now());

        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID().toString());
        reservation.setUserId(userId);
        reservation.setBookId(bookId);
        reservation.setQueuePosition((int) queuePosition);
        reservation.setReserveTime(LocalDateTime.now());
        reservation.setExpireTime(LocalDateTime.now().plusHours(reservationExpireHours));
        reservation.setIsNotified(false);
        reservation.setIsFulfilled(false);
        reservation.setIsExpired(false);

        dataRepository.reservations.put(reservation.getId(), reservation);

        return reservation;
    }

    public void cancelReservation(String reservationId) {
        Reservation reservation = dataRepository.reservations.get(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }

        if (reservation.getIsExpired() || reservation.getIsFulfilled()) {
            throw new RuntimeException("该预约已过期或已完成，无法取消");
        }

        Book book = dataRepository.books.get(reservation.getBookId());
        book.setReservedQuantity(Math.max(0, book.getReservedQuantity() - 1));
        book.setAvailableQuantity(book.getAvailableQuantity() + 1);
        book.setUpdateTime(LocalDateTime.now());

        reservation.setIsExpired(true);
    }

    public void payFine(String userId, double amount) {
        User user = dataRepository.users.get(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (amount > user.getUnpaidFine()) {
            throw new RuntimeException("支付金额超过未缴罚款");
        }

        user.setUnpaidFine(user.getUnpaidFine() - amount);
        user.setUpdateTime(LocalDateTime.now());
    }

    public void reportLost(String borrowRecordId) {
        BorrowRecord record = dataRepository.borrowRecords.get(borrowRecordId);
        if (record == null || record.getIsReturned() || record.getIsLost()) {
            throw new RuntimeException("无法执行丢失登记");
        }

        Book book = dataRepository.books.get(record.getBookId());
        User user = dataRepository.users.get(record.getUserId());

        record.setIsLost(true);
        record.setIsReturned(true);
        record.setReturnTime(LocalDateTime.now());

        double fine = 50.0;
        record.setFineAmount(fine);
        record.setIsFinePaid(false);
        user.setTotalFine(user.getTotalFine() + fine);
        user.setUnpaidFine(user.getUnpaidFine() + fine);

        book.setTotalQuantity(book.getTotalQuantity() - 1);
        book.setBorrowedQuantity(book.getBorrowedQuantity() - 1);
        book.setUpdateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
    }

    public List<BorrowRecord> getBorrowRecordsByUser(String userId) {
        return dataRepository.borrowRecords.values().stream()
                .filter(r -> r.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    public List<Reservation> getReservationsByBook(String bookId) {
        return dataRepository.reservations.values().stream()
                .filter(r -> r.getBookId().equals(bookId))
                .sorted(Comparator.comparing(Reservation::getQueuePosition))
                .collect(Collectors.toList());
    }

    public List<Reservation> getAllReservations() {
        return new ArrayList<>(dataRepository.reservations.values());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        List<Book> topBooks = dataRepository.books.values().stream()
                .sorted((a, b) -> b.getBorrowCount() - a.getBorrowCount())
                .limit(10)
                .collect(Collectors.toList());
        stats.put("topBooks", topBooks);

        long totalBorrows = dataRepository.borrowRecords.size();
        long overdueCount = dataRepository.borrowRecords.values().stream()
                .filter(BorrowRecord::getIsOverdue)
                .count();
        double overdueRate = totalBorrows > 0 ? (double) overdueCount / totalBorrows * 100 : 0;
        stats.put("overdueRate", String.format("%.2f%%", overdueRate));

        stats.put("totalBorrowCount", totalBorrows);

        int activeUsers = (int) dataRepository.users.values().stream()
                .filter(u -> u.getBorrowCount() > 0)
                .count();
        stats.put("activeUserCount", activeUsers);
        stats.put("totalUserCount", dataRepository.users.size());

        return stats;
    }

    public TaskLog processExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<String> processedDetails = new ArrayList<>();
        
        List<Reservation> expiredReservations = dataRepository.reservations.values().stream()
                .filter(r -> !r.getIsExpired() && !r.getIsFulfilled() && now.isAfter(r.getExpireTime()))
                .collect(Collectors.toList());

        for (Reservation r : expiredReservations) {
            r.setIsExpired(true);
            Book book = dataRepository.books.get(r.getBookId());
            if (book != null) {
                book.setReservedQuantity(Math.max(0, book.getReservedQuantity() - 1));
                book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                book.setUpdateTime(now);
                processedDetails.add(String.format("图书[%s]释放预约锁定1本，用户ID: %s", book.getTitle(), r.getUserId()));
            }
            
            List<Reservation> remaining = dataRepository.reservations.values().stream()
                    .filter(res -> res.getBookId().equals(r.getBookId()) && !res.getIsExpired() && !res.getIsFulfilled())
                    .sorted(Comparator.comparing(Reservation::getQueuePosition))
                    .collect(Collectors.toList());
            for (int i = 0; i < remaining.size(); i++) {
                remaining.get(i).setQueuePosition(i + 1);
            }
        }

        TaskLog log = new TaskLog();
        log.setId(UUID.randomUUID().toString());
        log.setTaskType("RESERVATION_EXPIRE");
        log.setTaskName("预约超时释放锁定");
        log.setProcessedCount(expiredReservations.size());
        log.setDetails(String.join("; ", processedDetails));
        log.setExecuteTime(now);
        log.setSuccess(true);
        dataRepository.taskLogs.put(log.getId(), log);

        return log;
    }

    public TaskLog processOverdueBooks() {
        LocalDateTime now = LocalDateTime.now();
        List<String> processedDetails = new ArrayList<>();
        
        List<BorrowRecord> overdueRecords = dataRepository.borrowRecords.values().stream()
                .filter(r -> !r.getIsReturned() && !r.getIsOverdue() && now.isAfter(r.getDueTime()))
                .collect(Collectors.toList());

        for (BorrowRecord r : overdueRecords) {
            r.setIsOverdue(true);
            long overdueDays = ChronoUnit.DAYS.between(r.getDueTime(), now);
            double fine = Math.max(1, overdueDays) * fineRate;
            r.setFineAmount(fine);
            r.setIsFinePaid(false);

            User user = dataRepository.users.get(r.getUserId());
            if (user != null) {
                user.setTotalFine(user.getTotalFine() + fine);
                user.setUnpaidFine(user.getUnpaidFine() + fine);
                Book book = dataRepository.books.get(r.getBookId());
                String bookTitle = book != null ? book.getTitle() : "未知图书";
                processedDetails.add(String.format("用户[%s]图书[%s]逾期，罚款%.2f元", user.getName(), bookTitle, fine));
            }
        }

        TaskLog log = new TaskLog();
        log.setId(UUID.randomUUID().toString());
        log.setTaskType("OVERDUE_PROCESS");
        log.setTaskName("逾期图书罚款处理");
        log.setProcessedCount(overdueRecords.size());
        log.setDetails(String.join("; ", processedDetails));
        log.setExecuteTime(now);
        log.setSuccess(true);
        dataRepository.taskLogs.put(log.getId(), log);

        return log;
    }

    public TaskLog sendOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        List<String> processedDetails = new ArrayList<>();
        
        List<BorrowRecord> reminders = dataRepository.borrowRecords.values().stream()
                .filter(r -> !r.getIsReturned() && !r.getIsOverdue())
                .filter(r -> {
                    long daysUntilDue = ChronoUnit.DAYS.between(now, r.getDueTime());
                    return daysUntilDue >= 0 && daysUntilDue <= 3;
                })
                .collect(Collectors.toList());

        for (BorrowRecord r : reminders) {
            User user = dataRepository.users.get(r.getUserId());
            Book book = dataRepository.books.get(r.getBookId());
            if (user != null && book != null) {
                long daysLeft = ChronoUnit.DAYS.between(now, r.getDueTime());
                processedDetails.add(String.format("通知用户[%s]: 图书[%s]还有%d天到期", user.getName(), book.getTitle(), daysLeft));
            }
        }

        TaskLog log = new TaskLog();
        log.setId(UUID.randomUUID().toString());
        log.setTaskType("OVERDUE_REMINDER");
        log.setTaskName("借阅到期催还通知");
        log.setProcessedCount(reminders.size());
        log.setDetails(String.join("; ", processedDetails));
        log.setExecuteTime(now);
        log.setSuccess(true);
        dataRepository.taskLogs.put(log.getId(), log);

        return log;
    }

    public List<TaskLog> getAllTaskLogs() {
        return dataRepository.taskLogs.values().stream()
                .sorted((a, b) -> b.getExecuteTime().compareTo(a.getExecuteTime()))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getTaskStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("totalTaskExecutions", dataRepository.taskLogs.size());
        
        long expiredReservationCount = dataRepository.taskLogs.values().stream()
                .filter(l -> "RESERVATION_EXPIRE".equals(l.getTaskType()))
                .mapToInt(TaskLog::getProcessedCount)
                .sum();
        status.put("totalExpiredReservationsReleased", expiredReservationCount);
        
        long overdueBooksProcessed = dataRepository.taskLogs.values().stream()
                .filter(l -> "OVERDUE_PROCESS".equals(l.getTaskType()))
                .mapToInt(TaskLog::getProcessedCount)
                .sum();
        status.put("totalOverdueBooksProcessed", overdueBooksProcessed);
        
        long remindersSent = dataRepository.taskLogs.values().stream()
                .filter(l -> "OVERDUE_REMINDER".equals(l.getTaskType()))
                .mapToInt(TaskLog::getProcessedCount)
                .sum();
        status.put("totalRemindersSent", remindersSent);
        
        List<TaskLog> recentLogs = dataRepository.taskLogs.values().stream()
                .sorted((a, b) -> b.getExecuteTime().compareTo(a.getExecuteTime()))
                .limit(10)
                .collect(Collectors.toList());
        status.put("recentTaskLogs", recentLogs);

        return status;
    }

    public List<BorrowRecord> getOverdueReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderThreshold = now.plusDays(3);

        return dataRepository.borrowRecords.values().stream()
                .filter(r -> !r.getIsReturned() && !r.getIsOverdue() 
                        && r.getDueTime().isBefore(reminderThreshold))
                .collect(Collectors.toList());
    }
}
