package com.library.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Runs on its own background thread and writes activity messages to
 * "librarydata/activity.log" without blocking the caller.
 * Demonstrates multithreading via the classic producer-consumer pattern:
 * callers (producers) push messages into a {@link BlockingQueue}, and this
 * object's {@link #run()} method (the consumer) drains it on a separate
 * thread.
 */
public class ActivityLogger implements Runnable {

    private static final String LOG_FILE = "librarydata" + File.separator + "activity.log";
    private static final String POISON_PILL = "__STOP__";

    private final BlockingQueue<String> queue = new LinkedBlockingQueue<String>();
    private volatile boolean running = true;

    public ActivityLogger() {
        new File("librarydata").mkdirs();
    }

    /** Called by any thread to enqueue a log line; never blocks the caller for long. */
    public void log(String message) {
        queue.offer(LocalDateTime.now() + " - " + message);
    }

    /** Signals the background thread to flush remaining messages and stop. */
    public void stop() {
        running = false;
        queue.offer(POISON_PILL);
    }

    @Override
    public void run() {
        try (FileWriter writer = new FileWriter(LOG_FILE, true)) {
            while (running || !queue.isEmpty()) {
                String message = queue.take();
                if (POISON_PILL.equals(message)) {
                    break;
                }
                writer.write(message + System.lineSeparator());
                writer.flush();
            }
        } catch (IOException e) {
            System.out.println("[Logger] I/O error: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
