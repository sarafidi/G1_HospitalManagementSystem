package model;

// Doctor class - stores info about the doctor
// also extends Person like Patient
public class Doctor extends Person {

    // doctor details
    private String doctorId;
    private int age;
    private String gender;
    private String phone;
    private String email;
    private String specialization;
    private String licenseNo;
    private String department;

    // empty constructor
    public Doctor() {

    }

    // constructor with all details
    public Doctor(String doctorId, String name, int age, String gender,
                  String phone, String email, String specialization,
                  String licenseNo, String department) {
        super(name); // call Person constructor
        this.doctorId = doctorId;
        this.age = age;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.specialization = specialization;
        this.licenseNo = licenseNo;
        this.department = department;
    }

    // override getInfo from Person class
    @Override
    public String getInfo() {
        return "Doctor ID: " + doctorId + " | Name: " + getName() + " | Specialization: " + specialization;
    }

    // getter and setter for doctorId
    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    // getter and setter for age
    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    // getter and setter for gender
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    // getter and setter for phone
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // getter and setter for email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // getter and setter for specialization
    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // this method is used to update only the specialization
    public void updateSpecialization(String specialization) {
        this.specialization = specialization;
    }

    // getter and setter for licenseNo
    public String getLicenseNo() {
        return licenseNo;
    }

    public void setLicenseNo(String licenseNo) {
        this.licenseNo = licenseNo;
    }

    // getter and setter for department
    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    // show doctor name and specialization in dropdown
    @Override
    public String toString() {
        return getName() + " - " + specialization + " (" + doctorId + ")";
    }
}
