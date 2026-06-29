package controller;

import model.Role;
import model.User;
import util.*;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserController {
    private final DataStore dataStore = DataStore.getInstance();

    public String addUser(String username, String name, String email, String phone, Role role, String tempPassword, String doctorId) {
        // Validation checks
        if (!Validator.isNonEmpty(username)) return "Error: Username is empty!";
        if (!Validator.isValidEmail(email)) return "Error: Email is invalid!";
        if (isUsernameTaken(username)) return "Error: Username is already taken!";
        if (tempPassword == null || tempPassword.trim().isEmpty()) return "Error: Password is empty!";
        if (role == null) return "Error: Role is not picked!";
        if (!Validator.isValidPhone(phone)) return "Error: Phone must start with 0 and be 10-11 digits!";

        String password = HashUtil.sha256(tempPassword);
        String newUserId = IDGenerator.generateUserId();

        User user = new User(newUserId, username, name, email,
                phone, password, role, true, true,
                doctorId, LocalDateTime.now().toString());

        dataStore.getUsers().add(user);
        dataStore.saveUsers();
        return null;
    }

    public String handleUserActive(String userId) {
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && user.getUserId().equalsIgnoreCase(currentUser.getUserId())) {
            return "Error: Cannot deactivate your own account!";
        }

        user.setActive(!user.isActive());
        dataStore.saveUsers();
        return null;
    }

    public String deletedUser(String userId) {
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        User currentUser = SessionManager.getInstance().getCurrentUser();
        if (currentUser != null && user.getUserId().equalsIgnoreCase(currentUser.getUserId())) {
            return "Error: Cannot delete your own account!";
        }

        dataStore.getUsers().removeIf(u -> u.getUserId().equalsIgnoreCase(userId));
        dataStore.saveUsers();
        return null;
    }

    public ArrayList<User> getAllUsers() { 
        return dataStore.getUsers(); 
    }

    public String updatePassword(String userId, String newPassword) {
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        String passwordHash = HashUtil.sha256(newPassword);
        user.setPasswordHash(passwordHash);
        user.setFirstLogin(false);

        dataStore.saveUsers();
        return null;
    }

    public String forcePasswordChange(String userId) {
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        user.setFirstLogin(true);
        dataStore.saveUsers();
        return null;
    }

    private User findByUserId(String userId) {
        return dataStore.getUsers().stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

    private boolean isUsernameTaken(String username) {
        return dataStore.getUsers().stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));
    }
}