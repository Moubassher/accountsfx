package com.moubassher.accountsfx;

import java.util.List;

public class AccountType<F extends Enum<F> & Feature<F>> {

    private int accountTypeId;
    private String type;
    private List<F> privileges;
    private boolean isAdmin;

    public AccountType(String type, List<F> privileges, boolean isAdmin) {
        this.type = type;
        this.privileges = privileges;
        this.isAdmin = isAdmin;
    }

    public AccountType(int accountTypeId, String type, List<F> privileges, boolean isAdmin) {
        this.accountTypeId = accountTypeId;
        this.type = type;
        this.privileges = privileges;
        this.isAdmin = isAdmin;
    }


    public void validate() {
        if(type == null || type.isBlank()) {
            throw new IllegalArgumentException("Cannot add a nameless account type");
        }

        if(privileges ==null || privileges.isEmpty()) {
            throw new IllegalArgumentException("Cannot add an account type with NO privileges!");
        }
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

    public List<F> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<F> privileges) {
        this.privileges = privileges;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountType<?> that = (AccountType<?>) o;
        return accountTypeId == that.accountTypeId;
    }


    @Override
    public int hashCode() {
        return Integer.hashCode(accountTypeId);
    }

    @Override
    public String toString() {
        return type;
    }

    public void setId(int id) {
        accountTypeId = id;
    }
}
