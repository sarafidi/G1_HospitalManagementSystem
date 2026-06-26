package controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import model.AppStatus;
import model.MedicalNote;
import util.DataStore;

public class MedicalNoteController {

    private final DataStore dataStore = DataStore.getInstance();
    private final AppointmentController apptController = new AppointmentController();

    // Add Medical Note
    public void addMedicalNote(MedicalNote note) {
        for (MedicalNote existingNote : dataStore.getMedicalNotes()) {
            if (existingNote.getAppointmentId().equalsIgnoreCase(note.getAppointmentId())) {
                System.out.println("Error: Notes for this appointment already exist. Please update the existing note instead of adding a new one.");
                return;
            }
        }

        dataStore.getMedicalNotes().add(note);
        dataStore.saveMedicalNotes();
        System.out.println("Medical Note " + note.getNoteId() + " successfully saved!");

        apptController.updateStatus(note.getAppointmentId(), AppStatus.COMPLETED);
    }

    // View Medical Notes by Patient
    public List<MedicalNote> getNotesByPatient(String patientId) {
        List<MedicalNote> filteredList = new ArrayList<>();
        for (MedicalNote note : dataStore.getMedicalNotes()) {
            if (note.getPatientId().equalsIgnoreCase(patientId)) {
                filteredList.add(note);
            }
        }
        return filteredList;
    }

    // Update Medical Note
    public void updateMedicalNote(String noteId, String newDiagnosis, String newPrescription) {
        for (MedicalNote note : dataStore.getMedicalNotes()) {
            if (note.getNoteId().equalsIgnoreCase(noteId)) {
                note.setDiagnosis(newDiagnosis);
                note.setPrescription(newPrescription);
                note.setUpdatedAt(LocalDateTime.now());
                
                dataStore.saveMedicalNotes();
                System.out.println("Medical Note " + noteId + " successfully updated!");
                return;
            }
        }
    }
}