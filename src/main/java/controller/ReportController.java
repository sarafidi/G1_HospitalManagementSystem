package controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.stream.Collectors;

import model.Appointment;
import model.Doctor;
import model.MedicalNote;
import model.Patient;
import util.DataStore;

public class ReportController {

    private final DataStore dataStore = DataStore.getInstance();

    /**
     * Returns total number of patients.
     */
    public int getTotalPatients() {
        return dataStore.getPatients().size();
    }

    /**
     * Returns total number of doctors.
     */
    public int getTotalDoctors() {
        return dataStore.getDoctors().size();
    }

    /**
     * Returns total number of appointments.
     */
    public int getTotalAppointments() {
        return dataStore.getAppointments().size();
    }

    /**
     * Returns total appointments by status.
     */
    public int getTotalAppointmentsByStatus(String status) {
        return (int) dataStore.getAppointments().stream()
                .filter(a -> a.getStatus().toString().equals(status))
                .count();
    }

    /**
     * Returns total appointments for a specific doctor.
     */
    public int getTotalAppointmentsByDoctor(String doctorId) {
        return (int) dataStore.getAppointments().stream()
                .filter(a -> a.getDoctorId().equals(doctorId))
                .count();
    }

    /**
     * Returns filtered total appointments by status based on filter criteria.
     */
    public int getFilteredTotalAppointmentsByStatus(String status, String doctorId, String period) {
        ArrayList<Appointment> filtered = new ArrayList<>(dataStore.getAppointments());

        // Filter by doctor
        if (doctorId != null && !doctorId.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> a.getDoctorId().equals(doctorId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Filter by period
        if (period != null && !period.isEmpty() && !period.equals("All")) {
            LocalDateTime now = LocalDateTime.now();
            filtered = filtered.stream()
                    .filter(a -> {
                        LocalDateTime apptTime = parseDateTime(a.getAppointmentDateTime());
                        if (apptTime == null) {
                            return false;
                        }
                        switch (period) {
                            case "Today":
                                return apptTime.toLocalDate().equals(now.toLocalDate());
                            case "This Week":
                                return apptTime.isAfter(now.minusDays(7));
                            case "This Month":
                                return apptTime.getMonth().equals(now.getMonth())
                                        && apptTime.getYear() == now.getYear();
                            case "Past":
                                return apptTime.isBefore(now);
                            case "Upcoming":
                                return apptTime.isAfter(now);
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Filter by status
        return (int) filtered.stream()
                .filter(a -> a.getStatus().toString().equals(status))
                .count();
    }

    /**
     * Returns filtered total appointments based on filter criteria.
     */
    public int getFilteredTotalAppointments(String doctorId, String period) {
        ArrayList<Appointment> filtered = new ArrayList<>(dataStore.getAppointments());

        // Filter by doctor
        if (doctorId != null && !doctorId.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> a.getDoctorId().equals(doctorId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Filter by period
        if (period != null && !period.isEmpty() && !period.equals("All")) {
            LocalDateTime now = LocalDateTime.now();
            filtered = filtered.stream()
                    .filter(a -> {
                        LocalDateTime apptTime = parseDateTime(a.getAppointmentDateTime());
                        if (apptTime == null) {
                            return false;
                        }
                        switch (period) {
                            case "Today":
                                return apptTime.toLocalDate().equals(now.toLocalDate());
                            case "This Week":
                                return apptTime.isAfter(now.minusDays(7));
                            case "This Month":
                                return apptTime.getMonth().equals(now.getMonth())
                                        && apptTime.getYear() == now.getYear();
                            case "Past":
                                return apptTime.isBefore(now);
                            case "Upcoming":
                                return apptTime.isAfter(now);
                            default:
                                return true;
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        return filtered.size();
    }

    /**
     * Returns doctor schedule as ArrayList of String arrays. Each String[]
     * contains: {Doctor Name, Patient Name, DateTime, Status}
     */
    public ArrayList<String[]> getDoctorSchedule() {
        ArrayList<String[]> schedule = new ArrayList<>();

        for (Appointment appt : dataStore.getAppointments()) {
            String doctorName = getDoctorName(appt.getDoctorId());
            String patientName = getPatientName(appt.getPatientId());
            String dateTime = formatDateTime(appt.getAppointmentDateTime());

            schedule.add(new String[]{
                doctorName,
                patientName,
                dateTime,
                appt.getStatus().toString()
            });
        }
        return schedule;
    }

    /**
     * Returns filtered doctor schedule with search/filter criteria. ALWAYS
     * resolves doctor IDs to names for display.
     */
    public ArrayList<String[]> getFilteredDoctorSchedule(
            String doctorId, String status, String period, String patientSearch) {

        ArrayList<Appointment> filtered = new ArrayList<>(dataStore.getAppointments());

        // Filter by doctor
        if (doctorId != null && !doctorId.isEmpty()) {
            filtered = filtered.stream()
                    .filter(a -> a.getDoctorId().equals(doctorId))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Filter by status
        if (status != null && !status.isEmpty() && !status.equals("All Status")) {
            filtered = filtered.stream()
                    .filter(a -> a.getStatus().toString().equals(status))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Filter by period
        if (period != null && !period.isEmpty() && !period.equals("All")) {
            LocalDateTime now = LocalDateTime.now();
            filtered = filtered.stream()
                    .filter(a -> {
                        LocalDateTime apptTime = parseDateTime(a.getAppointmentDateTime());
                        if (apptTime == null) {
                            return false;
                        }
                        switch (period) {
                            case "Today":
                                    return apptTime.toLocalDate().equals(now.toLocalDate());
                            case "This Week":
                                    return apptTime.isAfter(now.minusDays(7));
                            case "This Month":
                                    return apptTime.getMonth().equals(now.getMonth())
                                            && apptTime.getYear() == now.getYear();
                            case "Past":
                                    return apptTime.isBefore(now);
                            case "Upcoming":
                                    return apptTime.isAfter(now);
                            default:
                                    return true;
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Filter by patient name
        if (patientSearch != null && !patientSearch.trim().isEmpty()) {
            String search = patientSearch.trim().toLowerCase();
            filtered = filtered.stream()
                    .filter(a -> getPatientName(a.getPatientId()).toLowerCase().contains(search))
                    .collect(Collectors.toCollection(ArrayList::new));
        }

        // Convert to String array format with NAMES (not IDs)
        ArrayList<String[]> result = new ArrayList<>();
        for (Appointment appt : filtered) {
            String doctorName = getDoctorName(appt.getDoctorId());   // Resolve doctor ID to NAME
            String patientName = getPatientName(appt.getPatientId()); // Resolve patient ID to NAME
            String dateTime = formatDateTime(appt.getAppointmentDateTime());

            result.add(new String[]{
                doctorName,
                patientName,
                dateTime,
                appt.getStatus().toString()
            });
        }
        return result;
    }

    /**
     * Returns doctor schedule for a specific doctor.
     */
    public ArrayList<String[]> getDoctorScheduleByDoctor(String doctorId) {
        return getFilteredDoctorSchedule(doctorId, null, null, null);
    }

    /**
     * Returns all medical notes for a specific patient.
     */
    public ArrayList<MedicalNote> getNotesByPatient(String patientId) {
        return dataStore.getMedicalNotes().stream()
                .filter(n -> n.getPatientId().equals(patientId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Gets all doctors from DataStore for dropdown.
     */
    public ArrayList<Doctor> getAllDoctors() {
        return dataStore.getDoctors();
    }

    /**
     * Gets all appointment statuses for dropdown.
     */
    public String[] getAllStatuses() {
        return new String[]{"All Status", "SCHEDULED", "CONFIRMED", "COMPLETED", "CANCELLED"};
    }

    /**
     * Gets period options for dropdown.
     */
    public String[] getPeriodOptions() {
        return new String[]{"All", "Today", "This Week", "This Month", "Past", "Upcoming"};
    }

    // --- Helper Methods ---
    /**
     * Gets doctor name from doctor ID. If not found, returns "Unknown Doctor".
     */
    private String getDoctorName(String doctorId) {
        for (Doctor d : dataStore.getDoctors()) {
            if (d.getDoctorId().equals(doctorId)) {
                return d.getName();
            }
        }
        return "Unknown Doctor";
    }

    /**
     * Gets patient name from patient ID. If not found, returns "Unknown
     * Patient".
     */
    private String getPatientName(String patientId) {
        for (Patient p : dataStore.getPatients()) {
            if (p.getPatientId().equals(patientId)) {
                return p.getName();
            }
        }
        return "Unknown Patient";
    }

    /**
     * Formats LocalDateTime to "dd MMM yyyy HH:mm" format.
     */
    private String formatDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.isEmpty()) {
            return "N/A";
        }
        LocalDateTime dateTime = parseDateTime(dateTimeText);
        if (dateTime == null) {
            return dateTimeText;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");
        return dateTime.format(formatter);
    }

    private LocalDateTime parseDateTime(String dateTimeText) {
        if (dateTimeText == null || dateTimeText.trim().isEmpty()) {
            return null;
        }

        String value = dateTimeText.trim();

        // Data from appointments.json is saved like: 2026-06-29 11:30
        // LocalDateTime.parse() only accepts: 2026-06-29T11:30
        // So we support both formats here.
        DateTimeFormatter[] formats = new DateTimeFormatter[]{
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")
        };

        for (DateTimeFormatter formatter : formats) {
            try {
                return LocalDateTime.parse(value, formatter);
            } catch (Exception ignored) {
                // Try next format
            }
        }

        return null;
    }
}
