package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import controller.MedicalNoteController;
import model.MedicalNote;

public class MedicalNotePanel extends JPanel {

    private final MedicalNoteController controller = new MedicalNoteController();

    // UI Components - Form Fields
    private JTextField txtAppointmentId;
    private JTextField txtDoctorId;
    private JTextField txtPatientId;
    private JTextArea txtDiagnosis;
    private JTextArea txtPrescription;
    private JButton btnSave;

    public MedicalNotePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Title
        JLabel lblTitle = new JLabel("Doctor's Medical Notes Consultation", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // Input Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Write Clinical Note"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

            // Input Appointment ID
            gbc.gridx = 0; gbc.gridy = 0;
            formPanel.add(new JLabel("Appointment ID:"), gbc);
            gbc.gridx = 1;
            txtAppointmentId = new JTextField(20);
            formPanel.add(txtAppointmentId, gbc);

            // Input Doctor ID
            gbc.gridx = 0; gbc.gridy = 1;
            formPanel.add(new JLabel("Doctor ID:"), gbc);
            gbc.gridx = 1;
            txtDoctorId = new JTextField(20);
            formPanel.add(txtDoctorId, gbc);

            // Input Patient ID
            gbc.gridx = 0; gbc.gridy = 2;
            formPanel.add(new JLabel("Patient ID:"), gbc);
            gbc.gridx = 1;
            txtPatientId = new JTextField(20);
            formPanel.add(txtPatientId, gbc);

            // Input Diagnosis
            gbc.gridx = 0; gbc.gridy = 3;
            formPanel.add(new JLabel("Diagnosis:"), gbc);
            gbc.gridx = 1;
            txtDiagnosis = new JTextArea(4, 20);
            txtDiagnosis.setLineWrap(true);
            formPanel.add(new JScrollPane(txtDiagnosis), gbc);

            // Input Prescription / Medications
            gbc.gridx = 0; gbc.gridy = 4;
            formPanel.add(new JLabel("Prescription / Medications:"), gbc);
            gbc.gridx = 1;
            txtPrescription = new JTextArea(4, 20);
            txtPrescription.setLineWrap(true);
            formPanel.add(new JScrollPane(txtPrescription), gbc);

            // Save & Complete Consultation Button
            gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 2;
            btnSave = new JButton("Save & Complete Consultation");
            btnSave.setBackground(new Color(34, 139, 34));
            btnSave.setForeground(Color.WHITE);
            formPanel.add(btnSave, gbc);

            add(formPanel, BorderLayout.CENTER);

        btnSave.addActionListener(e -> handleSaveNote());
    }

    private void handleSaveNote() {
        String apptId = txtAppointmentId.getText().trim();
        String docId = txtDoctorId.getText().trim();
        String patId = txtPatientId.getText().trim();
        String diagnosis = txtDiagnosis.getText().trim();
        String prescription = txtPrescription.getText().trim();

        // Validation
        if (apptId.isEmpty() || docId.isEmpty() || patId.isEmpty() || diagnosis.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Generate a unique note ID
        String noteId = "NOTE-" + (System.currentTimeMillis() % 1000);
        MedicalNote newNote = new MedicalNote(noteId, apptId, docId, patId, diagnosis, prescription);
        controller.addMedicalNote(newNote);

        // Clear form fields after successful save
        txtAppointmentId.setText("");
        txtDoctorId.setText("");
        txtPatientId.setText("");
        txtDiagnosis.setText("");
        txtPrescription.setText("");

        JOptionPane.showMessageDialog(this, "Medical note saved! Appointment status updated to COMPLETED.", "Success", JOptionPane.INFORMATION_MESSAGE);
    }
}