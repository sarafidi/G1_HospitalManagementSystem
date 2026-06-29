package controller;

import java.time.LocalDateTime;
import java.util.List;

import model.MedicalNote;
import util.DataStore;

public class MedicalNoteController {
    private final DataStore dataStore = DataStore.getInstance();
    private final List<MedicalNote> notesList = dataStore.getMedicalNotes();

    // Save or update a medical note
    public void submitMedicalNote(MedicalNote note) {
        notesList.removeIf(n -> n.getAppointmentId().equals(note.getAppointmentId()));
        notesList.add(note);
        dataStore.saveMedicalNotes();
    }

    // Update an existing medical note
    public void updateMedicalNote(MedicalNote note) {
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
    @SuppressWarnings("unused")
    public List<MedicalNote> getAllMedicalNotes() {
        return notesList;
    }

    // Retrieve a medical note by appointment ID
    public MedicalNote getNoteByAppointmentId(String apptId) {
        for (MedicalNote note : notesList) {
            if (note.getAppointmentId().equals(apptId)) {
                return note;
            }
        }
        return null;
    }
}