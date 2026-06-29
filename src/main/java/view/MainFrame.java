package view;

import java.awt.*;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import controller.*;
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
    private final AuthController authController;
    private final UserController userController;
    private DoctorController doctorController;
    private final AppointmentController appointmentController;
    private final MedicalNoteController medicalNoteController;

    // panels owned by Member 1
    private LoginPanel loginPanel;
    private UserPanel userPanel;

    // reason: allows MainFrame to compile before teammates finish their panels
    private JPanel appointmentPanel;
    private JPanel patientPanel;
    private JPanel doctorPanel;
    private JPanel reportPanel;
    private JPanel medicalNotePanel;

    // reason: so onLoginSuccess() can show/hide them by role
    private JButton userMgmtBtn;
    private JButton medicalNotesButton;
    private JButton refreshButton;

    public MainFrame() {
        // reason: controllers created here, injected into panels — panels never create controllers themselves
        this.authController = new AuthController();
        this.userController = new UserController();
        this.doctorController = new DoctorController();
        this.appointmentController = new AppointmentController();
        this.medicalNoteController = new MedicalNoteController();
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
        // reason: Flowlayout.LEFT stacks buttons vertically to the left in sidebar adds margin between buttons
        // reason: EtchedBorder gives subtle visual separation between nav and content
        navPanel = new JPanel();
        navPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 15));
        navPanel.setPreferredSize(new Dimension(180, 0));
        navPanel.setBorder(BorderFactory.createEtchedBorder());

        JButton dashboardButton = new JButton("Dashboard");
        JButton appointmentsButton = new JButton("Appointments");
        JButton patientsButton = new JButton("Patients");
        JButton doctorsButton = new JButton("Doctors");
        medicalNotesButton = new JButton("Medical Notes");
        JButton reportsButton = new JButton("Reports");
        JButton logoutButton = new JButton("Logout");
        userMgmtBtn = new JButton("User Management");

        refreshButton = new JButton("Refresh Data");
        refreshButton.setBackground(new Color(220, 220, 220));

        dashboardButton.addActionListener(_ -> showPanel("LOGIN"));
        appointmentsButton.addActionListener(_ -> showPanel("APPOINTMENTS"));
        patientsButton.addActionListener(_ -> showPanel("PATIENTS"));
        doctorsButton.addActionListener(_ -> showPanel("DOCTORS"));
        medicalNotesButton.addActionListener(_ -> showPanel("MEDICAL_NOTES"));
        reportsButton.addActionListener(_ -> showPanel("REPORTS"));
        userMgmtBtn.addActionListener(_ -> showPanel("USER_MANAGEMENT"));
        logoutButton.addActionListener(_ -> handleLogout());

        // Refresh button action listener to refresh data in the currently visible panel
        refreshButton.addActionListener(_ -> {
            if (userPanel != null && userPanel.isVisible()) userPanel.setVisible(true);
            if (patientPanel != null && patientPanel.isVisible()) patientPanel.setVisible(true);
            if (doctorPanel != null && doctorPanel.isVisible()) doctorPanel.setVisible(true);
            if (reportPanel != null && reportPanel.isVisible()) reportPanel.setVisible(true);
            if (appointmentPanel != null && appointmentPanel.isVisible()) {
                ((view.AppointmentPanel) appointmentPanel).refreshPanel();
            }
            if (medicalNotePanel != null && medicalNotePanel.isVisible()) {
                ((MedicalNotesPanel) medicalNotePanel).refreshPanel();
            }

            javax.swing.JOptionPane.showMessageDialog(this, "All system data has been refreshed!", "Refresh Success", 
            javax.swing.JOptionPane.INFORMATION_MESSAGE);
        });

        // reason: nav sidebar should not be visible on the login screen
        navPanel.add(dashboardButton);
        navPanel.add(appointmentsButton);
        navPanel.add(patientsButton);
        navPanel.add(doctorsButton);
        navPanel.add(medicalNotesButton);
        navPanel.add(reportsButton);
        navPanel.add(userMgmtBtn);
        navPanel.add(logoutButton);

        navPanel.add(Box.createVerticalGlue());
        navPanel.add(refreshButton);
        navPanel.add(Box.createVerticalStrut(10));

        add(navPanel, BorderLayout.WEST);
        navPanel.setVisible(false);

        // --- CARD PANEL ---
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        loginPanel = new LoginPanel(authController, () -> onLoginSuccess());
        userPanel = new UserPanel(userController, doctorController, () -> handleLogout());

        appointmentPanel = new view.AppointmentPanel();

        patientPanel = new JPanel(new BorderLayout());
        patientPanel = new PatientPanel();

        doctorPanel = new JPanel(new BorderLayout());
        doctorPanel = new DoctorPanel();

        reportPanel = new ReportPanel();

        medicalNotePanel = new MedicalNotesPanel(appointmentController, medicalNoteController);

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
            ChangePasswordDialog dialog = new ChangePasswordDialog(this, userController, user.getUserId(), true);
            dialog.setVisible(true);
            if (!dialog.isSuccess()) {
                handleLogout();
                return;
            }
        }

        navPanel.setVisible(true);
        userMgmtBtn.setVisible(user.getRole() == Role.ADMIN);
        medicalNotesButton.setVisible(user.getRole() == Role.DOCTOR);
        
        if (user.getRole() == Role.ADMIN) {
            showPanel("USER_MANAGEMENT");
        }

        if (user.getRole() == Role.DOCTOR) {
            showPanel("APPOINTMENTS");
        }
    }

    private void handleLogout() {
        authController.logout();
        navPanel.setVisible(false);
        if (medicalNotePanel != null) {
            cardPanel.remove(medicalNotePanel);
            medicalNotePanel = null;
        }
        showPanel("LOGIN");
    }

    public void showPanel(String name) {
        if (name.equals("MEDICAL_NOTES")) {
            if (medicalNotePanel == null) {
                medicalNotePanel = new MedicalNotesPanel(appointmentController, medicalNoteController);
                cardPanel.add(medicalNotePanel, "MEDICAL_NOTES");
            }
            ((MedicalNotesPanel) medicalNotePanel).refreshPanel();
        }
        cardLayout.show(cardPanel, name);
    }
}