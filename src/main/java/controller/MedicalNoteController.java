package controller;

import java.util.ArrayList;
import java.util.List;

import model.MedicalNote;

public class MedicalNoteController {
    private List<MedicalNote> notesList;

    public MedicalNoteController() {
        this.notesList = new ArrayList<>();
    }

    // Save or update a medical note
    public void submitMedicalNote(MedicalNote note) {
        notesList.removeIf(n -> n.getAppointmentId().equals(note.getAppointmentId()));
        notesList.add(note);        
    }

    // Update an existing medical note
    public void updateMedicalNote(MedicalNote note) {
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).getAppointmentId().equals(note.getAppointmentId())) {
                notesList.set(i, note);
                break;
            }
        }
    }

    // Retrieve all medical notes
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