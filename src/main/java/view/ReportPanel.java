package view;

import controller.ReportController;
import model.Doctor;
import model.Role;
import model.User;
import util.SessionManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ReportPanel extends JPanel {

    private ReportController reportController;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JLabel totalPatientsLabel;
    private JLabel totalAppointmentsLabel;
    private JLabel totalScheduledLabel;
    private JLabel totalCompletedLabel;
    private JLabel lastUpdatedLabel;
    private JLabel doctorScheduleTitleLabel;      // Shows doctor name
    private JLabel patientFilterDisplayLabel;      // ★ NEW: Shows patient name
    private JLabel resultsCountLabel;              // ★ NEW: Shows count summary
    private JButton refreshButton;
    private JButton printButton;
    private JPanel contentPanel;

    // --- Search/Filter Components ---
    private JComboBox<String> filterDoctorCombo;
    private JComboBox<String> filterStatusCombo;
    private JComboBox<String> filterPeriodCombo;
    private JTextField searchPatientField;
    private JButton filterButton;
    private JButton clearFilterButton;

    // Store doctors
    private ArrayList<Doctor> allDoctors;

    public ReportPanel() {
        this.reportController = new ReportController();
        this.allDoctors = new ArrayList<>();
        initComponents();
        checkRoleAccess();
        loadAllData();
        updateTimestamp();
    }

    private void initComponents() {
        setLayout(new BorderLayout(10, 10));

        // --- Content Panel ---
        contentPanel = new JPanel(new BorderLayout(10, 10));

        // --- Top Panel: Title + Buttons ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));

        // Title (Left)
        JLabel titleLabel = new JLabel("📊 Reports Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // Button Panel (Center)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));

        refreshButton = new JButton("🔄 Refresh Reports");
        refreshButton.setFont(new Font("Arial", Font.PLAIN, 12));
        refreshButton.addActionListener(e -> {
            loadAllData();
            updateTimestamp();
        });
        buttonPanel.add(refreshButton);

        printButton = new JButton("🖨️ Print Report");
        printButton.setFont(new Font("Arial", Font.PLAIN, 12));
        printButton.addActionListener(e -> printReport());
        buttonPanel.add(printButton);

        topPanel.add(buttonPanel, BorderLayout.CENTER);
        topPanel.add(new JLabel(), BorderLayout.EAST);

        contentPanel.add(topPanel, BorderLayout.NORTH);

        // --- Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        filterPanel.setBorder(BorderFactory.createTitledBorder("🔍 Filter Reports"));

        // Doctor filter
        filterPanel.add(new JLabel("Doctor:"));
        filterDoctorCombo = new JComboBox<>();
        filterDoctorCombo.addItem("All Doctors");
        filterPanel.add(filterDoctorCombo);

        // Status filter
        filterPanel.add(new JLabel("Status:"));
        filterStatusCombo = new JComboBox<>(reportController.getAllStatuses());
        filterPanel.add(filterStatusCombo);

        // Period filter
        filterPanel.add(new JLabel("Period:"));
        filterPeriodCombo = new JComboBox<>(reportController.getPeriodOptions());
        filterPanel.add(filterPeriodCombo);

        // Patient search
        filterPanel.add(new JLabel("Patient:"));
        searchPatientField = new JTextField(15);
        filterPanel.add(searchPatientField);

        // Filter buttons
        filterButton = new JButton("🔍 Apply Filter");
        filterButton.addActionListener(e -> applyFilters());
        filterPanel.add(filterButton);

        clearFilterButton = new JButton("✖ Clear");
        clearFilterButton.addActionListener(e -> clearFilters());
        filterPanel.add(clearFilterButton);

        contentPanel.add(filterPanel, BorderLayout.NORTH);

        // --- Summary Panel (4 stats) ---
        JPanel summaryPanel = new JPanel(new GridLayout(1, 4, 10, 10));
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        totalPatientsLabel = new JLabel("👤 Patients: 0", SwingConstants.CENTER);
        totalPatientsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalPatientsLabel.setForeground(new Color(0, 102, 204));

        totalAppointmentsLabel = new JLabel("📋 Total: 0", SwingConstants.CENTER);
        totalAppointmentsLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalAppointmentsLabel.setForeground(new Color(0, 102, 204));

        totalScheduledLabel = new JLabel("⏳ Scheduled: 0", SwingConstants.CENTER);
        totalScheduledLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalScheduledLabel.setForeground(new Color(255, 140, 0));

        totalCompletedLabel = new JLabel("✅ Completed: 0", SwingConstants.CENTER);
        totalCompletedLabel.setFont(new Font("Arial", Font.BOLD, 14));
        totalCompletedLabel.setForeground(new Color(0, 153, 0));

        summaryPanel.add(totalPatientsLabel);
        summaryPanel.add(totalAppointmentsLabel);
        summaryPanel.add(totalScheduledLabel);
        summaryPanel.add(totalCompletedLabel);
        contentPanel.add(summaryPanel, BorderLayout.CENTER);

        // --- Table Panel with Dynamic Titles ---
        JPanel tableContainer = new JPanel(new BorderLayout(10, 5));
        tableContainer.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        // ★ Title Panel: Doctor Name + Patient Name
        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

        // Doctor schedule title
        doctorScheduleTitleLabel = new JLabel("📋 All Doctors Schedule", SwingConstants.CENTER);
        doctorScheduleTitleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        doctorScheduleTitleLabel.setForeground(new Color(0, 0, 139));
        titlePanel.add(doctorScheduleTitleLabel);

        // ★ NEW: Patient filter display
        patientFilterDisplayLabel = new JLabel("", SwingConstants.CENTER);
        patientFilterDisplayLabel.setFont(new Font("Arial", Font.ITALIC, 14));
        patientFilterDisplayLabel.setForeground(new Color(0, 102, 204));
        patientFilterDisplayLabel.setVisible(false);
        titlePanel.add(patientFilterDisplayLabel);

        tableContainer.add(titlePanel, BorderLayout.NORTH);

        // Table
        String[] columns = {"Doctor", "Patient", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(25);
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(""));

        tableContainer.add(scrollPane, BorderLayout.CENTER);

        // --- Results Count (Below Table) ---
        resultsCountLabel = new JLabel("", SwingConstants.CENTER);
        resultsCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        resultsCountLabel.setForeground(Color.GRAY);
        resultsCountLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        tableContainer.add(resultsCountLabel, BorderLayout.SOUTH);

        contentPanel.add(tableContainer, BorderLayout.SOUTH);

        // --- Bottom: Last Updated Timestamp ---
        JPanel bottomPanel = new JPanel(new BorderLayout());
        lastUpdatedLabel = new JLabel("", SwingConstants.CENTER);
        lastUpdatedLabel.setForeground(Color.GRAY);
        lastUpdatedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        lastUpdatedLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        bottomPanel.add(lastUpdatedLabel, BorderLayout.SOUTH);
        contentPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    /**
     * Loads all data from DataStore.
     */
    private void loadAllData() {
        allDoctors = reportController.getAllDoctors();
        populateDoctorDropdown();
        loadStats();
        loadSchedule();
        updateScheduleTitle("All Doctors", null);
        updateResultsCount();
    }

    /**
     * Populates the doctor dropdown.
     */
    private void populateDoctorDropdown() {
        filterDoctorCombo.removeAllItems();
        filterDoctorCombo.addItem("All Doctors");

        for (Doctor d : allDoctors) {
            filterDoctorCombo.addItem(d.getName());
        }
    }

    /**
     * Gets doctor ID by name.
     */
    private String getDoctorIdByName(String doctorName) {
        for (Doctor d : allDoctors) {
            if (d.getName().equals(doctorName)) {
                return d.getDoctorId();
            }
        }
        return null;
    }

    /**
     * ★ NEW: Updates the title above the table with doctor and patient info.
     */
    private void updateScheduleTitle(String doctorName, String patientName) {
        // Update doctor title
        if (doctorName == null || doctorName.equals("All Doctors")) {
            doctorScheduleTitleLabel.setText("📋 All Doctors Schedule");
        } else {
            doctorScheduleTitleLabel.setText("📋 Schedule for: " + doctorName);
        }

        // Update patient display
        if (patientName != null && !patientName.trim().isEmpty()) {
            patientFilterDisplayLabel.setText("👤 Patient: " + patientName);
            patientFilterDisplayLabel.setVisible(true);
        } else {
            patientFilterDisplayLabel.setVisible(false);
        }
    }

    /**
     * ★ NEW: Updates the results count below the table.
     */
    private void updateResultsCount() {
        int rowCount = tableModel.getRowCount();
        int totalCount = reportController.getTotalAppointments();

        String selectedDoctor = (String) filterDoctorCombo.getSelectedItem();
        String selectedStatus = (String) filterStatusCombo.getSelectedItem();
        String selectedPeriod = (String) filterPeriodCombo.getSelectedItem();
        String patientSearch = searchPatientField.getText().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Showing ").append(rowCount).append(" of ").append(totalCount).append(" appointments");

        // Add doctor info
        if (selectedDoctor != null && !selectedDoctor.equals("All Doctors")) {
            sb.append(" for ").append(selectedDoctor);
        }

        // Add status info
        if (selectedStatus != null && !selectedStatus.equals("All Status")) {
            sb.append(" (").append(selectedStatus);

            // Add period info
            if (selectedPeriod != null && !selectedPeriod.equals("All")) {
                sb.append(" - ").append(selectedPeriod);
            }
            sb.append(")");
        } else if (selectedPeriod != null && !selectedPeriod.equals("All")) {
            sb.append(" (").append(selectedPeriod).append(")");
        }

        // Add patient search info
        if (!patientSearch.isEmpty()) {
            sb.append(" matching \"").append(patientSearch).append("\"");
        }

        resultsCountLabel.setText(sb.toString());
    }

    /**
     * Applies filters and updates the table with filtered data.
     */
    private void applyFilters() {
        String selectedDoctor = (String) filterDoctorCombo.getSelectedItem();
        String selectedStatus = (String) filterStatusCombo.getSelectedItem();
        String selectedPeriod = (String) filterPeriodCombo.getSelectedItem();
        String patientSearch = searchPatientField.getText().trim();

        String doctorId = null;
        if (selectedDoctor != null && !selectedDoctor.equals("All Doctors")) {
            doctorId = getDoctorIdByName(selectedDoctor);
        }

        // Get filtered data
        ArrayList<String[]> filteredData = reportController.getFilteredDoctorSchedule(
                doctorId, selectedStatus, selectedPeriod, patientSearch
        );

        // Update table
        tableModel.setRowCount(0);
        for (String[] row : filteredData) {
            tableModel.addRow(row);
        }

        // ★ Update title with doctor and patient name
        updateScheduleTitle(selectedDoctor, patientSearch.isEmpty() ? null : patientSearch);

        // ★ Update results count
        updateResultsCount();

        updateTimestamp();
    }

    /**
     * Clears all filters and shows all data.
     */
    private void clearFilters() {
        filterDoctorCombo.setSelectedIndex(0);
        filterStatusCombo.setSelectedIndex(0);
        filterPeriodCombo.setSelectedIndex(0);
        searchPatientField.setText("");

        // Reset titles
        updateScheduleTitle("All Doctors", null);

        loadSchedule();
        updateResultsCount();
        updateTimestamp();
    }

    /**
     * Loads and displays statistics.
     */
    public void loadStats() {
        totalPatientsLabel.setText("👤 Patients: " + reportController.getTotalPatients());
        totalAppointmentsLabel.setText("📋 Total: " + reportController.getTotalAppointments());
        totalScheduledLabel.setText("⏳ Scheduled: " + reportController.getTotalAppointmentsByStatus("SCHEDULED"));
        totalCompletedLabel.setText("✅ Completed: " + reportController.getTotalAppointmentsByStatus("COMPLETED"));
    }

    /**
     * Loads all appointments into table.
     */
    public void loadSchedule() {
        tableModel.setRowCount(0);

        ArrayList<String[]> schedule = reportController.getDoctorSchedule();
        for (String[] row : schedule) {
            tableModel.addRow(row);
        }
        updateResultsCount();
    }

    /**
     * Updates the "Last updated" timestamp.
     */
    private void updateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        lastUpdatedLabel.setText("📅 Last updated: " + now.format(formatter));
    }

    /**
     * Checks role access.
     */
    private void checkRoleAccess() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null) {
            showAccessDeniedDialog("Please login to view reports");
            return;
        }

        Role role = currentUser.getRole();

        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            contentPanel.setVisible(true);
        } else {
            contentPanel.setVisible(false);
            showAccessDeniedDialog("⛔ Access Denied\n\nOnly Admin and Receptionist can view reports.");
        }
    }

    /**
     * Shows access denied pop-up.
     */
    private void showAccessDeniedDialog(String message) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JOptionPane.showMessageDialog(
                parentWindow,
                message,
                "Access Denied",
                JOptionPane.ERROR_MESSAGE
        );
    }

    /**
     * Prints the report.
     */
    private void printReport() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !(currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONIST)) {
            JOptionPane.showMessageDialog(
                    this,
                    "⛔ You do not have permission to print reports.",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        try {
            JPanel printPanel = createPrintPanel();
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setJobName("Hospital Report - " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));

            job.setPrintable((graphics, pageFormat, pageIndex) -> {
                if (pageIndex > 0) {
                    return Printable.NO_SUCH_PAGE;
                }

                Graphics2D g2d = (Graphics2D) graphics;
                double scaleX = pageFormat.getImageableWidth() / printPanel.getWidth();
                double scaleY = pageFormat.getImageableHeight() / printPanel.getHeight();
                double scale = Math.min(scaleX, scaleY) * 0.95;

                g2d.translate(
                        pageFormat.getImageableX() + (pageFormat.getImageableWidth() - printPanel.getWidth() * scale) / 2,
                        pageFormat.getImageableY() + (pageFormat.getImageableHeight() - printPanel.getHeight() * scale) / 2
                );
                g2d.scale(scale, scale);
                printPanel.print(g2d);
                return Printable.PAGE_EXISTS;
            });

            boolean doPrint = job.printDialog();
            if (doPrint) {
                job.print();
                JOptionPane.showMessageDialog(
                        this,
                        "✅ Report printed successfully!",
                        "Print Success",
                        JOptionPane.INFORMATION_MESSAGE
                );
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(
                    this,
                    "❌ Error printing report: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Creates print panel.
     */
    private JPanel createPrintPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(headerLabel, BorderLayout.NORTH);

        JPanel subHeaderPanel = new JPanel(new GridLayout(1, 3, 10, 5));
        subHeaderPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel reportLabel = new JLabel("REPORT: Doctor Schedules", SwingConstants.CENTER);
        reportLabel.setFont(new Font("Arial", Font.BOLD, 14));
        subHeaderPanel.add(reportLabel);

        JLabel dateLabel = new JLabel(
                "Date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy")),
                SwingConstants.CENTER
        );
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subHeaderPanel.add(dateLabel);

        JLabel timeLabel = new JLabel(
                "Time: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                SwingConstants.CENTER
        );
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        subHeaderPanel.add(timeLabel);

        panel.add(subHeaderPanel, BorderLayout.NORTH);

        // ★ Include doctor and patient info in print
        String doctorTitle = doctorScheduleTitleLabel.getText();
        String patientInfo = patientFilterDisplayLabel.isVisible() ? patientFilterDisplayLabel.getText() : "";

        JPanel titlePrintPanel = new JPanel(new GridLayout(patientInfo.isEmpty() ? 1 : 2, 1, 0, 2));
        JLabel doctorPrintLabel = new JLabel(doctorTitle, SwingConstants.CENTER);
        doctorPrintLabel.setFont(new Font("Arial", Font.BOLD, 14));
        doctorPrintLabel.setForeground(new Color(0, 0, 139));
        titlePrintPanel.add(doctorPrintLabel);

        if (!patientInfo.isEmpty()) {
            JLabel patientPrintLabel = new JLabel(patientInfo, SwingConstants.CENTER);
            patientPrintLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            patientPrintLabel.setForeground(new Color(0, 102, 204));
            titlePrintPanel.add(patientPrintLabel);
        }
        panel.add(titlePrintPanel, BorderLayout.CENTER);

        JPanel summaryPrintPanel = new JPanel(new GridLayout(1, 4, 10, 5));
        summaryPrintPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JLabel patientsPrintLabel = new JLabel(
                "Patients: " + reportController.getTotalPatients(),
                SwingConstants.CENTER
        );
        patientsPrintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        summaryPrintPanel.add(patientsPrintLabel);

        JLabel totalPrintLabel = new JLabel(
                "Total: " + reportController.getTotalAppointments(),
                SwingConstants.CENTER
        );
        totalPrintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        summaryPrintPanel.add(totalPrintLabel);

        JLabel scheduledPrintLabel = new JLabel(
                "Scheduled: " + reportController.getTotalAppointmentsByStatus("SCHEDULED"),
                SwingConstants.CENTER
        );
        scheduledPrintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        summaryPrintPanel.add(scheduledPrintLabel);

        JLabel completedPrintLabel = new JLabel(
                "Completed: " + reportController.getTotalAppointmentsByStatus("COMPLETED"),
                SwingConstants.CENTER
        );
        completedPrintLabel.setFont(new Font("Arial", Font.PLAIN, 11));
        summaryPrintPanel.add(completedPrintLabel);

        panel.add(summaryPrintPanel, BorderLayout.CENTER);

        JPanel tablePrintPanel = new JPanel();
        tablePrintPanel.setLayout(new BoxLayout(tablePrintPanel, BoxLayout.Y_AXIS));
        tablePrintPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JPanel headerRow = new JPanel(new GridLayout(1, 4, 5, 2));
        headerRow.setBackground(Color.LIGHT_GRAY);
        String[] columns = {"Doctor", "Patient", "Date & Time", "Status"};
        for (String col : columns) {
            JLabel label = new JLabel(col, SwingConstants.CENTER);
            label.setFont(new Font("Monospaced", Font.BOLD, 11));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            headerRow.add(label);
        }
        tablePrintPanel.add(headerRow);

        for (int i = 0; i < scheduleTable.getRowCount(); i++) {
            JPanel dataRow = new JPanel(new GridLayout(1, 4, 5, 2));
            for (int j = 0; j < 4; j++) {
                Object value = scheduleTable.getValueAt(i, j);
                JLabel label = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                label.setFont(new Font("Monospaced", Font.PLAIN, 10));
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                dataRow.add(label);
            }
            tablePrintPanel.add(dataRow);
        }

        panel.add(tablePrintPanel, BorderLayout.CENTER);

        JLabel footerLabel = new JLabel(
                "Report generated on " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")),
                SwingConstants.CENTER
        );
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        footerLabel.setForeground(Color.GRAY);
        panel.add(footerLabel, BorderLayout.SOUTH);

        return panel;
    }
}
