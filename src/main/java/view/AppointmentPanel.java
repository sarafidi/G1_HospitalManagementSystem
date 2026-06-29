package view;

import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalTime;
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
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import controller.AppointmentController;
import exception.DuplicateSlotException;
import model.*;
import util.IDGenerator;

import static util.UIConfig.*;

public class AppointmentPanel extends JPanel {

    private final AppointmentController controller = new AppointmentController();
    private String isDoctorLoggedIn;

    // UI Components - Form Fields
    private final JComboBox<String> txtPatientId;
    private final JComboBox<String> txtDoctorId;
    private final JComboBox<String> comboTime;
    private final JTextField txtDate;
    private final JTextArea txtNotes;
    private final JButton btnBook;
    private final JButton btnCancel;
    private final JButton btnUpdate;

    // UI Components - Table
    private JTable table;
    private DefaultTableModel tableModel;

    // Filter Components
    private final JComboBox<String> filterDoctorCombo;
    private final JButton btnFilter;
    private final JButton btnClearFilter;

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
            // Populate with all patients
            for (String pId : controller.getActivePatientIds()) {
                txtPatientId.addItem(pId);
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

            JPanel buttonGroupPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

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
                btnUpdate.addActionListener(_ -> {
                    int selectedRow = table.getSelectedRow();
                    if (selectedRow == -1) {
                        JOptionPane.showMessageDialog(this, "Please select an appointment from the table to update.", "No Selection", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    int modelRow = table.convertRowIndexToModel(selectedRow);
                    String apptId = tableModel.getValueAt(modelRow, 0).toString();
                    String currentStatus = tableModel.getValueAt(modelRow, 4).toString();

                    // Check if the selected appointment is already canceled
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

                // Cancel Button Logic: Mark Appointment as Canceled with Reason
                btnCancel.addActionListener(_ -> {
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
                    if (status.equalsIgnoreCase(AppStatus.SCHEDULED.toString())) {
                        STATUS_SCHEDULED(c);
                    } else if (status.equalsIgnoreCase(AppStatus.COMPLETED.toString())) {
                        STATUS_COMPLETED(c);
                    } else if (status.equalsIgnoreCase(AppStatus.CANCELLED.toString())) {
                        STATUS_CANCELLED(c);
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
            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent evt) {
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

                        for (MouseListener ml : txtPatientId.getMouseListeners()) { txtPatientId.removeMouseListener(ml); }
                        for (MouseListener ml : txtDoctorId.getMouseListeners()) { txtDoctorId.removeMouseListener(ml); }
                        for (MouseListener ml : comboTime.getMouseListeners()) { comboTime.removeMouseListener(ml); }

                        MouseAdapter mouseShield = new MouseAdapter() {
                            @Override public void mousePressed(MouseEvent e) { e.consume(); }
                            @Override public void mouseClicked(MouseEvent e) { e.consume(); }
                        };

                        // Non-editable fields - Patient ID
                        txtPatientId.setEditable(false);
                        txtPatientId.addMouseListener(mouseShield);
                        for (Component comp : txtPatientId.getComponents()) {
                            comp.setEnabled(false); 
                            comp.addMouseListener(mouseShield); 
                        }
                        txtPatientId.getEditor().getEditorComponent().setForeground(Color.BLACK);

                        // Non-editable fields - Doctor ID
                        txtDoctorId.setEditable(false);
                        txtDoctorId.addMouseListener(mouseShield);
                        for (Component comp : txtDoctorId.getComponents()) {
                            comp.setEnabled(false);
                            comp.addMouseListener(mouseShield);
                        }
                        txtDoctorId.getEditor().getEditorComponent().setForeground(Color.BLACK);

                        // Non-editable fields - Time Slot
                        comboTime.addMouseListener(mouseShield);
                        for (Component comp : comboTime.getComponents()) {
                            comp.setEnabled(false);
                            comp.addMouseListener(mouseShield);
                        }
                        comboTime.setRenderer(new javax.swing.DefaultListCellRenderer() {
                            @Override
                            public Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                                Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                                c.setForeground(Color.BLACK);
                                c.setBackground(Color.LIGHT_GRAY);
                                return c;
                            }
                        });
                        comboTime.setBackground(Color.LIGHT_GRAY);
                    }
                }
            });

        // Filter Panel
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        Border innerTitle = BorderFactory.createTitledBorder("Filter Appointments");
        Border outerMargin = BorderFactory.createEmptyBorder(5, 0, 15, 0);
        filterPanel.setBorder(BorderFactory.createCompoundBorder(outerMargin, innerTitle));

        filterDoctorCombo = new JComboBox<>();
        filterDoctorCombo.setPreferredSize(new Dimension(150, 25));
        btnFilter = new JButton("Filter");
        btnClearFilter = new JButton("Clear Filter");
        
        filterPanel.add(new JLabel("Doctor ID:"));
        filterPanel.add(filterDoctorCombo);
        filterPanel.add(btnFilter);
        filterPanel.add(btnClearFilter);
        
        btnFilter.addActionListener(_ -> loadTableData());
        btnClearFilter.addActionListener(_ -> {
            filterDoctorCombo.setSelectedItem("");
            loadTableData();
        });

        JPanel centerContainer = new JPanel(new BorderLayout());
        centerContainer.add(filterPanel, BorderLayout.NORTH);
        centerContainer.add(new JScrollPane(table), BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);
        
        loadTableData();
        btnBook.addActionListener(_ -> handleBookAppointment());
    }

    // Handle Booking Appointment
    private void handleBookAppointment() {
        String pId = (txtPatientId.getSelectedItem() != null) ? txtPatientId.getSelectedItem().toString().trim().toUpperCase() : "";
        String dId;
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
            String apptId = IDGenerator.generateAppointmentId();
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



    // Clear and Reset Form Fields
    private void clearAndResetForm() {
        for (MouseListener ml : txtPatientId.getMouseListeners()) { txtPatientId.removeMouseListener(ml); }
        for (MouseListener ml : txtDoctorId.getMouseListeners()) { txtDoctorId.removeMouseListener(ml); }
        for (MouseListener ml : comboTime.getMouseListeners()) { comboTime.removeMouseListener(ml); }

        // Date
        txtDate.setEditable(true);
        txtDate.setBackground(Color.WHITE);
        txtDate.setForeground(Color.BLACK);

        // Patient ID
        txtPatientId.setEditable(true);
        for (Component comp : txtPatientId.getComponents()) {
            comp.setEnabled(true);
            for (MouseListener ml : comp.getMouseListeners()) { comp.removeMouseListener(ml); }
        }
        txtPatientId.getEditor().getEditorComponent().setBackground(Color.WHITE);

        // Doctor ID
        for (Component comp : txtDoctorId.getComponents()) {
            for (MouseListener ml : comp.getMouseListeners()) { comp.removeMouseListener(ml); }
        }
        txtDoctorId.getEditor().getEditorComponent().setBackground(Color.WHITE);
        configureDoctorSelection();
        if (isDoctorLoggedIn == null) {
            txtDoctorId.setSelectedItem("");
        }

        // Time Slot
        for (Component comp : comboTime.getComponents()) {
            comp.setEnabled(true);
            for (MouseListener ml : comp.getMouseListeners()) { comp.removeMouseListener(ml); }
        }
        comboTime.setBackground(Color.WHITE);
        comboTime.setRenderer(new javax.swing.DefaultListCellRenderer());
        
        // Set default values
        txtPatientId.setSelectedItem("");
        txtDoctorId.setSelectedItem("");
        filterDoctorCombo.setSelectedItem("");
        txtNotes.setText("");
        txtDate.setText(LocalDate.now().toString());
        comboTime.setSelectedIndex(0);
    }

    // Refresh Panel Data
    public void refreshPanel() {
        isDoctorLoggedIn = controller.isDoctor();
        clearForm();
        loadTableData();
        setFormEnabled();
        configureDoctorSelection();
    }

    private void configureDoctorSelection() {
        if (isDoctorLoggedIn != null) {
            txtDoctorId.removeAllItems();
            txtDoctorId.addItem(isDoctorLoggedIn);
            txtDoctorId.setSelectedItem(isDoctorLoggedIn);
            txtDoctorId.setEnabled(false);

            // Lock filter for doctor
            filterDoctorCombo.removeAllItems();
            filterDoctorCombo.addItem(isDoctorLoggedIn);
            filterDoctorCombo.setSelectedItem(isDoctorLoggedIn);
            filterDoctorCombo.setEnabled(false);
            btnFilter.setEnabled(false);
            btnClearFilter.setEnabled(false);
        } else {
            txtDoctorId.setEnabled(true);
            
            // Enable filter for Admin/Receptionist
            filterDoctorCombo.setEnabled(true);
            btnFilter.setEnabled(true);
            btnClearFilter.setEnabled(true);
            
            // Populate with all active doctors
            txtDoctorId.removeAllItems();
            
            filterDoctorCombo.removeAllItems();
            filterDoctorCombo.addItem(""); // add blank for "All"

            for (String docId : controller.getActiveDoctorIds()) {
                txtDoctorId.addItem(docId);
                filterDoctorCombo.addItem(docId);
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
        txtDate.setText(LocalDate.now().toString());
        comboTime.setSelectedIndex(0);
    }

    private void setFormEnabled() {
        txtDate.setEditable(true);
        txtNotes.setEditable(true);
        
        txtPatientId.setEnabled(true);
        txtDoctorId.setEnabled(true);
        
        btnBook.setEnabled(true);
        btnCancel.setEnabled(true);
        btnUpdate.setEnabled(true);

        Color bg = Color.WHITE;
        txtPatientId.setBackground(bg);
        txtDoctorId.setBackground(bg);
        txtDate.setBackground(bg);
        txtNotes.setBackground(bg);
        
        txtPatientId.setForeground(Color.BLACK);
        txtDoctorId.setForeground(Color.BLACK);
        txtDate.setForeground(Color.BLACK);
        txtNotes.setForeground(Color.BLACK);
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        List<Appointment> list = controller.getAllAppointments();

        String activeFilterDoc = null;
        if (isDoctorLoggedIn != null) {
            activeFilterDoc = isDoctorLoggedIn;
        } else if (filterDoctorCombo != null) {
            String selected = (String) filterDoctorCombo.getSelectedItem();
            if (selected != null && !selected.trim().isEmpty()) {
                activeFilterDoc = selected;
            }
        }

        if (activeFilterDoc != null) {
            String finalDocId = activeFilterDoc;
            list = list.stream()
                    .filter(a -> a.getDoctorId().equalsIgnoreCase(finalDocId))
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
    @SuppressWarnings("unused")
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