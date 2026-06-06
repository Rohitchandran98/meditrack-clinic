package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Singleton ID generator.
 * Demonstrates: Singleton (eager + lazy with double-checked locking), AtomicInteger.
 */
public class IdGenerator {

    // --- Eager Singleton (instantiated at class load) ---
    private static final IdGenerator EAGER_INSTANCE = new IdGenerator();

    // --- Lazy Singleton holder (loaded only when getInstanceLazy() is called) ---
    private static class LazyHolder {
        static final IdGenerator INSTANCE = new IdGenerator();
    }

    // Counters for each entity type
    private final AtomicInteger patientCounter     = new AtomicInteger(1000);
    private final AtomicInteger doctorCounter      = new AtomicInteger(1000);
    private final AtomicInteger appointmentCounter = new AtomicInteger(1000);
    private final AtomicInteger billCounter        = new AtomicInteger(1000);

    private IdGenerator() {
        System.out.println("[IdGenerator] Singleton instance created.");
    }

    /** Returns the eager singleton instance. */
    public static IdGenerator getInstance() {
        return EAGER_INSTANCE;
    }

    /** Returns the lazy singleton instance (initialization-on-demand holder). */
    public static IdGenerator getInstanceLazy() {
        return LazyHolder.INSTANCE;
    }

    public String nextPatientId()     { return Constants.PATIENT_ID_PREFIX     + patientCounter.getAndIncrement(); }
    public String nextDoctorId()      { return Constants.DOCTOR_ID_PREFIX      + doctorCounter.getAndIncrement(); }
    public String nextAppointmentId() { return Constants.APPOINTMENT_ID_PREFIX + appointmentCounter.getAndIncrement(); }
    public String nextBillId()        { return Constants.BILL_ID_PREFIX        + billCounter.getAndIncrement(); }

    /** Reset counters — useful for testing. */
    public void reset() {
        patientCounter.set(1000);
        doctorCounter.set(1000);
        appointmentCounter.set(1000);
        billCounter.set(1000);
    }
}
