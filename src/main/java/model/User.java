package model;

import util.HashUtil;

public class User {
    // private fields
    private String userId;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String passwordHash;
    private Role role;
    private boolean isActive;
    private boolean isFirstLogin;
    private String doctorId;
    private String createdAt;

    // constructor
    public User(String userId, String username, String name, String email, String phone, String passwordHash, Role role, boolean isActive, boolean isFirstLogin, String doctorId, String createdAt) {
        this.userId = userId;
        this.username = username;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.passwordHash = passwordHash;
        this.role = role;
        this.isActive = isActive;
        this.isFirstLogin = isFirstLogin;
        this.doctorId = doctorId;
        this.createdAt = createdAt;
    }

    // getters
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return isActive; }
    public boolean isFirstLogin() { return isFirstLogin; }
    public String getDoctorId() { return doctorId; }
    public String getCreatedAt() { return createdAt; }

    // setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public void setActive(boolean active) { isActive = active; }
    public void setFirstLogin(boolean firstLogin) { isFirstLogin = firstLogin; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public boolean authenticate(String inputPassword) {
        return HashUtil.verify(inputPassword, this.passwordHash);
    }

    public boolean hasPermission(Role requiredRole) {
        return this.role == requiredRole;
    }
}