package com.airtribe.meditrack.entity;

/**
 * Enum representing doctor specializations with display name and base fee.
 */
public enum Specialization {
    CARDIOLOGIST("Cardiology", 1500.0),
    DERMATOLOGIST("Dermatology", 1000.0),
    NEUROLOGIST("Neurology", 1800.0),
    ORTHOPEDIST("Orthopedics", 1200.0),
    PEDIATRICIAN("Pediatrics", 800.0),
    PSYCHIATRIST("Psychiatry", 1400.0),
    GENERAL_PHYSICIAN("General Medicine", 500.0),
    GYNECOLOGIST("Gynecology", 1100.0),
    OPHTHALMOLOGIST("Ophthalmology", 900.0),
    ENT_SPECIALIST("ENT", 950.0);

    private final String displayName;
    private final double baseFee;

    Specialization(String displayName, double baseFee) {
        this.displayName = displayName;
        this.baseFee = baseFee;
    }

    public String getDisplayName() { return displayName; }
    public double getBaseFee() { return baseFee; }

    @Override
    public String toString() { return displayName; }
}
