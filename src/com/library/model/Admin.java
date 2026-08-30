package com.library.model;

/** Represents a library staff member who manages the book catalog. */
public class Admin extends Person {

    private static final long serialVersionUID = 1L;

    private String employeeCode;

    public Admin(String id, String name, String username, String password, String employeeCode) {
        super(id, name, username, password);
        this.employeeCode = employeeCode;
    }

    public String getEmployeeCode() {
        return employeeCode;
    }

    @Override
    public String getRole() {
        return "ADMIN";
    }

    @Override
    public String displayProfile() {
        return "Admin  [ID=" + id + ", Name=" + name + ", EmployeeCode=" + employeeCode + "]";
    }
}
