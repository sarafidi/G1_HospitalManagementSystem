package controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import exception.DuplicateSlotException;
import model.AppStatus;
import model.Appointment;
import util.DataStore;

public class AppointmentController {
    
    private final DataStore dataStore = DataStore.getInstance();

    // Book Appointment
    public void bookAppointment(Appointment appt) throws DuplicateSlotException {
        if (isDuplicate(appt.getDoctorId(), appt.getAppointmentDateTime())) {
            throw new DuplicateSlotException("This doctor already had an appointment during that time. Please choose another time slot.");
        }

        dataStore.getAppointments().add(appt);
        dataStore.saveAppointments();
        System.out.println("Appointment " + appt.getAppointmentId() + " successfully saved to JSON file.");
    }

    // Duplicate Check
    public boolean isDuplicate(String doctorId, LocalDateTime dateTime) {
        for (Appointment appt : dataStore.getAppointments()) {
            if (appt.getDoctorId().equalsIgnoreCase(doctorId) && 
                appt.getAppointmentDateTime().equals(dateTime) && 
                appt.getStatus() != AppStatus.CANCELLED) {
                return true;
            }
        }
        return false;
    }

    // Display All Appointments
    public List<Appointment> getAllAppointments() {
        return dataStore.getAppointments();
    }

    // Display Appointments by Doctor
    public List<Appointment> getByDoctor(String doctorId) {
        List<Appointment> filteredList = new ArrayList<>();
        for (Appointment appt : dataStore.getAppointments()) {
            if (appt.getDoctorId().equalsIgnoreCase(doctorId)) {
                filteredList.add(appt);
            }
        }
        return filteredList;
    }

    // Update Status
    public void updateStatus(String appointmentId, AppStatus newStatus) {
        for (Appointment appt : dataStore.getAppointments()) {
            if (appt.getAppointmentId().equalsIgnoreCase(appointmentId)) {
                appt.setStatus(newStatus);
                dataStore.saveAppointments();
                System.out.println("Status appointment " + appointmentId + " successfully changed to " + newStatus);
                return;
            }
        }
    }
}