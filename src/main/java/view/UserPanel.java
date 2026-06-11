package view;

import controller.UserController;
import model.Role;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class UserPanel extends JPanel implements MouseListener {
    // reason: panel never touches DataStore directly
    private UserController userController;

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

    public UserPanel(UserController userController) {
        this.userController = userController;
        initComponents();
        refreshTable();  // load existing users into table on startup
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        JLabel title = new JLabel("User Management", SwingConstants.CENTER);
        title.setFont(new Font("JetBrains Mono", Font.BOLD, 20));
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

        // reason: Role.values() returns all enum constants automatically
        roleComboBox = new JComboBox<>(Role.values());

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

        // --- BUTTON SECTION ---
        JPanel buttonPanel = new JPanel(new FlowLayout());

        JButton addUserButton = new JButton("Add User");
        JButton deactivateUserButton = new JButton("Deactivate User");
        JButton forcePasswordChangeButton = new JButton("Force Password Change");

        addUserButton.addActionListener(e -> handleAddUser());
        deactivateUserButton.addActionListener(e -> handleDeactivate());
        forcePasswordChangeButton.addActionListener(e -> handleForcePasswordChange());

        buttonPanel.add(addUserButton);
        buttonPanel.add(deactivateUserButton);
        buttonPanel.add(forcePasswordChangeButton);

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
        roleComboBox.setSelectedItem((Role) tableModel.getValueAt(row, 3));
    }

    private void handleAddUser() {
        String username = usernameField.getText();
        String name = nameField.getText();
        String email = emailField.getText();
        String phone = phoneField.getText();
        Role role = (Role) roleComboBox.getSelectedItem();
        String password = new String(passwordField.getPassword());
        String doctorId = doctorIdField.getText();

        String error = userController.addUser(username, name, email, phone, role, password, doctorId);

        if (error != null) {
            messageLabel.setText(error);
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText("User added successfully!");
            messageLabel.setForeground(Color.GREEN);
            refreshTable();
            clearForm();
        }
    }

    private void handleDeactivate() {
        if (selectedUserId == null) {
            messageLabel.setText("Please select a user first!");
            return;
        }

        String error = userController.deactivateUser(selectedUserId);

        if (error != null) {
            messageLabel.setText(error);
            messageLabel.setForeground(Color.RED);
        } else {
            messageLabel.setText("User deactivated!");
            messageLabel.setForeground(Color.GREEN);
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
            messageLabel.setText("Password changed successfully!");
            messageLabel.setForeground(Color.GREEN);
            refreshTable();
        }
    }

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

        selectedUserId = null;
        messageLabel.setText("");
    }

    @Override
    public void mouseClicked(MouseEvent e) { handleRowClick(); }

    @Override
    public void mousePressed(MouseEvent e) { }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}