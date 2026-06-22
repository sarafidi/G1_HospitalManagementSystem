package controller;

import model.Doctor;
import model.Gender;
import util.DataStore;
import util.IDGenerator;
import util.Validator;

import java.util.ArrayList;


public class DoctorController {

    // get the DataStore singleton to access doctor list
    DataStore dataStore = DataStore.getInstance();

    // add a new doctor
    public String addDoctor(String name, int age, Gender gender,
                            String phone, String email,
                            String specialization, String licenseNo,
                            String department) {

        // check required fields
        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isNonEmpty(specialization)) {
            throw new IllegalArgumentException("Specialization cannot be empty.");
        }

        if (!Validator.isNonEmpty(licenseNo)) {
            throw new IllegalArgumentException("License number cannot be empty.");
        }

        if (!Validator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must start with 0 and be 10-11 digits.");
        }

        if (!Validator.isValidAge(age)) {
            throw new IllegalArgumentException("Age must be between 0 and 125.");
        }

        // check if license number already exists
        ArrayList<Doctor> doctors = dataStore.getDoctors();
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getLicenseNo().equals(licenseNo)) {
                throw new IllegalArgumentException("License number already exists.");
            }
        }

        // generate a new doctor id like DOC-0001
        String id = IDGenerator.generateDoctorId();

        // create doctor object
        Doctor newDoctor = new Doctor(id, name, age, gender, phone, email, specialization, licenseNo, department);

        // add to list and save
        dataStore.getDoctors().add(newDoctor);
        dataStore.saveDoctors();

        return id;
    }

    // get all doctors
    public ArrayList<Doctor> getAllDoctors() {
        return dataStore.getDoctors();
    }

    // find one doctor by id
    public Doctor getDoctorById(String doctorId) {
        ArrayList<Doctor> doctors = dataStore.getDoctors();

        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            if (d.getDoctorId().equals(doctorId)) {
                return d;
            }
        }

        return null;
    }

    // search doctors by name
    public ArrayList<Doctor> searchDoctors(String keyword) {
        ArrayList<Doctor> results = new ArrayList<>();
        ArrayList<Doctor> doctors = dataStore.getDoctors();

        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            if (d.getName().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(d);
            }
        }

        return results;
    }

    // update a doctor's info
    public void updateDoctor(String doctorId, String name, int age, Gender gender,
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

        // find the doctor and update their fields
        Doctor d = getDoctorById(doctorId);

        if (d == null) {
            throw new IllegalArgumentException("Doctor not found.");
        }

        d.setName(name);
        d.setGender(gender);
        d.setPhone(phone);
        d.setEmail(email);
        d.setSpecialization(specialization);
        d.setLicenseNo(licenseNo);
        d.setDepartment(department);

        // save changes
        dataStore.saveDoctors();
    }

    // update only the specialization of a doctor
    public void updateSpecialization(String doctorId, String specialization) {

        if (!Validator.isNonEmpty(specialization)) {
            throw new IllegalArgumentException("Specialization cannot be empty.");
        }

        Doctor d = getDoctorById(doctorId);

        if (d == null) {
            throw new IllegalArgumentException("Doctor not found.");
        }

        d.setSpecialization(specialization);
        dataStore.saveDoctors();
    }

    // delete a doctor
    public void deleteDoctor(String doctorId) {
        ArrayList<Doctor> doctors = dataStore.getDoctors();

        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getDoctorId().equals(doctorId)) {
                doctors.remove(i);
                break;
            }
        }

        dataStore.saveDoctors();
    }
}
