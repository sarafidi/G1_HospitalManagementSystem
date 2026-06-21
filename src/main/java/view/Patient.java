package model;

public class Patient extends Person {

    private String patientId;
    private int age;
    private String gender;
    private String phone;
    private String email;
    private String bloodType;
    private String medicalHistory;
    private String registeredDate;


    public Patient() {
    }

    public Patient(String patientId, String name, int age, String gender,
                   String phone, String email, String bloodType,
                   String medicalHistory, String registeredDate) {
        super(name); // call Person constructor to set the name
        this.patientId = patientId;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.bloodType = bloodType;
        this.medicalHistory = medicalHistory;
        this.registeredDate = registeredDate;
    }

    @Override
    public String getInfo() {
        return "Patient ID: " + patientId + " | Name: " + getName() + " | Age: " + age;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }


    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    @Override
    public String toString() {
        return getName() + " (" + patientId + ")";
    }
}
