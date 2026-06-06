package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Doctor;
import com.airtribe.meditrack.entity.Specialization;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;

import java.util.*;
import java.util.stream.Collectors;

/**
 * CRUD and search operations for Doctor entities.
 * Demonstrates: streams, lambdas (Bonus D), dynamic dispatch, Comparator.
 */
public class DoctorService {

    private final DataStore<Doctor> store = new DataStore<>(Doctor::getId);

    // ---- CRUD ----

    public Doctor addDoctor(String name, int age, String phone, String email,
                            Specialization specialization, double consultationFee)
            throws InvalidDataException {
        String id = IdGenerator.getInstance().nextDoctorId();
        Doctor doc = new Doctor(id, name, age, phone, email, specialization, consultationFee);
        store.save(doc);
        System.out.println("  Doctor added: " + doc.getDescription());
        return doc;
    }

    public Optional<Doctor> findById(String id) { return store.findById(id); }

    public List<Doctor> findAll() { return store.findAll(); }

    public boolean deleteDoctor(String id) {
        boolean removed = store.delete(id);
        if (removed) System.out.println("  Doctor " + id + " removed.");
        else          System.out.println("  Doctor " + id + " not found.");
        return removed;
    }

    public void updateFee(String id, double newFee) throws InvalidDataException {
        Doctor doc = store.findById(id)
                .orElseThrow(() -> new InvalidDataException("id", "Doctor not found: " + id));
        doc.setConsultationFee(newFee);
        System.out.println("  [*] Fee updated for Dr. " + doc.getName() + " → ₹" + newFee);
    }

    // ---- Search (polymorphism via overloading) ----

    public List<Doctor> searchByName(String name) {
        return store.filter(d -> d.getName().toLowerCase().contains(name.toLowerCase()));
    }

    public List<Doctor> searchBySpecialization(Specialization spec) {
        return store.filter(d -> d.getSpecialization() == spec);
    }

    public List<Doctor> searchAvailable() {
        return store.filter(Doctor::isAvailable);
    }

    // ---- Streams & Lambdas (Bonus D) ----

    /** Average consultation fee across all doctors. */
    public OptionalDouble averageConsultationFee() {
        return findAll().stream()
                .mapToDouble(Doctor::getConsultationFee)
                .average();
    }

    /** Group doctors by specialization. */
    public Map<Specialization, List<Doctor>> groupBySpecialization() {
        return findAll().stream()
                .collect(Collectors.groupingBy(Doctor::getSpecialization));
    }

    /** Top N doctors by fee (descending). */
    public List<Doctor> topByFee(int n) {
        return findAll().stream()
                .sorted(Comparator.comparingDouble(Doctor::getConsultationFee).reversed())
                .limit(n)
                .collect(Collectors.toList());
    }

    /** Count appointments per doctor (from an appointment list). */
    public Map<String, Long> appointmentsPerDoctor(
            List<com.airtribe.meditrack.entity.Appointment> appointments) {
        return appointments.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDoctor().getId(), Collectors.counting()));
    }

    public DataStore<Doctor> getStore() { return store; }

    public void printAll() {
        System.out.println("\n--- Doctors (" + store.size() + ") ---");
        if (store.isEmpty()) { System.out.println("  No doctors registered."); return; }
        store.forEach(System.out::println);
    }
}
