package com.airtribe.meditrack.service;

import com.airtribe.meditrack.entity.Patient;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * CRUD and search operations for Patient entities.
 * Demonstrates: overloaded search methods (polymorphism), streams.
 */
public class PatientService {

    private final DataStore<Patient> store = new DataStore<>(Patient::getId);

    // ---- CRUD ----

    public Patient addPatient(String name, int age, String phone, String email,
                              String bloodGroup, String address) throws InvalidDataException {
        String id = IdGenerator.getInstance().nextPatientId();
        Patient pat = new Patient(id, name, age, phone, email, bloodGroup, address);
        store.save(pat);
        System.out.println("  Patient added: " + pat.getDescription());
        return pat;
    }

    public Optional<Patient> findById(String id) { return store.findById(id); }

    public List<Patient> findAll() { return store.findAll(); }

    public boolean deletePatient(String id) {
        boolean removed = store.delete(id);
        System.out.println(removed
                ? "  Patient " + id + " removed."
                : "  Patient " + id + " not found.");
        return removed;
    }

    public void addMedicalHistory(String patientId, String record) throws InvalidDataException {
        Patient p = store.findById(patientId)
                .orElseThrow(() -> new InvalidDataException("id", "Patient not found: " + patientId));
        p.addMedicalHistory(record);
        System.out.println("  [*] Medical record added for " + p.getName());
    }

    // ---- Search — overloaded (polymorphism) ----

    /** Search by ID. */
    public Optional<Patient> searchPatient(String id) {
        return store.findById(id);
    }

    /** Search by name (partial match). */
    public List<Patient> searchPatient(String name, boolean byName) {
        if (!byName) return List.of();
        return store.filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()));
    }

    /** Search by age (exact). */
    public List<Patient> searchPatient(int age) {
        return store.filter(p -> p.getAge() == age);
    }

    /** Search by blood group. */
    public List<Patient> searchByBloodGroup(String bg) {
        return store.filter(p -> p.getBloodGroup().equalsIgnoreCase(bg));
    }

    // ---- Streams & Lambdas ----

    public double averageAge() {
        return findAll().stream().mapToInt(Patient::getAge).average().orElse(0);
    }

    public List<Patient> sortedByName() {
        return findAll().stream()
                .sorted(Comparator.comparing(Patient::getName))
                .collect(Collectors.toList());
    }

    public List<Patient> seniorPatients(int ageThreshold) {
        return store.filter(p -> p.getAge() >= ageThreshold);
    }

    public DataStore<Patient> getStore() { return store; }

    public void printAll() {
        System.out.println("\n--- Patients (" + store.size() + ") ---");
        if (store.isEmpty()) { System.out.println("  No patients registered."); return; }
        store.forEach(System.out::println);
    }
}
