package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.Searchable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a doctor.
 * Demonstrates: inheritance (Person → Doctor), Searchable, static block, enums.
 */
public class Doctor extends Person implements Searchable<Doctor> {

    private static final long serialVersionUID = 1L;

    private Specialization specialization;
    private double consultationFee;
    private List<String> availableSlots;
    private boolean isAvailable;

    private static int totalDoctors = 0;

    static {
        System.out.println("[Doctor] Doctor registry initialized.");
    }

    public Doctor(String id, String name, int age, String phone, String email,
                  Specialization specialization, double consultationFee) throws InvalidDataException {
        super(id, name, age, phone, email);
        this.specialization   = specialization;
        this.consultationFee  = consultationFee;
        this.availableSlots   = new ArrayList<>();
        this.isAvailable      = true;
        totalDoctors++;
    }

    // --- Getters / Setters ---
    public Specialization getSpecialization()           { return specialization; }
    public void setSpecialization(Specialization s)     { this.specialization = s; }
    public double getConsultationFee()                  { return consultationFee; }
    public void setConsultationFee(double fee)          { this.consultationFee = fee; }
    public boolean isAvailable()                        { return isAvailable; }
    public void setAvailable(boolean available)         { this.isAvailable = available; }
    public List<String> getAvailableSlots()             { return new ArrayList<>(availableSlots); }
    public static int getTotalDoctors()                 { return totalDoctors; }

    public void addSlot(String slot)    { availableSlots.add(slot); }
    public void removeSlot(String slot) { availableSlots.remove(slot); }

    // --- Searchable ---
    @Override
    public Doctor searchById(String id) {
        return this.getId().equals(id) ? this : null;
    }

    @Override
    public Doctor searchByName(String name) {
        return this.getName().toLowerCase().contains(name.toLowerCase()) ? this : null;
    }

    /** Extra search by specialization (overloading concept). */
    public Doctor searchBySpecialization(Specialization spec) {
        return this.specialization == spec ? this : null;
    }

    /** Rule-based AI helper: suggest this doctor for given symptom keywords. */
    public boolean matchesSymptoms(String symptoms) {
        String s = symptoms.toLowerCase();
        return switch (specialization) {
            case CARDIOLOGIST     -> s.contains("chest") || s.contains("heart") || s.contains("cardiac");
            case DERMATOLOGIST    -> s.contains("skin") || s.contains("rash") || s.contains("acne");
            case NEUROLOGIST      -> s.contains("headache") || s.contains("brain") || s.contains("nerve");
            case ORTHOPEDIST      -> s.contains("bone") || s.contains("joint") || s.contains("fracture");
            case PEDIATRICIAN     -> s.contains("child") || s.contains("fever") || s.contains("kid");
            case PSYCHIATRIST     -> s.contains("anxiety") || s.contains("depression") || s.contains("mental");
            case GYNECOLOGIST     -> s.contains("pregnancy") || s.contains("menstrual") || s.contains("ovarian");
            case OPHTHALMOLOGIST  -> s.contains("eye") || s.contains("vision") || s.contains("blind");
            case ENT_SPECIALIST   -> s.contains("ear") || s.contains("nose") || s.contains("throat");
            default               -> s.contains("general") || s.contains("cold") || s.contains("flu");
        };
    }

    @Override
    public String getDescription() {
        return String.format("Dr. %-25s | %-20s | Fee: ₹%-8.2f | %s",
                getName(), specialization.getDisplayName(), consultationFee,
                isAvailable ? "Available" : "Busy");
    }

    @Override
    public String toString() {
        return String.format("%-10s | Dr. %-22s | %-20s | ₹%-8.2f | %s",
                getId(), getName(), specialization.getDisplayName(), consultationFee,
                isAvailable ? "Available" : "Busy");
    }

    /** CSV serialization. */
    public String toCsv() {
        return String.join(",",
                getId(), getName(), String.valueOf(getAge()),
                getPhone(), getEmail(),
                specialization.name(),
                String.valueOf(consultationFee),
                String.valueOf(isAvailable));
    }

    /** Build Doctor from a CSV line. */
    public static Doctor fromCsv(String line) throws InvalidDataException {
        String[] p = line.split(",", -1);
        Doctor d = new Doctor(p[0], p[1], Integer.parseInt(p[2]),
                p[3], p[4], Specialization.valueOf(p[5]), Double.parseDouble(p[6]));
        d.setAvailable(Boolean.parseBoolean(p[7]));
        return d;
    }
}
