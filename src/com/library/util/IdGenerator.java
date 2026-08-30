package com.library.util;

import java.util.concurrent.atomic.AtomicInteger;

/** Generates unique, human-readable IDs for members and borrow records. */
public class IdGenerator {

    private static final AtomicInteger memberCounter = new AtomicInteger(1000);
    private static final AtomicInteger recordCounter = new AtomicInteger(1);

    private IdGenerator() {
    }

    public static String nextMemberId() {
        return "MEM" + memberCounter.incrementAndGet();
    }

    public static String nextRecordId() {
        return "REC" + String.format("%04d", recordCounter.getAndIncrement());
    }

    /** Used when reloading saved data, so new IDs never clash with old ones. */
    public static void fastForwardMemberCounter(int atLeast) {
        memberCounter.updateAndGet(current -> Math.max(current, atLeast));
    }

    public static void fastForwardRecordCounter(int atLeast) {
        recordCounter.updateAndGet(current -> Math.max(current, atLeast));
    }
}
