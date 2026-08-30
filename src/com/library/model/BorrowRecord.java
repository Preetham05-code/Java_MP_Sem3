package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Records a single borrow transaction, including due date and fine calculation. */
public class BorrowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0;

    private final String recordId;
    private final String memberId;
    private final String isbn;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private boolean returned;

    public BorrowRecord(String recordId, String memberId, String isbn) {
        this.recordId = recordId;
        this.memberId = memberId;
        this.isbn = isbn;
        this.borrowDate = LocalDate.now();
        this.dueDate = borrowDate.plusDays(LOAN_PERIOD_DAYS);
        this.returned = false;
    }

    public String getRecordId() {
        return recordId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getIsbn() {
        return isbn;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public boolean isOverdue() {
        return !returned && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Marks this record as returned today and returns the fine incurred (0 if on time).
     */
    public double markReturned() {
        this.returnDate = LocalDate.now();
        this.returned = true;
        long lateDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        return lateDays > 0 ? lateDays * FINE_PER_DAY : 0.0;
    }

    @Override
    public String toString() {
        String status;
        if (returned) {
            status = "Returned on " + returnDate;
        } else if (isOverdue()) {
            status = "OVERDUE";
        } else {
            status = "Borrowed";
        }
        return String.format("%-8s | Member:%-8s | ISBN:%-10s | Borrowed:%-11s | Due:%-11s | %s",
                recordId, memberId, isbn, borrowDate, dueDate, status);
    }
}
