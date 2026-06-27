package view;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import controller.AuthController;
import controller.UserController;
import model.Role;
import model.User;
import util.SessionManager;

public class MainFrame extends JFrame {

    // CardLayout — switches between panels by name
    // reason: cleaner than removing/adding components dynamically
    private CardLayout cardLayout;

    // cardPanel — container that holds all panels
    // reason: CardLayout needs a single parent container to manage
    private JPanel cardPanel;

    // navPanel — sidebar shown after login
    // reason: persistent navigation visible on all post-login screens
    private JPanel navPanel;

    // controllers — created once, injected into panels that need them
    // reason: one controller instance shared across the frame avoids duplicate DataStore calls
    private AuthController authController;
    private UserController userController;

    // panels owned by Member 1
    private LoginPanel loginPanel;
    private UserPanel userPanel;

    // TODO: After all models completed
    // placeholder panels for teammates' modules
    // reason: allows MainFrame to compile before teammates finish their panels
    private JPanel appointmentPanel;
    private JPanel patientPanel;
    private JPanel doctorPanel;
    private JPanel reportPanel;
    private JPanel medicalNotePanel;

    // reason: so onLoginSuccess() can show/hide them by role
    private JButton userMgmtBtn;

    public MainFrame() {
        // reason: controllers created here, injected into panels — panels never create controllers themselves
        this.authController = new AuthController();
        this.userController = new UserController();
        initComponents();
        setupFrame();
    }

    private void setupFrame() {
        setTitle("Hospital Management System");
        setSize(1100, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // --- NAV PANEL (sidebar) ---
        // reason: BoxLayout Y_AXIS stacks buttons vertically in sidebar
        // reason: EtchedBorder gives subtle visual separation between nav and content
        navPanel = new JPanel();
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setPreferredSize(new Dimension(180, 0));
        navPanel.setBorder(BorderFactory.createEtchedBorder());

        JButton dashboardButton = new JButton("Dashboard");
        JButton appointmentsButton = new JButton("Appointments");
        JButton patientsButton = new JButton("Patients");
        JButton doctorsButton = new JButton("Doctors");
        JButton medicalNotesButton = new JButton("Medical Notes");
        JButton reportsButton = new JButton("Reports");
        JButton logoutButton = new JButton("Logout");
        userMgmtBtn = new JButton("User Management");

        dashboardButton.addActionListener(e -> showPanel("LOGIN"));
        appointmentsButton.addActionListener(e -> showPanel("APPOINTMENTS"));
        patientsButton.addActionListener(e -> showPanel("PATIENTS"));
        doctorsButton.addActionListener(e -> showPanel("DOCTORS"));
        medicalNotesButton.addActionListener(e -> showPanel("MEDICAL_NOTES"));
        reportsButton.addActionListener(e -> showPanel("REPORTS"));
        userMgmtBtn.addActionListener(e -> showPanel("USER_MANAGEMENT"));
        logoutButton.addActionListener(e -> handleLogout());

        // reason: nav sidebar should not be visible on the login screen
        navPanel.add(dashboardButton);
        navPanel.add(appointmentsButton);
        navPanel.add(patientsButton);
        navPanel.add(doctorsButton);
        navPanel.add(medicalNotesButton);  
        navPanel.add(reportsButton);
        navPanel.add(userMgmtBtn);
        navPanel.add(logoutButton);
        add(navPanel, BorderLayout.WEST);
        navPanel.setVisible(false);

        // --- CARD PANEL ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        loginPanel = new LoginPanel(authController, () -> onLoginSuccess());
        userPanel = new UserPanel(userController);

        appointmentPanel = new view.AppointmentPanel();

        patientPanel = new JPanel(new BorderLayout());
        patientPanel.add(new JLabel("Patients - Coming Soon", SwingConstants.CENTER), BorderLayout.CENTER);

        doctorPanel = new JPanel(new BorderLayout());
        doctorPanel.add(new JLabel("Doctors - Coming Soon", SwingConstants.CENTER), BorderLayout.CENTER);

        reportPanel = new ReportPanel();

        medicalNotePanel = new view.MedicalNotePanel();

        cardPanel.add(loginPanel, "LOGIN");
        cardPanel.add(userPanel, "USER_MANAGEMENT");
        cardPanel.add(appointmentPanel, "APPOINTMENTS");
        cardPanel.add(doctorPanel, "DOCTORS");
        cardPanel.add(medicalNotePanel, "MEDICAL_NOTES");
        cardPanel.add(patientPanel, "PATIENTS");
        cardPanel.add(reportPanel, "REPORTS");

        add(cardPanel, BorderLayout.CENTER);
        cardLayout.show(cardPanel, "LOGIN");
    }

    private void onLoginSuccess() {
        // called by LoginPanel's callback when login succeeds
        User user = SessionManager.getInstance().getCurrentUser();
        if (user.isFirstLogin()) {
            new ChangePasswordDialog(this, userController, user.getUserId(), true).setVisible(true);
        }

        navPanel.setVisible(true);
        userMgmtBtn.setVisible(user.getRole() == Role.ADMIN);

        if (user.getRole() == Role.ADMIN || user.getRole() == Role.RECEPTIONIST) {
            showPanel("PATIENTS");
        }
        if (user.getRole() == Role.DOCTOR) {
            showPanel("APPOINTMENTS");
        }
    }

    private void handleLogout() {
        authController.logout();
        navPanel.setVisible(false);
        showPanel("LOGIN");
    }

    public void showPanel(String name) {
        cardLayout.show(cardPanel, name);
    }
}
