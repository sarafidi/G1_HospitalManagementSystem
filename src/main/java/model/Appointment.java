package model;

import java.time.LocalDateTime;

public class Appointment {

    // Private Fields - Encapsulation
    private String appointmentId;        
    private String patientId;           
    private String doctorId;             
    private LocalDateTime appointmentDateTime; 
    private AppStatus status;            
    private String notes;                
    private LocalDateTime createdAt;     

    // Empty Constructor
    public Appointment() {
    }

    // Full Constructor
    public Appointment(String appointmentId, String patientId, String doctorId, LocalDateTime appointmentDateTime, AppStatus status, String notes) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentDateTime = appointmentDateTime;
        this.status = status;
        this.notes = notes;
        this.createdAt = LocalDateTime.now();
    }

    // Getters
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDateTime getAppointmentDateTime() { return appointmentDateTime; }
    public AppStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) { this.appointmentDateTime = appointmentDateTime; }
    public void setStatus(AppStatus status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}