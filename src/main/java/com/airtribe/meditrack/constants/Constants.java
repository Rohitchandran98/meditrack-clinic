package com.airtribe.meditrack.constants;

/**
 * Application-wide constants for MediTrack.
 * Uses static initialization block to log startup.
 */
public final class Constants {

    // Tax and discount rates
    public static final double DEFAULT_TAX_RATE = 0.18;       // 18% GST
    public static final double SENIOR_DISCOUNT_RATE = 0.10;   // 10% for seniors (age >= 60)
    public static final int SENIOR_AGE_THRESHOLD = 60;

    // File paths
    public static final String DATA_DIR = "data/";
    public static final String PATIENTS_CSV = "data/patients.csv";
    public static final String DOCTORS_CSV = "data/doctors.csv";
    public static final String APPOINTMENTS_CSV = "data/appointments.csv";
    public static final String BILLS_CSV = "data/bills.csv";
    public static final String PATIENTS_SER = "data/patients.ser";
    public static final String DOCTORS_SER = "data/doctors.ser";
    public static final String APPOINTMENTS_SER = "data/appointments.ser";

    // ID prefixes
    public static final String PATIENT_ID_PREFIX = "PAT";
    public static final String DOCTOR_ID_PREFIX = "DOC";
    public static final String APPOINTMENT_ID_PREFIX = "APT";
    public static final String BILL_ID_PREFIX = "BILL";

    // Validation
    public static final int MIN_AGE = 0;
    public static final int MAX_AGE = 150;
    public static final String PHONE_REGEX = "^[0-9]{10}$";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";

    // Application info
    public static final String APP_NAME = "MediTrack";
    public static final String APP_VERSION = "1.0.0";

    // Static initializer — runs once when class is loaded
    static {
        System.out.println("[Constants] " + APP_NAME + " v" + APP_VERSION + " — constants loaded.");
    }

    private Constants() {} // Prevent instantiation
}
