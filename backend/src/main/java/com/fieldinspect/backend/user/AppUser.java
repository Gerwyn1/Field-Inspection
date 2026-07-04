package com.fieldinspect.backend.user;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A person who uses the app — the technician doing inspections, or their supervisor.
 *
 * Why "AppUser" and not just "User"?
 *   1. USER is a reserved word in most databases (H2 and SQL Server included), so a
 *      table named "user" breaks the generated SQL — we map to "users" instead.
 *   2. In sub-step 4 we add Spring Security, which ships its own `User` class;
 *      calling ours AppUser avoids a confusing import clash.
 *
 * No password field yet — it arrives in sub-step 4 together with hashing, so we are
 * never tempted to store (or accidentally expose over JSON) a plain-text password.
 */
@Entity
@Table(name = "users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    /** unique = true adds a UNIQUE constraint — the DB itself rejects duplicate emails. */
    @Column(unique = true)
    private String email;

    private String role; // TECHNICIAN, SUPERVISOR

    /**
     * The BCrypt HASH of the password — never the plain text.
     * @JsonIgnore -> Jackson skips this field when serializing to JSON, so no API
     * response can ever leak it (not even the hash).
     */
    @JsonIgnore
    private String password;

    /** No-arg constructor required by JPA. */
    protected AppUser() {
    }

    public AppUser(String fullName, String email, String role, String password) {
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
