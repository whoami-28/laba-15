package com.library.model;

import com.library.exception.MaterialNotAvailableException;

public interface Borrowable {

    boolean isAvailable();

    void borrowCopy() throws MaterialNotAvailableException;

    void returnCopy();

    int getAvailableCopies();
}
