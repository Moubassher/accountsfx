package com.moubassher.accountsfx;

public interface Feature<E extends Enum<E> & Feature<E>> {

    E saleApproval();
}
