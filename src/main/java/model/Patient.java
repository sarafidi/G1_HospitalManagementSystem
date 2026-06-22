package model;

public class Patient extends Person {

    private String patientId;
    private String bloodType;
    private String medicalHistory;
    private String registeredDate;


    public Patient() {
        super(null, 0, null, null, null);
    }


    public Patient(String patientId, String name, int age, Gender gender,
                   String phone, String email, String bloodType,
                   String medicalHistory, String registeredDate) {
        
        super(name, age, gender, phone, email);
        this.patientId = patientId;
        this.bloodType = bloodType;
        this.medicalHistory = medicalHistory;
        this.registeredDate = registeredDate;
    }

    
    @Override
    public String getInfo() {
        return "Patient ID: " + patientId + " | Name: " + getName() + " | Age: " + getAge();
    }

    
    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }


    public String getMedicalHistory() {
        return medicalHistory;
    }

    public void setMedicalHistory(String medicalHistory) {
        this.medicalHistory = medicalHistory;
    }

    public String getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(String registeredDate) {
        this.registeredDate = registeredDate;
    }

    // used to show patient name in dropdowns or lists
    @Override
    public String toString() {
        return getName() + " (" + patientId + ")";
    }
}
