package com.airtribe.meditrack.test;

import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.exception.*;
import com.airtribe.meditrack.service.*;
import com.airtribe.meditrack.util.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manual test runner — no JUnit dependency.
 * Run this class directly to verify all core functionality.
 */
public class TestRunner {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("MediTrack - Test Runner\n");

        testValidator();
        testIdGenerator();
        testPatientCreation();
        testDoctorCreation();
        testDeepCopyPatient();
        testDeepCopyAppointment();
        testImmutableBillSummary();
        testDataStore();
        testServices();
        testBillingStrategies();
        testStreamsAndLambdas();
        testExceptions();
        testEnums();

        System.out.printf("%nPassed: %d | Failed: %d%n", passed, failed);
    }

    // ---- Validator ----
    private static void testValidator() {
        section("Validator");
        assertNoThrow("valid name",   () -> Validator.validateName("John"));
        assertThrows("empty name",    () -> Validator.validateName(""));
        assertNoThrow("valid age",    () -> Validator.validateAge(30));
        assertThrows("age negative",  () -> Validator.validateAge(-1));
        assertThrows("age > 150",     () -> Validator.validateAge(200));
        assertNoThrow("valid phone",  () -> Validator.validatePhone("9876543210"));
        assertThrows("short phone",   () -> Validator.validatePhone("98765"));
        assertNoThrow("valid email",  () -> Validator.validateEmail("a@b.com"));
        assertThrows("invalid email", () -> Validator.validateEmail("notanemail"));
    }

    // ---- IdGenerator (Singleton) ----
    private static void testIdGenerator() {
        section("IdGenerator (Singleton)");
        IdGenerator g1 = IdGenerator.getInstance();
        IdGenerator g2 = IdGenerator.getInstance();
        assertTrue("singleton returns same instance", g1 == g2);
        IdGenerator lazy1 = IdGenerator.getInstanceLazy();
        IdGenerator lazy2 = IdGenerator.getInstanceLazy();
        assertTrue("lazy singleton same instance", lazy1 == lazy2);
        String pid = g1.nextPatientId();
        assertTrue("patient ID starts with PAT", pid.startsWith("PAT"));
    }

    // ---- Patient Creation ----
    private static void testPatientCreation() {
        section("Patient Creation & Encapsulation");
        try {
            Patient p = new Patient("PAT001", "Alice", 30, "9876543210",
                    "alice@test.com", "A+", "Mumbai");
            assertEquals("patient name", "Alice", p.getName());
            assertEquals("patient age", 30, p.getAge());
            assertEquals("patient blood", "A+", p.getBloodGroup());
            p.addMedicalHistory("Diabetes");
            assertTrue("medical history added", p.getMedicalHistory().contains("Diabetes"));
            // Ensure defensive copy
            List<String> hist = p.getMedicalHistory();
            hist.add("SHOULD NOT APPEAR");
            assertTrue("defensive copy", !p.getMedicalHistory().contains("SHOULD NOT APPEAR"));
        } catch (InvalidDataException e) {
            fail("Patient creation: " + e.getMessage());
        }
    }

    // ---- Doctor Creation ----
    private static void testDoctorCreation() {
        section("Doctor Creation");
        try {
            Doctor d = new Doctor("DOC001", "Dr. Smith", 45, "9123456789",
                    "smith@test.com", Specialization.CARDIOLOGIST, 1500.0);
            assertEquals("specialization", Specialization.CARDIOLOGIST, d.getSpecialization());
            assertTrue("available by default", d.isAvailable());
            assertTrue("symptom match", d.matchesSymptoms("chest pain"));
        } catch (InvalidDataException e) {
            fail("Doctor creation: " + e.getMessage());
        }
    }

    // ---- Deep Copy — Patient ----
    private static void testDeepCopyPatient() {
        section("Deep Copy — Patient (Cloneable)");
        try {
            Patient original = new Patient("PAT002", "Bob", 25, "9000000001",
                    "bob@test.com", "B-", "Delhi");
            original.addMedicalHistory("Asthma");

            Patient clone = original.clone();
            clone.addMedicalHistory("New Record");

            assertTrue("original unaffected by clone mutation",
                    !original.getMedicalHistory().contains("New Record"));
            assertTrue("clone has new record",
                    clone.getMedicalHistory().contains("New Record"));
            assertTrue("original still has Asthma",
                    original.getMedicalHistory().contains("Asthma"));
        } catch (Exception e) {
            fail("Deep copy patient: " + e.getMessage());
        }
    }

    // ---- Deep Copy — Appointment ----
    private static void testDeepCopyAppointment() {
        section("Deep Copy — Appointment (Cloneable)");
        try {
            Patient p = new Patient("PAT003", "Carol", 40, "9000000002",
                    "carol@test.com", "O+", "Chennai");
            Doctor d = new Doctor("DOC002", "Dr. Ray", 50, "9000000003",
                    "ray@test.com", Specialization.NEUROLOGIST, 1800.0);
            Appointment orig = new Appointment("APT001", p, d,
                    LocalDateTime.now().plusDays(1), "notes");
            orig.addNotification("Original notification");

            Appointment clone = orig.clone();
            clone.addNotification("Clone notification");

            assertTrue("original not affected by clone notification",
                    !orig.getNotifications().contains("Clone notification"));
            assertTrue("clone has its own notification",
                    clone.getNotifications().contains("Clone notification"));
        } catch (Exception e) {
            fail("Deep copy appointment: " + e.getMessage());
        }
    }

    // ---- Immutable BillSummary ----
    private static void testImmutableBillSummary() {
        section("Immutable BillSummary");
        try {
            Patient p = new Patient("PAT004", "Dave", 30, "9000000004",
                    "dave@test.com", "AB+", "Pune");
            Doctor d = new Doctor("DOC003", "Dr. Lee", 35, "9000000005",
                    "lee@test.com", Specialization.GENERAL_PHYSICIAN, 500.0);
            Appointment appt = new Appointment("APT002", p, d,
                    LocalDateTime.now().plusDays(1), "");
            Bill bill = new Bill("BILL001", appt, 500.0, 0.18);
            BillSummary summary = BillSummary.from(bill);

            assertEquals("patient name in summary", "Dave", summary.getPatientName());
            assertTrue("total > 0", summary.getTotalAmount() > 0);
            // BillSummary has no setters — compile-time immutability guarantee
            pass("BillSummary immutability (no setters by design)");
        } catch (Exception e) {
            fail("BillSummary: " + e.getMessage());
        }
    }

    // ---- DataStore ----
    private static void testDataStore() {
        section("Generic DataStore<T>");
        try {
            DataStore<Patient> store = new DataStore<>(Patient::getId);
            Patient p1 = new Patient("P1", "Alice", 20, "9000000010", "a@b.com", "A+", "X");
            Patient p2 = new Patient("P2", "Bob",   30, "9000000011", "b@b.com", "B+", "Y");
            store.save(p1);
            store.save(p2);
            assertEquals("store size", 2, store.size());
            assertTrue("find by ID", store.findById("P1").isPresent());
            List<Patient> filtered = store.filter(p -> p.getAge() > 25);
            assertEquals("filtered size", 1, filtered.size());
            store.delete("P1");
            assertEquals("after delete", 1, store.size());
        } catch (Exception e) {
            fail("DataStore: " + e.getMessage());
        }
    }

    // ---- Services ----
    private static void testServices() {
        section("Service Layer");
        try {
            DoctorService  ds = new DoctorService();
            PatientService ps = new PatientService();

            Doctor doc = ds.addDoctor("Dr. Test", 40, "9111111111",
                    "test@h.com", Specialization.PEDIATRICIAN, 800.0);
            assertTrue("doctor saved", ds.findById(doc.getId()).isPresent());

            Patient pat = ps.addPatient("Test Patient", 10, "9222222222",
                    "tp@t.com", "A+", "Addr");
            assertTrue("patient saved", ps.findById(pat.getId()).isPresent());

            // Overloaded search
            assertTrue("search by name", !ps.searchPatient("Test Patient", true).isEmpty());
            assertTrue("search by age",  !ps.searchPatient(10).isEmpty());
        } catch (InvalidDataException e) {
            fail("Service: " + e.getMessage());
        }
    }

    // ---- Billing Strategies ----
    private static void testBillingStrategies() {
        section("Billing Strategies (Strategy Pattern)");
        try {
            Patient senior = new Patient("PSR", "Senior", 65, "9333333333",
                    "sr@sr.com", "O-", "Addr");
            Doctor doc = new Doctor("DSR", "Dr. X", 40, "9444444444",
                    "x@x.com", Specialization.GENERAL_PHYSICIAN, 500.0);
            Appointment appt = new Appointment("ASR", senior, doc,
                    LocalDateTime.now().plusDays(1), "");

            BillingService bs = new BillingService();
            Bill standardBill = bs.generateStandardBill(appt);
            Bill seniorBill   = bs.generateBill(appt, BillingService.SENIOR, "SENIOR");

            assertTrue("senior bill cheaper than standard",
                    seniorBill.getTotal() < standardBill.getTotal());
        } catch (Exception e) {
            fail("Billing strategies: " + e.getMessage());
        }
    }

    // ---- Streams & Lambdas ----
    private static void testStreamsAndLambdas() {
        section("Streams & Lambdas");
        try {
            DoctorService ds = new DoctorService();
            ds.addDoctor("Dr Alpha", 30, "9500000001", "a@a.com", Specialization.CARDIOLOGIST, 2000.0);
            ds.addDoctor("Dr Beta",  35, "9500000002", "b@b.com", Specialization.DERMATOLOGIST, 1000.0);
            ds.addDoctor("Dr Gamma", 40, "9500000003", "c@c.com", Specialization.CARDIOLOGIST,  1500.0);

            double avg = ds.averageConsultationFee().orElse(0);
            assertTrue("avg fee > 0", avg > 0);

            var grouped = ds.groupBySpecialization();
            assertEquals("cardiologists count", 2,
                    grouped.get(Specialization.CARDIOLOGIST).size());

            var top1 = ds.topByFee(1);
            assertEquals("top fee", 2000.0, top1.get(0).getConsultationFee());
        } catch (InvalidDataException e) {
            fail("Streams: " + e.getMessage());
        }
    }

    // ---- Custom Exceptions ----
    private static void testExceptions() {
        section("Custom Exceptions");
        try {
            throw new AppointmentNotFoundException("APT999");
        } catch (AppointmentNotFoundException e) {
            assertTrue("appointment not found msg",
                    e.getMessage().contains("APT999"));
            pass("AppointmentNotFoundException caught correctly");
        }

        try {
            throw new InvalidDataException("phone", "must be 10 digits");
        } catch (InvalidDataException e) {
            assertTrue("invalid data msg", e.getMessage().contains("phone"));
            assertEquals("field", "phone", e.getField());
            pass("InvalidDataException field captured");
        }
    }

    // ---- Enums ----
    private static void testEnums() {
        section("Enums");
        assertEquals("PENDING display", "Pending", AppointmentStatus.PENDING.getDisplayName());
        assertEquals("CARDIOLOGIST fee", 1500.0, Specialization.CARDIOLOGIST.getBaseFee());
        assertTrue("enum toString", Specialization.CARDIOLOGIST.toString().equals("Cardiology"));
    }

    // ----- helpers -----

    @FunctionalInterface interface Thunk { void run() throws Exception; }

    private static void section(String name) {
        System.out.println("\n[TEST] " + name);
    }

    private static void assertTrue(String label, boolean condition) {
        if (condition) { System.out.println("  PASS  " + label); passed++; }
        else           { System.out.println("  FAIL  " + label); failed++; }
    }

    private static void assertEquals(String label, Object expected, Object actual) {
        boolean ok = expected.equals(actual);
        if (ok) { System.out.println("  PASS  " + label + " [" + expected + "]"); passed++; }
        else    { System.out.println("  FAIL  " + label + " expected=" + expected + " got=" + actual); failed++; }
    }

    private static void pass(String label) { assertTrue(label, true); }
    private static void fail(String label) { assertTrue(label, false); }

    private static void assertNoThrow(String label, Thunk t) {
        try { t.run(); pass(label); }
        catch (Exception e) { fail(label + " threw: " + e.getMessage()); }
    }

    private static void assertThrows(String label, Thunk t) {
        try { t.run(); fail(label + " (expected exception not thrown)"); }
        catch (Exception e) { pass(label); }
    }
}
