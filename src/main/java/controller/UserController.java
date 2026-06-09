package controller;

import model.Role;
import model.User;
import util.DataStore;
import util.HashUtil;
import util.IDGenerator;
import util.Validator;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class UserController {
    private ArrayList<User> users = DataStore.getInstance().getUsers();

    public String addUser(String username, String name, String email, String phone, Role role, String tempPassword, String doctorId) {
        boolean usernameTaken = users.stream()
                .anyMatch(u -> u.getUsername().equalsIgnoreCase(username));

        // check if username is not empty
        if (!Validator.isNonEmpty(username)) return "Error: Username is empty!";
        // check if email is valid
        if (!Validator.isValidEmail(email)) return "Error: Email is invalid!";
        if (usernameTaken) return "Error: Username is already taken!";
        // check if password is not empty
        if (tempPassword.trim().isEmpty()) return "Error: Password is empty!";
        // check if role is not null
        if (role == null) return "Error: Role is not picked!";

        // if all checks passed
        String password = HashUtil.sha256(tempPassword);
        String newUserId = IDGenerator.generateUserId();

        // create user object
        User user = new User(newUserId, username, name, email,
                phone, password, role, true, true,
                doctorId, LocalDateTime.now().toString());

        // add to the list
        users.add(user);

        // save users
        DataStore.getInstance().saveUsers();
        return null;
    }

    public String deactivateUser(String userId) {
        // find user by userId, if not found return error
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        // set isActive = false
        user.setActive(false);

        // save and return null
        DataStore.getInstance().saveUsers();
        return null;
    }

    public ArrayList<User> getAllUsers() { return users; }

    public String updatePassword(String userId, String newPassword) {
        // find user by userId
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        // hash new password
        String passwordHash = HashUtil.sha256(newPassword);

        // setPasswordHash, setFirstLogin(false)
        user.setPasswordHash(passwordHash);
        user.setFirstLogin(false);

        // save and return null
        DataStore.getInstance().saveUsers();
        return null;
    }

    public String forcePasswordChange(String userId) {
        // find user by userId
        User user = findByUserId(userId);
        if (user == null) return "Error: User not found!";

        user.setFirstLogin(true);

        DataStore.getInstance().saveUsers();
        return null;
    }

    public User findByUserId(String userId) {
        return users.stream()
                .filter(u -> u.getUserId().equals(userId))
                .findFirst()
                .orElse(null);
    }

}