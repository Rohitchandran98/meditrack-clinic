package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.exception.AppointmentNotFoundException;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages appointment lifecycle: create, view, cancel, complete.
 * Demonstrates: Observer pattern (notifications), AppointmentStatus enum, streams.
 */
public class AppointmentService {

    private final DataStore<Appointment> store = new DataStore<>(Appointment::getAppointmentId);

    // Observer list — notified on appointment events
    private final List<AppointmentObserver> observers = new ArrayList<>();

    // ---- Observer Pattern ----

    public interface AppointmentObserver {
        void onAppointmentCreated(Appointment a);
        void onAppointmentCancelled(Appointment a);
        void onAppointmentCompleted(Appointment a);
    }

    public void addObserver(AppointmentObserver observer) { observers.add(observer); }

    private void notifyCreated(Appointment a) {
        String msg = "Appointment " + a.getAppointmentId()
                + " created for " + a.getPatient().getName()
                + " with Dr. " + a.getDoctor().getName()
                + " on " + a.getDateTime().format(Appointment.FORMATTER);
        a.addNotification(msg);
        observers.forEach(o -> o.onAppointmentCreated(a));
        System.out.println("  " + msg);
    }

    private void notifyCancelled(Appointment a) {
        String msg = "Appointment " + a.getAppointmentId() + " has been CANCELLED.";
        a.addNotification(msg);
        observers.forEach(o -> o.onAppointmentCancelled(a));
        System.out.println("  " + msg);
    }

    private void notifyCompleted(Appointment a) {
        String msg = "Appointment " + a.getAppointmentId() + " marked as COMPLETED.";
        a.addNotification(msg);
        observers.forEach(o -> o.onAppointmentCompleted(a));
        System.out.println("  " + msg);
    }

    // ---- CRUD ----

    public Appointment book(Patient patient, Doctor doctor,
                            LocalDateTime dateTime, String notes) throws InvalidDataException {
        if (!doctor.isAvailable())
            throw new InvalidDataException("doctor", "Dr. " + doctor.getName() + " is not available.");
        if (!dateTime.isAfter(LocalDateTime.now()))
            throw new InvalidDataException("dateTime", "Appointment must be in the future.");

        String id = IdGenerator.getInstance().nextAppointmentId();
        Appointment appt = new Appointment(id, patient, doctor, dateTime, notes);
        appt.setStatus(AppointmentStatus.CONFIRMED);
        store.save(appt);
        notifyCreated(appt);
        return appt;
    }

    public Appointment findById(String id) {
        return store.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException(id));
    }

    public List<Appointment> findAll() { return store.findAll(); }

    public void cancel(String appointmentId) {
        Appointment a = findById(appointmentId);
        if (a.getStatus() == AppointmentStatus.CANCELLED)
            throw new IllegalStateException("Appointment already cancelled.");
        a.setStatus(AppointmentStatus.CANCELLED);
        notifyCancelled(a);
    }

    public void complete(String appointmentId) {
        Appointment a = findById(appointmentId);
        a.setStatus(AppointmentStatus.COMPLETED);
        notifyCompleted(a);
    }

    // ---- Queries ----

    public List<Appointment> findByPatient(String patientId) {
        return store.filter(a -> a.getPatient().getId().equals(patientId));
    }

    public List<Appointment> findByDoctor(String doctorId) {
        return store.filter(a -> a.getDoctor().getId().equals(doctorId));
    }

    public List<Appointment> findUpcoming() {
        return store.filter(Appointment::isUpcoming)
                .stream()
                .sorted(Comparator.comparing(Appointment::getDateTime))
                .collect(Collectors.toList());
    }

    public Map<String, Long> appointmentsPerDoctor() {
        return findAll().stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getName(), Collectors.counting()));
    }

    public DataStore<Appointment> getStore() { return store; }

    public void printAll() {
        System.out.println("\n--- Appointments (" + store.size() + ") ---");
        if (store.isEmpty()) { System.out.println("  No appointments found."); return; }
        store.forEach(System.out::println);
    }
}
