# MediTrack — Clinic & Appointment Management System

MediTrack is a console-based clinic management system I built as part of the Airtribe Backend Java Track (Module 7). The goal was to apply Core Java concepts end-to-end , from basic OOP all the way to design patterns, file I/O, and Java 8 streams.

It manages doctors, patients, appointments, and billing through a menu-driven CLI. No frameworks, no databases - just pure Java.

---

## What's Inside

| Feature | Details |
|---|---|
| OOP | Encapsulation, Inheritance (MedicalEntity → Person → Doctor/Patient), Polymorphism (method overloading + overriding + dynamic dispatch), Abstraction (abstract classes + interfaces with default methods) |
| Advanced OOP | Deep/Shallow copy (Cloneable on Patient and Appointment), Immutable BillSummary, Enums with data (Specialization, AppointmentStatus), Static initializer blocks |
| Design Patterns | Singleton (IdGenerator — eager + lazy), Strategy (BillingStrategy as lambdas), Observer (AppointmentObserver), Factory (BillSummary.from) |
| Collections & Generics | Generic DataStore<T> with filter/sort, HashMap, ArrayList, Comparator, Iterator |
| Exception Handling | Custom checked (InvalidDataException) and unchecked (AppointmentNotFoundException) exceptions with chaining |
| File I/O | CSV persistence with try-with-resources, Java Serialization for binary save/load |
| Java 8+ | Streams, lambdas, Optional, Predicate, method references |
| Concurrency Basics | AtomicInteger for thread-safe ID generation and entity counting |
| AI Feature | Rule-based symptom → doctor recommendation, auto slot suggestion |
| Testing | Manual TestRunner (48 tests, no JUnit) |

---

## Project Structure

```
src/main/java/com/airtribe/meditrack/
├── Main.java
├── constants/
│   └── Constants.java
├── entity/
│   ├── MedicalEntity.java       ← abstract base (id, createdAt, AtomicInteger count)
│   ├── Person.java              ← abstract (name, age, phone, email + validation)
│   ├── Doctor.java              ← extends Person, Searchable<Doctor>
│   ├── Patient.java             ← extends Person, Cloneable (deep copy)
│   ├── Appointment.java         ← Cloneable (deep copy), Serializable
│   ├── Bill.java                ← implements Payable
│   ├── BillSummary.java         ← immutable (final class, final fields)
│   ├── Specialization.java      ← enum with displayName + baseFee
│   └── AppointmentStatus.java   ← enum with displayName
├── service/
│   ├── DoctorService.java       ← CRUD + streams analytics
│   ├── PatientService.java      ← CRUD + overloaded search
│   ├── AppointmentService.java  ← Observer pattern
│   └── BillingService.java      ← Strategy pattern
├── util/
│   ├── Validator.java           ← all validation in one place
│   ├── DateUtil.java
│   ├── CSVUtil.java             ← file I/O
│   ├── IdGenerator.java         ← Singleton (eager + lazy)
│   ├── DataStore.java           ← generic DataStore<T>
│   └── AIHelper.java            ← rule-based recommendations
├── exception/
│   ├── InvalidDataException.java
│   └── AppointmentNotFoundException.java
├── interfaces/
│   ├── Searchable.java          ← generic interface with default method
│   └── Payable.java             ← interface with default method
└── test/
    └── TestRunner.java          ← 48 manual tests

docs/
├── JVM_Report.md
├── Setup_Instructions.md
└── Design_Decisions.md
```

---

## Quick Start

### Compile

**Mac / Linux:**
```bash
mkdir -p out
find src/main/java -name "*.java" > sources.txt
javac --release 17 -d out @sources.txt
```

**Windows:**
```cmd
mkdir out
dir /s /b src\main\java\*.java > sources.txt
javac --release 17 -d out @sources.txt
```

### Run

```bash
# Starts with sample data (4 doctors, 3 patients, 2 appointments)
java -cp out com.airtribe.meditrack.Main

# Load previously saved CSV data instead
java -cp out com.airtribe.meditrack.Main --loadData
```

### Run Tests

```bash
java -cp out com.airtribe.meditrack.test.TestRunner
```

Expected result: `Passed: 48 | Failed: 0`

---

## Sample Output

```

  MediTrack v1.0.0

[+] Doctor added: Dr. Anjali Sharma | Cardiology | Fee: ₹1500.00 | Available
[+] Patient added: Rohit Sharma | Blood: B+ | Mumbai
[NOTIFY] Appointment APT1000 created for Rohit Sharma with Dr. Anjali Sharma on 2026-03-09 10:00

 MAIN MENU 
1. Doctors
2. Patients
3. Appointments
4. Billing
5. Analytics
6. AI Recommendations
7. Save / Load Data
0. Exit
```

### AI Recommendation

```
Describe your symptoms: chest pain and shortness of breath

--- AI Doctor Recommendations for: "chest pain..." ---
  Dr. Anjali Sharma | Cardiology | Fee: ₹1500.00 | Available

  Suggested slots:
  -> 2026-03-09 10:00
  -> 2026-03-09 10:30
  -> 2026-03-09 11:00
```

### Deep Copy Demo (from Patient menu)

```
Patient ID to clone: PAT1000
  Original history size: 1
  Clone    history size: 2
  Deep copy verified: lists are independent.
```

### Billing

```
BILL 
Bill ID    : BILL1000
Type       : STANDARD
Patient    : Rohit Sharma
Doctor     : Dr. Anjali Sharma
Date       : 09-03-2026 10:00
Base Amount: ₹1500.00
Tax (18%): ₹270.00
Discount   : ₹0.00
TOTAL      : ₹1770.00
Status     : UNPAID

```

---

## Bonus Features Implemented

All four optional bonus categories are covered:

**A — File I/O & Persistence:** CSV save/load with `try-with-resources`; Java Serialization for binary format; `--loadData` command-line flag.

**B — Design Patterns:** Singleton (IdGenerator, eager + lazy), Strategy (4 billing strategies as lambdas), Observer (AppointmentObserver), Factory (BillSummary.from).

**C — AI Feature:** `AIHelper` does rule-based symptom matching to recommend doctors by specialization and auto-suggests appointment slots.

**D — Streams & Lambdas:** `DoctorService` computes average fee, groups by specialization, returns top doctors by fee; `AppointmentService` produces appointments-per-doctor analytics — all using streams.

---

## Documentation

- [JVM Report](docs/JVM_Report.md) — How the JVM works: Class Loader, memory areas, JIT vs Interpreter, Write Once Run Anywhere
- [Setup Instructions](docs/Setup_Instructions.md) — How to install Java, compile, and run the project
- [Design Decisions](docs/Design_Decisions.md) — Why I structured things the way I did
