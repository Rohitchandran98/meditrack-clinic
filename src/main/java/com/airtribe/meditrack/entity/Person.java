package com.airtribe.meditrack.entity;

import com.airtribe.meditrack.exception.InvalidDataException;
import com.airtribe.meditrack.util.Validator;

/**
 * Abstract base for all persons in the system (Doctor, Patient).
 * Demonstrates: inheritance, encapsulation, constructor chaining, Cloneable.
 */
public abstract class Person extends MedicalEntity implements Cloneable {

    private static final long serialVersionUID = 1L;

    private String name;
    private int age;
    private String phone;
    private String email;

    // Constructor chaining via this(...)
    protected Person(String id, String name, int age, String phone, String email)
            throws InvalidDataException {
        super(id); // calls MedicalEntity(id)
        Validator.validateName(name);
        Validator.validateAge(age);
        Validator.validatePhone(phone);
        Validator.validateEmail(email);
        this.name = name;
        this.age = age;
        this.phone = phone;
        this.email = email;
    }

    // --- Getters ---
    public String getName()  { return name; }
    public int    getAge()   { return age; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }

    // --- Setters with centralized validation ---
    public void setName(String name) throws InvalidDataException {
        Validator.validateName(name);
        this.name = name;
    }

    public void setAge(int age) throws InvalidDataException {
        Validator.validateAge(age);
        this.age = age;
    }

    public void setPhone(String phone) throws InvalidDataException {
        Validator.validatePhone(phone);
        this.phone = phone;
    }

    public void setEmail(String email) throws InvalidDataException {
        Validator.validateEmail(email);
        this.email = email;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone(); // shallow clone — subclasses handle deep copy
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;
        return id.equals(((Person) o).id);
    }

    @Override
    public int hashCode() { return id.hashCode(); }

    @Override
    public String toString() {
        return String.format("ID: %-10s | Name: %-25s | Age: %3d | Phone: %s | Email: %s",
                id, name, age, phone, email);
    }
}
