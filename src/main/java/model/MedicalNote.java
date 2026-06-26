package model;

import java.time.LocalDateTime;

public class MedicalNote {

    // Private Fields - Encapsulation
    private String noteId;          
    private String appointmentId;    
    private String doctorId;         
    private String patientId;        
    private String diagnosis;        
    private String prescription;     
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; 

    // Empty Constructor
    public MedicalNote() {
    }

    // Full Constructor
    public MedicalNote(String noteId, String appointmentId, String doctorId, String patientId, String diagnosis, String prescription) {
        this.noteId = noteId;
        this.appointmentId = appointmentId;
        this.doctorId = doctorId;
        this.patientId = patientId;
        this.diagnosis = diagnosis;
        this.prescription = prescription;
        this.createdAt = LocalDateTime.now(); 
        this.updatedAt = null;
    }

    // Getters
    public String getNoteId() { return noteId; }
    public String getAppointmentId() { return appointmentId; }
    public String getDoctorId() { return doctorId; }
    public String getPatientId() { return patientId; }
    public String getDiagnosis() { return diagnosis; }
    public String getPrescription() { return prescription; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setNoteId(String noteId) { this.noteId = noteId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setPrescription(String prescription) { this.prescription = prescription; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}