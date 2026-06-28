package util;

import model.*;
import java.util.ArrayList;

public class IDGenerator {
    private static int extractMaxNumeric(ArrayList<String> ids) {
        // id.split("-") gives ["PAT", "0023"]
        // [1] gives "0023"
        // parseInt converts to 23
        int max = 0;
        for (String id : ids) {
            int num = Integer.parseInt(id.split("-")[1]);
            if (num > max) max = num;
        }
        return max;
    }

    public static String generateUserId() {
        ArrayList<String> ids = new ArrayList<>();
        for (User p : DataStore.getInstance().getUsers()) {
            ids.add(p.getUserId());
        }
        // %04d -> integer, min 4 digits, pad with zeros on left
        return String.format("USR-%04d", extractMaxNumeric(ids) + 1);
    }

    public static String generatePatientId() {
        ArrayList<String> ids = new ArrayList<>();
        for (Patient p : DataStore.getInstance().getPatients()) {
            ids.add(p.getPatientId());
        }
        // %04d -> integer, min 4 digits, pad with zeros on left
        return String.format("PAT-%04d", extractMaxNumeric(ids) + 1);
    }

    public static String generateDoctorId() {
        ArrayList<String> ids = new ArrayList<>();
        for (Doctor p : DataStore.getInstance().getDoctors()) {
            ids.add(p.getDoctorId());
        }
        // %04d -> integer, min 4 digits, pad with zeros on left
        return String.format("DOC-%04d", extractMaxNumeric(ids) + 1);
    }

    public static String generateAppointmentId() {
        ArrayList<String> ids = new ArrayList<>();
        for (Appointment p : DataStore.getInstance().getAppointments()) {
            ids.add(p.getAppointmentId());
        }
        // %04d -> integer, min 4 digits, pad with zeros on left
        return String.format("APT-%04d", extractMaxNumeric(ids) + 1);
    }

    public static String generateNoteId() {
        ArrayList<String> ids = new ArrayList<>();
        for (MedicalNote p : DataStore.getInstance().getMedicalNotes()) {
            ids.add(p.getNoteId());
        }
        // %04d -> integer, min 4 digits, pad with zeros on left
        return String.format("NOTE-%03d", extractMaxNumeric(ids) + 1);
    }

}