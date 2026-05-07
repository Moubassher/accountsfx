package com.moubassher.accountsfx.audit;

import com.moubassher.accountsfx.Feature;

import java.time.LocalDateTime;

public class UserActivity <F extends Enum<F> & Feature<F>> {

    private int activityId;
    private F action;
    private String userName, accountType, accountClass;
    private LocalDateTime timestamp;

    public UserActivity(F action, String userName, String accountType, String accountClass, LocalDateTime timestamp) {
        this.action = action;
        this.userName = userName;
        this.accountType = accountType;
        this.accountClass = accountClass;
        this.timestamp = timestamp;
    }

    public UserActivity(int activityId, F action, String userName, String accountType, String accountClass, LocalDateTime timestamp) {
        this.activityId = activityId;
        this.action = action;
        this.userName = userName;
        this.accountType = accountType;
        this.accountClass = accountClass;
        this.timestamp = timestamp;
    }

    public int getActivityId() {
        return activityId;
    }

    public void setActivityId(int activityId) {
        this.activityId = activityId;
    }

    public F getAction() {
        return action;
    }

    public String getUserName() {
        return userName;
    }

    public String getAccountType() {
        return accountType;
    }

    public String getAccountClass() {
        return accountClass;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
