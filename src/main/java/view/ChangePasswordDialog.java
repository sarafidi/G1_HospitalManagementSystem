package view;

import controller.UserController;
import util.Validator;

import javax.swing.*;
import java.awt.*;

public class ChangePasswordDialog extends JDialog {
    // reason: dialog never touches DataStore directly, only talks to controller
    private UserController userController;

    // reason: needed to tell controller which user is changing their password
    private String userId;

    // reason: controls whether cancel is allowed and which title to show
    private boolean isForced;

    // reason: JPasswordField masks input — more secure than JTextField for passwords
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;

    // reason: inline error display is less disruptive than a popup dialog
    private JLabel errorLabel;

    public ChangePasswordDialog(Frame parent, UserController userController, String userId, boolean isForced) {
        super(parent, "Change Password", true);
        this.userController = userController;
        this.userId = userId;
        this.isForced = isForced;
        initiateComponents();
        pack();                         // auto-sizes dialog to fit its contents
        setLocationRelativeTo(parent);  // centers dialog over parent window
    }

    private void initiateComponents() {
        // reason: 10,10 adds horizontal and vertical gaps between zones
        setLayout(new BorderLayout(10, 10));

        JLabel title = new JLabel(
                isForced ? "You must change your password before continuing"
                        : "Change your password",
                SwingConstants.CENTER
        );
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        add(title, BorderLayout.NORTH);

        // reason: 3 rows for new password / confirm password / error, 2 cols for label+field
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        newPasswordField = new JPasswordField();
        confirmPasswordField = new JPasswordField();

        formPanel.add(new JLabel("New Password"));
        formPanel.add(newPasswordField);
        formPanel.add(new JLabel("Confirm Password"));
        formPanel.add(confirmPasswordField);

        errorLabel = new JLabel("", SwingConstants.CENTER);
        errorLabel.setForeground(Color.RED);
        formPanel.add(new JLabel(""));
        formPanel.add(errorLabel);

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> handleCancel());

        JButton confirmButton = new JButton("Confirm");
        confirmButton.addActionListener(e -> handleConfirm());

        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(isForced ? DO_NOTHING_ON_CLOSE : DISPOSE_ON_CLOSE);
    }

    private void handleConfirm() {
        String newPassword = new String(newPasswordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!Validator.isNonEmpty(newPassword)) {
            errorLabel.setText("Password cannot be empty!");
            return;
        }

        if (!Validator.passwordsMatch(newPassword, confirmPassword)) {
            errorLabel.setText("Passwords do not match!");
            return;
        }

        // store the returned String as "error"
        String error = userController.updatePassword(userId, newPassword);
        if (error != null) {
            errorLabel.setText(error);
            return;
        } else { dispose(); }

    }

    private void handleCancel() {
        // reason: forced password change cannot be skipped
        if (isForced) return;
        else { dispose(); }
    }
}