package com.airtribe.meditrack.exception;

/**
 * Thrown when an appointment lookup fails.
 * Demonstrates custom unchecked exception with exception chaining.
 */
public class AppointmentNotFoundException extends RuntimeException {

    private final String appointmentId;

    public AppointmentNotFoundException(String appointmentId) {
        super("Appointment not found with ID: " + appointmentId);
        this.appointmentId = appointmentId;
    }

    public AppointmentNotFoundException(String appointmentId, Throwable cause) {
        super("Appointment not found with ID: " + appointmentId, cause);
        this.appointmentId = appointmentId;
    }

    public String getAppointmentId() { return appointmentId; }
}
