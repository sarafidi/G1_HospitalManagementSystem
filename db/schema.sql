CREATE
DATABASE IF NOT EXISTS hms_db;
USE
hms_db;

-- Users table (stores Admin, Doctor, Receptionist)
CREATE TABLE users
(
    user_id VARCHAR(10) PRIMARY KEY,
    username VARCHAR(50)                            NOT NULL UNIQUE,
    password VARCHAR(100)                           NOT NULL,
    role     ENUM ('ADMIN','DOCTOR','RECEPTIONIST') NOT NULL
);

-- Doctors table (extends user info for Doctor Role)
CREATE TABLE doctors
(
    doctor_id      VARCHAR(10) PRIMARY KEY,
    user_id        VARCHAR(10)  NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    contact_number VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users (user_id)
);

-- Patients table
CREATE TABLE patients
(
    patient_id     VARCHAR(10) PRIMARY KEY,
    name           VARCHAR(100) NOT NULL,
    age            INT          NOT NULL,
    gender         enum('Male','Female','Other') NOT NULL,
    contact_number VARCHAR(20),
    address        VARCHAR(200)
);

-- Medical history (one patient -> many records)
CREATE TABLE medical_history
(
    history_id  INT AUTO_INCREMENT PRIMARY KEY,
    patient_id  VARCHAR(10) NOT NULL,
    record      text        NOT NULL,
    recorded_at datetime DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (patient_id) REFERENCES patients (patient_id)
);

-- Appointments
CREATE TABLE appointments
(
    appointment_id VARCHAR(10) PRIMARY KEY,
    patient_id     VARCHAR(10) NOT NULL,
    doctor_id      VARCHAR(10) NOT NULL,
    date_time      datetime    NOT NULL,
    notes          text,
    status         enum('SCHEDULED', 'COMPLETED', 'CANCELLED') DEFAULT 'SCHEDULED',
    FOREIGN KEY (patient_id) REFERENCES patients (patient_id),
    FOREIGN KEY (doctor_id) REFERENCES doctors (doctor_id)
);

-- Seed data: default users
    INSERT INTO users VALUES
('U001', 'admin', 'admin123', 'ADMIN'),
('U002', 'receptionist', 'rec123', 'RECEPTIONIST'),
('U003', 'ali', 'doc123', 'DOCTOR'),
('U004', 'sara', 'doc345', 'DOCTOR');

INSERT INTO doctors VALUES
    ('D201', 'U003', 'Cardiology', '011-1234567'),
    ('D202', 'U004', 'Neurology', '011-1345455');