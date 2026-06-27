package model;

public class MedicalNote {
    private String noteId;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    
    // SOAP Elements
    private String subjective;
    private String objective;
    private String assessment;
    private String plan;
    
    // Radio & Checkbox Elements
    private boolean requireMc;
    private boolean followUpNeeded;
    private boolean referralNeeded;
    private boolean urgentCase;
    private boolean labTestOrdered;

    // Empty Constructor
    public MedicalNote() {}

    // Full Constructor
    public MedicalNote(String noteId, String appointmentId, String patientId, String doctorId, String subjective, String objective, String assessment, String plan, boolean requireMc, boolean followUpNeeded, boolean referralNeeded, boolean urgentCase, boolean labTestOrdered) {
        this.noteId = noteId;
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.subjective = subjective;
        this.objective = objective;
        this.assessment = assessment;
        this.plan = plan;
        this.requireMc = requireMc;
        this.followUpNeeded = followUpNeeded;
        this.referralNeeded = referralNeeded;
        this.urgentCase = urgentCase;
        this.labTestOrdered = labTestOrdered;
    }

    // Getters
    public String getNoteId() { return noteId; }
    public String getAppointmentId() { return appointmentId; }
    public String getPatientId() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public String getSubjective() { return subjective; }
    public String getObjective() { return objective; }
    public String getAssessment() { return assessment; }
    public String getPlan() { return plan; }

    public boolean isRequireMc() { return requireMc; }
    public boolean isFollowUpNeeded() { return followUpNeeded; }
    public boolean isReferralNeeded() { return referralNeeded; }
    public boolean isUrgentCase() { return urgentCase; }
    public boolean isLabTestOrdered() { return labTestOrdered; }

    // Setters
    public void setNoteId(String noteId) { this.noteId = noteId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }
    public void setSubjective(String subjective) { this.subjective = subjective; }
    public void setObjective(String objective) { this.objective = objective; }
    public void setAssessment(String assessment) { this.assessment = assessment; }
    public void setPlan(String plan) { this.plan = plan; } 

    public void setRequireMc(boolean requireMc) { this.requireMc = requireMc; }
    public void setFollowUpNeeded(boolean followUpNeeded) { this.followUpNeeded = followUpNeeded; }
    public void setReferralNeeded(boolean referralNeeded) { this.referralNeeded = referralNeeded; }
    public void setUrgentCase(boolean urgentCase) { this.urgentCase = urgentCase; }
    public void setLabTestOrdered(boolean labTestOrdered) { this.labTestOrdered = labTestOrdered; }
}