package view;

import controller.PatientController;
import model.Gender;
import model.Patient;
import model.Role;
import model.User;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class PatientPanel extends JPanel {

    PatientController patientController = new PatientController();

    // table to show all patients
    JTable patientTable;
    DefaultTableModel tableModel;

    // form input fields
    JTextField tfName;
    JTextField tfAge;
    JTextField tfPhone;
    JTextField tfEmail;
    JTextField tfBloodType;
    JTextField tfSearch;
    JComboBox<Gender> cbGender;  
    JTextArea taHistory;

    // action buttons
    JButton btnAdd;
    JButton btnUpdate;
    JButton btnDelete;
    JButton btnClear;
    JButton btnSearch;

    // stores the id of the patient currently selected in the table
    String selectedPatientId = null;

    //sets up the whole UI
    public PatientPanel() {

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // search bar 
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search by name: ");
        tfSearch = new JTextField(15);
        btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Show All");

        // search button
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

        // show all button clears search and reloads table
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

        // patient table
        String[] columnNames = {"Patient ID", "Name", "Age", "Gender", "Phone", "Email", "Blood Type", "Registered"};
        tableModel = new DefaultTableModel(columnNames, 0) {

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        patientTable = new JTable(tableModel);
        patientTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        patientTable.setRowHeight(22);

        // clicking a row fills the form with that patient's data
        patientTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = patientTable.getSelectedRow();
                if (row >= 0) {
                    // get patient id from first column
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

        // form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(270, 0));
        formPanel.setBorder(BorderFactory.createTitledBorder("Patient Details"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // create form inputs
        tfName = new JTextField(15);
        tfAge = new JTextField(5);
        cbGender = new JComboBox<>(Gender.values());  // fill dropdown with MALE, FEMALE, OTHER
        tfPhone = new JTextField(15);
        tfEmail = new JTextField(15);
        tfBloodType = new JTextField(5);
        taHistory = new JTextArea(4, 15);
        taHistory.setLineWrap(true);

        // add each field to the form
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

        // buttons
        btnAdd = new JButton("Add");
        btnUpdate = new JButton("Update");
        btnDelete = new JButton("Delete");
        btnClear = new JButton("Clear");

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleAdd();
            }
        });

        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleUpdate();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleDelete();
            }
        });

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

        // load data when panel first opens
        loadTable();

        // hide buttons based on role
        checkRole();
    }

    // reload the table with all patients from DataStore
    public void loadTable() {
        tableModel.setRowCount(0);

        ArrayList<Patient> patients = patientController.getAllPatients();

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

    // search for patients by name keyword
    private void handleSearch() {
        String keyword = tfSearch.getText().trim();

        // if search box is empty just show all
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

    // fill form with selected patient's data
    private void populateForm(String patientId) {
        Patient p = patientController.getPatientById(patientId);

        if (p == null) {
            return;
        }

        // save selected id for use in update/delete
        selectedPatientId = patientId;

        tfName.setText(p.getName());
        tfAge.setText(String.valueOf(p.getAge()));
        cbGender.setSelectedItem(p.getGender()); 
        tfPhone.setText(p.getPhone());
        tfEmail.setText(p.getEmail());
        tfBloodType.setText(p.getBloodType());
        taHistory.setText(p.getMedicalHistory());
    }

    // handle clicking Add button
    private void handleAdd() {
        try {
            int age = Integer.parseInt(tfAge.getText().trim());

            // get selected gender from the dropdown
            Gender gender = (Gender) cbGender.getSelectedItem();

            String newId = patientController.addPatient(
                tfName.getText().trim(),
                age,
                gender,
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

    // handle clicking Update button
    private void handleUpdate() {
        // must select a patient first
        if (selectedPatientId == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
            return;
        }

        try {
            int age = Integer.parseInt(tfAge.getText().trim());
            Gender gender = (Gender) cbGender.getSelectedItem();

            patientController.updatePatient(
                selectedPatientId,
                tfName.getText().trim(),
                age,
                gender,
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

    // handle clicking Delete button
    private void handleDelete() {
        if (selectedPatientId == null) {
            JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
            return;
        }

        // ask to confirm before deleting
        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this patient?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            try {
                patientController.deletePatient(selectedPatientId);
                JOptionPane.showMessageDialog(this, "Patient deleted!");
                loadTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Cannot delete patient.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // reset all form fields
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

    // doctor role can only view
    public void checkRole() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            return;
        }
        Role role = currentUser.getRole();

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
        } else {
            btnAdd.setVisible(true);
            btnUpdate.setVisible(true);
            btnDelete.setVisible(true);
            tfName.setEditable(true);
            tfAge.setEditable(true);
            tfPhone.setEditable(true);
            tfEmail.setEditable(true);
            tfBloodType.setEditable(true);
            taHistory.setEditable(true);
            cbGender.setEnabled(true);
        }
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            checkRole();
            loadTable();
        }
    }
}