package com.moubassher.accountsfx.accessControl;


/* The first time a user logs in

 */

import java.time.LocalTime;
import java.util.Objects;

public  class User {
    private String name;
    private String hashedPassword;
    private  String userType;
    private int userId, salary;
    private LocalTime shiftStart, shiftEnd;

    public User() {

    }

    public User(String employeeName) {
        this.name = employeeName;
    }

    public User(int userId, String name, String hashedPassword, String userType, int salary, LocalTime shiftStart,
                LocalTime shiftEnd) {
        this.userId = userId;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.userType = userType;
        this.salary = salary;
        this.shiftStart = shiftStart;
        this.shiftEnd = shiftEnd;
        if(isWorkHoursSet() && shiftEnd.isBefore(shiftStart)) {
            throw new IllegalArgumentException("Shift end cannot be after shift start");
        }

        if(isWorkHoursSet()) {

        }
        if(isWorkHoursSet() && shiftStart.equals(shiftEnd)) {
            throw new IllegalArgumentException("shift start and shift end for employee (" + name + ")" +
                    " are the same. Shift start and shift end cannot be the same");
        }

    }

    private boolean isWorkHoursSet() {
        return (shiftStart != null && shiftEnd != null);
    }

    public User(String name, String hashedPassword, String userType) {
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getUserType() {
        return userType;
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

    public boolean isManager() {
       return userType.equals("إدارة");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (User) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.userType, that.userType) &&
                Objects.equals(this.shiftStart, that.shiftStart) &&
                Objects.equals(this.shiftEnd, that.shiftEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, userType, shiftStart, shiftEnd);
    }

    @Override
    public String toString() {
        return "User[" +
                "name=" + name + ", " +
                "userType=" + userType + ", " +
                "workStart=" + shiftStart + ", " +
                "workFinish=" + shiftEnd + ']';
    }


}
