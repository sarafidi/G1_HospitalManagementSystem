package model;

public class Doctor extends Person {

    private String doctorId;
    private int age;
    private String gender;
    private String phone;
    private String email;
    private String specialization;
    private String licenseNo;
    private String department;

    public Doctor() {
    }

    public Doctor(String doctorId, String name, int age, String gender,
                  String phone, String email, String specialization,
                  String licenseNo, String department) {
        super(name); 
        this.doctorId = doctorId;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
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
