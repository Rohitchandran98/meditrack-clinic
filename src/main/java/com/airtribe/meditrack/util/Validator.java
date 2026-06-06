package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.exception.InvalidDataException;

/**
 * Centralized validation utility.
 * All validation logic lives here to enforce the Single Responsibility Principle.
 */
public final class Validator {

    private Validator() {}

    public static void validateName(String name) throws InvalidDataException {
        if (name == null || name.isBlank())
            throw new InvalidDataException("name", "Name cannot be empty.");
        if (name.trim().length() < 2)
            throw new InvalidDataException("name", "Name must be at least 2 characters.");
    }

    public static void validateAge(int age) throws InvalidDataException {
        if (age < Constants.MIN_AGE || age > Constants.MAX_AGE)
            throw new InvalidDataException("age",
                    "Age must be between " + Constants.MIN_AGE + " and " + Constants.MAX_AGE + ".");
    }

    public static void validatePhone(String phone) throws InvalidDataException {
        if (phone == null || !phone.matches(Constants.PHONE_REGEX))
            throw new InvalidDataException("phone", "Phone must be a 10-digit number.");
    }

    public static void validateEmail(String email) throws InvalidDataException {
        if (email == null || !email.matches(Constants.EMAIL_REGEX))
            throw new InvalidDataException("email", "Invalid email format.");
    }

    public static void validateFee(double fee) throws InvalidDataException {
        if (fee < 0)
            throw new InvalidDataException("fee", "Consultation fee cannot be negative.");
    }

    public static void validateNotNull(Object obj, String fieldName) throws InvalidDataException {
        if (obj == null)
            throw new InvalidDataException(fieldName, fieldName + " cannot be null.");
    }

    public static void validateNotBlank(String value, String fieldName) throws InvalidDataException {
        if (value == null || value.isBlank())
            throw new InvalidDataException(fieldName, fieldName + " cannot be blank.");
    }
}
