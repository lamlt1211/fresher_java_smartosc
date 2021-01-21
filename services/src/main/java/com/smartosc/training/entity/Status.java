package com.smartosc.training.entity;

/**
 * fres-parent
 *
 * @author thanhdat
 * @created_at 21/04/2020 - 2:34 PM
 * @since 21/04/2020
 */
public enum Status {

    ACTIVE(1), INACTIVE(0);

    private final int value;

    Status(final int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
