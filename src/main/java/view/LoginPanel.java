package view;

import controller.AuthController;
import util.Validator;
import javax.swing.*;
import java.awt.*;
import static util.UIConfig.*;

public class LoginPanel extends JPanel {
    private AuthController authController;
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
        title.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, 32));
        title.setBorder(BorderFactory.createEmptyBorder(100, 60, 40, 60));
        add(title, BorderLayout.NORTH);

        // suitable consistent alignment without manual positioning
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));

        // EmptyBorder - adds padding around the form so it doesn't touch panel edges
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 60, 30, 60));

        formPanel.setPreferredSize(new Dimension(600, 150));
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        usernameLabel.setFont(new Font(DEF_FONT_FAMILY, usernameLabel.getFont().getStyle(), DEF_FONT_SIZE_LABEL));
        passwordLabel.setFont(new Font(DEF_FONT_FAMILY, usernameLabel.getFont().getStyle(), DEF_FONT_SIZE_LABEL));

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);

        // placeholder to fill the left cell so errorLabel sits in the right column
        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        errorLabel.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, DEF_FONT_SIZE_NORMAL));
        formPanel.add(new JLabel());
        formPanel.add(errorLabel);

        centerWrapper.add(formPanel);
//        add(formPanel, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        loginButton = new JButton("Login");
        loginButton.setPreferredSize(new Dimension(100, 30));
        loginButton.setFont(new Font(DEF_FONT_FAMILY, Font.BOLD, 16));
        buttonPanel.add(loginButton);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 100, 60));
        add(buttonPanel, BorderLayout.SOUTH);

        // TEMPORARY
        usernameField.setText("admin");
        passwordField.setText("admin");

        // triggers handleLogin() on button click
        loginButton.addActionListener(e -> handleLogin());

        // triggers handleLogin() on keyboard entered
        usernameField.addActionListener(e -> handleLogin());
        passwordField.addActionListener(e -> handleLogin());
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

        if (!authController.findByUsername(usernameEntered).isActive()) {
            errorLabel.setText("User is NOT active!");
            return;
        }

        if (authController.login(usernameEntered, passwordEntered) == null) {
            errorLabel.setText("Invalid username or password!");
            return;
        }
        onLoginSuccess.run();
    }
}