package com.moubassher.accountsfx;

public interface MainController <F extends Enum<F> & Feature<F>> {

    void setAccountDao(AccountDao<F> accountDao);
    void onLogin(Account<F> loggedInUser);
    void onLogout(Runnable runnable);
}
