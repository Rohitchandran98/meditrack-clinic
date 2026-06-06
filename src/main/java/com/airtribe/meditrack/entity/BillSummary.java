package com.airtribe.meditrack.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Immutable snapshot of a completed bill.
 * Demonstrates: immutability (final class, final fields, no setters), thread-safety.
 */
public final class BillSummary implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String    billId;
    private final String    patientName;
    private final String    doctorName;
    private final double    baseAmount;
    private final double    taxAmount;
    private final double    discountAmount;
    private final double    totalAmount;
    private final boolean   isPaid;
    private final LocalDateTime date;

    public BillSummary(String billId, String patientName, String doctorName,
                       double baseAmount, double taxAmount, double discountAmount,
                       double totalAmount, boolean isPaid, LocalDateTime date) {
        this.billId         = billId;
        this.patientName    = patientName;
        this.doctorName     = doctorName;
        this.baseAmount     = baseAmount;
        this.taxAmount      = taxAmount;
        this.discountAmount = discountAmount;
        this.totalAmount    = totalAmount;
        this.isPaid         = isPaid;
        this.date           = date;
    }

    /** Factory method — builds a BillSummary from an existing Bill. */
    public static BillSummary from(Bill bill) {
        double tax = bill.getBaseAmount() * bill.getTaxRate();
        return new BillSummary(
                bill.getBillId(),
                bill.getAppointment().getPatient().getName(),
                "Dr. " + bill.getAppointment().getDoctor().getName(),
                bill.getBaseAmount(), tax,
                bill.getDiscountAmount(),
                bill.getTotal(),
                bill.isPaid(),
                bill.getAppointment().getDateTime());
    }

    // --- Only getters, no setters ---
    public String      getBillId()         { return billId; }
    public String      getPatientName()    { return patientName; }
    public String      getDoctorName()     { return doctorName; }
    public double      getBaseAmount()     { return baseAmount; }
    public double      getTaxAmount()      { return taxAmount; }
    public double      getDiscountAmount() { return discountAmount; }
    public double      getTotalAmount()    { return totalAmount; }
    public boolean     isPaid()            { return isPaid; }
    public LocalDateTime getDate()         { return date; }

    @Override
    public String toString() {
        return String.format(
                "BillSummary[%s | Patient: %s | Dr: %s | Total: ₹%.2f | %s | %s]",
                billId, patientName, doctorName, totalAmount,
                isPaid ? "PAID" : "UNPAID",
                date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
    }
}
