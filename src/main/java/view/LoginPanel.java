package view;

import controller.AuthController;
import util.Validator;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private AuthController authController;      // panel never touches DataStore directly, only talks to controller
    // Runnable callback - lets LoginPanel notify MainFrame without LoginPanel knowing
    // about MainFrame's internals, keeps panels loosely coupled
    private Runnable onLoginSuccess;

    private JTextField usernameField;           // appropriate for username
    private JPasswordField passwordField;       // same as JTextField but masks characters
    private JButton loginButton;                // clickable trigger for form submission
    private JLabel errorLabel;                  // easier to update than showing a new dialog for every failed attempt

    public LoginPanel(AuthController authController, Runnable onLoginSuccess) {
        this.authController = authController;
        this.onLoginSuccess = onLoginSuccess;
        initiateComponents();
    }

    private void initiateComponents() {
        setLayout(new BorderLayout());

        // title at top, SwingConstants.CENTER centers the text inside the label
        // SwingConstants interface -> constants collection generally used for positioning
        JLabel title = new JLabel("Hospital Management System", SwingConstants.CENTER);
        title.setFont(new Font("JetBrains Mono", Font.BOLD, 20));
        add(title, BorderLayout.NORTH);

        // suitable consistent alignment without manual positioning
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        // EmptyBorder - adds padding around the form so it doesn't touch panel edges
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));

        usernameField = new JTextField();
        passwordField = new JPasswordField();

        formPanel.add(new JLabel("Username:"));
        formPanel.add(usernameField);
        formPanel.add(new JLabel("Password:"));
        formPanel.add(passwordField);

        // placeholder to fill the left cell so errorLabel sits in the right column
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        formPanel.add(new JLabel());
        formPanel.add(errorLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        loginButton = new JButton("Login");
        buttonPanel.add(loginButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // triggers handleLogin() on button click
        loginButton.addActionListener(e -> handleLogin());
    }

    private void handleLogin() {
        String usernameEntered = usernameField.getText();
        String passwordEntered = new String(passwordField.getPassword());

        if (!Validator.isNonEmpty(usernameEntered)) {
            errorLabel.setText("Username should not be empty!");
            return;
        }

        if (!Validator.isNonEmpty(passwordEntered)) {
            errorLabel.setText("Password should not be empty!");
            return;
        }

        if (authController.login(usernameEntered, passwordEntered) == null ) {
            errorLabel.setText("Invalid username or password!");
            return;
        }
        onLoginSuccess.run();
    }
}