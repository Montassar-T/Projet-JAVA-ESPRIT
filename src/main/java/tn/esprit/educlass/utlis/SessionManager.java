package tn.esprit.educlass.utlis;

import tn.esprit.educlass.model.User;

/**
 * Singleton class to manage user session globally across the application
 */
public class SessionManager {
    private static SessionManager instance;
    private User currentUser;

    private SessionManager() {}

    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void clearSession() {
        this.currentUser = null;
    }

    public boolean isUserLoggedIn() {
        return currentUser != null;
    }

    public String getUserRole() {
        return currentUser != null ? currentUser.getRole().name() : null;
    }

    public long getUserId() {
        return currentUser != null ? currentUser.getId() : -1;
    }

    public String getUserFullName() {
        return currentUser != null ? currentUser.getFullName() : null;
    }

    public String getUserEmail() {
        return currentUser != null ? currentUser.getEmail() : null;
    }
}

