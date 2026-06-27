package view;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import controller.AppointmentController;
import controller.MedicalNoteController;
import model.AppStatus;
import model.Appointment;
import model.MedicalNote;

public class MedicalNotesPanel extends JPanel {
    private AppointmentController apptController;
    private MedicalNoteController notesController;
    private String currentDocId = "DOC-0002";

    // Components
    private JComboBox<String> comboApptId;
    private JTextField txtDocId, txtPatientId, txtDateTime;
    private JRadioButton radNoMc, radReqMc, radNoFollow, radReqFollow;
    private JCheckBox chkReferral, chkUrgent, chkLabTest;
    private JTextArea txtS, txtO, txtA, txtP;
    private JButton btnUpdate, btnSubmit;
    
    // Table for displaying appointments
    private JTable table;
    private DefaultTableModel tableModel;

    // TextArea placeholders
    private final String hintO = "  Measurable data (vital signs, physical exam findings, and lab results)";
    private final String hintA = "  Clinical impression, provisional diagnosis or differential diagnoses";
    private final String hintP = "  Prescribed medications, therapies, follow-up appointments, or lifestyle changes";

    public MedicalNotesPanel(AppointmentController apptController, MedicalNoteController notesController) {
        this.apptController = apptController;
        this.notesController = notesController;
        
        setLayout(new GridBagLayout()); 
        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.fill = GridBagConstraints.BOTH;
        mainGbc.weighty = 1.0;

        // Add medical notes form on the left and appointment table on the right
        JPanel leftPanel = new JPanel(new GridBagLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Add Medical Record"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 6, 8, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Appointment ID & Doctor ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1;
        leftPanel.add(new JLabel("Appointment ID:"), gbc);
        gbc.gridx = 1;
        comboApptId = new JComboBox<>();
        leftPanel.add(comboApptId, gbc);

        gbc.gridx = 2;
        leftPanel.add(new JLabel("Doctor ID:"), gbc);
        gbc.gridx = 3;
        txtDocId = new JTextField(10);
        leftPanel.add(txtDocId, gbc);

        // Patient ID & Date Time
        gbc.gridx = 0; gbc.gridy = 1;
        leftPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1;
        txtPatientId = new JTextField(10);
        leftPanel.add(txtPatientId, gbc);

        gbc.gridx = 2;
        leftPanel.add(new JLabel("Date & Time:"), gbc);
        gbc.gridx = 3;
        txtDateTime = new JTextField(12);
        leftPanel.add(txtDateTime, gbc);

        // Medical Leave & Follow-up
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        leftPanel.add(new JLabel("Medical Leave:"), gbc);
        gbc.gridx = 1;
        radNoMc = new JRadioButton("No MC", true);
        radReqMc = new JRadioButton("Require MC");
        ButtonGroup bgMc = new ButtonGroup(); bgMc.add(radNoMc); bgMc.add(radReqMc);
        JPanel pnlMc = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlMc.add(radNoMc); pnlMc.add(radReqMc);
        leftPanel.add(pnlMc, gbc);

        gbc.gridx = 2;
        leftPanel.add(new JLabel("Follow-up:"), gbc);
        gbc.gridx = 3;
        radNoFollow = new JRadioButton("No", true);
        radReqFollow = new JRadioButton("Needed");
        ButtonGroup bgFollow = new ButtonGroup(); bgFollow.add(radNoFollow); bgFollow.add(radReqFollow);
        JPanel pnlFollow = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        pnlFollow.add(radNoFollow); pnlFollow.add(radReqFollow);
        leftPanel.add(pnlFollow, gbc);

        // Categories
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST; 
        leftPanel.add(new JLabel("Categories:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.NONE;
        chkReferral = new JCheckBox("Referral Needed");
        chkUrgent = new JCheckBox("Urgent Case");
        chkLabTest = new JCheckBox("Lab Test Ordered");
        JPanel pnlChk = new JPanel();
        pnlChk.setLayout(new BoxLayout(pnlChk, BoxLayout.Y_AXIS));
        pnlChk.add(chkReferral); pnlChk.add(chkUrgent); pnlChk.add(chkLabTest);
        leftPanel.add(pnlChk, gbc);

        // Reset anchor & fill for text areas
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;

        // Subjective
        gbc.gridx = 0; gbc.gridy = 4;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Subjective:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtS = new JTextArea(3, 30); txtS.setLineWrap(true);
        leftPanel.add(new JScrollPane(txtS), gbc);

        // Objective
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Objective:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtO = new JTextArea(3, 30); txtO.setLineWrap(true);
        setupPlaceholder(txtO, hintO);
        leftPanel.add(new JScrollPane(txtO), gbc);

        // Assessment
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Assessment:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtA = new JTextArea(3, 30); txtA.setLineWrap(true);
        setupPlaceholder(txtA, hintA);
        leftPanel.add(new JScrollPane(txtA), gbc);

        // Plan
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST;
        leftPanel.add(new JLabel("Plan:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3;
        txtP = new JTextArea(3, 30); txtP.setLineWrap(true);
        setupPlaceholder(txtP, hintP);
        leftPanel.add(new JScrollPane(txtP), gbc);

        // Reset anchor & fill for buttons
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;

        // Buttons
        gbc.gridx = 1; gbc.gridy = 8; gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.LINE_END;
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        
        btnUpdate = new JButton("Update");
        btnUpdate.setBackground(new Color(30, 144, 255)); btnUpdate.setForeground(Color.WHITE);
        
        btnSubmit = new JButton("Submit");
        btnSubmit.setBackground(new Color(46, 139, 87)); btnSubmit.setForeground(Color.WHITE);
        
        btnPanel.add(btnUpdate); btnPanel.add(btnSubmit);
        leftPanel.add(btnPanel, gbc);

        mainGbc.gridx = 0; mainGbc.gridy = 0; mainGbc.weightx = 0.35;
        add(leftPanel, mainGbc);

        // Right panel: Appointment table
        String[] cols = {"Appointment ID", "Patient ID", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);

        DefaultTableCellRenderer customRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER); 
                
                if (column == 3 && value != null) { 
                    String status = value.toString();
                    if (status.equalsIgnoreCase("SCHEDULED")) {
                        c.setBackground(new Color(255, 239, 150)); c.setForeground(Color.BLACK);
                    } else if (status.equalsIgnoreCase("COMPLETED")) {
                        c.setBackground(new Color(175, 238, 175)); c.setForeground(Color.BLACK);
                    } else if (status.equalsIgnoreCase("CANCELLED")) {
                        c.setBackground(new Color(255, 182, 193)); c.setForeground(Color.BLACK);
                    }
                } else {
                    if (isSelected) {
                        c.setBackground(table.getSelectionBackground());
                        c.setForeground(table.getSelectionForeground());
                    } else {
                        c.setBackground(table.getBackground());
                        c.setForeground(table.getForeground());
                    }
                }
                return c;
            }
        };

        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(customRenderer);
        }

        mainGbc.gridx = 1; mainGbc.weightx = 0.65;
        add(new JScrollPane(table), mainGbc);

        // Listeners and Validation
        lockFixedFields();
        loadDoctorAppointments();

        comboApptId.addActionListener(e -> {
            String selectedId = (String) comboApptId.getSelectedItem();
            handleAppointmentSelection(selectedId);
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = table.getSelectedRow();
                if (row != -1) {
                    int modelRow = table.convertRowIndexToModel(row);
                    String apptId = tableModel.getValueAt(modelRow, 0).toString();
                    comboApptId.setSelectedItem(apptId);
                }
            }
        });

        // Submit Action
        btnSubmit.addActionListener(e -> {
            String apptId = (String) comboApptId.getSelectedItem();
            if (apptId == null || apptId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an Appointment ID.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!validateOAP()) return; 

            Appointment appt = apptController.getAllAppointments().stream()
                    .filter(a -> a.getAppointmentId().equals(apptId)).findFirst().orElse(null);
            
            if (appt != null && appt.getStatus() == AppStatus.COMPLETED) {
                JOptionPane.showMessageDialog(this, "This appointment is already COMPLETED. Please use the UPDATE button.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            notesController.submitMedicalNote(createNoteFromForm(apptId));
            if (appt != null) appt.setStatus(AppStatus.COMPLETED);

            JOptionPane.showMessageDialog(this, "Medical Note submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshPanel();
        });

        // Update Action
        btnUpdate.addActionListener(e -> {
            String apptId = (String) comboApptId.getSelectedItem();
            if (apptId == null || apptId.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please select an Appointment ID.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!validateOAP()) return; 

            Appointment appt = apptController.getAllAppointments().stream()
                    .filter(a -> a.getAppointmentId().equals(apptId)).findFirst().orElse(null);

            if (appt == null || appt.getStatus() != AppStatus.COMPLETED) {
                JOptionPane.showMessageDialog(this, "Only COMPLETED appointments can be updated!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            notesController.updateMedicalNote(createNoteFromForm(apptId));
            JOptionPane.showMessageDialog(this, "Medical note updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshPanel();
        });
    }

    private boolean validateOAP() {
        String oText = txtO.getText().trim();
        String aText = txtA.getText().trim();
        String pText = txtP.getText().trim();

        if (oText.isEmpty() || oText.equals(hintO.trim())) {
            JOptionPane.showMessageDialog(this, "Field 'Objective' is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (aText.isEmpty() || aText.equals(hintA.trim())) {
            JOptionPane.showMessageDialog(this, "Field 'Assessment' is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        if (pText.isEmpty() || pText.equals(hintP.trim())) {
            JOptionPane.showMessageDialog(this, "Field 'Plan' is required!", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    private void handleAppointmentSelection(String apptId) {
        if (apptId == null || apptId.isEmpty()) {
            setFormEnabled(true);
            return;
        }

        Appointment appt = apptController.getAllAppointments().stream()
                .filter(a -> a.getAppointmentId().equals(apptId)).findFirst().orElse(null);
        
        if (appt != null) {
            txtDocId.setText(appt.getDoctorId());
            txtPatientId.setText(appt.getPatientId());
            txtDateTime.setText(appt.getAppointmentDateTime());
            txtS.setText(appt.getNotes());

            txtDocId.setForeground(Color.BLACK);
            txtPatientId.setForeground(Color.BLACK);
            txtDateTime.setForeground(Color.BLACK);

            MedicalNote oldNote = notesController.getNoteByAppointmentId(apptId);
            if (oldNote != null) {
                txtO.setText(oldNote.getObjective()); txtO.setForeground(Color.BLACK); txtO.setFont(new Font("sansserif", Font.PLAIN, 12));
                txtA.setText(oldNote.getAssessment()); txtA.setForeground(Color.BLACK); txtA.setFont(new Font("sansserif", Font.PLAIN, 12));
                txtP.setText(oldNote.getPlan()); txtP.setForeground(Color.BLACK); txtP.setFont(new Font("sansserif", Font.PLAIN, 12));
                radReqMc.setSelected(oldNote.isRequireMc());
                radReqFollow.setSelected(oldNote.isFollowUpNeeded());
                chkReferral.setSelected(oldNote.isReferralNeeded());
                chkUrgent.setSelected(oldNote.isUrgentCase());
                chkLabTest.setSelected(oldNote.isLabTestOrdered());
            } else {
                if (appt.getStatus() == AppStatus.CANCELLED) {
                    txtO.setText("  - No Record -"); txtO.setForeground(Color.BLACK); txtO.setFont(new Font("sansserif", Font.PLAIN, 12));
                    txtA.setText("  - No Record -"); txtA.setForeground(Color.BLACK); txtA.setFont(new Font("sansserif", Font.PLAIN, 12));
                    txtP.setText("  - No Record -"); txtP.setForeground(Color.BLACK); txtP.setFont(new Font("sansserif", Font.PLAIN, 12));
                } else {
                    setupPlaceholder(txtO, hintO);
                    setupPlaceholder(txtA, hintA);
                    setupPlaceholder(txtP, hintP);
                }
            }
            
            if (appt.getStatus() == AppStatus.CANCELLED) {
                setFormEnabled(false);
                JOptionPane.showMessageDialog(this, "This appointment has been CANCELLED. You are only allowed to view.", "View Only", JOptionPane.WARNING_MESSAGE);
            } else {
                setFormEnabled(true);
                lockFixedFields(); 
            }
        }
    }

    private void setFormEnabled(boolean enabled) {
        txtO.setEditable(enabled); 
        txtA.setEditable(enabled); 
        txtP.setEditable(enabled);
        
        txtO.setForeground(Color.BLACK);
        txtA.setForeground(Color.BLACK);
        txtP.setForeground(Color.BLACK);

        radNoMc.setEnabled(enabled); radReqMc.setEnabled(enabled);
        radNoFollow.setEnabled(enabled); radReqFollow.setEnabled(enabled);
        chkReferral.setEnabled(enabled); chkUrgent.setEnabled(enabled); chkLabTest.setEnabled(enabled);
        btnSubmit.setEnabled(enabled); btnUpdate.setEnabled(enabled);
        
        Color bg = enabled ? Color.WHITE : Color.LIGHT_GRAY;
        txtO.setBackground(bg); txtA.setBackground(bg); txtP.setBackground(bg);
    }

    public void refreshPanel() {
    setFormEnabled(true);
    clearForm();             
    loadDoctorAppointments(); 
    lockFixedFields();
}

    private void lockFixedFields() {
        JTextField[] fields = {txtDocId, txtPatientId, txtDateTime};
        for (JTextField f : fields) {
            f.setEditable(false); 
            f.setBackground(Color.LIGHT_GRAY); 
            f.setForeground(Color.BLACK);
        }
        txtS.setEditable(false); txtS.setBackground(Color.LIGHT_GRAY); txtS.setForeground(Color.BLACK);
    }

    private void loadDoctorAppointments() {
        comboApptId.removeAllItems();
        comboApptId.addItem("");
        tableModel.setRowCount(0);

        List<Appointment> list = apptController.getAllAppointments();
        list.sort((a1, a2) -> a1.getAppointmentId().compareTo(a2.getAppointmentId()));

        for (Appointment appt : list) {
            if (appt.getDoctorId().equalsIgnoreCase(currentDocId)) {
                comboApptId.addItem(appt.getAppointmentId());
                Object[] row = {appt.getAppointmentId(), appt.getPatientId(), appt.getAppointmentDateTime(), appt.getStatus().toString()};
                tableModel.addRow(row);
            }
        }
    }

    private MedicalNote createNoteFromForm(String apptId) {
        return new MedicalNote(
            "NOTE-" + String.format("%04d", (System.currentTimeMillis() % 10000)),
            apptId, txtPatientId.getText(), txtDocId.getText(),
            txtS.getText(), txtO.getText(), txtA.getText(), txtP.getText(),
            radReqMc.isSelected(), radReqFollow.isSelected(),
            chkReferral.isSelected(), chkUrgent.isSelected(), chkLabTest.isSelected()
        );
    }

    private void clearForm() {
        comboApptId.setSelectedIndex(0);
        txtDocId.setText(""); txtPatientId.setText(""); txtDateTime.setText(""); txtS.setText("");
        txtO.setText(""); txtA.setText(""); txtP.setText("");
        radNoMc.setSelected(true); radNoFollow.setSelected(true);
        chkReferral.setSelected(false); chkUrgent.setSelected(false); chkLabTest.setSelected(false);
        setupPlaceholder(txtO, hintO); setupPlaceholder(txtA, hintA); setupPlaceholder(txtP, hintP);
    }

    private void setupPlaceholder(JTextArea textArea, String hint) {
        textArea.setText(hint); 
        textArea.setForeground(Color.GRAY);
        textArea.setFont(new Font("sansserif", Font.ITALIC, 12)); 
        
        for (java.awt.event.FocusListener fl : textArea.getFocusListeners()) { textArea.removeFocusListener(fl); }
        textArea.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textArea.getText().equals(hint)) {
                    textArea.setText(""); 
                    textArea.setForeground(Color.BLACK);
                    textArea.setFont(new Font("sansserif", Font.PLAIN, 12)); 
                }
            }
            @Override
            public void focusLost(FocusEvent e) {
                if (textArea.getText().trim().isEmpty()) {
                    textArea.setText(hint); 
                    textArea.setForeground(Color.GRAY);
                    textArea.setFont(new Font("sansserif", Font.ITALIC, 12)); 
                }
            }
        });
    }
}