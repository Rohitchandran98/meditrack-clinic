package com.airtribe.meditrack.util;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.exception.InvalidDataException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV read/write utility.
 * Demonstrates: File I/O, try-with-resources, String.split(",").
 */
public final class CSVUtil {

    private CSVUtil() {}

    // ---- Generic helpers ----

    /** Reads all lines from a CSV file (skips header). */
    public static List<String> readLines(String filePath) throws IOException {
        List<String> lines = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) return lines;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                if (firstLine) { firstLine = false; continue; } // skip header
                if (!line.isBlank()) lines.add(line);
            }
        }
        return lines;
    }

    /** Writes lines to a CSV file. The first element in lines[] is treated as the header. */
    public static void writeLines(String filePath, String header, List<String> rows) throws IOException {
        Files.createDirectories(Paths.get(Constants.DATA_DIR));
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath))) {
            bw.write(header);
            bw.newLine();
            for (String row : rows) {
                bw.write(row);
                bw.newLine();
            }
        }
    }

    // ---- Doctors ----

    public static void saveDoctors(List<Doctor> doctors) throws IOException {
        List<String> rows = new ArrayList<>();
        for (Doctor d : doctors) rows.add(d.toCsv());
        writeLines(Constants.DOCTORS_CSV,
                "id,name,age,phone,email,specialization,consultationFee,isAvailable", rows);
        System.out.println("[CSVUtil] Saved " + doctors.size() + " doctors to " + Constants.DOCTORS_CSV);
    }

    public static List<Doctor> loadDoctors() throws IOException, InvalidDataException {
        List<Doctor> doctors = new ArrayList<>();
        for (String line : readLines(Constants.DOCTORS_CSV)) {
            doctors.add(Doctor.fromCsv(line));
        }
        System.out.println("[CSVUtil] Loaded " + doctors.size() + " doctors.");
        return doctors;
    }

    // ---- Patients ----

    public static void savePatients(List<Patient> patients) throws IOException {
        List<String> rows = new ArrayList<>();
        for (Patient p : patients) rows.add(p.toCsv());
        writeLines(Constants.PATIENTS_CSV,
                "id,name,age,phone,email,bloodGroup,address,medicalHistory", rows);
        System.out.println("[CSVUtil] Saved " + patients.size() + " patients.");
    }

    public static List<Patient> loadPatients() throws IOException, InvalidDataException {
        List<Patient> patients = new ArrayList<>();
        for (String line : readLines(Constants.PATIENTS_CSV)) {
            patients.add(Patient.fromCsv(line));
        }
        System.out.println("[CSVUtil] Loaded " + patients.size() + " patients.");
        return patients;
    }

    // ---- Appointments (references by ID — requires pre-loaded doctors & patients) ----

    public static void saveAppointments(List<Appointment> appointments) throws IOException {
        List<String> rows = new ArrayList<>();
        for (Appointment a : appointments) rows.add(a.toCsv());
        writeLines(Constants.APPOINTMENTS_CSV,
                "appointmentId,patientId,doctorId,dateTime,status,notes", rows);
        System.out.println("[CSVUtil] Saved " + appointments.size() + " appointments.");
    }

    /**
     * Load appointments from CSV.
     * Requires DataStore<Patient> and DataStore<Doctor> to resolve references.
     */
    public static List<Appointment> loadAppointments(
            DataStore<Patient> patients, DataStore<Doctor> doctors) throws IOException {
        List<Appointment> list = new ArrayList<>();
        for (String line : readLines(Constants.APPOINTMENTS_CSV)) {
            String[] p = line.split(",", -1);
            patients.findById(p[1]).ifPresent(pat ->
                    doctors.findById(p[2]).ifPresent(doc -> {
                        try {
                            Appointment a = new Appointment(
                                    p[0], pat, doc,
                                    DateUtil.parse(p[3]),
                                    p.length > 5 ? p[5].replace(";", ",") : "");
                            a.setStatus(AppointmentStatus.valueOf(p[4]));
                            list.add(a);
                        } catch (Exception ignored) {}
                    }));
        }
        System.out.println("[CSVUtil] Loaded " + list.size() + " appointments.");
        return list;
    }

    // ---- Java Serialization ----

    public static void serialize(Object obj, String filePath) throws IOException {
        Files.createDirectories(Paths.get(Constants.DATA_DIR));
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(obj);
        }
        System.out.println("[CSVUtil] Serialized to " + filePath);
    }

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(String filePath) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            return (T) ois.readObject();
        }
    }
}
