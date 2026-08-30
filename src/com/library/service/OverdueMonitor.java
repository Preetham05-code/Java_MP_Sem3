package com.library.service;

import com.library.model.BorrowRecord;
import java.util.List;

/**
 * A background daemon thread that periodically checks the library for
 * overdue books and records a notice via {@link ActivityLogger}.
 * Demonstrates multithreading through a custom {@link Thread} subclass
 * running concurrently with the main console loop.
 */
public class OverdueMonitor extends Thread {

    private final Library library;
    private final long intervalMillis;
    private volatile boolean running = true;

    public OverdueMonitor(Library library, long intervalMillis) {
        super("OverdueMonitor");
        this.library = library;
        this.intervalMillis = intervalMillis;
        setDaemon(true);
    }

    public void stopMonitoring() {
        running = false;
        this.interrupt();
    }

    @Override
    public void run() {
        while (running) {
            try {
                List<BorrowRecord> overdue = library.getOverdueRecords();
                if (!overdue.isEmpty()) {
                    library.getLogger().log("[OverdueMonitor] " + overdue.size()
                            + " book(s) currently overdue.");
                }
                Thread.sleep(intervalMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}
