package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.interfaces.Searchable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a patient.
 * Demonstrates: deep copy via Cloneable, overloaded search, static block.
 */
public class Patient extends Person implements Searchable<Patient>, Cloneable {

    private static final long serialVersionUID = 1L;

    private String bloodGroup;
    private String address;
    private List<String> medicalHistory;

    private static int totalPatients = 0;

    static {
        System.out.println("[Patient] Patient registry initialized.");
    }

    public Patient(String id, String name, int age, String phone, String email,
                   String bloodGroup, String address) throws InvalidDataException {
        super(id, name, age, phone, email);
        this.bloodGroup     = bloodGroup;
        this.address        = address;
        this.medicalHistory = new ArrayList<>();
        totalPatients++;
    }

    // --- Getters / Setters ---
    public String       getBloodGroup()             { return bloodGroup; }
    public void         setBloodGroup(String bg)    { this.bloodGroup = bg; }
    public String       getAddress()                { return address; }
    public void         setAddress(String address)  { this.address = address; }
    public List<String> getMedicalHistory()         { return new ArrayList<>(medicalHistory); }
    public static int   getTotalPatients()          { return totalPatients; }

    public void addMedicalHistory(String record) { medicalHistory.add(record); }

    // --- Deep Copy ---
    @Override
    public Patient clone() throws CloneNotSupportedException {
        Patient cloned = (Patient) super.clone();       // shallow copy of primitives + id
        cloned.medicalHistory = new ArrayList<>(this.medicalHistory); // deep copy list
        return cloned;
    }

    // --- Searchable (overloading search methods) ---
    @Override
    public Patient searchById(String id) {
        return this.getId().equals(id) ? this : null;
    }

    @Override
    public Patient searchByName(String name) {
        return this.getName().toLowerCase().contains(name.toLowerCase()) ? this : null;
    }

    /** Overloaded: search by exact age. */
    public Patient searchByAge(int age) {
        return this.getAge() == age ? this : null;
    }

    /** Overloaded: search by blood group. */
    public Patient searchByBloodGroup(String bg) {
        return this.bloodGroup.equalsIgnoreCase(bg) ? this : null;
    }

    @Override
    public String getDescription() {
        return String.format("Patient: %-25s | Blood: %-5s | Address: %s",
                getName(), bloodGroup, address);
    }

    @Override
    public String toString() {
        return String.format("%-10s | %-25s | Age: %3d | Blood: %-5s | Phone: %s",
                getId(), getName(), getAge(), bloodGroup, getPhone());
    }

    public String toCsv() {
        return String.join(",",
                getId(), getName(), String.valueOf(getAge()),
                getPhone(), getEmail(), bloodGroup, address,
                String.join(";", medicalHistory));
    }

    public static Patient fromCsv(String line) throws InvalidDataException {
        String[] p = line.split(",", -1);
        Patient pat = new Patient(p[0], p[1], Integer.parseInt(p[2]),
                p[3], p[4], p[5], p[6]);
        if (p.length > 7 && !p[7].isBlank()) {
            for (String rec : p[7].split(";")) {
                if (!rec.isBlank()) pat.addMedicalHistory(rec);
            }
        }
        return pat;
    }
}
