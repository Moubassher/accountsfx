package com.moubassher.accountsfx;

import java.util.List;
import java.util.Objects;

public class AccountType<F extends Feature<?>> {

    private int accountTypeId;
    private String type;
    private List<F> privileges;
    private boolean isAdmin;

    private AccountType(String type, List<F> privileges, boolean isAdmin) {
        this.type = type;
        this.privileges = privileges;
        this.isAdmin = isAdmin;
    }



    public static <F extends Enum<F> & Feature<F>>
    AccountType<F> of(String type, List<F> privileges, boolean isAdmin)
    {
        return new AccountType<>(type, privileges, isAdmin);
    }

    private AccountType(int accountTypeId, String type, List<F> privileges, boolean isAdmin) {
        this.accountTypeId = accountTypeId;
        this.type = type;
        this.privileges = privileges;
        this.isAdmin = isAdmin;
    }


    public static <F extends Enum<F> & Feature<F>> AccountType<F> of(
            int accountTypeId, String type, List<F> privileges, boolean isAdmin) {
        return new AccountType<>(accountTypeId, type, privileges, isAdmin);
    };


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
