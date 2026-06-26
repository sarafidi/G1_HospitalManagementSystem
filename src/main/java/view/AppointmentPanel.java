package view;

import controller.AppointmentController;
import exception.DuplicateSlotException;
import model.Appointment;
import model.AppStatus;
import util.IDGenerator;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class AppointmentPanel extends JPanel {

    private final AppointmentController controller = new AppointmentController();

    // Komponen UI Borang
    private JTextField txtPatientId;
    private JTextField txtDoctorId;
    private JComboBox<String> comboTime;
    private JTextField txtDate; // Format: YYYY-MM-DD
    private JTextArea txtNotes;
    private JButton btnBook;

    // Komponen Jadual (Table)
    private JTable table;
    private DefaultTableModel tableModel;

    public AppointmentPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 1. BAHAGIAN ATAS: Tajuk Panel
        JLabel lblTitle = new JLabel("Appointment Booking Management", JLabel.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle, BorderLayout.NORTH);

        // 2. BAHAGIAN KIRI: Borang Input (Form)
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Book New Appointment"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Input Patient ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Patient ID:"), gbc);
        gbc.gridx = 1;
        txtPatientId = new JTextField(15);
        formPanel.add(txtPatientId, gbc);

        // Input Doctor ID
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(new JLabel("Doctor ID:"), gbc);
        gbc.gridx = 1;
        txtDoctorId = new JTextField(15);
        formPanel.add(txtDoctorId, gbc);

        // Input Tarikh
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1;
        txtDate = new JTextField(LocalDate.now().toString(), 15); // Letak tarikh hari ni sebagai default
        formPanel.add(txtDate, gbc);

        // Input Masa (Slot gandaan 30 minit ikut spec)
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(new JLabel("Time Slot:"), gbc);
        gbc.gridx = 1;
        String[] timeSlots = {
            "09:00", "09:30", "10:00", "10:30", "11:00", "11:30",
            "14:00", "14:30", "15:00", "15:30", "16:00", "16:30"
        };
        comboTime = new JComboBox<>(timeSlots);
        formPanel.add(comboTime, gbc);

        // Input Nota
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Notes:"), gbc);
        gbc.gridx = 1;
        txtNotes = new JTextArea(3, 15);
        txtNotes.setLineWrap(true);
        formPanel.add(new JScrollPane(txtNotes), gbc);

        // Butang Book (Event Handling - ActionListener)
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridWidth = 2;
        btnBook = new JButton("Book Appointment");
        btnBook.setBackground(new Color(231, 84, 128)); // Warna pink gelap sikit macam kau suka dulu!
        btnBook.setForeground(Color.WHITE);
        formPanel.add(btnBook, gbc);

        add(formPanel, BorderLayout.WEST);

        // 3. BAHAGIAN KANAN / TENGAH: Jadual Paparan List Janji Temu
        String[] columns = {"Appt ID", "Patient ID", "Doctor ID", "Date & Time", "Status", "Notes"};
        tableModel = new DefaultTableModel(columns, 0);
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Muat data awal masuk jadual
        loadTableData();

        // 4. LOGIK BUTANG TEKAN (Action Listener)
        btnBook.addActionListener(e -> handleBookAppointment());
    }

    // Fungsi untuk isi data ke dalam JTable
    private void loadTableData() {
        tableModel.setRowCount(0); // Kosongkan jadual dulu
        List<Appointment> list = controller.getAllAppointments();
        for (Appointment appt : list) {
            Object[] row = {
                appt.getAppointmentId(),
                appt.getPatientId(),
                appt.getDoctorId(),
                appt.getAppointmentDateTime().toString().replace("T", " "),
                appt.getStatus(),
                appt.getNotes()
            };
            tableModel.addRow(row);
        }
    }

    // Pengendali fungsi butang Book ditekan
    private void handleBookAppointment() {
        String pId = txtPatientId.getText().trim();
        String dId = txtDoctorId.getText().trim();
        String dateStr = txtDate.getText().trim();
        String timeStr = comboTime.getSelectedItem().toString();
        String notes = txtNotes.getText().trim();

        // Validasi input ringkas
        if (pId.isEmpty() || dId.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sila isi semua ruangan yang wajib!", "Input Kosong", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Gabungkan String Tarikh + Masa menjadi LocalDateTime
            LocalDate date = LocalDate.parse(dateStr);
            LocalTime time = LocalTime.parse(timeStr);
            LocalDateTime dateTime = LocalDateTime.of(date, time);

            // Rebat ID baru otomatik guna IDGenerator milik Sara (Kalau nama method beza, kita tukar kemudian)
            String apptId = "APT-" + System.currentTimeMillis() % 10000; 

            // Cipta objek appointment baru
            Appointment newAppt = new Appointment(apptId, pId, dId, dateTime, AppStatus.SCHEDULED, notes);

            // Simpan guna controller
            controller.bookAppointment(newAppt);

            // Refresh balik jadual kat skrin
            loadTableData();
            
            // Bersihkan borang
            txtPatientId.setText("");
            txtDoctorId.setText("");
            txtNotes.setText("");
            
            JOptionPane.showMessageDialog(this, "Janji temu berjaya didaftarkan!", "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (DuplicateSlotException ex) {
            // Tangkap kes masa bertembung polymorphically! (Poin penting untuk video)
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Slot Bertembung", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format tarikh salah! Sila guna YYYY-MM-DD.", "Format Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}