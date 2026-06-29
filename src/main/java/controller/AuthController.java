package controller;

import model.*;
import util.DataStore;
import util.SessionManager;

public class AuthController {
    private final DataStore dataStore = DataStore.getInstance();

    public User login(String username, String password) {
        User currUser = findByUsername(username);
        if (currUser == null) { 
            return null; 
        }
        if (currUser.authenticate(password)) {
            SessionManager.getInstance().setCurrentUser(currUser);
            return currUser;
        }
        return null;
    }

    public void logout() {
        SessionManager.getInstance().clearSession();
    }

    public User getCurrentUser() {
        return SessionManager.getInstance().getCurrentUser();
    }

    public User findByUsername(String username) {
        return dataStore.getUsers().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }
}