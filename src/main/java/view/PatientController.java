package controller;

import model.Patient;
import util.DatabaseManager;
import util.IDGenerator;
import util.Validator;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;

public class PatientController {

  
    DatabaseManager db = DatabaseManager.getInstance();


    public String addPatient(String name, int age, String gender,
                             String phone, String email,
                             String bloodType, String medicalHistory) {


        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isNonEmpty(phone)) {
            throw new IllegalArgumentException("Phone cannot be empty.");
        }

        if (!Validator.isNonEmpty(gender)) {
            throw new IllegalArgumentException("Gender cannot be empty.");
        }

        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150.");
        }
      
        String id = IDGenerator.generatePatientId();

        String today = LocalDate.now().toString();

        String sql = "INSERT INTO patients (patient_id, name, age, gender, phone, email, blood_type, medical_history, registered_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        db.executeUpdate(sql, id, name, age, gender, phone, email, bloodType, medicalHistory, today);

        return id;
    }

    public ArrayList<Patient> getAllPatients() {

        ArrayList<Patient> list = new ArrayList<>();

        String sql = "SELECT * FROM patients ORDER BY name ASC";

        try {
            ResultSet rs = db.executeQuery(sql);


            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getString("patient_id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setGender(rs.getString("gender"));
                p.setPhone(rs.getString("phone"));
                p.setEmail(rs.getString("email"));
                p.setBloodType(rs.getString("blood_type"));
                p.setMedicalHistory(rs.getString("medical_history"));
                p.setRegisteredDate(rs.getString("registered_date"));

                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public Patient getPatientById(String patientId) {

        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        Patient p = null;

        try {
            ResultSet rs = db.executeQuery(sql, patientId);

            if (rs.next()) {
                p = new Patient();
                p.setPatientId(rs.getString("patient_id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setGender(rs.getString("gender"));
                p.setPhone(rs.getString("phone"));
                p.setEmail(rs.getString("email"));
                p.setBloodType(rs.getString("blood_type"));
                p.setMedicalHistory(rs.getString("medical_history"));
                p.setRegisteredDate(rs.getString("registered_date"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return p;
    }

    public ArrayList<Patient> searchPatient(String keyword) {

        ArrayList<Patient> list = new ArrayList<>();

        String sql = "SELECT * FROM patients WHERE LOWER(name) LIKE ?";

        try {
            ResultSet rs = db.executeQuery(sql, "%" + keyword.toLowerCase() + "%");

            while (rs.next()) {
                Patient p = new Patient();
                p.setPatientId(rs.getString("patient_id"));
                p.setName(rs.getString("name"));
                p.setAge(rs.getInt("age"));
                p.setGender(rs.getString("gender"));
                p.setPhone(rs.getString("phone"));
                p.setEmail(rs.getString("email"));
                p.setBloodType(rs.getString("blood_type"));
                p.setMedicalHistory(rs.getString("medical_history"));
                p.setRegisteredDate(rs.getString("registered_date"));

                list.add(p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public void updatePatient(String patientId, String name, int age,
                              String gender, String phone, String email,
                              String bloodType, String medicalHistory) {

        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isNonEmpty(phone)) {
            throw new IllegalArgumentException("Phone cannot be empty.");
        }

        if (age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150.");
        }

        String sql = "UPDATE patients SET name=?, age=?, gender=?, phone=?, email=?, blood_type=?, medical_history=? WHERE patient_id=?";

        db.executeUpdate(sql, name, age, gender, phone, email, bloodType, medicalHistory, patientId);
    }

    public void deletePatient(String patientId) {

        String sql = "DELETE FROM patients WHERE patient_id = ?";

        db.executeUpdate(sql, patientId);
    }
}
