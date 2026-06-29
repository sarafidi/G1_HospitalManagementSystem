package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import controller.AppointmentController;
import exception.DuplicateSlotException;
import model.*;
import util.DataStore;
import util.SessionManager;
import util.UIConfig;

import static util.UIConfig.*;

public class AppointmentPanel extends JPanel {

    private final AppointmentController controller = new AppointmentController();
    private DataStore dataStore = DataStore.getInstance();
    private String isDoctorLoggedIn;

    // UI Components - Form Fields
    private JComboBox<String> txtPatientId;
    private JComboBox<String> txtDoctorId;
    private JComboBox<String> comboTime;
    private JTextField txtDate;
    private JTextArea txtNotes;
    private JButton btnBook;
    private JButton btnCancel;
    private JButton btnUpdate;

    // UI Components - Table
    private JTable table;
    private DefaultTableModel tableModel;

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel lblTitle = new JLabel("Appointment Booking Management", JLabel.CENTER);
        lblTitle.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Input Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Book New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

            // Patient ID & Doctor ID
            gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
            formPanel.add(new JLabel("Patient ID:"), gbc);
            gbc.gridx = 1;
            txtPatientId = new JComboBox<>();
            txtPatientId.setEditable(true);
            // Populate with all active doctors
            for (Patient p : dataStore.getPatients()) {
                txtPatientId.addItem(p.getPatientId());
            }
            formPanel.add(txtPatientId, gbc);

            gbc.gridx = 2;
            formPanel.add(new JLabel("Doctor ID:"), gbc);
            gbc.gridx = 3;
            txtDoctorId = new JComboBox<>();
            if (isDoctorLoggedIn != null) {
                txtDoctorId.addItem(isDoctorLoggedIn);
                txtDoctorId.setEnabled(false);
            } else {
                txtDoctorId.setEnabled(true);
                txtDoctorId.addItem("");
            }
            formPanel.add(txtDoctorId, gbc);

            //  Date & Time Slot
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Date:"), gbc);
            gbc.gridx = 1;
            txtDate = new JTextField(LocalDate.now().toString(), 15);
            formPanel.add(txtDate, gbc);

            gbc.gridx = 2;
            formPanel.add(new JLabel("Time Slot:"), gbc);
            gbc.gridx = 3;
            String[] timeSlots = {
                "09:00", "09:30", "10:00", "10:30", "11:00", "11:30", "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
            };
            comboTime = new JComboBox<>(timeSlots);
            formPanel.add(comboTime, gbc);

            // Notes
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Notes:"), gbc);
            gbc.gridx = 1; gbc.gridwidth = 3;
            txtNotes = new JTextArea(3, 15);
            txtNotes.setLineWrap(true);
            formPanel.add(new JScrollPane(txtNotes), gbc);

            // Buttons Panel
            gbc.gridy = 3; gbc.gridwidth = 1;
            gbc.fill = GridBagConstraints.NONE;
            gbc.anchor = GridBagConstraints.LINE_END;

