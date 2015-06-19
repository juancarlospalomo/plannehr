package com.householdplanner.shoppingapp.exceptions;

/**
 * Created by JuanCarlos on 18/06/2015.
 */
public class MarketException extends Exception {

    /**
     * Constructs a new {@code AlarmException} that includes the
     * current stack trace.
     */
    public MarketException() {
        super();
    }

    /**
     * Constructs a new {@code AlarmException} with the current
     * stack trace and the specified detail message.
     *
     * @param message the detail message for this exception.
     */
    public MarketException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code AlarmException} with the current
     * stack trace, the specified detail message and the specified cause.
     *
     * @param message the detail message for this exception.
     * @param cause   the optional cause of this exception, may be {@code null}.
     */
    public MarketException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code AlarmException} with the current
     * stack trace and the specified cause.
     *
     * @param cause the optional cause of this exception, may be {@code null}.
     */
    public MarketException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
