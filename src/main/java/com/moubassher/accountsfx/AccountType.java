package com.moubassher.accountsfx;

import java.util.List;

public class AccountType<F extends Enum<F> & Feature<F>> {

    enum Class {MAINTAINER, ADMIN, NON_ADMIN}

    private int accountTypeId;
    private String accountTypeName;
    private List<F> privileges;
    private Class accountClass;
    public AccountType(String accountTypeName, List<F> privileges, Class accountClass) {
        this.accountTypeName = accountTypeName;
        this.privileges = privileges;
        this.accountClass = accountClass;
    }

    public AccountType(int accountTypeId, String accountTypeName, List<F> privileges, Class accountClass) {
        this.accountTypeId = accountTypeId;
        this.accountTypeName = accountTypeName;
        this.privileges = privileges;
        this.accountClass = accountClass;
    }


    public void validate() {
        if(accountTypeName == null || accountTypeName.isBlank()) {
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

    public String getTypeName() {
        return accountTypeName;
    }

    public Class getAccountClass() {
        return accountClass;
    }

    public List<F> getPrivileges() {
        return privileges;
    }

    public void setPrivileges(List<F> privileges) {
        this.privileges = privileges;
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
        return accountTypeName;
    }

    public void setId(int id) {
        accountTypeId = id;
    }
}
