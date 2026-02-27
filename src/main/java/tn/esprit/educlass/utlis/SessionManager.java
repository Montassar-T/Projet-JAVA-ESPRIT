package tn.esprit.educlass.utlis;

import tn.esprit.educlass.model.User;

/**
 * Simple static holder for the currently logged-in user.
 * Set during login, cleared on logout.
 */
public class SessionManager {

    private static User currentUser;

    public static User getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static void clear() {
        currentUser = null;
    }
}
