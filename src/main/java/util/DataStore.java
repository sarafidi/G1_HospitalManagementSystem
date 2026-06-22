package util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import model.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class DataStore {

    private static DataStore instance;

    // five on-memory lists
    private ArrayList<User> users;
    private ArrayList<Patient> patients;
    private ArrayList<Doctor> doctors;
    private ArrayList<Appointment> appointments;
    private ArrayList<MedicalNote> medicalNotes;

    private DataStore() {
        users = new ArrayList<>();
        patients = new ArrayList<>();
        doctors = new ArrayList<>();
        appointments = new ArrayList<>();
        medicalNotes = new ArrayList<>();
    }

    // every caller does DataStore.getInstance - never new DataStore
    // singleton access point
    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    public void init() {
        // creates `data/` folder if it doesnt exists
        // loads each json file into matching list
        // if users.json is empty, seeds the default admin account
        createDataFolder();
        loadAll();
        if (users.isEmpty()) {
            seedAdmin();
        }
    }

    private void createDataFolder() {
        File folder = new File("data/");
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
    }

    private void loadAll() {
        users = loadList("data/users.json", new TypeToken<ArrayList<User>>() {
        }.getType());
        patients = loadList("data/patients.json", new TypeToken<ArrayList<Patient>>() {
        }.getType());
        doctors = loadList("data/doctors.json", new TypeToken<ArrayList<Doctor>>() {
        }.getType());
        appointments = loadList("data/appointments.json", new TypeToken<ArrayList<Appointment>>() {
        }.getType());
        medicalNotes = loadList("data/medical_notes.json", new TypeToken<ArrayList<MedicalNote>>() {
        }.getType());
    }

    private <T> ArrayList<T> loadList(String path, Type type) {
        try {
            FileReader file = new FileReader(path);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            ArrayList<T> result = gson.fromJson(file, type);
            file.close();
            return result != null ? result : new ArrayList<T>();
        } catch (IOException e) {
            return new ArrayList<T>();
        }
    }

    private void saveList(String path, Object list) {
        try {
            FileWriter file = new FileWriter(path);
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(list, file);
            file.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed to save: " + path, e);
        }
    }

    private void seedAdmin() {
        // your seed admin code here
        String hash = HashUtil.sha256("Admin@1234");
        String createdAt = LocalDateTime.now().toString();
        User admin = new User(
                "USR-0001", "admin", "Admin1",
                "", "", hash, Role.ADMIN,
                true, true, null, createdAt
        );
        users.add(admin);
        saveUsers();
    }

    // getters
    public ArrayList<User> getUsers() {
        return users;
    }

    public ArrayList<Patient> getPatients() {
        return patients;
    }

    public ArrayList<Doctor> getDoctors() {
        return doctors;
    }

    public ArrayList<Appointment> getAppointments() {
        return appointments;
    }

    public ArrayList<MedicalNote> getMedicalNotes() {
        return medicalNotes;
    }

    public void saveUsers() {
        saveList("data/users.json", users);
    }

    public void savePatients() {
        saveList("data/patients.json", patients);
    }

    public void saveDoctors() {
        saveList("data/doctors.json", doctors);
    }

    public void saveAppointments() {
        saveList("data/appointments.json", appointments);
    }

    public void saveMedicalNotes() {
        saveList("data/medical_notes.json", medicalNotes);
    }
}
