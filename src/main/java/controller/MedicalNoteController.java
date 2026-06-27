package controller;

import java.util.ArrayList;
import java.util.List;

import model.MedicalNote;

public class MedicalNoteController {
    private List<MedicalNote> notesList;

    public MedicalNoteController() {
        // Sepatutnya pancing dari DataStore JSON korang, buat masa ni kita simpan dalam Memory List dulu
        this.notesList = new ArrayList<>();
    }

    // Fungsi untuk hantar/simpan Medical Note baharu
    public void submitMedicalNote(MedicalNote note) {
        // Cek kalau dah wujud nota untuk Appt ID ni (elak duplicate), kita remove yang lama dan ganti baru
        notesList.removeIf(n -> n.getAppointmentId().equals(note.getAppointmentId()));
        notesList.add(note);
        
        // TODO: Sarafina/Putra punya DataStore.saveMedicalNotes(notesList) panggil kat sini kalau ada
    }

    // Fungsi untuk update Medical Note sedia ada
    public void updateMedicalNote(MedicalNote note) {
        for (int i = 0; i < notesList.size(); i++) {
            if (notesList.get(i).getAppointmentId().equals(note.getAppointmentId())) {
                notesList.set(i, note);
                break;
            }
        }
        // TODO: Panggil DataStore JSON untuk simpan perubahan
    }

    // Ambil semua senarai nota perubatan
    public List<MedicalNote> getAllMedicalNotes() {
        return notesList;
    }

    // Cari nota spesifik berdasarkan Appointment ID
    public MedicalNote getNoteByAppointmentId(String apptId) {
        for (MedicalNote note : notesList) {
            if (note.getAppointmentId().equals(apptId)) {
                return note;
            }
        }
        return null;
    }
}