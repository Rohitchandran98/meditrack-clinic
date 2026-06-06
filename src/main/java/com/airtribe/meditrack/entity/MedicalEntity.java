package com.airtribe.meditrack.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Abstract base for all domain entities.
 * Demonstrates: abstract class, static initialization, AtomicInteger.
 */
public abstract class MedicalEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    // AtomicInteger for thread-safe entity counting
    private static final AtomicInteger totalEntitiesCreated = new AtomicInteger(0);

    protected String id;
    protected LocalDateTime createdAt;

    // Static initializer block — runs once when class is first loaded
    static {
        System.out.println("[MedicalEntity] Entity tracking system initialized.");
    }

    protected MedicalEntity(String id) {
        this.id = id;
        this.createdAt = LocalDateTime.now();
        totalEntitiesCreated.incrementAndGet();
    }

    public String getId() { return id; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public static int getTotalEntitiesCreated() { return totalEntitiesCreated.get(); }

    /** Each subclass must describe itself. */
    public abstract String getDescription();

    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "] ID=" + id;
    }
}
