package controller;

import model.Doctor;
import model.Gender;
import util.DataStore;
import util.IDGenerator;
import util.Validator;

import java.util.ArrayList;

public class DoctorController {

    // Encapsulate singleton instance as a private final field
    private final DataStore dataStore = DataStore.getInstance();

    // Add a new doctor profile
    public String addDoctor(String name, int age, Gender gender,
                            String phone, String email,
                            String specialization, String licenseNo,
                            String department) {

        // Encapsulate required, format, and uniqueness checks (isNew = true)
        validateDoctorDetails(null, name, age, phone, email, specialization, licenseNo, true);

        // Generate a new doctor ID (e.g. DOC-0001)
        String id = IDGenerator.generateDoctorId();

        // Instantiate new Doctor object
        Doctor newDoctor = new Doctor(id, name, age, gender, phone, email, specialization, licenseNo, department);

        // Add to active collection and write back to data store
        dataStore.getDoctors().add(newDoctor);
        dataStore.saveDoctors();

        return id;
    }

    // Retrieve list of all doctors
    public ArrayList<Doctor> getAllDoctors() {
        return dataStore.getDoctors();
    }

    // Retrieve doctor profile by unique ID
    public Doctor getDoctorById(String doctorId) {
        ArrayList<Doctor> doctors = dataStore.getDoctors();
        for (Doctor d : doctors) {
            if (d.getDoctorId().equals(doctorId)) {
                return d;
            }
        }
        return null;
    }

    // Search doctor list by name keyword
    public ArrayList<Doctor> searchDoctors(String keyword) {
        ArrayList<Doctor> results = new ArrayList<>();
        ArrayList<Doctor> doctors = dataStore.getDoctors();

        for (Doctor d : doctors) {
            if (d.getName().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(d);
            }
        }
        return results;
    }

    // Update doctor profile attributes
    public void updateDoctor(String doctorId, String name, int age, Gender gender,
                             String phone, String email,
                             String specialization, String licenseNo,
                             String department) {

        Doctor d = getDoctorById(doctorId);
        if (d == null) {
            throw new IllegalArgumentException("Doctor not found.");
        }

        // Encapsulate required, format, and uniqueness checks (isNew = false)
        validateDoctorDetails(doctorId, name, age, phone, email, specialization, licenseNo, false);

        // Update fields (Encapsulation allows state changes through setter interfaces)
        d.setName(name);
        d.setAge(age); // Fixed silent bug: Previously age was validated but never set on the Doctor object
        d.setGender(gender);
        d.setPhone(phone);
        d.setEmail(email);
        d.setSpecialization(specialization);
        d.setLicenseNo(licenseNo);
        d.setDepartment(department);

        // Save modifications to database
        dataStore.saveDoctors();
    }

    // Update specialization attribute only
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

    // Delete a doctor profile
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

    // helper method to validate doctor details
    private void validateDoctorDetails(String doctorId, String name, int age, String phone, String email,
                                       String specialization, String licenseNo, boolean isNew) {
        // required fields checks
        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }
        if (!Validator.isNonEmpty(specialization)) {
            throw new IllegalArgumentException("Specialization cannot be empty.");
        }
        if (!Validator.isNonEmpty(licenseNo)) {
            throw new IllegalArgumentException("License number cannot be empty.");
        }

        // format checks
        if (!Validator.isValidEmail(email)) {
            throw new IllegalArgumentException("Error: Email is invalid.");
        }
        if (!Validator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must start with 0 and be 10-11 digits.");
        }
        
        // apply consistent professional age restrictions (Doctors must be 18 to 125)
        if (age < 18 || age > 125) {
            throw new IllegalArgumentException("Age not eligible, must be between 18 and 125.");
        }

        // uniqueness checks
        ArrayList<Doctor> doctors = dataStore.getDoctors();
        for (Doctor d : doctors) {
            if (!isNew && d.getDoctorId().equals(doctorId)) {
                continue; // Skip current profile checks during update operations
            }
            if (d.getName().equalsIgnoreCase(name)) {
                throw new IllegalArgumentException("Doctor name already exists.");
            }
            if (d.getEmail().equalsIgnoreCase(email)) {
                throw new IllegalArgumentException("Email already exists.");
            }
            if (d.getLicenseNo().equalsIgnoreCase(licenseNo)) {
                throw new IllegalArgumentException("License number already exists.");
            }
        }
    }
}