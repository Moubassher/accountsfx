package com.moubassher.accountsfx.audit;

import com.moubassher.accountsfx.Feature;
import com.moubassher.search.Searchable;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class UserActivity <F extends Enum<F> & Feature<F>> implements Searchable<UserActivity<F>> {

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

    @Override
    public Map<String, Function<UserActivity<F>, String>> getSearchableFields() {
        return Map.of("اسم المستخدم", activity -> activity.getUserName(),
                "فئة الحساب", activity -> activity.getAccountClass(),
                "نوع الحساب", activity -> activity.getAccountType(),
                "الوقت", activity -> activity.getTimestamp().toLocalTime().toString(),
                "التاريخ", activity -> activity.getTimestamp().toLocalDate().toString());
    }

    @Override
    public Map<String, Function<UserActivity<F>, Boolean>> getBooleanFilters() {
        return Map.of();
    }

    @Override
    public Map<String, ToIntFunction<UserActivity<F>>> getRangeFilters() {
        return Map.of();
    }
}
