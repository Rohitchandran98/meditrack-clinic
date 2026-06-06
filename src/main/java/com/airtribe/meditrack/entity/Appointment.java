package com.airtribe.meditrack.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a scheduled appointment between a patient and a doctor.
 * Demonstrates: Cloneable (deep copy), AppointmentStatus enum, static block.
 */
public class Appointment implements Serializable, Cloneable {

    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private String appointmentId;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private String notes;
    private List<String> notifications; // observer messages stored here

    private static int totalAppointments = 0;

    static {
        System.out.println("[Appointment] Appointment system initialized.");
    }

    public Appointment(String appointmentId, Patient patient, Doctor doctor,
                       LocalDateTime dateTime, String notes) {
        this.appointmentId = appointmentId;
        this.patient       = patient;
        this.doctor        = doctor;
        this.dateTime      = dateTime;
        this.status        = AppointmentStatus.PENDING;
        this.notes         = (notes == null) ? "" : notes;
        this.notifications = new ArrayList<>();
        totalAppointments++;
    }

    // --- Getters ---
    public String            getAppointmentId()  { return appointmentId; }
    public Patient           getPatient()         { return patient; }
    public Doctor            getDoctor()          { return doctor; }
    public LocalDateTime     getDateTime()        { return dateTime; }
    public AppointmentStatus getStatus()          { return status; }
    public String            getNotes()           { return notes; }
    public List<String>      getNotifications()   { return new ArrayList<>(notifications); }
    public static int        getTotalAppointments() { return totalAppointments; }

    // --- Setters ---
    public void setStatus(AppointmentStatus status) { this.status = status; }
    public void setNotes(String notes)               { this.notes = notes; }
    public void setDateTime(LocalDateTime dt)        { this.dateTime = dt; }
    public void addNotification(String msg)          { this.notifications.add(msg); }

    public boolean isUpcoming() {
        return dateTime.isAfter(LocalDateTime.now())
                && status != AppointmentStatus.CANCELLED
                && status != AppointmentStatus.COMPLETED;
    }

    // --- Deep Copy ---
    @Override
    public Appointment clone() throws CloneNotSupportedException {
        Appointment cloned = (Appointment) super.clone();
        cloned.notifications = new ArrayList<>(this.notifications); // deep copy list
        try {
            cloned.patient = patient.clone(); // deep copy patient
        } catch (CloneNotSupportedException ignored) { /* fallback to shallow */ }
        // doctor is intentionally shallow (shared resource)
        return cloned;
    }

    @Override
    public String toString() {
        return String.format("%-10s | Patient: %-20s | Dr. %-20s | %s | %s",
                appointmentId, patient.getName(), doctor.getName(),
                dateTime.format(FORMATTER), status.getDisplayName());
    }

    public String toCsv() {
        return String.join(",",
                appointmentId,
                patient.getId(),
                doctor.getId(),
                dateTime.format(FORMATTER),
                status.name(),
                notes.replace(",", ";"));
    }
}
