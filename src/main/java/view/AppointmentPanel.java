package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import controller.AppointmentController;
import exception.DuplicateSlotException;
import model.AppStatus;
import model.Appointment;

public class AppointmentPanel extends JPanel {

    private final AppointmentController controller = new AppointmentController();

    // UI Components - Form Fields
    private JTextField txtPatientId;
    private JTextField txtDoctorId;
    private JComboBox<String> comboTime;
    private JTextField txtDate;
    private JTextArea txtNotes;
    private JButton btnBook;

    // UI Components - Table
    private JTable table;
    private DefaultTableModel tableModel;

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel lblTitle = new JLabel("Appointment Booking Management", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Input Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Book New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

            // Input Patient ID
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Patient ID:"), gbc);
            gbc.gridx = 1;
            txtPatientId = new JTextField(15);
            formPanel.add(txtPatientId, gbc);

            // Input Doctor ID
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Doctor ID:"), gbc);
            gbc.gridx = 1;
            txtDoctorId = new JTextField(15);
            formPanel.add(txtDoctorId, gbc);

            // Input Date
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
            gbc.gridx = 1;
            txtDate = new JTextField(LocalDate.now().toString(), 15);
            formPanel.add(txtDate, gbc);

            // Input Time Slot
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Time Slot:"), gbc);
            gbc.gridx = 1;
            String[] timeSlots = {
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
            };
            comboTime = new JComboBox<>(timeSlots);
            formPanel.add(comboTime, gbc);

            // Input Notes
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1;
            txtNotes = new JTextArea(3, 15);
            txtNotes.setLineWrap(true);
            formPanel.add(new JScrollPane(txtNotes), gbc);

            // Book Appointment Button
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            btnBook = new JButton("Book Appointment");
            btnBook.setBackground(new Color(231, 84, 128));
            btnBook.setForeground(Color.WHITE);
            formPanel.add(btnBook, gbc);

            add(formPanel, BorderLayout.WEST);

        // Table to Display Appointments
        String[] columns = {"Appt ID", "Patient ID", "Doctor ID", "Date & Time", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        loadTableData();
        btnBook.addActionListener(e -> handleBookAppointment());
    }

    // Load Data into Table
    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Appointment> list = controller.getAllAppointments();
        for (Appointment appt : list) {
            Object[] row = {
                appt.getAppointmentId(),
                appt.getPatientId(),
                appt.getDoctorId(),
                appt.getAppointmentDateTime(),
                appt.getStatus(),
                appt.getNotes()
            };
            tableModel.addRow(row);
        }
    }

    // Handle Booking Appointment
    private void handleBookAppointment() {
        String pId = txtPatientId.getText().trim();
        String dId = txtDoctorId.getText().trim();
        String dateStr = txtDate.getText().trim();
        String timeStr = comboTime.getSelectedItem().toString();
        String notes = txtNotes.getText().trim();

        // Validation
        if (pId.isEmpty() || dId.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Parse date and time into LocalDateTime
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);
            String dateTimeStr = date.toString() + " " + time.toString();

            // Generate a unique appointment ID
            String apptId = "APT-" + System.currentTimeMillis() % 10000; 
            Appointment newAppt = new Appointment(apptId, pId, dId, dateTimeStr, AppStatus.SCHEDULED, notes);
            controller.bookAppointment(newAppt);

            loadTableData();
            
            // Clear form fields after successful booking
            txtPatientId.setText("");
            txtDoctorId.setText("");
            txtNotes.setText("");
            
            JOptionPane.showMessageDialog(this, "Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (DuplicateSlotException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate Slot", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            // Ini untuk kita nampak error sebenar kat terminal bawah VS Code
            ex.printStackTrace(); 
            
            // Mesej popup sementara untuk tengok error
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Debugging", JOptionPane.ERROR_MESSAGE);
        }
    }
}