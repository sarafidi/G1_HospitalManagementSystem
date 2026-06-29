package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import controller.ReportController;
import model.Doctor;
import model.Role;
import model.User;
import util.SessionManager;

public class ReportPanel extends JPanel {

    private static final Color PAGE_BG = new Color(238, 238, 238);
    private static final Color CARD_BG = Color.WHITE;
    private static final Color BORDER = new Color(210, 210, 210);
    private static final Color TEXT = new Color(35, 35, 35);
    private static final Color MUTED = new Color(110, 110, 110);

    private ReportController reportController;
    private JTable scheduleTable;
    private DefaultTableModel tableModel;
    private JLabel totalDoctorsLabel;
    private JLabel totalPatientsLabel;
    private JLabel lastUpdatedLabel;
    private JLabel doctorScheduleTitleLabel;
    private JLabel patientFilterDisplayLabel;
    private JLabel resultsCountLabel;
    private JButton refreshButton;
    private JButton printButton;
    private JPanel contentPanel;

    private JComboBox<String> filterDoctorCombo;
    private JComboBox<String> filterStatusCombo;
    private JComboBox<String> filterPeriodCombo;
    private JTextField searchPatientField;
    private JButton filterButton;
    private JButton clearFilterButton;

    private ArrayList<Doctor> allDoctors;
    private boolean isLoadingFilters = false;

    // UI components that need role-based visibility
    private JPanel tablePanel;
    private JPanel topPanel;
    private JPanel summaryPanel;

    public ReportPanel() {
        this.reportController = new ReportController();
        this.allDoctors = new ArrayList<>();
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setBackground(PAGE_BG);

        contentPanel = new JPanel(new BorderLayout(0, 8));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(12, 16, 8, 16));
        contentPanel.setBackground(PAGE_BG);

        ScrollablePanel mainPanel = new ScrollablePanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(PAGE_BG);

        // Store references to panels for role-based visibility
        topPanel = createTopPanel();
        mainPanel.add(topPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        mainPanel.add(createFilterPanel());
        mainPanel.add(Box.createVerticalStrut(10));

        summaryPanel = createSummaryPanel();
        mainPanel.add(summaryPanel);
        mainPanel.add(Box.createVerticalStrut(10));

        tablePanel = createTablePanel();
        mainPanel.add(tablePanel);

        JScrollPane pageScroll = new JScrollPane(mainPanel);
        pageScroll.setBorder(null);
        pageScroll.getVerticalScrollBar().setUnitIncrement(16);
        pageScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        contentPanel.add(pageScroll, BorderLayout.CENTER);

        lastUpdatedLabel = new JLabel("", SwingConstants.CENTER);
        lastUpdatedLabel.setForeground(MUTED);
        lastUpdatedLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        contentPanel.add(lastUpdatedLabel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel topPanel = createCardPanel(new BorderLayout(10, 10), 12, 18, 12, 18);
        topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));
        topPanel.setPreferredSize(new Dimension(10, 66));

        JLabel titleLabel = new JLabel("Reports Dashboard");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setForeground(TEXT);
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttonPanel.setOpaque(false);

        refreshButton = createButton("Refresh Reports", 140, 32);
        refreshButton.addActionListener(e -> {
            loadAllData();
            updateTimestamp();
        });
        buttonPanel.add(refreshButton);

