package controller;

import model.Doctor;
import util.DatabaseManager;
import util.IDGenerator;
import util.Validator;

import java.sql.ResultSet;
import java.util.ArrayList;

public class DoctorController {

    DatabaseManager db = DatabaseManager.getInstance();

    public String addDoctor(String name, int age, String gender,
                            String phone, String email,
                            String specialization, String licenseNo,
                            String department) {

        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isNonEmpty(specialization)) {
            throw new IllegalArgumentException("Specialization cannot be empty.");
        }

        if (!Validator.isNonEmpty(licenseNo)) {
            throw new IllegalArgumentException("License number cannot be empty.");
        }

        if (!Validator.isNonEmpty(phone)) {
            throw new IllegalArgumentException("Phone cannot be empty.");
        }

        if (age < 0 || age > 100) {
            throw new IllegalArgumentException("Age must be between 0 and 100.");
        }

        String id = IDGenerator.generateDoctorId();

        String sql = "INSERT INTO doctors (doctor_id, name, age, gender, phone, email, specialization, license_no, department) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        db.executeUpdate(sql, id, name, age, gender, phone, email, specialization, licenseNo, department);

        return id;
    }

 
    public ArrayList<Doctor> getAllDoctors() {

        ArrayList<Doctor> list = new ArrayList<>();

        String sql = "SELECT * FROM doctors ORDER BY name ASC";

        try {
            ResultSet rs = db.executeQuery(sql);

            while (rs.next()) {
                Doctor d = new Doctor();
                d.setDoctorId(rs.getString("doctor_id"));
                d.setName(rs.getString("name"));
                d.setAge(rs.getInt("age"));
                d.setGender(rs.getString("gender"));
                d.setPhone(rs.getString("phone"));
                d.setEmail(rs.getString("email"));
                d.setSpecialization(rs.getString("specialization"));
                d.setLicenseNo(rs.getString("license_no"));
                d.setDepartment(rs.getString("department"));

                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Doctor getDoctorById(String doctorId) {

        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        Doctor d = null;

        try {
            ResultSet rs = db.executeQuery(sql, doctorId);

            if (rs.next()) {
                d = new Doctor();
                d.setDoctorId(rs.getString("doctor_id"));
                d.setName(rs.getString("name"));
                d.setAge(rs.getInt("age"));
                d.setGender(rs.getString("gender"));
                d.setPhone(rs.getString("phone"));
                d.setEmail(rs.getString("email"));
                d.setSpecialization(rs.getString("specialization"));
                d.setLicenseNo(rs.getString("license_no"));
                d.setDepartment(rs.getString("department"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return d;
    }


    public ArrayList<Doctor> searchDoctors(String keyword) {

        ArrayList<Doctor> list = new ArrayList<>();

        String sql = "SELECT * FROM doctors WHERE LOWER(name) LIKE ?";

        try {
            ResultSet rs = db.executeQuery(sql, "%" + keyword.toLowerCase() + "%");

            while (rs.next()) {
                Doctor d = new Doctor();
                d.setDoctorId(rs.getString("doctor_id"));
                d.setName(rs.getString("name"));
                d.setAge(rs.getInt("age"));
                d.setGender(rs.getString("gender"));
                d.setPhone(rs.getString("phone"));
                d.setEmail(rs.getString("email"));
                d.setSpecialization(rs.getString("specialization"));
                d.setLicenseNo(rs.getString("license_no"));
                d.setDepartment(rs.getString("department"));

                list.add(d);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updateDoctor(String doctorId, String name, int age,
                             String gender, String phone, String email,
                             String specialization, String licenseNo,
                             String department) {

        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isNonEmpty(specialization)) {
            throw new IllegalArgumentException("Specialization cannot be empty.");
        }

        if (!Validator.isNonEmpty(licenseNo)) {
            throw new IllegalArgumentException("License number cannot be empty.");
        }

        String sql = "UPDATE doctors SET name=?, age=?, gender=?, phone=?, email=?, specialization=?, license_no=?, department=? WHERE doctor_id=?";

        db.executeUpdate(sql, name, age, gender, phone, email, specialization, licenseNo, department, doctorId);
    }


    public void updateSpecialization(String doctorId, String specialization) {

        if (!Validator.isNonEmpty(specialization)) {
            throw new IllegalArgumentException("Specialization cannot be empty.");
        }

        String sql = "UPDATE doctors SET specialization=? WHERE doctor_id=?";

        db.executeUpdate(sql, specialization, doctorId);
    }
 
    // delete a doctor will fail if doctor still has appointments (database will block it)
    public void deleteDoctor(String doctorId) {

        String sql = "DELETE FROM doctors WHERE doctor_id = ?";

        db.executeUpdate(sql, doctorId);
    }
}
