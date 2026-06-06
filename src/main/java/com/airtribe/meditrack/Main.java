package com.airtribe.meditrack;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.service.*;
import com.airtribe.meditrack.util.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * MediTrack — Clinic & Appointment Management System
 * Entry point with menu-driven console UI.
 *
 * Command-line flag:  --loadData   loads persisted CSV data on startup.
 */
public class Main {

    private static final DoctorService      doctorService      = new DoctorService();
    private static final PatientService     patientService     = new PatientService();
    private static final AppointmentService appointmentService = new AppointmentService();
    private static final BillingService     billingService     = new BillingService();
    private static final Scanner            sc                 = new Scanner(System.in);

    // Static block — app-level initialization
    static {
        System.out.println(Constants.APP_NAME + " v" + Constants.APP_VERSION);
        // Register a console observer for appointment events
        appointmentService.addObserver(new AppointmentService.AppointmentObserver() {
            public void onAppointmentCreated(Appointment a)   { /* already logged in service */ }
            public void onAppointmentCancelled(Appointment a) { /* already logged in service */ }
            public void onAppointmentCompleted(Appointment a) { /* already logged in service */ }
        });
    }

    public static void main(String[] args) {
        // Handle --loadData command-line argument
        boolean loadData = Arrays.asList(args).contains("--loadData");
        if (loadData) loadPersistedData();
        else          seedSampleData();

        boolean running = true;
        while (running) {
            printMainMenu();
            int choice = readInt();
            switch (choice) {
                case 1  -> doctorMenu();
                case 2  -> patientMenu();
                case 3  -> appointmentMenu();
                case 4  -> billingMenu();
                case 5  -> analyticsMenu();
                case 6  -> aiMenu();
                case 7  -> persistMenu();
                case 0  -> running = false;
                default -> System.out.println("  Invalid choice.");
            }
        }
        System.out.println("Goodbye!");
        sc.close();
    }

    // ----- menus -----

    private static void printMainMenu() {
        System.out.println("""

                Main Menu
                1. Doctors
                2. Patients
                3. Appointments
                4. Billing
                5. Analytics
                6. AI Recommendations
                7. Save / Load Data
                0. Exit
                Choice: """);
    }

    // ---- Doctor Menu ----
    private static void doctorMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""

