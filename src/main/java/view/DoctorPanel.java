package view;

import controller.DoctorController;
import model.Doctor;
import model.Gender;
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

public class DoctorPanel extends JPanel {

    // handle all doctor data operations
    DoctorController doctorController = new DoctorController();

    // table to show all doctors
    JTable doctorTable;
    DefaultTableModel tableModel;

    // form input fields
    JTextField tfName;
    JTextField tfAge;
    JTextField tfPhone;
    JTextField tfEmail;
    JTextField tfSpecialization;
    JTextField tfLicenseNo;
    JTextField tfDepartment;
    JTextField tfSearch;
    JComboBox<Gender> cbGender;  // uses Gender enum from model package

    // action buttons
    JButton btnAdd;
    JButton btnUpdate;
    JButton btnDelete;
    JButton btnClear;

    // stores the id of the doctor currently selected in the table
    String selectedDoctorId = null;

    // sets up the whole UI
    public DoctorPanel() {

        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // search bar
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel searchLabel = new JLabel("Search by name: ");
        tfSearch = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnRefresh = new JButton("Show All");

        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                handleSearch();
            }
        });

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

        // doctor table 
        String[] columnNames = {"Doctor ID", "Name", "Age", "Gender", "Specialization", "License No", "Phone", "Department"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        doctorTable = new JTable(tableModel);
        doctorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorTable.setRowHeight(22);

        // clicking a row fills the form with that doctor's data
        doctorTable.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = doctorTable.getSelectedRow();
                if (row >= 0) {
                    String did = (String) tableModel.getValueAt(row, 0);
                    populateForm(did);
                }
            }

            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseReleased(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        });

        JScrollPane scrollPane = new JScrollPane(doctorTable);
        add(scrollPane, BorderLayout.CENTER);

        // form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setPreferredSize(new Dimension(280, 0));
        formPanel.setBorder(BorderFactory.createTitledBorder("Doctor Details"));

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
        tfSpecialization = new JTextField(15);
        tfLicenseNo = new JTextField(15);
        tfDepartment = new JTextField(15);

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
        formPanel.add(new JLabel("Specialization *"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfSpecialization, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("License No *"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfLicenseNo, gbc);
        row++;

        gbc.gridx = 0; gbc.gridy = row;
        formPanel.add(new JLabel("Department"), gbc);
        gbc.gridx = 1;
        formPanel.add(tfDepartment, gbc);
        row++;

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

    // reload the table with all doctors from DataStore
    public void loadTable() {
        tableModel.setRowCount(0);

        ArrayList<Doctor> doctors = doctorController.getAllDoctors();

        for (int i = 0; i < doctors.size(); i++) {
            Doctor d = doctors.get(i);
            tableModel.addRow(new Object[]{
                d.getDoctorId(),
                d.getName(),
                d.getAge(),
                d.getGender(),   
                d.getSpecialization(),
                d.getLicenseNo(),
                d.getPhone(),
                d.getDepartment()
            });
        }

        clearForm();
    }

    // search for doctors by name keyword
    private void handleSearch() {
        String keyword = tfSearch.getText().trim();

        if (keyword.equals("")) {
            loadTable();
            return;
        }

        tableModel.setRowCount(0);

        ArrayList<Doctor> results = doctorController.searchDoctors(keyword);

        for (int i = 0; i < results.size(); i++) {
            Doctor d = results.get(i);
            tableModel.addRow(new Object[]{
                d.getDoctorId(),
                d.getName(),
                d.getAge(),
                d.getGender(),
                d.getSpecialization(),
                d.getLicenseNo(),
                d.getPhone(),
                d.getDepartment()
            });
        }
    }

    // fill form with selected doctor's data
    private void populateForm(String doctorId) {
        Doctor d = doctorController.getDoctorById(doctorId);

        if (d == null) {
            return;
        }

        selectedDoctorId = doctorId;

        tfName.setText(d.getName());
        tfAge.setText(String.valueOf(d.getAge()));
        cbGender.setSelectedItem(d.getGender()); 
        tfPhone.setText(d.getPhone());
        tfEmail.setText(d.getEmail());
        tfSpecialization.setText(d.getSpecialization());
        tfLicenseNo.setText(d.getLicenseNo());
        tfDepartment.setText(d.getDepartment());
    }

    // handle clicking Add button
    private void handleAdd() {
        try {
            int age = Integer.parseInt(tfAge.getText().trim());
            Gender gender = (Gender) cbGender.getSelectedItem();

            String newId = doctorController.addDoctor(
                tfName.getText().trim(),
                age,
                gender,
                tfPhone.getText().trim(),
                tfEmail.getText().trim(),
                tfSpecialization.getText().trim(),
                tfLicenseNo.getText().trim(),
                tfDepartment.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Doctor added! ID: " + newId);
            loadTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // handle clicking Update button
    private void handleUpdate() {
        if (selectedDoctorId == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the table first.");
            return;
        }

        try {
            int age = Integer.parseInt(tfAge.getText().trim());
            Gender gender = (Gender) cbGender.getSelectedItem();

            doctorController.updateDoctor(
                selectedDoctorId,
                tfName.getText().trim(),
                age,
                gender,
                tfPhone.getText().trim(),
                tfEmail.getText().trim(),
                tfSpecialization.getText().trim(),
                tfLicenseNo.getText().trim(),
                tfDepartment.getText().trim()
            );

            JOptionPane.showMessageDialog(this, "Doctor updated!");
            loadTable();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Age must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // handle clicking Delete button
    private void handleDelete() {
        if (selectedDoctorId == null) {
            JOptionPane.showMessageDialog(this, "Please select a doctor from the table first.");
            return;
        }

        int answer = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this doctor?", "Confirm Delete", JOptionPane.YES_NO_OPTION);

        if (answer == JOptionPane.YES_OPTION) {
            try {
                doctorController.deleteDoctor(selectedDoctorId);
                JOptionPane.showMessageDialog(this, "Doctor deleted!");
                loadTable();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Cannot delete doctor.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // reset all form fields
    private void clearForm() {
        selectedDoctorId = null;
        tfName.setText("");
        tfAge.setText("");
        cbGender.setSelectedIndex(0);
        tfPhone.setText("");
        tfEmail.setText("");
        tfSpecialization.setText("");
        tfLicenseNo.setText("");
        tfDepartment.setText("");
        doctorTable.clearSelection();
    }

    // only admin can add, update, delete doctors
    private void checkRole() {
        Role role = SessionManager.getInstance().getCurrentUser().getRole();

        if (role != Role.ADMIN) {
            btnAdd.setVisible(false);
            btnUpdate.setVisible(false);
            btnDelete.setVisible(false);
            tfName.setEditable(false);
            tfAge.setEditable(false);
            tfPhone.setEditable(false);
            tfEmail.setEditable(false);
            tfSpecialization.setEditable(false);
            tfLicenseNo.setEditable(false);
            tfDepartment.setEditable(false);
            cbGender.setEnabled(false);
        }
    }
}
