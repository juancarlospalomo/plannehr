package com.householdplanner.shoppingapp.exceptions;

/**
 * Created by JuanCarlos on 18/06/2015.
 */
public class ProductException extends Exception {

    /**
     * Constructs a new {@code AlarmException} that includes the
     * current stack trace.
     */
    public ProductException() {
        super();
    }

    /**
     * Constructs a new {@code AlarmException} with the current
     * stack trace and the specified detail message.
     *
     * @param message the detail message for this exception.
     */
    public ProductException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code AlarmException} with the current
     * stack trace, the specified detail message and the specified cause.
     *
     * @param message the detail message for this exception.
     * @param cause   the optional cause of this exception, may be {@code null}.
     */
    public ProductException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new {@code AlarmException} with the current
     * stack trace and the specified cause.
     *
     * @param cause the optional cause of this exception, may be {@code null}.
     */
    public ProductException(Throwable cause) {
        super((cause == null ? null : cause.toString()), cause);
    }
}
