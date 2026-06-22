package controller;

import model.Gender;
import model.Patient;
import util.DataStore;
import util.IDGenerator;
import util.Validator;

import java.time.LocalDate;
import java.util.ArrayList;

public class PatientController {

    // get the DataStore singleton to access patient list
    DataStore dataStore = DataStore.getInstance();

    // add a new patient
    public String addPatient(String name, int age, Gender gender,
                             String phone, String email,
                             String bloodType, String medicalHistory) {

        // check required fields are not empty
        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must start with 0 and be 10-11 digits.");
        }

        if (gender == null) {
            throw new IllegalArgumentException("Gender cannot be empty.");
        }

        if (!Validator.isValidAge(age)) {
            throw new IllegalArgumentException("Age must be between 0 and 125.");
        }

        // generate a new patient id 
        String id = IDGenerator.generatePatientId();

        // get today's date as the registered date
        String today = LocalDate.now().toString();

        // create the patient object
        Patient newPatient = new Patient(id, name, age, gender, phone, email, bloodType, medicalHistory, today);

        // add to the list in DataStore
        dataStore.getPatients().add(newPatient);

        // save to json file
        dataStore.savePatients();

        return id;
    }

    // get all patients from DataStore
    public ArrayList<Patient> getAllPatients() {
        return dataStore.getPatients();
    }

    // find one patient by their id
    public Patient getPatientById(String patientId) {
        ArrayList<Patient> patients = dataStore.getPatients();

        // loop through all patients to find the matching one
        for (int i = 0; i < patients.size(); i++) {
            Patient p = patients.get(i);
            if (p.getPatientId().equals(patientId)) {
                return p;
            }
        }

        // if not found
        return null;
    }

    // search patients by name - returns list of matches
    public ArrayList<Patient> searchPatient(String keyword) {
        ArrayList<Patient> results = new ArrayList<>();
        ArrayList<Patient> patients = dataStore.getPatients();

        // check each patient if name contains the keyword
        for (int i = 0; i < patients.size(); i++) {
            Patient p = patients.get(i);
            if (p.getName().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(p);
            }
        }

        return results;
    }

    // update an existing patient's info
    public void updatePatient(String patientId, String name, int age, Gender gender,
                              String phone, String email,
                              String bloodType, String medicalHistory) {

        // validate before updating
        if (!Validator.isNonEmpty(name)) {
            throw new IllegalArgumentException("Name cannot be empty.");
        }

        if (!Validator.isValidPhone(phone)) {
            throw new IllegalArgumentException("Phone must start with 0 and be 10-11 digits.");
        }

        if (!Validator.isValidAge(age)) {
            throw new IllegalArgumentException("Age must be between 0 and 125.");
        }

        // find the patient and update their fields
        Patient p = getPatientById(patientId);

        if (p == null) {
            throw new IllegalArgumentException("Patient not found.");
        }

        p.setName(name);
        p.setAge(age);  
        p.setGender(gender);
        p.setPhone(phone);
        p.setEmail(email);
        p.setBloodType(bloodType);
        p.setMedicalHistory(medicalHistory);

        // save changes to json file
        dataStore.savePatients();
    }

    // delete a patient by id
    public void deletePatient(String patientId) {
        ArrayList<Patient> patients = dataStore.getPatients();

        // find the patient and remove them
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getPatientId().equals(patientId)) {
                patients.remove(i);
                break;
            }
        }

        // save changes
        dataStore.savePatients();
    }
}