        printButton = createButton("Print Report", 125, 32);
        printButton.addActionListener(e -> printReport());
        buttonPanel.add(printButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);
        return topPanel;
    }

    private JPanel createFilterPanel() {
        JPanel outer = createCardPanel(new BorderLayout(8, 8), 8, 14, 10, 14);
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(BorderFactory.createLineBorder(BORDER), "Filter Reports"),
                BorderFactory.createEmptyBorder(6, 12, 8, 12)
        ));
        outer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JPanel fields = new JPanel(new GridBagLayout());
        fields.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 5, 4, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridy = 0;

        // Doctor
        gbc.gridx = 0;
        gbc.weightx = 0;
        JLabel doctorLabel = new JLabel("Doctor:");
        doctorLabel.setFont(new Font("Arial", Font.BOLD, 12));
        fields.add(doctorLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.15;
        filterDoctorCombo = new JComboBox<>();
        filterDoctorCombo.addItem("All Doctors");
        filterDoctorCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        filterDoctorCombo.setPreferredSize(new Dimension(140, 28));
        fields.add(filterDoctorCombo, gbc);

        // Status
        gbc.gridx = 2;
        gbc.weightx = 0;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Arial", Font.BOLD, 12));
        fields.add(statusLabel, gbc);

        gbc.gridx = 3;
        gbc.weightx = 0.15;
        filterStatusCombo = new JComboBox<>(reportController.getAllStatuses());
        filterStatusCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        filterStatusCombo.setPreferredSize(new Dimension(140, 28));
        fields.add(filterStatusCombo, gbc);

        // Period
        gbc.gridx = 4;
        gbc.weightx = 0;
        JLabel periodLabel = new JLabel("Period:");
        periodLabel.setFont(new Font("Arial", Font.BOLD, 12));
        fields.add(periodLabel, gbc);

        gbc.gridx = 5;
        gbc.weightx = 0.15;
        filterPeriodCombo = new JComboBox<>(reportController.getPeriodOptions());
        filterPeriodCombo.setFont(new Font("Arial", Font.PLAIN, 12));
        filterPeriodCombo.setPreferredSize(new Dimension(140, 28));
        fields.add(filterPeriodCombo, gbc);

        // Patient (wider)
        gbc.gridx = 6;
        gbc.weightx = 0;
        JLabel patientLabel = new JLabel("Patient:");
        patientLabel.setFont(new Font("Arial", Font.BOLD, 12));
        fields.add(patientLabel, gbc);

        gbc.gridx = 7;
        gbc.weightx = 0.55; // Give patient search more weight
        searchPatientField = new JTextField();
        searchPatientField.setFont(new Font("Arial", Font.PLAIN, 12));
        searchPatientField.setPreferredSize(new Dimension(220, 28)); // Wider
        fields.add(searchPatientField, gbc);

        // Buttons
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        buttons.setOpaque(false);
        filterButton = createButton("Apply Filter", 115, 30);
        filterButton.addActionListener(e -> applyFilters());
        buttons.add(filterButton);

        clearFilterButton = createButton("Clear", 80, 30);
        clearFilterButton.addActionListener(e -> clearFilters());
        buttons.add(clearFilterButton);

        outer.add(fields, BorderLayout.CENTER);
        outer.add(buttons, BorderLayout.EAST);

        filterDoctorCombo.addActionListener(e -> applyFiltersIfReady());
        filterStatusCombo.addActionListener(e -> applyFiltersIfReady());
        filterPeriodCombo.addActionListener(e -> applyFiltersIfReady());
        searchPatientField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyFiltersIfReady();
            }

            public void removeUpdate(DocumentEvent e) {
                applyFiltersIfReady();
            }

            public void changedUpdate(DocumentEvent e) {
                applyFiltersIfReady();
            }
        });

        return outer;
    }

    private void addFilterField(JPanel panel, GridBagConstraints gbc, int x, String labelText, JComponent field) {
        gbc.gridx = x * 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Arial", Font.BOLD, 12));
        panel.add(label, gbc);

        gbc.gridx = x * 2 + 1;
        gbc.weightx = 1;
        field.setPreferredSize(new Dimension(120, 28));
        panel.add(field, gbc);
    }

    private JPanel createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        summaryPanel.setBackground(PAGE_BG);
        summaryPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        summaryPanel.setPreferredSize(new Dimension(10, 64));

        totalDoctorsLabel = createStatCard("Total Doctors", "0");
        totalPatientsLabel = createStatCard("Total Patients", "0");

        summaryPanel.add(totalDoctorsLabel);
        summaryPanel.add(totalPatientsLabel);
        return summaryPanel;
    }

    private JLabel createStatCard(String title, String value) {
        JLabel label = new JLabel(makeStatHtml(title, value), SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(CARD_BG);
        label.setForeground(TEXT);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(150, 56));
        label.setMinimumSize(new Dimension(150, 56));
        label.setMaximumSize(new Dimension(150, 56));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return label;
    }

    private JPanel createTablePanel() {
        JPanel tableContainer = createCardPanel(new BorderLayout(8, 8), 12, 14, 10, 14);
        tableContainer.setPreferredSize(new Dimension(10, 330));
        tableContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 380));

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 0, 2));
        titlePanel.setOpaque(false);

        doctorScheduleTitleLabel = new JLabel("All Doctors Schedule", SwingConstants.LEFT);
        doctorScheduleTitleLabel.setFont(new Font("Arial", Font.BOLD, 15));
        doctorScheduleTitleLabel.setForeground(TEXT);
        titlePanel.add(doctorScheduleTitleLabel);

        patientFilterDisplayLabel = new JLabel("", SwingConstants.LEFT);
        patientFilterDisplayLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        patientFilterDisplayLabel.setForeground(MUTED);
        patientFilterDisplayLabel.setVisible(false);
        titlePanel.add(patientFilterDisplayLabel);
        tableContainer.add(titlePanel, BorderLayout.NORTH);

        String[] columns = {"Doctor", "Patient", "Date & Time", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        scheduleTable = new JTable(tableModel);
        scheduleTable.setRowHeight(28);
        scheduleTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        scheduleTable.getTableHeader().setReorderingAllowed(false);
        scheduleTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 12));
        scheduleTable.getTableHeader().setBackground(new Color(235, 235, 235));
        scheduleTable.getTableHeader().setForeground(TEXT);
        scheduleTable.setFont(new Font("Arial", Font.PLAIN, 12));
        scheduleTable.setForeground(TEXT);
        scheduleTable.setGridColor(new Color(225, 225, 225));
        scheduleTable.setFillsViewportHeight(true);

        TableColumnModel columnModel = scheduleTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(160);
        columnModel.getColumn(1).setPreferredWidth(180);
        columnModel.getColumn(2).setPreferredWidth(190);
        columnModel.getColumn(3).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(scheduleTable);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        tableContainer.add(scrollPane, BorderLayout.CENTER);

        resultsCountLabel = new JLabel("", SwingConstants.CENTER);
        resultsCountLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        resultsCountLabel.setForeground(MUTED);
        tableContainer.add(resultsCountLabel, BorderLayout.SOUTH);

        return tableContainer;
    }

    private JPanel createCardPanel(LayoutManager layout, int top, int left, int bottom, int right) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(CARD_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(top, left, bottom, right)
        ));
        return panel;
    }

    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setFocusPainted(false);
        button.setFont(new Font("Arial", Font.BOLD, 12));
        return button;
    }

    private void loadAllData() {
        isLoadingFilters = true;
        allDoctors = reportController.getAllDoctors();
        populateDoctorDropdown();
        loadStats();
        isLoadingFilters = false;
        applyFilters();
    }

    private void populateDoctorDropdown() {
        Object selected = filterDoctorCombo.getSelectedItem();
        filterDoctorCombo.removeAllItems();
        filterDoctorCombo.addItem("All Doctors");

        for (Doctor d : allDoctors) {
            filterDoctorCombo.addItem(d.getName());
        }

        if (selected != null) {
            filterDoctorCombo.setSelectedItem(selected);
        }
        if (filterDoctorCombo.getSelectedItem() == null) {
            filterDoctorCombo.setSelectedIndex(0);
        }
    }

    private String getDoctorIdByName(String doctorName) {
        for (Doctor d : allDoctors) {
            if (d.getName().equals(doctorName)) {
                return d.getDoctorId();
            }
        }
        return null;
    }

    private void updateScheduleTitle(String doctorName, String patientName) {
        if (doctorName == null || doctorName.equals("All Doctors")) {
            doctorScheduleTitleLabel.setText("All Doctors Schedule");
        } else {
            doctorScheduleTitleLabel.setText("Schedule for: " + doctorName);
        }

        if (patientName != null && !patientName.trim().isEmpty()) {
            patientFilterDisplayLabel.setText("Patient: " + patientName);
            patientFilterDisplayLabel.setVisible(true);
        } else {
            patientFilterDisplayLabel.setVisible(false);
        }
    }

    private void updateResultsCount() {
        int rowCount = tableModel.getRowCount();
        int totalCount = reportController.getTotalAppointments();

        String selectedDoctor = (String) filterDoctorCombo.getSelectedItem();
        String selectedStatus = (String) filterStatusCombo.getSelectedItem();
        String selectedPeriod = (String) filterPeriodCombo.getSelectedItem();
        String patientSearch = searchPatientField.getText().trim();

        StringBuilder sb = new StringBuilder();
        sb.append("Showing ").append(rowCount).append(" of ").append(totalCount).append(" appointments");

        if (selectedDoctor != null && !selectedDoctor.equals("All Doctors")) {
            sb.append(" for ").append(selectedDoctor);
        }
        if (selectedStatus != null && !selectedStatus.equals("All Status")) {
            sb.append(" (").append(selectedStatus);
            if (selectedPeriod != null && !selectedPeriod.equals("All")) {
                sb.append(" - ").append(selectedPeriod);
            }
            sb.append(")");
        } else if (selectedPeriod != null && !selectedPeriod.equals("All")) {
            sb.append(" (").append(selectedPeriod).append(")");
        }
        if (!patientSearch.isEmpty()) {
            sb.append(" matching \"").append(patientSearch).append("\"");
        }
        resultsCountLabel.setText(sb.toString());
    }

    private void applyFiltersIfReady() {
        if (isLoadingFilters || tableModel == null || filterDoctorCombo == null
                || filterStatusCombo == null || filterPeriodCombo == null
                || searchPatientField == null) {
            return;
        }
        applyFilters();
    }

    private void applyFilters() {
        String selectedDoctor = (String) filterDoctorCombo.getSelectedItem();
        String selectedStatus = (String) filterStatusCombo.getSelectedItem();
        String selectedPeriod = (String) filterPeriodCombo.getSelectedItem();
        String patientSearch = searchPatientField.getText().trim();

        String doctorId = null;
        if (selectedDoctor != null && !selectedDoctor.equals("All Doctors")) {
            doctorId = getDoctorIdByName(selectedDoctor);
        }

        // Get filtered data for table
        ArrayList<String[]> filteredData;
        if (selectedDoctor != null && !selectedDoctor.equals("All Doctors") && doctorId == null) {
            filteredData = new ArrayList<>();
        } else {
            filteredData = reportController.getFilteredDoctorSchedule(
                    doctorId, selectedStatus, selectedPeriod, patientSearch
            );
        }

        tableModel.setRowCount(0);
        for (String[] row : filteredData) {
            tableModel.addRow(row);
        }

        updateScheduleTitle(selectedDoctor, patientSearch.isEmpty() ? null : patientSearch);
        updateResultsCount();
        updateTimestamp();
    }

    private void clearFilters() {
        isLoadingFilters = true;
        filterDoctorCombo.setSelectedIndex(0);
        filterStatusCombo.setSelectedIndex(0);
        filterPeriodCombo.setSelectedIndex(0);
        searchPatientField.setText("");
        isLoadingFilters = false;
        applyFilters();
    }

    public void loadStats() {
        totalDoctorsLabel.setText(makeStatHtml("Total Doctors", String.valueOf(reportController.getTotalDoctors())));
        totalPatientsLabel.setText(makeStatHtml("Total Patients", String.valueOf(reportController.getTotalPatients())));
    }

    private String makeStatHtml(String title, String value) {
        return "<html><div style='text-align:center;'><b>" + title + "</b><br><span style='font-size:17px;'>" + value + "</span></div></html>";
    }

    public void loadSchedule() {
        tableModel.setRowCount(0);
        ArrayList<String[]> schedule = reportController.getDoctorSchedule();
        for (String[] row : schedule) {
            tableModel.addRow(row);
        }
        updateResultsCount();
    }

    private void updateTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss");
        lastUpdatedLabel.setText("Last updated: " + now.format(formatter));
    }

    private void checkRoleAccess() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null) {
            showAccessDeniedDialog("Please login to view reports");
            contentPanel.setVisible(false);
            return;
        }

        Role role = currentUser.getRole();

        if (role == Role.ADMIN) {
            // ADMIN: Full access - everything visible
            contentPanel.setVisible(true);
            topPanel.setVisible(true);
            summaryPanel.setVisible(true);
            tablePanel.setVisible(true);
            printButton.setVisible(true);
            refreshButton.setVisible(true);
        } else if (role == Role.RECEPTIONIST) {
            // RECEPTIONIST: Stats and filters visible, table hidden, print available
            contentPanel.setVisible(true);
            topPanel.setVisible(true);
            summaryPanel.setVisible(true);
            tablePanel.setVisible(false);  // Hide schedule table
            printButton.setVisible(true);   // Print still available
            refreshButton.setVisible(true);
        } else if (role == Role.DOCTOR) {
            // DOCTOR: Show pop-up and hide everything
            contentPanel.setVisible(false);
            showAccessDeniedDialog("Access Denied\n\nOnly Admin and Receptionist can view reports.");
        } else {
            contentPanel.setVisible(false);
            showAccessDeniedDialog("Access Denied\n\nYou do not have permission to view reports.");
        }
    }

    private void showAccessDeniedDialog(String message) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JOptionPane.showMessageDialog(parentWindow, message, "Access Denied", JOptionPane.ERROR_MESSAGE);
    }

    private void printReport() {
        User currentUser = SessionManager.getInstance().getCurrentUser();

        if (currentUser == null || !(currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONIST)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to print reports.");
            return;
        }

        if (tableModel.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No data available to print.");
            return;
        }

        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Appointments Report");

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int pageW = (int) pageFormat.getImageableWidth();
            int pageH = (int) pageFormat.getImageableHeight();

            int margin = 28;
            int x = margin;
            int y = 35;
            int contentW = pageW - (margin * 2);

            Font titleFont = new Font("Arial", Font.BOLD, 18);
            Font bigFont = new Font("Arial", Font.BOLD, 24);
            Font headerFont = new Font("Arial", Font.BOLD, 11);
            Font normalFont = new Font("Arial", Font.PLAIN, 10);
            Font italicFont = new Font("Arial", Font.ITALIC, 10);

            g2.setColor(Color.BLACK);

            // Border
            g2.drawRect(8, 8, pageW - 16, pageH - 16);

            // Header
            g2.setFont(titleFont);
            g2.drawString("HOSPITAL MANAGEMENT SYSTEM", x, y);

            y += 35;
            g2.setFont(bigFont);
            g2.drawString("APPOINTMENTS REPORT", x, y);

            y += 34;
            g2.setFont(normalFont);
            g2.drawString("Printed On: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm")), x, y);

            y += 28;
            g2.drawLine(x, y, x + contentW, y);

            // Filter Summary
            y += 34;
            g2.setFont(headerFont);
            g2.drawString("FILTER SUMMARY", x, y);

            y += 18;

            String doctor = String.valueOf(filterDoctorCombo.getSelectedItem());
            String status = String.valueOf(filterStatusCombo.getSelectedItem());
            String period = String.valueOf(filterPeriodCombo.getSelectedItem());
            String patient = searchPatientField.getText().trim().isEmpty() ? "(All)" : searchPatientField.getText().trim();

            int filterBoxH = 55;
            g2.drawRect(x, y, contentW, filterBoxH);

            int filterColW = contentW / 4;
            for (int i = 1; i < 4; i++) {
                g2.drawLine(x + (filterColW * i), y + 8, x + (filterColW * i), y + filterBoxH - 8);
            }

            drawCenteredText(g2, "Doctor", x, y + 18, filterColW, headerFont);
            drawCenteredText(g2, doctor, x, y + 40, filterColW, normalFont);

            drawCenteredText(g2, "Status", x + filterColW, y + 18, filterColW, headerFont);
            drawCenteredText(g2, status, x + filterColW, y + 40, filterColW, normalFont);

            drawCenteredText(g2, "Period", x + filterColW * 2, y + 18, filterColW, headerFont);
            drawCenteredText(g2, period, x + filterColW * 2, y + 40, filterColW, normalFont);

            drawCenteredText(g2, "Patient", x + filterColW * 3, y + 18, filterColW, headerFont);
            drawCenteredText(g2, patient, x + filterColW * 3, y + 40, filterColW, normalFont);

            // Summary
            y += filterBoxH + 35;
            g2.setFont(headerFont);
            g2.drawString("SUMMARY", x, y);

            y += 20;

            int cardGap = 12;
            int cardW = (contentW - (cardGap * 3)) / 4;
            int cardH = 62;

            drawSummaryCard(g2, x, y, cardW, cardH, "Total Patients", String.valueOf(reportController.getTotalPatients()));
            drawSummaryCard(g2, x + cardW + cardGap, y, cardW, cardH, "Total Appointments", String.valueOf(reportController.getTotalAppointments()));
            drawSummaryCard(g2, x + (cardW + cardGap) * 2, y, cardW, cardH, "Scheduled", String.valueOf(reportController.getTotalAppointmentsByStatus("SCHEDULED")));
            drawSummaryCard(g2, x + (cardW + cardGap) * 3, y, cardW, cardH, "Completed", String.valueOf(reportController.getTotalAppointmentsByStatus("COMPLETED")));

            // Schedule List
            y += cardH + 35;
            g2.setFont(headerFont);
            g2.drawString("SCHEDULE LIST", x, y);

            y += 18;

            int tableX = x;
            int tableY = y;
            int rowH = 26;

            int noW = 45;
            int doctorW = (int) (contentW * 0.22);
            int patientW = (int) (contentW * 0.22);
            int dateW = (int) (contentW * 0.27);
            int statusW = contentW - noW - doctorW - patientW - dateW;

            int[] colW = {noW, doctorW, patientW, dateW, statusW};
            String[] headers = {"No.", "Doctor", "Patient", "Date & Time", "Status"};

            int currentX = tableX;
            for (int i = 0; i < headers.length; i++) {
                g2.drawRect(currentX, tableY, colW[i], rowH);
                drawCenteredText(g2, headers[i], currentX, tableY + 17, colW[i], headerFont);
                currentX += colW[i];
            }

            g2.setFont(normalFont);

            for (int r = 0; r < tableModel.getRowCount(); r++) {
                int rowY = tableY + rowH * (r + 1);
                currentX = tableX;

                String[] values = {
                    String.valueOf(r + 1),
                    getTableValue(r, 0),
                    getTableValue(r, 1),
                    getTableValue(r, 2),
                    getTableValue(r, 3)
                };

                for (int c = 0; c < values.length; c++) {
                    g2.drawRect(currentX, rowY, colW[c], rowH);

                    if (c == 0 || c == 4) {
                        drawCenteredText(g2, values[c], currentX, rowY + 17, colW[c], normalFont);
                    } else {
                        g2.setFont(normalFont);
                        g2.drawString(trimText(g2, values[c], colW[c] - 12), currentX + 6, rowY + 17);
                    }

                    currentX += colW[c];
                }
            }

            y = tableY + rowH * (tableModel.getRowCount() + 1) + 25;

            drawCenteredText(g2, resultsCountLabel.getText(), x, y, contentW, normalFont);

            // Notes box
            y += 35;
            int notesH = 36;
            g2.drawRect(x, y, contentW, notesH);
            g2.setFont(normalFont);
            g2.drawString("Notes: This report contains appointment records based on the selected filter criteria.", x + 10, y + 23);

            // Footer
            y += notesH + 35;
            g2.drawLine(x, y, x + contentW, y);

            y += 25;
            drawCenteredText(g2, "Thank you for using Hospital Management System", x, y, contentW, italicFont);

            y += 18;
            drawCenteredText(g2, "This is a system generated report. No signature is required.", x, y, contentW, italicFont);

            return Printable.PAGE_EXISTS;
        });

        try {
            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this, "Report printed successfully!");
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage());
        }
    }

    private void drawCenteredText(Graphics2D g2, String text, int x, int y, int width, Font font) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int textX = x + (width - textW) / 2;
        g2.drawString(text, textX, y);
    }

    private void drawSummaryCard(Graphics2D g2, int x, int y, int w, int h, String title, String value) {
        Font titleFont = new Font("Arial", Font.BOLD, 10);
        Font valueFont = new Font("Arial", Font.BOLD, 22);

        g2.drawRect(x, y, w, h);

        drawCenteredText(g2, title, x, y + 21, w, titleFont);
        drawCenteredText(g2, value, x, y + 49, w, valueFont);
    }

    private String trimText(Graphics2D g2, String text, int maxWidth) {
        if (text == null) {
            return "";
        }

        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(text) <= maxWidth) {
            return text;
        }

        String dots = "...";
        int dotsW = fm.stringWidth(dots);

        while (text.length() > 0 && fm.stringWidth(text) + dotsW > maxWidth) {
            text = text.substring(0, text.length() - 1);
        }

        return text + dots;
    }

    private String getTableValue(int row, int column) {
        Object value = tableModel.getValueAt(row, column);
        return value == null ? "" : value.toString();
    }

    private JPanel createPrintPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 120, 120)),
                BorderFactory.createEmptyBorder(28, 28, 22, 28)
        ));
        panel.setSize(850, 1100);

        Font titleFont = new Font("Arial", Font.BOLD, 22);
        Font subtitleFont = new Font("Arial", Font.BOLD, 28);
        Font sectionFont = new Font("Arial", Font.BOLD, 15);
        Font normalFont = new Font("Arial", Font.PLAIN, 13);
        Font smallFont = new Font("Arial", Font.PLAIN, 11);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

        // ===== HEADER =====
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setBackground(Color.WHITE);

        JLabel systemTitle = new JLabel("HOSPITAL MANAGEMENT SYSTEM");
        systemTitle.setFont(titleFont);
        systemTitle.setForeground(new Color(30, 30, 30));

        JLabel reportTitle = new JLabel("APPOINTMENTS REPORT");
        reportTitle.setFont(subtitleFont);
        reportTitle.setForeground(new Color(20, 20, 20));

        titleBox.add(systemTitle);
        titleBox.add(Box.createVerticalStrut(6));
        titleBox.add(reportTitle);

        JPanel infoBox = new JPanel(new GridLayout(3, 2, 8, 5));
        infoBox.setBackground(Color.WHITE);
        infoBox.setPreferredSize(new Dimension(250, 80));

        infoBox.add(makePrintInfoLabel("Printed On"));
        infoBox.add(makePrintInfoValue(now.format(dateTimeFormatter)));
        infoBox.add(makePrintInfoLabel("Printed By"));
        infoBox.add(makePrintInfoValue("Admin"));
        infoBox.add(makePrintInfoLabel("Report Type"));
        infoBox.add(makePrintInfoValue("Schedule Report"));

        headerPanel.add(titleBox, BorderLayout.WEST);
        headerPanel.add(infoBox, BorderLayout.EAST);
        panel.add(headerPanel);

        panel.add(Box.createVerticalStrut(14));
        panel.add(makeLine());
        panel.add(Box.createVerticalStrut(22));

        // ===== FILTER SUMMARY =====
        JPanel filterSummary = makePrintSectionPanel();
        filterSummary.setLayout(new BorderLayout(10, 10));

        JLabel filterTitle = new JLabel("FILTER SUMMARY");
        filterTitle.setFont(sectionFont);
        filterSummary.add(filterTitle, BorderLayout.NORTH);

        JPanel filterGrid = new JPanel(new GridLayout(1, 4, 18, 0));
        filterGrid.setBackground(Color.WHITE);

        String selectedDoctor = (String) filterDoctorCombo.getSelectedItem();
        String selectedStatus = (String) filterStatusCombo.getSelectedItem();
        String selectedPeriod = (String) filterPeriodCombo.getSelectedItem();
        String patientSearch = searchPatientField.getText().trim();

        filterGrid.add(makeFilterItem("Doctor", selectedDoctor == null ? "All Doctors" : selectedDoctor));
        filterGrid.add(makeFilterItem("Status", selectedStatus == null ? "All Status" : selectedStatus));
        filterGrid.add(makeFilterItem("Period", selectedPeriod == null ? "All" : selectedPeriod));
        filterGrid.add(makeFilterItem("Patient", patientSearch.isEmpty() ? "(All)" : patientSearch));

        filterSummary.add(filterGrid, BorderLayout.CENTER);
        panel.add(filterSummary);

        panel.add(Box.createVerticalStrut(18));

        // ===== SUMMARY CARDS =====
        JPanel summaryCards = new JPanel(new GridLayout(1, 4, 18, 0));
        summaryCards.setBackground(Color.WHITE);
        summaryCards.setMaximumSize(new Dimension(Integer.MAX_VALUE, 75));

        summaryCards.add(makeSummaryCard("Total Patients", String.valueOf(reportController.getTotalPatients())));
        summaryCards.add(makeSummaryCard("Total Appointments", String.valueOf(reportController.getTotalAppointments())));
        summaryCards.add(makeSummaryCard("Scheduled", String.valueOf(reportController.getTotalAppointmentsByStatus("SCHEDULED"))));
        summaryCards.add(makeSummaryCard("Completed", String.valueOf(reportController.getTotalAppointmentsByStatus("COMPLETED"))));

        panel.add(summaryCards);
        panel.add(Box.createVerticalStrut(22));

        // ===== TABLE SECTION =====
        JPanel tableSection = makePrintSectionPanel();
        tableSection.setLayout(new BoxLayout(tableSection, BoxLayout.Y_AXIS));

        JLabel tableTitle = new JLabel("SCHEDULE LIST");
        tableTitle.setFont(sectionFont);
        tableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        tableSection.add(tableTitle);
        tableSection.add(Box.createVerticalStrut(14));

        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        tablePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 500));

        JPanel headerRow = new JPanel(new GridLayout(1, 5));
        headerRow.setBackground(new Color(245, 245, 245));
        String[] columns = {"No.", "Doctor", "Patient", "Date & Time", "Status"};

        for (String col : columns) {
            JLabel label = new JLabel(col, SwingConstants.CENTER);
            label.setFont(new Font("Arial", Font.BOLD, 12));
            label.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
            label.setPreferredSize(new Dimension(100, 36));
            headerRow.add(label);
        }
        tablePanel.add(headerRow);

        int rowCount = tableModel.getRowCount();

        if (rowCount == 0) {
            JPanel emptyRow = new JPanel(new GridLayout(1, 1));
            emptyRow.setBackground(Color.WHITE);

            JLabel emptyLabel = new JLabel("No appointments found", SwingConstants.CENTER);
            emptyLabel.setFont(normalFont);
            emptyLabel.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
            emptyLabel.setPreferredSize(new Dimension(760, 36));

            emptyRow.add(emptyLabel);
            tablePanel.add(emptyRow);
        } else {
            for (int i = 0; i < rowCount; i++) {
                JPanel dataRow = new JPanel(new GridLayout(1, 5));
                dataRow.setBackground(Color.WHITE);

                dataRow.add(makeTableCell(String.valueOf(i + 1), true));
                dataRow.add(makeTableCell(getTableValue(i, 0), false));
                dataRow.add(makeTableCell(getTableValue(i, 1), false));
                dataRow.add(makeTableCell(getTableValue(i, 2), false));
                dataRow.add(makeTableCell(getTableValue(i, 3), true));

                tablePanel.add(dataRow);
            }
        }

        tableSection.add(tablePanel);
        tableSection.add(Box.createVerticalStrut(16));

        JLabel countLabel = new JLabel(resultsCountLabel.getText(), SwingConstants.CENTER);
        countLabel.setFont(normalFont);
        countLabel.setForeground(new Color(90, 90, 90));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        tableSection.add(countLabel);

        panel.add(tableSection);
        panel.add(Box.createVerticalStrut(18));

        // ===== NOTES =====
        JPanel notesPanel = makePrintSectionPanel();
        notesPanel.setLayout(new BorderLayout());

        JLabel notesLabel = new JLabel(
                "<html><b>Notes:</b>&nbsp;&nbsp;This report contains appointment records based on the selected filter criteria.</html>"
        );
        notesLabel.setFont(normalFont);

        notesPanel.add(notesLabel, BorderLayout.CENTER);
        panel.add(notesPanel);

        panel.add(Box.createVerticalStrut(22));
        panel.add(makeLine());
        panel.add(Box.createVerticalStrut(10));

        // ===== FOOTER =====
        JLabel footer1 = new JLabel("Thank you for using Hospital Management System", SwingConstants.CENTER);
        footer1.setFont(new Font("Arial", Font.ITALIC, 12));
        footer1.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel footer2 = new JLabel("This is a system generated report. No signature is required.", SwingConstants.CENTER);
        footer2.setFont(new Font("Arial", Font.ITALIC, 11));
        footer2.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(footer1);
        panel.add(Box.createVerticalStrut(5));
        panel.add(footer2);

        return panel;
    }

    private JPanel makePrintSectionPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 210)),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        return panel;
    }

    private void preparePanelForPrint(JPanel panel) {
        panel.setPreferredSize(new Dimension(850, 1100));
        panel.setSize(850, 1100);
        panel.doLayout();

        for (Component c : panel.getComponents()) {
            if (c instanceof JPanel) {
                preparePanelForPrint((JPanel) c);
            }
        }
    }

    private JLabel makePrintInfoLabel(String text) {
        JLabel label = new JLabel(text + "  :");
        label.setFont(new Font("Arial", Font.BOLD, 11));
        label.setForeground(new Color(40, 40, 40));
        return label;
    }

    private JLabel makePrintInfoValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setForeground(new Color(40, 40, 40));
        return label;
    }

    private JPanel makeFilterItem(String title, String value) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.PLAIN, 12));

        panel.add(titleLabel, BorderLayout.NORTH);
        panel.add(valueLabel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel makeSummaryCard(String title, String value) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));

        JLabel valueLabel = new JLabel(value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 22));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JLabel makeTableCell(String text, boolean center) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.PLAIN, 11));
        label.setBorder(BorderFactory.createLineBorder(new Color(220, 220, 220)));
        label.setPreferredSize(new Dimension(100, 34));

        if (center) {
            label.setHorizontalAlignment(SwingConstants.CENTER);
        } else {
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(220, 220, 220)),
                    BorderFactory.createEmptyBorder(0, 8, 0, 8)
            ));
        }

        return label;
    }

    private JComponent makeLine() {
        JPanel line = new JPanel();
        line.setBackground(new Color(150, 150, 150));
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        line.setPreferredSize(new Dimension(1, 1));
        return line;
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            checkRoleAccess();
            if (contentPanel.isVisible()) {
                loadAllData();
                updateTimestamp();
            }
        }
    }

    private static class ScrollablePanel extends JPanel implements Scrollable {

        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 80;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }
}
