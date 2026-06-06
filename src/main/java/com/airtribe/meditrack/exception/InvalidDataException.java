package com.airtribe.meditrack.exception;

/**
 * Thrown when input data fails validation (e.g., invalid phone, age out of range).
 * Demonstrates custom checked exception with chaining.
 */
public class InvalidDataException extends Exception {

    private final String field;

    public InvalidDataException(String message) {
        super(message);
        this.field = "unknown";
    }

    public InvalidDataException(String field, String message) {
        super("Invalid " + field + ": " + message);
        this.field = field;
    }

    public InvalidDataException(String field, String message, Throwable cause) {
        super("Invalid " + field + ": " + message, cause);
        this.field = field;
    }

    public String getField() { return field; }
}
