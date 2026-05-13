package com.library.service;

import com.library.model.TaskLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class ScheduledTaskService {

    @Autowired
    private LibraryService libraryService;

    @Scheduled(cron = "0 0 * * * ?")
    public TaskLog processExpiredReservations() {
        return libraryService.processExpiredReservations();
    }

    @Scheduled(cron = "0 0 1 * * ?")
    public TaskLog processOverdueBooks() {
        return libraryService.processOverdueBooks();
    }

    @Scheduled(cron = "0 0 9 * * ?")
    public TaskLog sendOverdueReminders() {
        return libraryService.sendOverdueReminders();
    }
}