                    --- Doctor Menu ---
                    1. Add Doctor
                    2. View All Doctors
                    3. Search Doctor
                    4. Update Fee
                    5. Delete Doctor
                    0. Back""");
            switch (readInt()) {
                case 1 -> addDoctor();
                case 2 -> doctorService.printAll();
                case 3 -> searchDoctor();
                case 4 -> updateDoctorFee();
                case 5 -> {
                    System.out.print("Doctor ID to delete: ");
                    doctorService.deleteDoctor(sc.nextLine().trim());
                }
                case 0 -> inMenu = false;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    private static void addDoctor() {
        try {
            System.out.print("Name: ");            String name = sc.nextLine().trim();
            System.out.print("Age: ");             int age  = readInt();
            System.out.print("Phone: ");           String phone = sc.nextLine().trim();
            System.out.print("Email: ");           String email = sc.nextLine().trim();

            System.out.println("Specializations:");
            Specialization[] specs = Specialization.values();
            for (int i = 0; i < specs.length; i++)
                System.out.printf("  %d. %s%n", i + 1, specs[i].getDisplayName());
            System.out.print("Choice: ");
            int si = readInt() - 1;
            Specialization spec = (si >= 0 && si < specs.length) ? specs[si] : Specialization.GENERAL_PHYSICIAN;

            System.out.print("Consultation Fee (₹): ");
            double fee = readDouble();

            doctorService.addDoctor(name, age, phone, email, spec, fee);
        } catch (InvalidDataException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void searchDoctor() {
        System.out.println("  1. By Name  2. By Specialization  3. Available only");
        switch (readInt()) {
            case 1 -> {
                System.out.print("Name: ");
                doctorService.searchByName(sc.nextLine().trim()).forEach(System.out::println);
            }
            case 2 -> {
                Specialization[] specs = Specialization.values();
                for (int i = 0; i < specs.length; i++)
                    System.out.printf("  %d. %s%n", i + 1, specs[i]);
                System.out.print("Choice: ");
                int si = readInt() - 1;
                if (si >= 0 && si < specs.length)
                    doctorService.searchBySpecialization(specs[si]).forEach(System.out::println);
            }
            case 3 -> doctorService.searchAvailable().forEach(System.out::println);
        }
    }

    private static void updateDoctorFee() {
        try {
            System.out.print("Doctor ID: ");  String id  = sc.nextLine().trim();
            System.out.print("New Fee (₹): "); double fee = readDouble();
            doctorService.updateFee(id, fee);
        } catch (InvalidDataException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ---- Patient Menu ----
    private static void patientMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""

                    --- Patient Menu ---
                    1. Add Patient
                    2. View All Patients
                    3. Search Patient
                    4. Add Medical History
                    5. Clone Patient (deep copy demo)
                    6. Delete Patient
                    0. Back""");
            switch (readInt()) {
                case 1 -> addPatient();
                case 2 -> patientService.printAll();
                case 3 -> searchPatient();
                case 4 -> addMedicalHistory();
                case 5 -> clonePatientDemo();
                case 6 -> {
                    System.out.print("Patient ID to delete: ");
                    patientService.deletePatient(sc.nextLine().trim());
                }
                case 0 -> inMenu = false;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    private static void addPatient() {
        try {
            System.out.print("Name: ");       String name   = sc.nextLine().trim();
            System.out.print("Age: ");        int age       = readInt();
            System.out.print("Phone: ");      String phone  = sc.nextLine().trim();
            System.out.print("Email: ");      String email  = sc.nextLine().trim();
            System.out.print("Blood Group: ");String bg     = sc.nextLine().trim();
            System.out.print("Address: ");    String addr   = sc.nextLine().trim();
            patientService.addPatient(name, age, phone, email, bg, addr);
        } catch (InvalidDataException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void searchPatient() {
        System.out.println("  1. By ID  2. By Name  3. By Age  4. By Blood Group");
        switch (readInt()) {
            case 1 -> {
                System.out.print("ID: ");
                patientService.searchPatient(sc.nextLine().trim())
                        .ifPresentOrElse(System.out::println,
                                () -> System.out.println("Not found."));
            }
            case 2 -> {
                System.out.print("Name: ");
                patientService.searchPatient(sc.nextLine().trim(), true).forEach(System.out::println);
            }
            case 3 -> {
                System.out.print("Age: ");
                patientService.searchPatient(readInt()).forEach(System.out::println);
            }
            case 4 -> {
                System.out.print("Blood Group: ");
                patientService.searchByBloodGroup(sc.nextLine().trim()).forEach(System.out::println);
            }
        }
    }

    private static void addMedicalHistory() {
        try {
            System.out.print("Patient ID: ");  String id  = sc.nextLine().trim();
            System.out.print("Record: ");       String rec = sc.nextLine().trim();
            patientService.addMedicalHistory(id, rec);
        } catch (InvalidDataException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void clonePatientDemo() {
        System.out.print("Patient ID to clone: ");
        patientService.findById(sc.nextLine().trim()).ifPresentOrElse(original -> {
            try {
                Patient clone = original.clone();
                clone.addMedicalHistory("[cloned record]");
                System.out.println("  Original history size: " + original.getMedicalHistory().size());
                System.out.println("  Clone    history size: " + clone.getMedicalHistory().size());
                System.out.println("  Deep copy verified: lists are independent.");
            } catch (CloneNotSupportedException e) {
                System.out.println("  Clone failed: " + e.getMessage());
            }
        }, () -> System.out.println("  Patient not found."));
    }

    // ---- Appointment Menu ----
    private static void appointmentMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""

                    --- Appointment Menu ---
                    1. Book Appointment
                    2. View All Appointments
                    3. View Upcoming Appointments
                    4. Cancel Appointment
                    5. Complete Appointment
                    6. Clone Appointment (deep copy demo)
                    0. Back""");
            switch (readInt()) {
                case 1 -> bookAppointment();
                case 2 -> appointmentService.printAll();
                case 3 -> {
                    System.out.println("\n--- Upcoming ---");
                    appointmentService.findUpcoming().forEach(System.out::println);
                }
                case 4 -> {
                    System.out.print("Appointment ID to cancel: ");
                    try { appointmentService.cancel(sc.nextLine().trim()); }
                    catch (Exception e) { System.out.println("  Error: " + e.getMessage()); }
                }
                case 5 -> {
                    System.out.print("Appointment ID to complete: ");
                    try { appointmentService.complete(sc.nextLine().trim()); }
                    catch (Exception e) { System.out.println("  Error: " + e.getMessage()); }
                }
                case 6 -> cloneAppointmentDemo();
                case 0 -> inMenu = false;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    private static void bookAppointment() {
        try {
            System.out.print("Patient ID: ");
            String pid = sc.nextLine().trim();
            Patient patient = patientService.findById(pid)
                    .orElseThrow(() -> new InvalidDataException("patientId", "Patient not found."));

            System.out.print("Doctor ID: ");
            String did = sc.nextLine().trim();
            Doctor doctor = doctorService.findById(did)
                    .orElseThrow(() -> new InvalidDataException("doctorId", "Doctor not found."));

            System.out.println("  Suggested slots:");
            DateUtil.suggestSlots(3).forEach(s -> System.out.println("    " + s));
            System.out.print("Date & Time (yyyy-MM-dd HH:mm): ");
            LocalDateTime dt = DateUtil.parse(sc.nextLine().trim());

            System.out.print("Notes (optional): ");
            String notes = sc.nextLine().trim();

            appointmentService.book(patient, doctor, dt, notes);
        } catch (InvalidDataException e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    private static void cloneAppointmentDemo() {
        System.out.print("Appointment ID to clone: ");
        try {
            Appointment original = appointmentService.findById(sc.nextLine().trim());
            Appointment clone    = original.clone();
            clone.addNotification("[cloned notification]");
            System.out.println("  Original notifications: " + original.getNotifications().size());
            System.out.println("  Clone    notifications: " + clone.getNotifications().size());
            System.out.println("  Deep copy verified: notification lists are independent.");
        } catch (Exception e) {
            System.out.println("  Error: " + e.getMessage());
        }
    }

    // ---- Billing Menu ----
    private static void billingMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""

                    --- Billing Menu ---
                    1. Generate Bill (Standard)
                    2. Generate Bill (Smart — auto senior discount)
                    3. Generate Bill (Emergency)
                    4. Generate Bill (Insurance)
                    5. Pay Bill
                    6. View All Bills
                    7. View Bill Summary (immutable snapshot)
                    0. Back""");
            int choice = readInt();
            switch (choice) {
                case 6 -> billingService.printAll();
                case 7 -> billingService.getSummaries().forEach(System.out::println);
                case 5 -> {
                    System.out.print("Bill ID: ");
                    billingService.processPayment(sc.nextLine().trim());
                }
                case 0 -> inMenu = false;
                default -> {
                    if (choice >= 1 && choice <= 4) {
                        System.out.print("Appointment ID: ");
                        try {
                            Appointment appt = appointmentService.findById(sc.nextLine().trim());
                            Bill bill = switch (choice) {
                                case 1  -> billingService.generateStandardBill(appt);
                                case 2  -> billingService.generateSmartBill(appt);
                                case 3  -> billingService.generateBill(appt, BillingService.EMERGENCY, "EMERGENCY");
                                case 4  -> billingService.generateBill(appt, BillingService.INSURANCE, "INSURANCE");
                                default -> null;
                            };
                            if (bill != null) System.out.println(bill.generateBill());
                        } catch (Exception e) {
                            System.out.println("  Error: " + e.getMessage());
                        }
                    } else {
                        System.out.println("  Invalid choice.");
                    }
                }
            }
        }
    }

    // ---- Analytics Menu ----
    private static void analyticsMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""

                    --- Analytics (Streams & Lambdas) ---
                    1. Avg doctor fee
                    2. Doctors by specialization
                    3. Appointments per doctor
                    4. Unpaid bills
                    5. Total revenue
                    6. Senior patients
                    0. Back""");
            switch (readInt()) {
                case 1 -> doctorService.averageConsultationFee()
                        .ifPresentOrElse(
                                avg -> System.out.printf("  Avg fee: ₹%.2f%n", avg),
                                () -> System.out.println("  No data."));
                case 2 -> doctorService.groupBySpecialization()
                        .forEach((spec, docs) ->
                                System.out.println("  " + spec.getDisplayName() + ": " + docs.size()));
                case 3 -> appointmentService.appointmentsPerDoctor()
                        .forEach((doc, count) ->
                                System.out.println("  Dr. " + doc + ": " + count + " appointments"));
                case 4 -> {
                    var unpaid = billingService.findUnpaid();
                    System.out.println("  Unpaid bills: " + unpaid.size());
                    unpaid.forEach(b -> System.out.println("  " + b));
                }
                case 5 -> System.out.printf("  Total revenue: ₹%.2f%n", billingService.totalRevenue());
                case 6 -> patientService.seniorPatients(Constants.SENIOR_AGE_THRESHOLD)
                        .forEach(p -> System.out.println("  " + p));
                case 0 -> inMenu = false;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    // ---- AI Menu ----
    private static void aiMenu() {
        System.out.print("\n  Describe your symptoms: ");
        String symptoms = sc.nextLine().trim();
        AIHelper.printRecommendations(doctorService.findAll(), symptoms);
    }

    // ---- Persist Menu ----
    private static void persistMenu() {
        boolean inMenu = true;
        while (inMenu) {
            System.out.println("""

                    --- Save / Load ---
                    1. Save all to CSV
                    2. Load from CSV
                    3. Serialize (binary)
                    0. Back""");
            switch (readInt()) {
                case 1 -> saveToCSV();
                case 2 -> loadFromCSV();
                case 3 -> serializeData();
                case 0 -> inMenu = false;
                default -> System.out.println("  Invalid choice.");
            }
        }
    }

    private static void saveToCSV() {
        try {
            CSVUtil.saveDoctors(doctorService.findAll());
            CSVUtil.savePatients(patientService.findAll());
            CSVUtil.saveAppointments(appointmentService.findAll());
            System.out.println("  Data saved to CSV.");
        } catch (IOException e) {
            System.out.println("  Save error: " + e.getMessage());
        }
    }

    private static void loadFromCSV() {
        try {
            var doctors = CSVUtil.loadDoctors();
            doctorService.getStore().loadAll(doctors);
            var patients = CSVUtil.loadPatients();
            patientService.getStore().loadAll(patients);
            var appointments = CSVUtil.loadAppointments(
                    patientService.getStore(), doctorService.getStore());
            appointmentService.getStore().loadAll(appointments);
            System.out.println("  Data loaded from CSV.");
        } catch (Exception e) {
            System.out.println("  Load error: " + e.getMessage());
        }
    }

    private static void serializeData() {
        try {
            CSVUtil.serialize(patientService.findAll(), Constants.PATIENTS_SER);
            CSVUtil.serialize(doctorService.findAll(), Constants.DOCTORS_SER);
            System.out.println("  Serialized successfully.");
        } catch (IOException e) {
            System.out.println("  Serialize error: " + e.getMessage());
        }
    }

    private static void loadPersistedData() {
        System.out.println("--loadData flag detected. Loading CSV data...");
        loadFromCSV();
    }

    // ----- seed data -----

    private static void seedSampleData() {
        System.out.println("Loading sample data...");
        try {
            Doctor d1 = doctorService.addDoctor("Anjali Sharma", 42, "9876543210",
                    "anjali@hospital.com", Specialization.CARDIOLOGIST, 1500);
            Doctor d2 = doctorService.addDoctor("Ravi Kumar", 38, "9123456780",
                    "ravi@hospital.com", Specialization.DERMATOLOGIST, 1000);
            Doctor d3 = doctorService.addDoctor("Priya Nair", 50, "9988776655",
                    "priya@hospital.com", Specialization.NEUROLOGIST, 1800);
            Doctor d4 = doctorService.addDoctor("Mohan Verma", 45, "9871234560",
                    "mohan@hospital.com", Specialization.GENERAL_PHYSICIAN, 500);

            Patient p1 = patientService.addPatient("Rohit Sharma", 28, "9090909090",
                    "rohit@email.com", "B+", "Mumbai");
            p1.addMedicalHistory("Hypertension diagnosed 2022");
            Patient p2 = patientService.addPatient("Sunita Patel", 65, "9191919191",
                    "sunita@email.com", "O+", "Delhi");
            Patient p3 = patientService.addPatient("Arjun Das", 35, "9292929292",
                    "arjun@email.com", "A-", "Bangalore");

            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
            Appointment a1 = appointmentService.book(p1, d1, tomorrow, "Regular checkup");
            Appointment a2 = appointmentService.book(p2, d3, tomorrow.plusHours(2), "Headache follow-up");

            billingService.generateSmartBill(a1);
            billingService.generateBill(a2, BillingService.SENIOR, "SENIOR");

        } catch (InvalidDataException e) {
            System.out.println("Error: " + e.getMessage());
        }
        System.out.println("Sample data ready.\n");
    }

    // ----- input helpers -----

    private static int readInt() {
        try {
            String line = sc.nextLine().trim();
            return Integer.parseInt(line);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static double readDouble() {
        try {
            String line = sc.nextLine().trim();
            return Double.parseDouble(line);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}
