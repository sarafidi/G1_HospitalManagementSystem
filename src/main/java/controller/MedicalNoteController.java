package controller;

import java.time.LocalDateTime;
import java.util.List;

import model.MedicalNote;
import util.DataStore;

public class MedicalNoteController {
    private final DataStore dataStore = DataStore.getInstance();

    // Save or update a medical note
    public void submitMedicalNote(MedicalNote note) {
        List<MedicalNote> notesList = dataStore.getMedicalNotes();
        notesList.removeIf(n -> n.getAppointmentId().equals(note.getAppointmentId()));
        notesList.add(note);
        dataStore.saveMedicalNotes();
    }

    // Update an existing medical note
    public void updateMedicalNote(MedicalNote note) {
        List<MedicalNote> notesList = dataStore.getMedicalNotes();
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).getAppointmentId().equals(note.getAppointmentId())) {
                notesList.set(i, note);
                break;
            }
        }
        note.setUpdatedAt(LocalDateTime.now());
        dataStore.saveMedicalNotes();
    }

    // Retrieve all medical notes
    public List<MedicalNote> getAllMedicalNotes() {
        return dataStore.getMedicalNotes();
    }

    // Retrieve a medical note by appointment ID
    public MedicalNote getNoteByAppointmentId(String apptId) {
        for (MedicalNote note : dataStore.getMedicalNotes()) {
            if (note.getAppointmentId().equals(apptId)) {
                return note;
            }
        }
        return null;
    }
}