package view;

import controller.DoctorController;
import controller.UserController;
import model.Role;
import model.User;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import static util.UIConfig.*;

public class UserPanel extends JPanel implements MouseListener {
    // reason: panel never touches DataStore directly
    private final UserController userController;
    private final DoctorController doctorController;
    private final Runnable onLogout;

    // JTable — displays all users in rows and columns
    // reason: best Swing component for tabular data display
    private JTable userTable;

    // reason: allows programmatic add/remove of rows without rebuilding the table
    private DefaultTableModel tableModel;

    // form fields — declared at class level so multiple methods can access them
    private JTextField usernameField;
    private JTextField nameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JTextField doctorIdField;
    private JPasswordField passwordField;

    // reason: restricts input to valid Role values, prevents typos
    private JComboBox<Role> roleComboBox;

    // reason: for error/success feedback
    private JLabel messageLabel;

    // tracks which user is selected in the table (for deactivate/force password)
    private String selectedUserId;

    // tracks user's active status button
    private JButton deactivateUserButton;

    public UserPanel(UserController userController, DoctorController doctorController, Runnable onLogout) {
        this.userController = userController;
        this.doctorController = doctorController;
        this.onLogout = onLogout;

        initComponents();
        refreshTable();  // load existing users into table on startup
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("User Management", SwingConstants.CENTER);
        title.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // --- TABLE SECTION ---
        // reason: users should edit via the form, not directly in the table
        Object[] columns = {"User ID", "Username", "Name", "Role", "Status", "Doctor ID"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int colum) {
                return false;
            }
        };

        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        userTable.addMouseListener(this);

        // reason: allows table to scroll when rows exceed visible area
        JScrollPane scrollPane = new JScrollPane(userTable);
        scrollPane.getViewport().addMouseListener(this);
        add(scrollPane, BorderLayout.CENTER);

        // --- FORM SECTION ---
        // reason: 4 rows, 4 columns fits all fields side by side
        JPanel formPanel = new JPanel(new GridLayout(4, 4, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        usernameField = new JTextField();
        nameField = new JTextField();
        emailField = new JTextField();
        phoneField = new JTextField();
        passwordField = new JPasswordField();
        doctorIdField = new JTextField();
        doctorIdField.setEnabled(false);

        // reason: Role.values() returns all enum constants automatically
        roleComboBox = new JComboBox<>(Role.values());
        roleComboBox.addActionListener(_ -> {
                Role selectedRole = (Role) roleComboBox.getSelectedItem();
                if (selectedRole == Role.DOCTOR) {
                    doctorIdField.setText(util.IDGenerator.generateDoctorId());
                } else {
                    doctorIdField.setText(null);
                }
        });

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Phone:"));
        formPanel.add(phoneField);
        formPanel.add(new JLabel("Role:"));
        formPanel.add(roleComboBox);
        formPanel.add(new JLabel("Doctor ID:"));
        formPanel.add(doctorIdField);
        formPanel.add(new JLabel("Temp Password:"));
        formPanel.add(passwordField);

        messageLabel = new JLabel("", SwingConstants.CENTER);
        messageLabel.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, DEF_FONT_SIZE_LABEL));

        // --- BUTTON SECTION ---
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addUserButton = new JButton("Add User");
        deactivateUserButton = new JButton("Deactivate User");
        JButton forcePasswordChangeButton = new JButton("Force Password Change");
        JButton deleteUserButton = new JButton("Delete User");

        addUserButton.addActionListener(_ -> handleAddUser());
        deactivateUserButton.addActionListener(_ -> handleDeactivate());
        forcePasswordChangeButton.addActionListener(_ -> handleForcePasswordChange());
        deleteUserButton.addActionListener(_ -> handleDeleteUser());

