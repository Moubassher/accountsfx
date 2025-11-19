package com.moubassher.accountsfx.accessControl;


/* The first time a user logs in

 */

import java.time.LocalTime;
import java.util.Objects;

public  class Employee {
    private String name;
    private String hashedPassword;
    private  String department;
    private int employeeId, salary;
    private LocalTime shiftStart, shiftEnd;

    public Employee() {

    }

    public Employee(String employeeName) {
        this.name = employeeName;
    }

    public Employee(int employeeId, String name, String hashedPassword, String department, int salary, LocalTime shiftStart,
                    LocalTime shiftEnd) {
        this.employeeId = employeeId;
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.department = department;
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

    public Employee(String name, String hashedPassword, String department) {
        this.name = name;
        this.hashedPassword = hashedPassword;
        this.department = department;
    }

    public String getName() {
        return name;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public String getDepartment() {
        return department;
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
       return department.equals("إدارة");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Employee) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.department, that.department) &&
                Objects.equals(this.shiftStart, that.shiftStart) &&
                Objects.equals(this.shiftEnd, that.shiftEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, department, shiftStart, shiftEnd);
    }

    @Override
    public String toString() {
        return "Employee[" +
                "name=" + name + ", " +
                "department=" + department + ", " +
                "workStart=" + shiftStart + ", " +
                "workFinish=" + shiftEnd + ']';
    }


}
