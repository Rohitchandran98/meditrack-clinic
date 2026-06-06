package com.airtribe.meditrack.service;

import com.airtribe.meditrack.constants.Constants;
import com.airtribe.meditrack.entity.*;
import com.airtribe.meditrack.util.DataStore;
import com.airtribe.meditrack.util.IdGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Billing service with Strategy Pattern for different billing types.
 * Demonstrates: Strategy Pattern, Factory Pattern (BillFactory), polymorphism via generateBill().
 */
public class BillingService {

    private final DataStore<Bill> store = new DataStore<>(Bill::getBillId);

    // ---- Strategy Pattern ----

    @FunctionalInterface
    public interface BillingStrategy {
        double computeDiscount(Appointment appointment, double baseAmount);
    }

    /** Standard billing — no discount. */
    public static final BillingStrategy STANDARD = (appt, base) -> 0.0;

    /** Senior citizen discount (10% off). */
    public static final BillingStrategy SENIOR = (appt, base) ->
            appt.getPatient().getAge() >= Constants.SENIOR_AGE_THRESHOLD
                    ? base * Constants.SENIOR_DISCOUNT_RATE : 0.0;

    /** Emergency surcharge (+20%). */
    public static final BillingStrategy EMERGENCY = (appt, base) -> -base * 0.20; // negative = surcharge

    /** Insurance billing — fixed 30% discount. */
    public static final BillingStrategy INSURANCE = (appt, base) -> base * 0.30;

    // ---- Factory Pattern ----

    /** Creates and saves a bill using the given strategy. */
    public Bill generateBill(Appointment appointment, BillingStrategy strategy, String type) {
        double base     = appointment.getDoctor().getConsultationFee();
        double discount = strategy.computeDiscount(appointment, base);

        String id = IdGenerator.getInstance().nextBillId();
        Bill bill = new Bill(id, appointment, base, Constants.DEFAULT_TAX_RATE);
        bill.applyDiscount(discount);
        bill.setBillType(type);
        store.save(bill);

        System.out.println("  [+] Bill generated: " + bill);
        return bill;
    }

    /** Convenience: standard bill. */
    public Bill generateStandardBill(Appointment appointment) {
        return generateBill(appointment, STANDARD, "STANDARD");
    }

    /** Convenience: auto-detect senior discount. */
    public Bill generateSmartBill(Appointment appointment) {
        int age = appointment.getPatient().getAge();
        BillingStrategy strategy = age >= Constants.SENIOR_AGE_THRESHOLD ? SENIOR : STANDARD;
        String type = age >= Constants.SENIOR_AGE_THRESHOLD ? "SENIOR" : "STANDARD";
        return generateBill(appointment, strategy, type);
    }

    // ---- Queries ----

    public Optional<Bill> findById(String id) { return store.findById(id); }

    public List<Bill> findAll() { return store.findAll(); }

    public List<Bill> findUnpaid() { return store.filter(Bill::isPaymentDue); }

    public double totalRevenue() {
        return store.filter(b -> !b.isPaymentDue())
                .stream().mapToDouble(Bill::getTotal).sum();
    }

    public List<BillSummary> getSummaries() {
        return findAll().stream()
                .map(BillSummary::from)
                .collect(Collectors.toList());
    }

    public void processPayment(String billId) {
        store.findById(billId).ifPresentOrElse(
                bill -> { bill.processPayment(); bill.printPaymentSummary(); },
                ()   -> System.out.println("  [!] Bill not found: " + billId));
    }

    public DataStore<Bill> getStore() { return store; }

    public void printAll() {
        System.out.println("\n--- Bills (" + store.size() + ") ---");
        if (store.isEmpty()) { System.out.println("  No bills found."); return; }
        store.forEach(System.out::println);
    }
}
