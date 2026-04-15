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

    public Expense(String expense, int cost, int madeByUserId) {

        this.expense = expense;
        this.cost = cost;
        this.madeByUserId = madeByUserId;
        this.date = LocalDateTime.now();
    }

    public Expense(String expense, int cost, int madeByUserId, String note) {
        this.expense = expense;
        this.cost = cost;
        this.madeByUserId = madeByUserId;
        this.note = note;
        this.date = LocalDateTime.now();
    }

    public Expense(int expenseId, String expense, int cost, LocalDateTime date, int madeByUserId, String note) {
        this.expenseId = expenseId;
        this.expense = expense;
        this.cost = cost;
        this.date = date;
        this.madeByUserId = madeByUserId;
        this.note = note;
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

    public LocalDateTime getDate() {
        return date;
    }

    public int getMadeByUserId() {
        return madeByUserId;
    }
}