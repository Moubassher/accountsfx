package com.moubassher.accountsfx;

public interface Feature<E extends Enum<E> & Feature<E>> {

    E login();

    E logout();

    E resetPassword();

    E createAdmin();

    E createNonAdmin();
}
