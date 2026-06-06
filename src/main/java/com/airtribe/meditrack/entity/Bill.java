package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.interfaces.Payable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a bill generated for an appointment.
 * Demonstrates: Payable interface, polymorphism via generateBill() override pattern,
 *               Strategy pattern hook (billingStrategy field).
 */
public class Bill implements Payable, Serializable {

    private static final long serialVersionUID = 1L;

    private String billId;
    private Appointment appointment;
    private double baseAmount;
    private double taxRate;
    private double discountAmount;
    private double totalAmount;
    private boolean isPaid;
    private LocalDateTime billedAt;
    private String billType; // STANDARD, EMERGENCY, INSURANCE

    private static int totalBills = 0;

    static {
        System.out.println("[Bill] Billing system initialized.");
    }

    public Bill(String billId, Appointment appointment, double baseAmount, double taxRate) {
        this.billId         = billId;
        this.appointment    = appointment;
        this.baseAmount     = baseAmount;
        this.taxRate        = taxRate;
        this.discountAmount = 0.0;
        this.isPaid         = false;
        this.billedAt       = LocalDateTime.now();
        this.billType       = "STANDARD";
        recomputeTotal();
        totalBills++;
    }

    private void recomputeTotal() {
        double tax = baseAmount * taxRate;
        this.totalAmount = baseAmount + tax - discountAmount;
    }

    /** Called to apply a discount (Strategy Pattern hook). */
    public void applyDiscount(double discountAmount) {
        this.discountAmount = discountAmount;
        recomputeTotal();
    }

    /** Polymorphic bill generation — subclasses can override for specialised billing. */
    public String generateBill() {
        return String.format(
                "%n====== BILL ======%n" +
                "Bill ID    : %s%n" +
                "Type       : %s%n" +
                "Patient    : %s%n" +
                "Doctor     : Dr. %s%n" +
                "Date       : %s%n" +
                "Base Amount: ₹%.2f%n" +
                "Tax (%.0f%%): ₹%.2f%n" +
                "Discount   : ₹%.2f%n" +
                "TOTAL      : ₹%.2f%n" +
                "Status     : %s%n" +
                "==================",
                billId, billType,
                appointment.getPatient().getName(),
                appointment.getDoctor().getName(),
                billedAt.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")),
                baseAmount, taxRate * 100, baseAmount * taxRate,
                discountAmount, totalAmount,
                isPaid ? "PAID" : "UNPAID");
    }

    // --- Payable ---
    @Override public double  getTotal()        { return totalAmount; }
    @Override public boolean isPaymentDue()    { return !isPaid; }
    @Override public void    processPayment()  {
        this.isPaid = true;
        System.out.printf("  Payment of ₹%.2f processed for Bill %s.%n", totalAmount, billId);
    }

    // --- Getters ---
    public String      getBillId()         { return billId; }
    public Appointment getAppointment()    { return appointment; }
    public double      getBaseAmount()     { return baseAmount; }
    public double      getTaxRate()        { return taxRate; }
    public double      getDiscountAmount() { return discountAmount; }
    public boolean     isPaid()            { return isPaid; }
    public String      getBillType()       { return billType; }
    public static int  getTotalBills()     { return totalBills; }

    public void setBillType(String type)   { this.billType = type; }

    public String toCsv() {
        return String.join(",",
                billId,
                appointment.getAppointmentId(),
                String.valueOf(baseAmount),
                String.valueOf(taxRate),
                String.valueOf(discountAmount),
                String.valueOf(totalAmount),
                String.valueOf(isPaid),
                billType);
    }

    @Override
    public String toString() {
        return String.format("Bill %-10s | Appt: %-10s | ₹%.2f | %s",
                billId, appointment.getAppointmentId(), totalAmount,
                isPaid ? "PAID" : "UNPAID");
    }
}
