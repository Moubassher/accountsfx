package com.moubassher.accountsfx.accessControl;


/* The first time a user logs in

 */

import java.time.LocalTime;
import java.util.Objects;

public  class Account {
    private String name;
    private String hashedPassword;
    private  AccountType accountType;
    private int userId, salary;
    private LocalTime shiftStart, shiftEnd;

    public Account() {

    }

    public Account(String name) {
        this.name = name;
    }

    public Account(int userId, String name, String hashedPassword, AccountType accountType,
                   int salary, LocalTime shiftStart,
                   LocalTime shiftEnd) {
        this.userId = userId;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.accountType = accountType;
        this.salary = salary;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        if(isWorkHoursSet() && shiftEnd.isBefore(shiftStart)) {
            throw new IllegalArgumentException("Shift end cannot be after shift start");
        }

        if(isWorkHoursSet() && shiftStart.equals(shiftEnd)) {
            throw new IllegalArgumentException("shift start and shift end for employee (" + name + ")" +
                    " are the same. Shift start and shift end cannot be the same");
        }

    }

    private boolean isWorkHoursSet() {
        return (shiftStart != null && shiftEnd != null);
    }

    public Account(String name, String hashedPassword, AccountType accountType) {
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.accountType = accountType;
    }


    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public int getSalary() {
        return salary;
    }

    public LocalTime getShiftStart() {
        return shiftStart;
    }

    public LocalTime getShiftEnd() {
        return shiftEnd;
    }

    public boolean isAdmin() {
       return accountType == null ? false : accountType.isAdmin();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Account) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.accountType, that.accountType) &&
                Objects.equals(this.shiftStart, that.shiftStart) &&
                Objects.equals(this.shiftEnd, that.shiftEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, accountType, shiftStart, shiftEnd);
    }

    @Override
    public String toString() {
        return "Account[" +
                "name=" + name + ", " +
                "userType=" + accountType + ", " +
                "workStart=" + shiftStart + ", " +
                "workFinish=" + shiftEnd + ']';
    }


}
