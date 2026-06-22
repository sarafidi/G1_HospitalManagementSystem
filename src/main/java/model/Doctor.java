package model;

public class Doctor extends Person {

    private String doctorId;
    private String specialization;
    private String licenseNo;
    private String department;

  
    public Doctor() {
        super(null, 0, null, null, null);
    }

    public Doctor(String doctorId, String name, int age, Gender gender,
                  String phone, String email, String specialization,
                  String licenseNo, String department) {
        super(name, age, gender, phone, email);
        this.doctorId = doctorId;
        this.specialization = specialization;
        this.licenseNo = licenseNo;
        this.department = department;
    }

    @Override
    public String getInfo() {
        return "Doctor ID: " + doctorId + " | Name: " + getName() + " | Specialization: " + specialization;
    }


    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }


    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    
    public void updateSpecialization(String specialization) {
        this.specialization = specialization;
    }

        public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }


    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    @Override
    public String toString() {
        return getName() + " - " + specialization + " (" + doctorId + ")";
    }
}
