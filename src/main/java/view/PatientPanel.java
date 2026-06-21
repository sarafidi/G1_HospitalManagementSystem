package view;

import controller.PatientController;
import model.Patient;
import model.Role;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

// this is the patient page, it shows the list of patients and a form to add or edit
public class PatientPanel extends JPanel {

    // controller to talk to the database
    PatientController patientController = new PatientController();

    // table to show patient list
    JTable patientTable;
    DefaultTableModel tableModel;

    // form fields
    JTextField tfName;
    JTextField tfAge;
    JTextField tfPhone;
    JTextField tfEmail;
    JTextField tfBloodType;
    JTextField tfSearch;
    JComboBox<String> cbGender;
    JTextArea taHistory;

    // buttons
    JButton btnAdd;
    JButton btnUpdate;
    JButton btnDelete;
    JButton btnClear;
    JButton btnSearch;

    // to remember which patient is selected in the table
    String selectedPatientId = null;

    // constructor - build the whole page here
    public PatientPanel() {

        // use border layout for the panel
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // top area - search bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search by name: ");
        tfSearch = new JTextField(15);
        btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Show All");

        // search button click
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

        // refresh button shows all patients again
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tfSearch.setText("");
                loadTable();
            }
        });

        topPanel.add(searchLabel);
        topPanel.add(tfSearch);
        topPanel.add(btnSearch);
        topPanel.add(btnRefresh);
        add(topPanel, BorderLayout.NORTH);

        // middle area - the table
        String[] columnNames = {"Patient ID", "Name", "Age", "Gender", "Phone", "Email", "Blood Type", "Registered"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            // make table cells not editable directly
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(22);

        // when user clicks a row, load patient info into form
        patientTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = patientTable.getSelectedRow();
                if (row >= 0) {
                    // get the patient id from first column
                    String pid = (String) tableModel.getValueAt(row, 0);
                    populateForm(pid);
                }
            }

            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        JScrollPane scrollPane = new JScrollPane(patientTable);
        add(scrollPane, BorderLayout.CENTER);

        // right side - the form to add or edit patient
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(270, 0));
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // create form fields
        tfName = new JTextField(15);
        tfAge = new JTextField(5);
        cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        tfPhone = new JTextField(15);
        tfEmail = new JTextField(15);
        tfBloodType = new JTextField(5);
        taHistory = new JTextArea(4, 15);
        taHistory.setLineWrap(true);

        // add fields row by row
        int row = 0;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Name *"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfName, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Age *"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfAge, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Gender *"), gbc);
        gbc.gridx = 1;
        formPanel.add(cbGender, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Phone *"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfPhone, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Email"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfEmail, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Blood Type"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfBloodType, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(new JLabel("Medical History"), gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(new JScrollPane(taHistory), gbc);
        row++;

        // buttons at the bottom of the form
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        // add button
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAdd();
            }
        });

        // update button
        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdate();
            }
        });

        // delete button
        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

        // clear button resets the form
        btnClear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearForm();
            }
        });

        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        btnPanel.add(btnClear);

        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2;
        formPanel.add(btnPanel, gbc);

        add(formPanel, BorderLayout.EAST);

        // load the table when panel first opens
        loadTable();

        // check what role the user is and hide buttons if needed
        checkRole();
    }

    // load all patients into the table
    public void loadTable() {
        // clear table first
        tableModel.setRowCount(0);

        ArrayList<Patient> patients = patientController.getAllPatients();

        // add each patient as a row
        for (int i = 0; i < patients.size(); i++) {
            Patient p = patients.get(i);
            tableModel.addRow(new Object[]{
                p.getPatientId(),
                p.getName(),
                p.getAge(),
                p.getGender(),
                p.getPhone(),
                p.getEmail(),
                p.getBloodType(),
                p.getRegisteredDate()
            });
        }

        clearForm();
    }

    // search patients by keyword
    private void handleSearch() {
        String keyword = tfSearch.getText().trim();

        // if empty just show all
        if (keyword.equals("")) {
            loadTable();
            return;
        }

        tableModel.setRowCount(0);

        ArrayList<Patient> results = patientController.searchPatient(keyword);

        for (int i = 0; i < results.size(); i++) {
            Patient p = results.get(i);
            tableModel.addRow(new Object[]{
                p.getPatientId(),
                p.getName(),
                p.getAge(),
                p.getGender(),
                p.getPhone(),
                p.getEmail(),
                p.getBloodType(),
                p.getRegisteredDate()
            });
        }
    }

    // fill the form with the selected patient data
    private void populateForm(String patientId) {
        Patient p = patientController.getPatientById(patientId);

        if (p == null) {
            return;
        }

        // remember the selected id for update/delete later
        selectedPatientId = patientId;

        tfName.setText(p.getName());
        tfAge.setText(String.valueOf(p.getAge()));
        cbGender.setSelectedItem(p.getGender());
        tfPhone.setText(p.getPhone());
        tfEmail.setText(p.getEmail());
        tfBloodType.setText(p.getBloodType());
        taHistory.setText(p.getMedicalHistory());
    }

    // handle add button
    private void handleAdd() {
        try {
            // get age from text field
            int age = Integer.parseInt(tfAge.getText().trim());

            String newId = patientController.addPatient(
                tfName.getText().trim(),
                age,
                (String) cbGender.getSelectedItem(),
                tfPhone.getText().trim(),
                tfEmail.getText().trim(),
                tfBloodType.getText().trim(),
                taHistory.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Patient added! ID: " + newId);
            loadTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // handle update button
    private void handleUpdate() {
        // make sure a patient is selected
        if (selectedPatientId == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
            return;
        }

        try {
            int age = Integer.parseInt(tfAge.getText().trim());

            patientController.updatePatient(
                selectedPatientId,
                tfName.getText().trim(),
                age,
                (String) cbGender.getSelectedItem(),
                tfPhone.getText().trim(),
                tfEmail.getText().trim(),
                tfBloodType.getText().trim(),
                taHistory.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Patient updated!");
            loadTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // handle delete button
    private void handleDelete() {
        if (selectedPatientId == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
            return;
        }

        // ask user to confirm before deleting
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            try {
                patientController.deletePatient(selectedPatientId);
                JOptionPane.showMessageDialog(this, "Patient deleted!");
                loadTable();
            } catch (Exception e) {
                // this happens if patient has appointments, cant delete
                JOptionPane.showMessageDialog(this, "Cannot delete. Patient might still have appointments.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // clear all the form fields
    private void clearForm() {
        selectedPatientId = null;
        tfName.setText("");
        tfAge.setText("");
        cbGender.setSelectedIndex(0);
        tfPhone.setText("");
        tfEmail.setText("");
        tfBloodType.setText("");
        taHistory.setText("");
        patientTable.clearSelection();
    }

    // hide add/update/delete buttons if the user is a doctor
    // doctors can only view patient info, not change it
    private void checkRole() {
        Role role = SessionManager.getInstance().getCurrentUser().getRole();

        if (role == Role.DOCTOR) {
            btnAdd.setVisible(false);
            btnUpdate.setVisible(false);
            btnDelete.setVisible(false);
            tfName.setEditable(false);
            tfAge.setEditable(false);
            tfPhone.setEditable(false);
            tfEmail.setEditable(false);
            tfBloodType.setEditable(false);
            taHistory.setEditable(false);
            cbGender.setEnabled(false);
        }
    }
}
