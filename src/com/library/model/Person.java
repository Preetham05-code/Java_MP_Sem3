package com.library.model;

import java.io.Serializable;

/**
 * Abstract base class shared by {@link Admin} and {@link Member}.
 * Demonstrates inheritance, encapsulation (private password field) and
 * abstraction (abstract methods implemented differently by subclasses,
 * i.e. polymorphism).
 */
public abstract class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String id;
    protected String name;
    protected String username;
    private String password;

    public Person(String id, String name, String username, String password) {
        this.id = id;
        this.name = name;
        this.username = username;
        this.password = password;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public boolean checkPassword(String candidate) {
        return password != null && password.equals(candidate);
    }

    /** @return a short role label, e.g. "ADMIN" or "MEMBER" (polymorphic). */
    public abstract String getRole();

    /** @return a human readable profile summary (polymorphic). */
    public abstract String displayProfile();
}
