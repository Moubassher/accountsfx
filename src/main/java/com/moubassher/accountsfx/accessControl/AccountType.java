package com.moubassher.accountsfx.accessControl;

import java.util.List;
import java.util.Objects;

public class AccountType {

    private int accountTypeId;
    private String type;
    private List<String> privileges;
    private boolean isAdmin;

    public AccountType(String type, List<String> privileges, boolean isAdmin) {
        this.type = type;
        this.privileges = privileges;
        this.isAdmin = isAdmin;
    }

    public AccountType(int accountTypeId, String type, List<String> privileges, boolean isAdmin) {
        this.accountTypeId = accountTypeId;
        this.type = type;
        this.privileges = privileges;
        this.isAdmin = isAdmin;
    }


    public int getAccountTypeId() {
        return accountTypeId;
    }

    public void setAccountTypeId(int accountTypeId) {
        this.accountTypeId = accountTypeId;
    }

    public String getType() {
        return type;
    }

    public List<String> getPrivileges() {
        return privileges;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        AccountType that = (AccountType) object;
        return getAccountTypeId() == that.getAccountTypeId()
                ||(isAdmin() == that.isAdmin()
                && Objects.equals(getType(), that.getType())
                && Objects.equals(getPrivileges(), that.getPrivileges()));
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAccountTypeId(), getType(), getPrivileges(), isAdmin());
    }

    @Override
    public String toString() {
        return type;
    }

    public void setId(int id) {
        accountTypeId = id;
    }
}