            JPanel buttonGroupPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 5, 0));

                // Book Button
                btnBook = new JButton("Book");
                btnBook.setBackground(new Color(46, 139, 87)); 
                btnBook.setForeground(Color.WHITE);
                buttonGroupPanel.add(btnBook);

                // Update Button
                btnUpdate = new JButton("Update");
                btnUpdate.setBackground(new Color(30, 144, 255));
                btnUpdate.setForeground(Color.WHITE);
                buttonGroupPanel.add(btnUpdate);

                // 3. Cancel Button
                btnCancel = new JButton("Cancel");
                btnCancel.setBackground(new Color(205, 92, 92)); 
                btnCancel.setForeground(Color.WHITE);
                buttonGroupPanel.add(btnCancel);

            gbc.gridx = 1; gbc.gridwidth = 3;
            formPanel.add(buttonGroupPanel, gbc);

                // Update Button Logic: Replace Existing Appointment Notes
                btnUpdate.addActionListener(e -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(this, "Please select an appointment from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    String apptId = tableModel.getValueAt(modelRow, 0).toString();
                    String currentStatus = tableModel.getValueAt(modelRow, 4).toString();

                    // Check if the selected appointment is already cancelled
                    if (currentStatus.equals("CANCELLED")) {
                        JOptionPane.showMessageDialog(this, "Cancelled appointments cannot be updated!", "Update Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String newNotes = txtNotes.getText().trim();

                    List<Appointment> list = controller.getAllAppointments();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getAppointmentId().equals(apptId)) {
                            Appointment original = list.get(i);
                            Appointment updated = new Appointment(
                                original.getAppointmentId(), 
                                original.getPatientId(), 
                                original.getDoctorId(), 
                                original.getAppointmentDateTime(), 
                                original.getStatus(), 
                                newNotes
                            );
                            
                            list.set(i, updated);
                            break;
                        }
                    }

                    // Save the updated list
                    loadTableData();
                    clearAndResetForm();
                    JOptionPane.showMessageDialog(this, "Appointment notes for " + apptId + " have been updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                });

                // Cancel Button Logic: Mark Appointment as Cancelled with Reason
                btnCancel.addActionListener(e -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(this, "Please select an appointment to cancel.", "No Selection", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    String apptId = tableModel.getValueAt(modelRow, 0).toString();
                    String currentStatus = tableModel.getValueAt(modelRow, 4).toString();

                    if (currentStatus.equals("CANCELLED")) {
                        JOptionPane.showMessageDialog(this, "This appointment is already cancelled!", "Info", JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }

                    String reason = JOptionPane.showInputDialog(this, "Enter cancellation reason for appointment " + apptId + ":", "Cancel Reason", JOptionPane.QUESTION_MESSAGE);
                    if (reason == null) return; 
                    if (reason.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Cancellation reason is required.", "Input Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    List<Appointment> list = controller.getAllAppointments();
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i).getAppointmentId().equals(apptId)) {
                            Appointment original = list.get(i);
                            String updatedNotes = original.getNotes() + " [CANCELLED REASON: " + reason.trim() + "]";
                            
                            Appointment cancelledAppt = new Appointment(
                                original.getAppointmentId(), 
                                original.getPatientId(), 
                                original.getDoctorId(), 
                                original.getAppointmentDateTime(), 
                                AppStatus.CANCELLED, 
                                updatedNotes
                            );

                            list.set(i, cancelledAppt);
                            controller.updateStatus(apptId, AppStatus.CANCELLED);
                            break;
                        }
                    }

                    loadTableData();
                    clearAndResetForm();

                    JOptionPane.showMessageDialog(this, "Appointment " + apptId + " has been cancelled successfully!", "Cancelled", JOptionPane.INFORMATION_MESSAGE);
                });
        
            // Add the form panel to the main panel
            add(formPanel, BorderLayout.NORTH);
        
            String[] columns = {"Appointment ID", "Patient ID", "Doctor ID", "Date & Time", "Status", "Notes"};
            tableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); 

        // 1. RENDERER KHAS UNTUK STATUS (Column Index 4 - Center + Warna)
        DefaultTableCellRenderer statusRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(SwingConstants.CENTER); // Status wajib center
                
                if (value != null) { 
                    String status = value.toString();
                    if (status.equalsIgnoreCase("SCHEDULED")) {
                        c.setBackground(new Color(255, 239, 150)); c.setForeground(Color.BLACK);
                    } else if (status.equalsIgnoreCase("COMPLETED")) {
                        c.setBackground(new Color(175, 238, 175)); c.setForeground(Color.BLACK);
                    } else if (status.equalsIgnoreCase("CANCELLED")) {
                        c.setBackground(new Color(255, 182, 193)); c.setForeground(Color.BLACK);
                    }
                }
                return c;
            }
        };

        DefaultTableCellRenderer leftRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                setHorizontalAlignment(SwingConstants.LEFT); 
                
                if (isSelected) {
                    c.setBackground(table.getSelectionBackground()); c.setForeground(table.getSelectionForeground());
                } else {
                    c.setBackground(table.getBackground()); c.setForeground(table.getForeground());
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            if (i == 4) {
                table.getColumnModel().getColumn(i).setCellRenderer(statusRenderer); 
            } else {
                table.getColumnModel().getColumn(i).setCellRenderer(leftRenderer); 
            }
        }

            // Set column widths
            table.getColumnModel().getColumn(0).setPreferredWidth(90); 
            table.getColumnModel().getColumn(1).setPreferredWidth(90);  
            table.getColumnModel().getColumn(2).setPreferredWidth(90);  
            table.getColumnModel().getColumn(3).setPreferredWidth(140); 
            table.getColumnModel().getColumn(4).setPreferredWidth(100); 
            table.getColumnModel().getColumn(5).setPreferredWidth(300);

            // Table Row Click Logic: Populate Form Fields for Update
            table.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow != -1) {
                        int modelRow = table.convertRowIndexToModel(selectedRow);
                        
                        String pId = tableModel.getValueAt(modelRow, 1).toString();
                        String dId = tableModel.getValueAt(modelRow, 2).toString();
                        String dateTimeStr = tableModel.getValueAt(modelRow, 3).toString();
                        String notes = tableModel.getValueAt(modelRow, 5).toString();

                        txtPatientId.setSelectedItem(pId);
                        txtDoctorId.setSelectedItem(dId);
                        txtNotes.setText(notes);

                        if (dateTimeStr.contains(" ")) {
                            String[] parts = dateTimeStr.split(" ");
                            txtDate.setText(parts[0]);
                            comboTime.setSelectedItem(parts[1]);
                        }
                                           
                        // Non-editable fields - Date
                        txtDate.setEditable(false);
//                        txtDate.setBackground(Color.LIGHT_GRAY);
                        txtDate.setForeground(Color.BLACK);

                        for (java.awt.event.MouseListener ml : txtPatientId.getMouseListeners()) { txtPatientId.removeMouseListener(ml); }
                        for (java.awt.event.MouseListener ml : txtDoctorId.getMouseListeners()) { txtDoctorId.removeMouseListener(ml); }
                        for (java.awt.event.MouseListener ml : comboTime.getMouseListeners()) { comboTime.removeMouseListener(ml); }

                        java.awt.event.MouseAdapter mouseShield = new java.awt.event.MouseAdapter() {
                            @Override public void mousePressed(java.awt.event.MouseEvent e) { e.consume(); }
                            @Override public void mouseClicked(java.awt.event.MouseEvent e) { e.consume(); }
                        };

                        // Non-editable fields - Patient ID
                        txtPatientId.setEditable(false);
                        txtPatientId.addMouseListener(mouseShield);
                        for (java.awt.Component comp : txtPatientId.getComponents()) {
                            comp.setEnabled(false); 
                            comp.addMouseListener(mouseShield); 
                        }
//                        txtPatientId.getEditor().getEditorComponent().setBackground(Color.LIGHT_GRAY);
                        txtPatientId.getEditor().getEditorComponent().setForeground(Color.BLACK);

                        // Non-editable fields - Doctor ID
                        txtDoctorId.setEditable(false);
                        txtDoctorId.addMouseListener(mouseShield);
                        for (java.awt.Component comp : txtDoctorId.getComponents()) {
                            comp.setEnabled(false);
                            comp.addMouseListener(mouseShield);
                        }
//                        txtDoctorId.getEditor().getEditorComponent().setBackground(Color.LIGHT_GRAY);
                        txtDoctorId.getEditor().getEditorComponent().setForeground(Color.BLACK);

                        // Non-editable fields - Time Slot
                        comboTime.addMouseListener(mouseShield);
                        for (java.awt.Component comp : comboTime.getComponents()) {
                            comp.setEnabled(false);
                            comp.addMouseListener(mouseShield);
                        }
                        comboTime.setRenderer(new javax.swing.DefaultListCellRenderer() {
                            @Override
                            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                java.awt.Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                c.setForeground(Color.BLACK);
                                c.setBackground(Color.LIGHT_GRAY);
                                return c;
                            }
                        });
                        comboTime.setBackground(Color.LIGHT_GRAY);
                    }
                }
            });

        add(new JScrollPane(table), BorderLayout.CENTER);
        
        loadTableData();
        btnBook.addActionListener(e -> handleBookAppointment());
    }

    // Handle Booking Appointment
    private void handleBookAppointment() {
        String pId = (txtPatientId.getSelectedItem() != null) ? txtPatientId.getSelectedItem().toString().trim().toUpperCase() : "";
        String dId = "";
        String dateStr = txtDate.getText().trim();
        String timeStr = comboTime.getSelectedItem().toString();
        String notes = txtNotes.getText().trim();

        if (isDoctorLoggedIn != null) {
            dId = txtDoctorId.getSelectedItem().toString();
        } else {
            dId = (txtDoctorId.getSelectedItem() != null) ? txtDoctorId.getSelectedItem().toString().trim().toUpperCase() : "";
        }

        // Validation
        if (pId.isEmpty() || dId.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!pId.matches("^PAT-\\d{4}$")) {
            JOptionPane.showMessageDialog(this, "Format for Patient ID is incorrect! Must follow the format PAT-XXXX.", "Format Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

//        if (!dId.matches("^DOC-\\d{4}$")) {
//            JOptionPane.showMessageDialog(this, "Format for Doctor ID is incorrect! Must follow the format DOC-XXXX.", "Format Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        try {
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);
            String dateTimeStr = date.toString() + " " + time.toString();

            List<Appointment> existingAppointments = controller.getAllAppointments();
            for (Appointment appt : existingAppointments) {

                // Check if the appointment is scheduled and if the patient has another appointment at the same time
                if (appt.getStatus() == AppStatus.SCHEDULED) {
                    if (appt.getAppointmentDateTime().equals(dateTimeStr) && appt.getPatientId().equalsIgnoreCase(pId)) {
                        JOptionPane.showMessageDialog(this, "Duplicate Error: Patient " + pId + " has another appointment at the same time (" + dateTimeStr + ") with " + appt.getDoctorId() + "!", "Patient Time Conflict", 
                            JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // Generate a unique appointment ID
            String apptId = "APT-" + String.format("%04d", (System.currentTimeMillis() % 10000)); 
            Appointment newAppt = new Appointment(apptId, pId, dId, dateTimeStr, AppStatus.SCHEDULED, notes);
            controller.bookAppointment(newAppt);

            loadTableData();
            
            // Clear form fields after successful booking
            txtPatientId.setSelectedItem("");
            txtDoctorId.setSelectedItem("");
            txtNotes.setText("");
            
            JOptionPane.showMessageDialog(this, "Appointment booked successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (DuplicateSlotException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Duplicate Slot", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Debugging", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    // Load Data into Table
    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Appointment> list = controller.getAllAppointments();

        list.sort((appt1, appt2) -> appt1.getAppointmentDateTime().compareTo(appt2.getAppointmentDateTime()));
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

    // Clear and Reset Form Fields
    private void clearAndResetForm() {
        for (java.awt.event.MouseListener ml : txtPatientId.getMouseListeners()) { txtPatientId.removeMouseListener(ml); }
        for (java.awt.event.MouseListener ml : txtDoctorId.getMouseListeners()) { txtDoctorId.removeMouseListener(ml); }
        for (java.awt.event.MouseListener ml : comboTime.getMouseListeners()) { comboTime.removeMouseListener(ml); }

        // Date
        txtDate.setEditable(true);
        txtDate.setBackground(Color.WHITE);
        txtDate.setForeground(Color.BLACK);

        // Patient ID
        txtPatientId.setEditable(true);
        for (java.awt.Component comp : txtPatientId.getComponents()) {
            comp.setEnabled(true);
            for (java.awt.event.MouseListener ml : comp.getMouseListeners()) { comp.removeMouseListener(ml); }
        }
        txtPatientId.getEditor().getEditorComponent().setBackground(Color.WHITE);

        // Doctor ID
        for (java.awt.Component comp : txtDoctorId.getComponents()) {
            for (java.awt.event.MouseListener ml : comp.getMouseListeners()) { comp.removeMouseListener(ml); }
        }
        txtDoctorId.getEditor().getEditorComponent().setBackground(Color.WHITE);
        configureDoctorSelection();
        if (isDoctorLoggedIn == null) {
            txtDoctorId.setSelectedItem("");
        }

        // Time Slot
        for (java.awt.Component comp : comboTime.getComponents()) {
            comp.setEnabled(true);
            for (java.awt.event.MouseListener ml : comp.getMouseListeners()) { comp.removeMouseListener(ml); }
        }
        comboTime.setBackground(Color.WHITE);
        comboTime.setRenderer(new javax.swing.DefaultListCellRenderer());
        
        // Set default values
        txtPatientId.setSelectedItem("");
        txtDoctorId.setSelectedItem("");
        txtNotes.setText("");
        txtDate.setText(java.time.LocalDate.now().toString());
        comboTime.setSelectedIndex(0);
    }

    // Refresh Panel Data
    public void refreshPanel() {
        isDoctorLoggedIn = controller.isDoctor();
        clearForm();                 
        loadTableData();
        loadAppointmentsData();    
        setFormEnabled(true);
        configureDoctorSelection();
    }

    private void configureDoctorSelection() {
        if (isDoctorLoggedIn != null) {
            txtDoctorId.removeAllItems();
            txtDoctorId.addItem(isDoctorLoggedIn);
            txtDoctorId.setSelectedItem(isDoctorLoggedIn);
            txtDoctorId.setEnabled(false);
        } else {
            txtDoctorId.setEnabled(true);
//            txtDoctorId.setEditable(true);
            
            // Populate with all active doctors
            txtDoctorId.removeAllItems();
//            txtDoctorId.addItem("");
            for (User u : dataStore.getUsers()) {
                if (u.getRole() == Role.DOCTOR && u.isActive()) {
//                    String dId = u.getDoctorId();
//                    if (dId != null && !dId.trim().isEmpty() && !dId.equals("—")) {
                        txtDoctorId.addItem(u.getDoctorId());
//                    }
                }
            }
        }
    }

    private void clearForm() {
        txtPatientId.setSelectedItem("");
        if (isDoctorLoggedIn != null) {
            txtDoctorId.setSelectedItem(isDoctorLoggedIn);
        } else {
            txtDoctorId.setSelectedItem("");
        }
        txtNotes.setText("");
        txtDate.setText(java.time.LocalDate.now().toString());
        comboTime.setSelectedIndex(0);
    }

    private void setFormEnabled(boolean enabled) {
//        txtPatientId.setEditable(enabled);
//        txtDoctorId.setEditable(enabled);
        txtDate.setEditable(enabled);
        txtNotes.setEditable(enabled);
        
        txtPatientId.setEnabled(enabled);
        txtDoctorId.setEnabled(enabled);
        
        btnBook.setEnabled(enabled);
        btnCancel.setEnabled(enabled);
        btnUpdate.setEnabled(enabled);

        Color bg = enabled ? Color.WHITE : Color.WHITE;
        txtPatientId.setBackground(bg);
        txtDoctorId.setBackground(bg);
        txtDate.setBackground(bg);
        txtNotes.setBackground(bg);
        
        txtPatientId.setForeground(Color.BLACK);
        txtDoctorId.setForeground(Color.BLACK);
        txtDate.setForeground(Color.BLACK);
        txtNotes.setForeground(Color.BLACK);
    }

    private void loadAppointmentsData() {
        tableModel.setRowCount(0);
        List<Appointment> list = controller.getAllAppointments();

        if (isDoctorLoggedIn != null) {
            list = list.stream()
                    .filter(a -> a.getDoctorId().equalsIgnoreCase(isDoctorLoggedIn))
                    .collect(Collectors.toList());

        }

        list.sort((a1, a2) -> a1.getAppointmentId().compareTo(a2.getAppointmentId()));

        for (Appointment appt : list) {
            Object[] row = {
                appt.getAppointmentId(),
                appt.getPatientId(),
                appt.getDoctorId(),
                appt.getAppointmentDateTime(),
                appt.getStatus().toString(),
                appt.getNotes()
            };
            tableModel.addRow(row);
        }
    }

    // ID Suggestions
    private void setupIdSuggestions() {
        String[] samplePatients = {"PAT-0001", "PAT-0042", "PAT-0123", "PAT-0999"};
        for (String pat : samplePatients) {
            txtPatientId.addItem(pat);
        }

        String[] sampleDoctors = {"DOC-0001", "DOC-0005", "DOC-0024", "DOC-0888"};
        for (String doc : sampleDoctors) {
            txtDoctorId.addItem(doc);
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refreshPanel();
        }
    }
}