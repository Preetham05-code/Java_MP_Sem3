package com.library.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Represents a registered library user who can search, borrow and return books. */
public class Member extends Person {

    private static final long serialVersionUID = 1L;

    public static final int MAX_BOOKS_ALLOWED = 3;

    private final LocalDate membershipDate;
    private final List<String> borrowedIsbns;
    private double totalFineDue;

    public Member(String id, String name, String username, String password) {
        super(id, name, username, password);
        this.membershipDate = LocalDate.now();
        this.borrowedIsbns = new ArrayList<String>();
        this.totalFineDue = 0.0;
    }

    public LocalDate getMembershipDate() {
        return membershipDate;
    }

    public List<String> getBorrowedIsbns() {
        return borrowedIsbns;
    }

    public double getTotalFineDue() {
        return totalFineDue;
    }

    public void addFine(double amount) {
        this.totalFineDue += amount;
    }

    public void payFine() {
        this.totalFineDue = 0.0;
    }

    public boolean canBorrowMore() {
        return borrowedIsbns.size() < MAX_BOOKS_ALLOWED;
    }

    @Override
    public String getRole() {
        return "MEMBER";
    }

    @Override
    public String displayProfile() {
        return "Member [ID=" + id + ", Name=" + name + ", Since=" + membershipDate
                + ", CurrentlyBorrowed=" + borrowedIsbns.size() + "/" + MAX_BOOKS_ALLOWED
                + ", FineDue=Rs." + totalFineDue + "]";
    }
}
