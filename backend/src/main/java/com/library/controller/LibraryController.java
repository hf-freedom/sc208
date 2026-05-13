package com.library.controller;

import com.library.common.Result;
import com.library.model.*;
import com.library.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:3001")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    @GetMapping("/books")
    public Result<List<Book>> getAllBooks() {
        return Result.success(libraryService.getAllBooks());
    }

    @GetMapping("/books/{id}")
    public Result<Book> getBookById(@PathVariable String id) {
        return Result.success(libraryService.getBookById(id));
    }

    @PostMapping("/books")
    public Result<Book> addBook(@RequestBody Book book) {
        return Result.success(libraryService.addBook(book));
    }

    @GetMapping("/users")
    public Result<List<User>> getAllUsers() {
        return Result.success(libraryService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public Result<User> getUserById(@PathVariable String id) {
        return Result.success(libraryService.getUserById(id));
    }

    @PostMapping("/users")
    public Result<User> addUser(@RequestBody User user) {
        return Result.success(libraryService.addUser(user));
    }

    @PostMapping("/borrow")
    public Result<BorrowRecord> borrowBook(@RequestParam String userId, @RequestParam String bookId) {
        try {
            return Result.success(libraryService.borrowBook(userId, bookId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/return/{id}")
    public Result<BorrowRecord> returnBook(@PathVariable String id) {
        try {
            return Result.success(libraryService.returnBook(id));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reserve")
    public Result<Reservation> reserveBook(@RequestParam String userId, @RequestParam String bookId) {
        try {
            return Result.success(libraryService.reserveBook(userId, bookId));
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/reserve/cancel/{id}")
    public Result<Void> cancelReservation(@PathVariable String id) {
        try {
            libraryService.cancelReservation(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/fine/pay")
    public Result<Void> payFine(@RequestParam String userId, @RequestParam double amount) {
        try {
            libraryService.payFine(userId, amount);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/lost/{id}")
    public Result<Void> reportLost(@PathVariable String id) {
        try {
            libraryService.reportLost(id);
            return Result.success();
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/borrow/user/{userId}")
    public Result<List<BorrowRecord>> getBorrowRecordsByUser(@PathVariable String userId) {
        return Result.success(libraryService.getBorrowRecordsByUser(userId));
    }

    @GetMapping("/reserve/book/{bookId}")
    public Result<List<Reservation>> getReservationsByBook(@PathVariable String bookId) {
        return Result.success(libraryService.getReservationsByBook(bookId));
    }

    @GetMapping("/reserves")
    public Result<List<Reservation>> getAllReservations() {
        return Result.success(libraryService.getAllReservations());
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(libraryService.getStatistics());
    }

    @GetMapping("/reminders")
    public Result<List<BorrowRecord>> getOverdueReminders() {
        return Result.success(libraryService.getOverdueReminders());
    }

    @GetMapping("/task-logs")
    public Result<List<TaskLog>> getAllTaskLogs() {
        return Result.success(libraryService.getAllTaskLogs());
    }

    @GetMapping("/task-status")
    public Result<Map<String, Object>> getTaskStatus() {
        return Result.success(libraryService.getTaskStatus());
    }

    @PostMapping("/task/expire-reservations")
    public Result<TaskLog> executeExpireReservationsTask() {
        return Result.success(libraryService.processExpiredReservations());
    }

    @PostMapping("/task/process-overdue")
    public Result<TaskLog> executeProcessOverdueTask() {
        return Result.success(libraryService.processOverdueBooks());
    }

    @PostMapping("/task/send-reminders")
    public Result<TaskLog> executeSendRemindersTask() {
        return Result.success(libraryService.sendOverdueReminders());
    }
}
