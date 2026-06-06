# Design Decisions — MediTrack

This document explains the main design choices I made while building MediTrack and why I made them. Some of these decisions came naturally from the requirements, others involved actual trade-offs I had to think through.

---

## 1. The Class Hierarchy

The entity hierarchy looks like this:

```
MedicalEntity  (abstract)
└── Person     (abstract)
    ├── Doctor
    └── Patient

Appointment    (implements Cloneable, Serializable)
Bill           (implements Payable, Serializable)
BillSummary    (immutable — final class)
```

I introduced `MedicalEntity` as the root abstract class so that things like `id`, `createdAt`, and the total entity count (via `AtomicInteger`) live in one place. The alternative was putting all that into `Person`, but `Appointment` and `Bill` also need IDs — and they're not persons. So having `MedicalEntity` as a separate layer made sense.

`Person` then sits between `MedicalEntity` and the actual `Doctor`/`Patient` classes. It holds name, age, phone, and email — fields that are common to both. This avoids duplicating validation logic in both classes.

One thing I was tempted to do was make `Person` concrete instead of abstract, but the system never creates a "bare" Person — only doctors and patients. Making it abstract enforces that and also forces both subclasses to implement `getDescription()`.

---

## 2. Using Enums Instead of String Constants

Early on I had `AppointmentStatus` as a String field — things like `"CONFIRMED"`, `"CANCELLED"`. The problem was nothing stopped you from setting it to `"CANCELLLED"` (a typo) and the code would just silently accept it.

Switching to enums fixed this completely. The compiler rejects any invalid value. On top of that, I made the enums carry data:

- `Specialization` holds a `displayName` (like "Cardiology") and a `baseFee` — so the enum itself becomes a source of truth for default fees per specialization
- `AppointmentStatus` holds a `displayName` for clean UI display

The `matchesSymptoms()` method in `Doctor` also uses a switch expression on `Specialization` which reads very cleanly compared to a long chain of if-else on strings.

---

## 3. Deep Copy vs Shallow Copy

This was one of the more interesting design decisions. The rule I followed was: **if a field is a mutable collection or object that belongs exclusively to this entity, deep copy it. If it's a shared resource, leave it as a shallow reference.**

For `Patient`:
- `medicalHistory` is a `List<String>` that belongs only to this patient. If I returned it directly (or cloned shallowly), external code could modify a patient's medical history without going through the proper method. So `clone()` creates a new `ArrayList` from the original.

For `Appointment`:
- `notifications` — same reasoning, copied deeply
- `patient` — deep copied to show that the cloned appointment has its own patient snapshot
- `doctor` — intentionally *not* deep copied. A doctor is a shared resource in the system. If you clone an appointment, you don't want a ghost doctor floating around with no connection to the actual doctor list.

The distinction between these two approaches is exactly what the deep vs shallow copy discussion is about in theory — here you can see it applied to a real scenario.

---

## 4. Why `BillSummary` is Immutable

`BillSummary` serves as an audit record — a snapshot of what a bill looked like at a specific point in time. Once generated, it should never change. If you could mutate it, you could retroactively alter billing records, which is obviously bad.

Making it immutable was straightforward:
- The class is `final` so it can't be subclassed (a subclass could add setters)
- All fields are `final`
- No setters
- The constructor sets everything upfront
- Collections are not exposed (there are none in this class, but if there were, I'd return copies)

It's also thread-safe by design — immutable objects can be freely shared between threads without synchronization.

---

## 5. The Singleton in `IdGenerator`

I implemented two flavours of Singleton here to demonstrate both approaches:

**Eager initialization:**
```java
private static final IdGenerator EAGER_INSTANCE = new IdGenerator();
```
This gets created when the class is loaded, regardless of whether anyone calls `getInstance()` yet. It's simple and thread-safe (JVM guarantees class initialization is atomic). The downside is you pay the instantiation cost even if it's never used — not a big deal for `IdGenerator`, but could matter for heavier objects.

**Lazy initialization (Initialization-on-Demand Holder):**
```java
private static class LazyHolder {
    static final IdGenerator INSTANCE = new IdGenerator();
}
```
The inner `LazyHolder` class is only loaded when `getInstanceLazy()` is first called. This is thread-safe without any `synchronized` block because class loading in the JVM is guaranteed to be atomic. It's my preferred approach for lazy singletons because it's clean and doesn't require double-checked locking.

---

## 6. Strategy Pattern for Billing

I didn't want the billing logic to be a big if-else block inside `BillingService`. The Strategy pattern lets me define billing behaviours as separate, swappable implementations.

`BillingStrategy` is a `@FunctionalInterface`, so each strategy is just a lambda:

- `STANDARD` — no discount
- `SENIOR` — 10% off if patient is 60+
- `EMERGENCY` — 20% surcharge
- `INSURANCE` — 30% discount

If the hospital ever adds a new billing type (say, corporate accounts with 15% off), you just add one new lambda. Nothing else changes. This follows the Open/Closed Principle — open for extension, closed for modification.

---

## 7. Observer Pattern for Appointment Events

When an appointment is booked, cancelled, or completed, multiple things might need to know about it — logging, sending notifications, updating a dashboard, etc. If I hardcoded all of that into `AppointmentService`, adding a new notification channel would require touching service code.

Instead, `AppointmentService` maintains a list of `AppointmentObserver` instances. Any code that wants to react to appointment events just registers itself. The service doesn't know or care what the observers do.

Currently there's a console observer registered in `Main.java`. In a real system you'd add an email observer, an SMS observer, and so on — all without touching `AppointmentService`.

---

## 8. Generic `DataStore<T>`

Rather than having each service maintain its own `HashMap<String, Doctor>`, `HashMap<String, Patient>`, etc., I built a generic `DataStore<T>` that all services reuse. You pass in a key extractor function when constructing it:

```java
new DataStore<>(Doctor::getId)
new DataStore<>(Patient::getId)
```

It also implements `Iterable<T>` so you can use it in enhanced for-loops and `forEach()`. The `filter(Predicate<T>)` and `sorted(Comparator<T>)` methods make stream-like querying easy without exposing the internal map directly.

---

## 9. File I/O Strategy — CSV + Serialization

I chose CSV as the primary persistence format because it's human-readable — you can open `patients.csv` in Excel and see exactly what's stored. The `--loadData` flag on startup loads from these files, which is useful for persistence between sessions.

Java Serialization (`.ser` files) is offered as an alternative. It's faster to write and handles complex object graphs automatically, but the files are binary and tied to the exact class structure. If you change a class field, old `.ser` files become unreadable. So I treat it as a quick save/restore option rather than a long-term storage solution.

Both use `try-with-resources` throughout, which guarantees streams are closed properly even if an exception is thrown midway through writing.