        buttonPanel.add(addUserButton);
        buttonPanel.add(deactivateUserButton);
        buttonPanel.add(forcePasswordChangeButton);
        buttonPanel.add(deleteUserButton);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(formPanel, BorderLayout.CENTER);
        southPanel.add(messageLabel, BorderLayout.NORTH);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }

    private void handleRowClick() {
        // if row == -1 (nothing selected), return
        int row = userTable.getSelectedRow();
        if (row == -1) return;

        selectedUserId = (String) tableModel.getValueAt(row, 0);
        usernameField.setText((String) tableModel.getValueAt(row, 1));
        nameField.setText((String) tableModel.getValueAt(row, 2));
        doctorIdField.setText((String) tableModel.getValueAt(row, 5));
        roleComboBox.setSelectedItem(tableModel.getValueAt(row, 3));
        String status = tableModel.getValueAt(userTable.getSelectedRow(), 4).toString();

        if (status.equalsIgnoreCase("active")) {
            deactivateUserButton.setText("Deactivate User");
        } else {
            deactivateUserButton.setText("Activate User");
        }

        List<User> users = userController.getAllUsers();
        User user = users.stream()
                .filter(u -> u.getUserId().equalsIgnoreCase(selectedUserId))
                .findFirst()
                .orElseThrow();

        emailField.setText(user.getEmail());
        phoneField.setText(user.getPhone());
        if (!user.isFirstLogin()) {
            passwordField.setText("--------");
            passwordField.setEnabled(false);
        }
    }

    private void handleAddUser() {
        messageLabel.setText("");
        String username = usernameField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        Role role = (Role) roleComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String doctorId = doctorIdField.getText();

        String newUser = userController.addUser(username, name, email, phone, role, password, doctorId);
        if (role == Role.DOCTOR) doctorController.addDoctor(name, 0, null, phone, email, "-", "-", "-");

        if (newUser != null) {
            messageLabel.setText(newUser);
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText("User added successfully!");
            messageLabel.setForeground(COLOR_SUCCESS);
            messageLabel.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, DEF_FONT_SIZE_LABEL));
            refreshTable();
            clearForm();
        }

        refreshTable();
    }

    private void handleDeactivate() {
        if (selectedUserId == null) {
            messageLabel.setText("Please select a user first!");
            return;
        }

        String error = userController.handleUserActive(selectedUserId);

        if (error != null) {
            JOptionPane.showMessageDialog(this, "Cannot deactivate you own account!" ,"Error", JOptionPane.ERROR_MESSAGE);
        } else {
            String buttonText = deactivateUserButton.getText().split(" ")[0].toLowerCase();
            JOptionPane.showMessageDialog(this, "User " + buttonText + "d!");
            refreshTable();
        }
    }

    private void handleForcePasswordChange() {
        if (selectedUserId == null) {
            messageLabel.setText("Please select a user first!");
            return;
        }

        String error = userController.forcePasswordChange(selectedUserId);

        if (error != null) {
            messageLabel.setText(error);
            messageLabel.setForeground(Color.RED);
        } else {
            String currUserId = SessionManager.getInstance().getCurrentUser().getUserId();

            if (selectedUserId.equalsIgnoreCase(currUserId)) {
                JDialog autoCloseDialog = new JOptionPane(
                        "Force password changed successfully!",
                        JOptionPane.INFORMATION_MESSAGE,
                        JOptionPane.DEFAULT_OPTION,
                        null,
                        new Object[]{},
                        null
                ).createDialog(this, "Success");

                // initialize background timer to kill dialog box
                Timer timer = new Timer(2000, _ -> {
                    if (autoCloseDialog.isVisible()) autoCloseDialog.dispose();
                });
                timer.setRepeats(false);
                timer.start();

                // reveal the dialog box (thread waits here until it closes or disposes)
                autoCloseDialog.setVisible(true);
                if (onLogout != null) {
                    onLogout.run();
                }
            } else {
                messageLabel.setText("Force password changed successfully!");
                messageLabel.setForeground(COLOR_SUCCESS);
            }
        }
    }

    private void handleDeleteUser() {
        if (selectedUserId == null) {
            messageLabel.setText("Please select a user first!");
            return;
        }

        String error = userController.deletedUser(selectedUserId);

        if (error != null) {
            messageLabel.setText(error);
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText("User deleted!");
            messageLabel.setForeground(Color.GREEN);
            refreshTable();
        }
    }

//    private void handleUpdate() {
//        int row = userTable.getSelectedRow();
//        if (row == -1) return;
//        selectedUserId = (String) tableModel.getValueAt(row, 0);
//
//        String username = usernameField.getText();
//        String name = nameField.getText();
//        String email = emailField.getText();
//        String phone = phoneField.getText();
//        Role role = (Role) roleComboBox.getSelectedItem();
//        String doctorId = doctorIdField.getText();
//
//
//
//        if (username)
//    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        ArrayList<User> users = userController.getAllUsers();

        for (User user : users) {
             tableModel.addRow(new Object[]{
                 user.getUserId(), user.getUsername(), user.getName(),
                 user.getRole(), user.isActive() ? "Active" : "Inactive",
                 user.getDoctorId()
             });
        }
    }

    private void clearForm() {
        usernameField.setText("");
        nameField.setText("");
        emailField.setText("");
        phoneField.setText("");
        roleComboBox.setSelectedIndex(0);
        passwordField.setText("");
        doctorIdField.setText("");
        deactivateUserButton.setText("Deactivate User");

        passwordField.setEnabled(true);
        passwordField.setBackground(Color.white);

        selectedUserId = null;
        messageLabel.setText("");

        userTable.clearSelection();
    }

    @Override
    public void mouseClicked(MouseEvent e) { handleRowClick(); }

    @Override
    public void mousePressed(MouseEvent e) { if (e.getSource() != userTable) clearForm(); }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refreshTable();
            clearForm();
        }
    }
}