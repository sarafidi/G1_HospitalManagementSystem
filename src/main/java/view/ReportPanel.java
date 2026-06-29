package view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
    private JLabel totalPatientsLabel;
    private JLabel totalAppointmentsLabel;
    private JLabel totalScheduledLabel;
    private JLabel totalCompletedLabel;
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

        mainPanel.add(createTopPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createFilterPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createSummaryPanel());
        mainPanel.add(Box.createVerticalStrut(10));
        mainPanel.add(createTablePanel());

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

        filterDoctorCombo = new JComboBox<>();
        filterDoctorCombo.addItem("All Doctors");
        filterDoctorCombo.setFont(new Font("Arial", Font.PLAIN, 12));

        filterStatusCombo = new JComboBox<>(reportController.getAllStatuses());
        filterStatusCombo.setFont(new Font("Arial", Font.PLAIN, 12));

        filterPeriodCombo = new JComboBox<>(reportController.getPeriodOptions());
        filterPeriodCombo.setFont(new Font("Arial", Font.PLAIN, 12));

        searchPatientField = new JTextField();
        searchPatientField.setFont(new Font("Arial", Font.PLAIN, 12));

        addFilterField(fields, gbc, 0, "Doctor:", filterDoctorCombo);
        addFilterField(fields, gbc, 1, "Status:", filterStatusCombo);
        addFilterField(fields, gbc, 2, "Period:", filterPeriodCombo);
        addFilterField(fields, gbc, 3, "Patient:", searchPatientField);

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

        totalPatientsLabel = createStatCard("Patients", "0");
        totalAppointmentsLabel = createStatCard("Total", "0");
        totalScheduledLabel = createStatCard("Scheduled", "0");
        totalCompletedLabel = createStatCard("Completed", "0");

        summaryPanel.add(totalPatientsLabel);
        summaryPanel.add(totalAppointmentsLabel);
        summaryPanel.add(totalScheduledLabel);
        summaryPanel.add(totalCompletedLabel);
        return summaryPanel;
    }

    private JLabel createStatCard(String title, String value) {
        JLabel label = new JLabel(makeStatHtml(title, value), SwingConstants.CENTER);
        label.setOpaque(true);
        label.setBackground(CARD_BG);
        label.setForeground(TEXT);
        label.setFont(new Font("Arial", Font.PLAIN, 12));
        label.setPreferredSize(new Dimension(135, 56));
        label.setMinimumSize(new Dimension(135, 56));
        label.setMaximumSize(new Dimension(135, 56));
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
        totalPatientsLabel.setText(makeStatHtml("Patients", String.valueOf(reportController.getTotalPatients())));
        totalAppointmentsLabel.setText(makeStatHtml("Total", String.valueOf(reportController.getTotalAppointments())));
        totalScheduledLabel.setText(makeStatHtml("Scheduled", String.valueOf(reportController.getTotalAppointmentsByStatus("SCHEDULED"))));
        totalCompletedLabel.setText(makeStatHtml("Completed", String.valueOf(reportController.getTotalAppointmentsByStatus("COMPLETED"))));
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
            return;
        }

        Role role = currentUser.getRole();
        if (role == Role.ADMIN || role == Role.RECEPTIONIST) {
            contentPanel.setVisible(true);
        } else {
            contentPanel.setVisible(false);
            showAccessDeniedDialog("Access Denied\n\nOnly Admin and Receptionist can view reports.");
        }
    }

    private void showAccessDeniedDialog(String message) {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JOptionPane.showMessageDialog(parentWindow, message, "Access Denied", JOptionPane.ERROR_MESSAGE);
    }

    private void printReport() {
        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser == null || !(currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.RECEPTIONIST)) {
            JOptionPane.showMessageDialog(this, "You do not have permission to print reports.", "Access Denied", JOptionPane.ERROR_MESSAGE);
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

            if (job.printDialog()) {
                job.print();
                JOptionPane.showMessageDialog(this, "Report printed successfully!", "Print Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (PrinterException e) {
            JOptionPane.showMessageDialog(this, "Error printing report: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createPrintPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setSize(800, 1000);

        JLabel headerLabel = new JLabel("HOSPITAL MANAGEMENT SYSTEM", SwingConstants.CENTER);
        headerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(headerLabel);
        panel.add(Box.createVerticalStrut(10));

        JLabel reportLabel = new JLabel("REPORT: Doctor Schedules", SwingConstants.CENTER);
        reportLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        reportLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(reportLabel);
        panel.add(Box.createVerticalStrut(8));

        JLabel dateLabel = new JLabel("Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm:ss")), SwingConstants.CENTER);
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        panel.add(dateLabel);
        panel.add(Box.createVerticalStrut(15));

        JLabel titleLabel = new JLabel(doctorScheduleTitleLabel.getText(), SwingConstants.CENTER);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(titleLabel);

        if (patientFilterDisplayLabel.isVisible()) {
            JLabel patientLabel = new JLabel(patientFilterDisplayLabel.getText(), SwingConstants.CENTER);
            patientLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            patientLabel.setFont(new Font("Arial", Font.ITALIC, 12));
            panel.add(patientLabel);
        }
        panel.add(Box.createVerticalStrut(12));

        JPanel summaryPrintPanel = new JPanel(new GridLayout(1, 4, 10, 5));
        summaryPrintPanel.setMaximumSize(new Dimension(760, 45));
        summaryPrintPanel.add(new JLabel("Patients: " + reportController.getTotalPatients(), SwingConstants.CENTER));
        summaryPrintPanel.add(new JLabel("Total: " + reportController.getTotalAppointments(), SwingConstants.CENTER));
        summaryPrintPanel.add(new JLabel("Scheduled: " + reportController.getTotalAppointmentsByStatus("SCHEDULED"), SwingConstants.CENTER));
        summaryPrintPanel.add(new JLabel("Completed: " + reportController.getTotalAppointmentsByStatus("COMPLETED"), SwingConstants.CENTER));
        panel.add(summaryPrintPanel);
        panel.add(Box.createVerticalStrut(12));

        JPanel tablePrintPanel = new JPanel();
        tablePrintPanel.setLayout(new BoxLayout(tablePrintPanel, BoxLayout.Y_AXIS));
        tablePrintPanel.setMaximumSize(new Dimension(760, 700));

        JPanel headerRow = new JPanel(new GridLayout(1, 4));
        String[] columns = {"Doctor", "Patient", "Date & Time", "Status"};
        for (String col : columns) {
            JLabel label = new JLabel(col, SwingConstants.CENTER);
            label.setFont(new Font("Monospaced", Font.BOLD, 11));
            label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            headerRow.add(label);
        }
        tablePrintPanel.add(headerRow);

        for (int i = 0; i < scheduleTable.getRowCount(); i++) {
            JPanel dataRow = new JPanel(new GridLayout(1, 4));
            for (int j = 0; j < 4; j++) {
                Object value = scheduleTable.getValueAt(i, j);
                JLabel label = new JLabel(value != null ? value.toString() : "", SwingConstants.CENTER);
                label.setFont(new Font("Monospaced", Font.PLAIN, 10));
                label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
                dataRow.add(label);
            }
            tablePrintPanel.add(dataRow);
        }
        panel.add(tablePrintPanel);
        return panel;
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            checkRoleAccess();
            loadAllData();
            updateTimestamp();
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
