package com.airtribe.meditrack.interfaces;

/**
 * Payable interface for billing entities.
 * Includes a default method for formatted payment display.
 */
public interface Payable {

    /** Returns the final total amount due. */
    double getTotal();

    /** Marks the payment as processed. */
    void processPayment();

    /** Returns true if payment has not yet been made. */
    boolean isPaymentDue();

    /** Default method — prints a formatted payment summary. */
    default void printPaymentSummary() {
        System.out.printf("Payment %s | Amount Due: ₹%.2f%n",
                isPaymentDue() ? "PENDING" : "COMPLETED", getTotal());
    }
}
