package com.moubassher.accountsfx.expenses;

import java.time.LocalDateTime;

//UP "Expense" is export-worthy. Simple as it is.
public final class Expense {

    private int expenseId;
    private final String expense;
    private String note;
    private final int cost;
    private final LocalDateTime date;
    private final int madeByUserId;
    private boolean approved;

    public Expense(String expense, int cost, int madeByUserId) {
        this.expense = expense;
        this.cost = cost;
        this.madeByUserId = madeByUserId;
        this.date = LocalDateTime.now();
        this.approved = false;
    }

    public Expense(String expense, int cost, int madeByUserId, String note) {
        this.expense = expense;
        this.cost = cost;
        this.madeByUserId = madeByUserId;
        this.note = note;
        this.date = LocalDateTime.now();
        this.approved = false;
    }

    public Expense(int expenseId, String expense, int cost, LocalDateTime date, int madeByUserId, String note, boolean approved) {
        this.expenseId = expenseId;
        this.expense = expense;
        this.cost = cost;
        this.date = date;
        this.madeByUserId = madeByUserId;
        this.note = note;
        this.approved = approved;
    }

    public int getExpenseId() {
        return expenseId;
    }

    public String getExpense() {
        return expense;
    }

    public String getNote() {
        return note;
    }

    public int getCost() {
        return cost;
    }

    public LocalDateTime getTimestamp() {
        return date;
    }

    public int getMadeByUserId() {
        return madeByUserId;
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }
}